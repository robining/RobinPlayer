//
// Created by Robining on 2019/1/11.
//

#include "AudioStreamDecoder.h"

AudioStreamDecoder::AudioStreamDecoder(AVStream *avStream, AVCodecContext *codecContext,
                                       SyncHandler *syncHandler)
        : IStreamDecoder(avStream, codecContext, syncHandler) {
    outBuffer = static_cast<uint8_t *>(malloc(static_cast<size_t>(codecContext->sample_rate * 2 * 2)));
    soundSampleInOutBuffer = static_cast<SAMPLETYPE *>(malloc(
            static_cast<size_t>(codecContext->sample_rate * 2 * 2 * 3)));

    soundTouch = new SoundTouch();
    soundTouch->setSampleRate(static_cast<uint>(codecContext->sample_rate));
    soundTouch->setChannels(2);
//    soundTouch->setPitch(1.5f);
//    soundTouch->setTempo(1.5f);
    pthread_create(&playerThread, NULL, __initPlayer, this);
}

void *AudioStreamDecoder::__initPlayer(void *data) {
    AudioStreamDecoder *audioStreamDecoder = static_cast<AudioStreamDecoder *>(data);
    audioStreamDecoder->initPlayer();
    pthread_t self = pthread_self();
    pthread_exit(&self);
}

int *AudioStreamDecoder::readOneFrame() {
    AVFrame *frame = popFrame();
    double progress = frame->pts * av_q2d(this->stream->time_base);
    JavaBridge::getInstance()->onProgressChanged(progress);
    LOGI(">>>player got a frame");
    if (syncHandler != NULL) {
        syncHandler->audioClock = progress;
    }

    SwrContext *swrContext = NULL;
    swrContext = swr_alloc_set_opts(swrContext, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16,
                                    codecContext->sample_rate, frame->channel_layout,
                                    static_cast<AVSampleFormat>(frame->format), frame->sample_rate,
                                    NULL, NULL);
    if (!swrContext || swr_init(swrContext) < 0) {
        LOGE(">>>sws context init failed");
        av_frame_free(&frame);
        if (swrContext != NULL) {
            swr_free(&swrContext);
        }
        return NULL;
    }

    int nb = swr_convert(swrContext, &outBuffer, frame->nb_samples,
                         reinterpret_cast<const uint8_t **>(&frame->data), frame->nb_samples);
    int outChannels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
    int dataSize = nb * outChannels * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);
    if (androidSimpleBufferQueueItf == NULL) {
        av_frame_free(&frame);
        swr_free(&swrContext);
        return NULL;
    }

    int result[] = {dataSize, nb};
    av_frame_free(&frame);
    swr_free(&swrContext);
    return result;
}

void AudioStreamDecoder::playFrame() {
    int *result = readOneFrame();
    if (result == NULL) {
        return;
    }
    int dataSize = result[0];
    JavaBridge::getInstance()->onPlayAudioFrame(dataSize,
                                                outBuffer);
    (*androidSimpleBufferQueueItf)->Enqueue(androidSimpleBufferQueueItf,
                                            outBuffer,
                                            static_cast<SLuint32>(dataSize));

//    if (readFinishedSoundSamples) {
//        int *result = readOneFrame();
//        if (result == NULL) {
//            return;
//        }
//        int nb = result[1];
//        int dataSize = result[0];
//        int sampleBufferSize = dataSize / 2 + 1;
//
//        for (int i = 0; i < sampleBufferSize; i++) {
//            soundSampleInOutBuffer[i] = outBuffer[i * 2] | (outBuffer[i * 2 + 1] << 8);
//        }
//
//        soundTouch->putSamples(soundSampleInOutBuffer, static_cast<uint>(nb * 2));
//        readFinishedSoundSamples = false;
//        currentSoundSamples = nb * 2;
//    }
//
//    uint outSampleCount = 0;
//    outSampleCount = soundTouch->receiveSamples(soundSampleInOutBuffer,
//                                                static_cast<uint>(5000));
//    if (outSampleCount > 0) {
//        LOGI(">>>soundtouch out :%d  while in %d", outSampleCount, currentSoundSamples);
//        JavaBridge::getInstance()->onPlayAudioFrame(outSampleCount * 2 * 2,
//                                                    soundSampleInOutBuffer);
//        (*androidSimpleBufferQueueItf)->Enqueue(androidSimpleBufferQueueItf,
//                                                soundSampleInOutBuffer,
//                                                static_cast<SLuint32>(outSampleCount * 2 * 2));
//        readFinishedSoundSamples = true;//只读取一次(否则不能播放，不知道为什么)
//    } else {
//        readFinishedSoundSamples = true;
//        playFrame();
//    }
}


void AudioStreamDecoder::__bufferQueueCallback(SLAndroidSimpleBufferQueueItf bf, void *pContext) {
    AudioStreamDecoder *audioStreamDecoder = static_cast<AudioStreamDecoder *>(pContext);
    audioStreamDecoder->playFrame();
}

