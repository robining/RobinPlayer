//
// Created by Robining on 2019/1/11.
//

#include "RobinPlayer.h"

RobinPlayer::RobinPlayer() {
    av_register_all();
    avformat_network_init();
}

void *RobinPlayer::__init(void *data) {
    RobinPlayer *player = static_cast<RobinPlayer *>(data);
    player->initInternal(player->url);
    pthread_t self = pthread_self();
    pthread_exit(&self);
}

void RobinPlayer::init(const char *url) {
    LOGI(">>>A111:ready release old player");
    release();

    pthread_mutex_init(&mutex_init, NULL);
    pthread_cond_init(&cond_init_over, NULL);
    pthread_cond_init(&condSeeking, NULL);

    LOGI(">>>A111:ready goto init player");
    pthread_mutex_lock(&mutex_init);
    LOGI(">>>A111:init player");
    this->url = url;
    pthread_create(&thread_init, NULL, __init, this);
}

void *RobinPlayer::__play(void *data) {
    RobinPlayer *robinPlayer = static_cast<RobinPlayer *>(data);
    robinPlayer->playInternal();
    pthread_exit(&robinPlayer->thread_play);
}

void RobinPlayer::play() {
    pthread_create(&thread_play, NULL, __play, this);
}

void RobinPlayer::playInternal() {
    pthread_mutex_lock(&mutex_init);
    if (state == NOT_INIT) {
        onError(CODE_ERROR_NEED_CALL_INIT, "need call init before play()");
        return;
    }

    if (state == INIT_FAILED) {
        return;
    }

    if (state == INITING) {//等待初始化结束
        LOGI(">>>will wait init complete");
        pthread_cond_wait(&cond_init_over, &mutex_init);
        play();
        return;
    }

    pthread_mutex_unlock(&mutex_init);

    if (state == PLAYING) {
        onWarn(CODE_ERROR_COMMON, "player is playing,please don't call repeat");
        return;
    }

    stateChanged(PLAYING);

    //decoding
    while (state == PLAYING) {
        if (seeking) {
            pthread_cond_wait(&condSeeking, NULL);
        }
        AVPacket *packet = av_packet_alloc();
        int result = av_read_frame(avFormatContext, packet);
        if (result < 0) {
            //the end
            //但是需要继续运行，以免在预加载完毕后不能完成seek操作
            av_packet_free(&packet);
            continue;
        }
        IStreamDecoder *streamDecoder = streamDecoders[packet->stream_index];
        if (streamDecoder != NULL && !seeking) {//only process support type
            streamDecoder->enqueue(packet);
        } else {
            av_packet_free(&packet);
        }
    }

    LOGI(">>>robin player read packet stopped");
}

void RobinPlayer::pause() {
    if (state == PLAYING) {
        stateChanged(PAUSED);
        for (IStreamDecoder *decoder : streamDecoders) {
            if (decoder != NULL) {
                decoder->pause();
            }
        }
    }
}

void RobinPlayer::resume() {
    if (state == PAUSED) {
        stateChanged(PLAYING);
        for (IStreamDecoder *decoder : streamDecoders) {
            if (decoder != NULL) {
                decoder->resume();
            }
        }
    }
}

void RobinPlayer::stop() {
    this->url = NULL;
    if (state == PLAYING || state == PAUSED) {
        stateChanged(STOPED);
//        if (thread_init != NULL) {
//            pthread_exit(&thread_init);
//        }
//        if (thread_play != NULL) {
//            pthread_exit(&thread_play);
//        }

        if (avFormatContext != NULL) {
            avformat_free_context(avFormatContext);
            avFormatContext = NULL;
        }

        if (syncHandler != NULL) {
            delete syncHandler;
        }

        LOGI(">>>A111:ready free decoders");
        for (IStreamDecoder *decoder : streamDecoders) {
            if (decoder != NULL) {
                decoder->stop();
                LOGI(">>>A111:freed a decoder 0 ");
                delete decoder; //释放内存
                decoder = NULL;
                LOGI(">>>A111:freed a decoder 1");
            }
        }
        LOGI(">>>A111:freed all decoder");
        streamDecoders.clear();
        pthread_mutex_destroy(&mutex_init);
        pthread_cond_destroy(&cond_init_over);
        pthread_cond_destroy(&condSeeking);
        seeking = false;
        seekTargetSeconds = 0;
    }
}

void RobinPlayer::release() {
    stop();
    stateChanged(NOT_INIT);
}

