//
// Created by Robining on 2019/1/11.
//

#include "AudioStreamDecoder.h"


void *__initPlayer(void *data) {
    AudioStreamDecoder *audioStreamDecoder = static_cast<AudioStreamDecoder *>(data);
    audioStreamDecoder->initPlayer();
    pthread_exit(&audioStreamDecoder->playerThread);
}

AudioStreamDecoder::AudioStreamDecoder(AVStream *avStream, AVCodecContext *codecContext,SyncHandler* syncHandler)
        : IStreamDecoder(avStream, codecContext,syncHandler) {
    outBuffer = static_cast<uint8_t *>(malloc(static_cast<size_t>(44100 * 2 * 2)));
    soundSampleInBuffer = static_cast<SAMPLETYPE *>(malloc(44100 * 2 * 2 * 2 / 3));
    soundSampleOutBuffer = static_cast<SAMPLETYPE *>(malloc(44100 * 2 * 2 * 2 / 3));

    soundTouch = new SoundTouch();
    soundTouch->setSampleRate(44100);
    soundTouch->setChannels(2);
//    soundTouch->setPitch(1.5f);
//    soundTouch->setTempo(1.5f);
    pthread_create(&playerThread, NULL, __initPlayer, this);
}


int *AudioStreamDecoder::readOneFrame() {
    AVFrame *frame = popFrame();
    double progress = frame->pts * av_q2d(this->stream->time_base);
    JavaBridge::getInstance()->onProgressChanged(progress);
    LOGI(">>>player got a frame");
    if(syncHandler != NULL){
        syncHandler->audioClock = progress;
    }

    SwrContext *swrContext = NULL;
    //此处设置的44100和2 关系到outBuffer大小初始化
    swrContext = swr_alloc_set_opts(swrContext, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16,
                                    44100, 2,
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
    if(readFinishedSoundSamples){
        int *result = readOneFrame();
        if (result == NULL) {
            return;
        }
        int nb = result[1];
        int dataSize = result[0];
        int sampleBufferSize = dataSize / 2 + 1;

        for (int i = 0; i < sampleBufferSize; i++) {
            soundSampleInBuffer[i] = outBuffer[i * 2] | (outBuffer[i * 2 + 1] << 8);
        }

        soundTouch->putSamples(soundSampleInBuffer, static_cast<uint>(nb));
        readFinishedSoundSamples = false;
        currentSoundSamples = nb;
    }

    uint outSampleCount = 0;
    outSampleCount = soundTouch->receiveSamples(soundSampleOutBuffer, static_cast<uint>(currentSoundSamples));
    if (outSampleCount > 0) {
        JavaBridge::getInstance()->onPlayAudioFrame(outSampleCount * 2 * 2,
                                                    soundSampleOutBuffer);
        (*androidSimpleBufferQueueItf)->Enqueue(androidSimpleBufferQueueItf,
                                                soundSampleOutBuffer,
                                                static_cast<SLuint32>(outSampleCount * 2 * 2));
        readFinishedSoundSamples = true;//只读取一次(否则不能播放，不知道为什么)
    } else{
        readFinishedSoundSamples = true;
        playFrame();
    }
}


static void __bufferQueueCallback(SLAndroidSimpleBufferQueueItf bf, void *pContext) {
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
                SL_SAMPLINGRATE_44_1,
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
        SLInterfaceID ids[2] = {SL_IID_BUFFERQUEUE, SL_IID_MUTESOLO};

        //reqs
        SLboolean reqs[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

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

        start();
    } catch (const char *msg) {
        LOGE(">>>player init failed :%s", msg);
    }
}

void AudioStreamDecoder::sureSLResultSuccess(SLresult result, const char *message) {
    if (result != SL_RESULT_SUCCESS) {
        throw message;
    }
}

void AudioStreamDecoder::start() {
    IStreamDecoder::start();
    LOGI(">>>player ready start play");
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
    __bufferQueueCallback(androidSimpleBufferQueueItf, this);
}

void AudioStreamDecoder::pause() {
    IStreamDecoder::pause();
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PAUSED);
}

void AudioStreamDecoder::resume() {
    IStreamDecoder::resume();
    start();
}

void AudioStreamDecoder::stop() {
    IStreamDecoder::stop();
    (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_STOPPED);
}

void AudioStreamDecoder::release() {
    IStreamDecoder::release();
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

}