void AudioStreamDecoder::initPlayer() {
    try {
        SLresult result;

        //create engine
        result = slCreateEngine(&engineObjItf, 0, 0, 0, 0, 0);
        sureSLResultSuccess(result, "create engine failed:0");
        result = (*engineObjItf)->Realize(engineObjItf, SL_BOOLEAN_FALSE);
        sureSLResultSuccess(result, "create engine failed:1");
        result = (*engineObjItf)->GetInterface(engineObjItf, SL_IID_ENGINE, &engineItf);
        sureSLResultSuccess(result, "create engine failed:2");

        //create output mix
        SLInterfaceID mids[1] = {SL_IID_ENVIRONMENTALREVERB};
        SLboolean merq[1] = {SL_BOOLEAN_FALSE};
        result = (*engineItf)->CreateOutputMix(engineItf, &outputMixObjItf, 1, mids, merq);
        sureSLResultSuccess(result, "create environment failed:0");
        result = (*outputMixObjItf)->Realize(outputMixObjItf, SL_BOOLEAN_FALSE);
        sureSLResultSuccess(result, "create environment failed:1");
        result = (*outputMixObjItf)->GetInterface(outputMixObjItf, SL_IID_ENVIRONMENTALREVERB,
                                                  &environmentalReverbItf);
        if (result == SL_RESULT_SUCCESS) {
            SLEnvironmentalReverbSettings environmentalReverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
            (*environmentalReverbItf)->SetEnvironmentalReverbProperties(environmentalReverbItf,
                                                                        &environmentalReverbSettings);
        }

        //create player########
        //data source
        SLDataLocator_AndroidBufferQueue androidBufferQueue = {
                SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                2
        };
        SLDataFormat_PCM pcm = {
                SL_DATAFORMAT_PCM,
                2,
                getCurrentSampleRateForOpensles(codecContext->sample_rate),
                SL_PCMSAMPLEFORMAT_FIXED_16,
                SL_PCMSAMPLEFORMAT_FIXED_16,
                SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                SL_BYTEORDER_LITTLEENDIAN
        };
        SLDataSource dataSource = {&androidBufferQueue, &pcm};

        //data sink
        SLDataLocator_OutputMix outputMix = {
                SL_DATALOCATOR_OUTPUTMIX,
                outputMixObjItf
        };
        SLDataSink dataSink = {&outputMix, NULL};

        //interface ids
        SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_MUTESOLO,
                                SL_IID_PLAYBACKRATE}; //SL_IID_PLAYBACKRATE 解决播放声音有间隔的问题(微调功能)

        //reqs
        SLboolean reqs[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

        //create
        result = (*engineItf)->CreateAudioPlayer(engineItf, &playerObjItf, &dataSource, &dataSink,
                                                 2,
                                                 ids, reqs);
        sureSLResultSuccess(result, "create player failed:0");
        result = (*playerObjItf)->Realize(playerObjItf, SL_BOOLEAN_FALSE);
        sureSLResultSuccess(result, "create player failed:1");
        result = (*playerObjItf)->GetInterface(playerObjItf, SL_IID_PLAY, &playItf);
        sureSLResultSuccess(result, "create player failed:2");

        //声道控制器
        result = (*playerObjItf)->GetInterface(playerObjItf, SL_IID_MUTESOLO, &muteSoloItf);
        sureSLResultSuccess(result, "create audio controller failed:0");

        //config
        result = (*playerObjItf)->GetInterface(playerObjItf, SL_IID_BUFFERQUEUE,
                                               &androidSimpleBufferQueueItf);
        sureSLResultSuccess(result, "create buffer packetQueue failed:0");
        result = (*androidSimpleBufferQueueItf)->RegisterCallback(androidSimpleBufferQueueItf,
                                                                  __bufferQueueCallback, this);
        sureSLResultSuccess(result, "create buffer packetQueue failed:1");
        LOGI(">>>player init success");

        startPlay();
    } catch (const char *msg) {
        LOGE(">>>player init failed :%s", msg);
    }
}

SLuint32 AudioStreamDecoder::getCurrentSampleRateForOpensles(int sample_rate) {
    SLuint32 rate = 0;
    switch (sample_rate)
    {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate =  SL_SAMPLINGRATE_44_1;
    }
    return rate;
}

void AudioStreamDecoder::sureSLResultSuccess(SLresult result, const char *message) {
    if (result != SL_RESULT_SUCCESS) {
        throw message;
    }
}

void AudioStreamDecoder::startPlay() {
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    __bufferQueueCallback(androidSimpleBufferQueueItf, this);
}

void AudioStreamDecoder::pause() {
    IStreamDecoder::pause();
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PAUSED);
}

void AudioStreamDecoder::resume() {
    IStreamDecoder::resume();
    startPlay();
}

void AudioStreamDecoder::stop() {
    IStreamDecoder::stop();
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
}

void AudioStreamDecoder::setAudioChannel(AUDIO_CHANNEL_TYPE channelType) {
    if (muteSoloItf != NULL) {
        SLboolean left = SL_BOOLEAN_FALSE;
        SLboolean right = SL_BOOLEAN_FALSE;
        switch (channelType) {
            case LEFT:
                LOGI(">>>set channel : left");
                left = SL_BOOLEAN_TRUE;
                break;
            case RIGHT:
                LOGI(">>>set channel : right");
                right = SL_BOOLEAN_TRUE;
                break;
            default:
                LOGI(">>>set channel : none");
                break;
        }
        (*muteSoloItf)->SetChannelMute(muteSoloItf,
                                       1,//0右声道1左声道
                                       left);

        (*muteSoloItf)->SetChannelMute(muteSoloItf,
                                       0,//0右声道1左声道
                                       right);
    }
}

AudioStreamDecoder::~AudioStreamDecoder() {
    if (outBuffer != NULL) {
        free(outBuffer);
    }

    if (engineObjItf != NULL) {
        (*engineObjItf)->Destroy(engineObjItf);
    }

    if (outputMixObjItf != NULL) {
        (*outputMixObjItf)->Destroy(outputMixObjItf);
    }

    if (playerObjItf != NULL) {
        (*playerObjItf)->Destroy(playerObjItf);
    }

    if(soundTouch != NULL){
        soundTouch->clear();
        delete soundTouch;
    }

    if(soundSampleInOutBuffer != NULL){
        free(soundSampleInOutBuffer);
    }

    if(playerThread != NULL){
        pthread_exit(&playerThread);
    }
}