void RobinPlayer::initInternal(const char *url) {
    stateChanged(INITING);
    try {
        //open the input
        int result = avformat_open_input(&avFormatContext, url, NULL, NULL);
        if (result < 0) {
            onError(CODE_ERROR_OPEN_INPUT_FAILED, av_err2str(result));
            goto initFailed;
        }
        //find streams
        result = avformat_find_stream_info(avFormatContext, NULL);
        if (result < 0) {
            onError(CODE_ERROR_FIND_STREAM_FAILED, av_err2str(result));
            goto initFailed;
        }
        if (avFormatContext->nb_streams == 0) {//todo 此处应该根据播放模式判断，如果仅播放音频 就判断音频流之类的
            onError(CODE_ERROR_NOT_FOUND_AVALIABLE_STREAM, "not fond avaliable stream");
            goto initFailed;
        }

        //find stream types
        std::vector<IStreamDecoder *> decoders(avFormatContext->nb_streams);
        streamDecoders = decoders;
        syncHandler = new SyncHandler();
        for (int i = 0; i < avFormatContext->nb_streams; i++) {
            AVStream *stream = avFormatContext->streams[i];
            AVCodecParameters *codecParameters = stream->codecpar;

            //find decoder
            AVCodec *codec = avcodec_find_decoder(codecParameters->codec_id);
            if (codec == NULL) {
                onWarn(CODE_WARN_DECODER_NOT_FOUND,
                       "cannot found a decoder for streamIndex:" + i);
                LOGI(">>>find codec failed at %d", i);
                continue;
            }

            //init decoder parameters
            AVCodecContext *codecContext = avcodec_alloc_context3(codec);
            result = avcodec_parameters_to_context(codecContext, codecParameters);
            if (result < 0) {
                onWarn(CODE_WARN_DECODER_NOT_FOUND, av_err2str(result));
                continue;
            }
//            av_opt_set_int(codecContext, "refcounted_frames", 1, 0);

            //open decoder
            result = avcodec_open2(codecContext, codec, NULL);
            if (result < 0) {
                onWarn(CODE_WARN_DECODER_OPEN_FAILED, av_err2str(result));
                continue;
            }

            //to decode
            if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
                //to decode video
                streamDecoders[i] = new VideoStreamDecoder(stream, codecContext, syncHandler);
            } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
                //to decode audio
                streamDecoders[i] = new AudioStreamDecoder(stream, codecContext, syncHandler);
            } else {
                onWarn(CODE_WARN_NOT_FOUND_STREAM_DECODER, "cannot found unknown stream decoder");
                //not support this codec_type at this time
            }
        }
        goto initSuccess;
    } catch (exception) {
        onError(CODE_ERROR_COMMON, "got a unknown error when during init");
        goto initFailed;
    }

    initFailed:
    stateChanged(INIT_FAILED);
    pthread_cond_signal(&cond_init_over);
    pthread_mutex_unlock(&mutex_init);
    return;

    initSuccess:
    stateChanged(INITED);
    double totalDuration = avFormatContext->duration / AV_TIME_BASE;
    JavaBridge::getInstance()->onGotTotalDuration(totalDuration);
    pthread_cond_signal(&cond_init_over);
    pthread_mutex_unlock(&mutex_init);
}


void *RobinPlayer::__seek(void *data) {
    RobinPlayer *robinPlayer = static_cast<RobinPlayer *>(data);
    robinPlayer->seekInternal(robinPlayer->seekTargetSeconds);
    pthread_t self = pthread_self();
    pthread_exit(&self);
}

void RobinPlayer::seekInternal(int seconds) {
    seeking = true;
    clock_t start = clock();
    LOGE(">>>seek start %ld", start);
    for (int i = 0; i < streamDecoders.size(); i++) {
        IStreamDecoder *decoder = streamDecoders[i];
        if (decoder != NULL) {
            decoder->changeSeekingState(true);
        }
    }
    //为什么只能用-1
    int result = avformat_seek_file(avFormatContext, -1, INT64_MIN, seconds * AV_TIME_BASE,
                                    INT64_MAX, 0);
    if (result >= 0) {
        LOGI(">>>seek success:%d", seconds);
    } else {
        LOGI(">>>seek failed:%d", seconds);
    }

    for (int i = 0; i < streamDecoders.size(); i++) {
        IStreamDecoder *decoder = streamDecoders[i];
        if (decoder != NULL) {
            decoder->changeSeekingState(false);
        }
    }

    clock_t end = clock();
    double costed = (double) (end - start) / CLOCKS_PER_SEC;
    LOGE(">>>seek end at:%ld, costed:%f", end, costed);
    seeking = false;
    pthread_cond_signal(&condSeeking);
}


void RobinPlayer::seekTo(int seconds) {
    if (state != PLAYING && state != PAUSED && state != INITED) {
        return;
    }
    if (avFormatContext != NULL && &streamDecoders != NULL) {
        if(seeking){
            return;
        }
        seekTargetSeconds = seconds;
        threadSeeking = pthread_t();
        //TODO seek线程处理
        pthread_create(&threadSeeking, NULL, __seek, this);
    }
}

void RobinPlayer::onError(int code, const char *msg) {
    JavaBridge::getInstance()->onError(code, msg);
}

void RobinPlayer::onWarn(int code, const char *msg) {
    JavaBridge::getInstance()->onWarn(code, msg);
}

void RobinPlayer::stateChanged(PLAYER_STATE oldState, PLAYER_STATE state) {
    this->state = state;
    if (oldState != state) {
        JavaBridge::getInstance()->onPlayStateChanged(oldState, state);
    }
}

void RobinPlayer::stateChanged(PLAYER_STATE state) {
    stateChanged(this->state, state);
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCDFAInspection"

void RobinPlayer::setAudioChannel(AUDIO_CHANNEL_TYPE channelType) {
    for (int i = 0; i < streamDecoders.size(); i++) {
        IStreamDecoder *decoder = streamDecoders[i];
        if (decoder != NULL) {
            AudioStreamDecoder *audioStreamDecoder = dynamic_cast<AudioStreamDecoder *>(decoder);
            if (audioStreamDecoder != NULL) {
                audioStreamDecoder->setAudioChannel(channelType);
            }
        }
    }
}

#pragma clang diagnostic pop

