//
// Created by Robining on 2019/1/11.
//

#include "RobinPlayer.h"

RobinPlayer::RobinPlayer() {
    av_register_all();
    avformat_network_init();
}

void *__init(void *data) {
    RobinPlayer *player = static_cast<RobinPlayer *>(data);
    player->initInternal(player->url);
    pthread_exit(&player->thread_init);
}

void RobinPlayer::init(const char *url) {
    release();

    pthread_mutex_init(&mutex_init, NULL);
    pthread_cond_init(&cond_init_over, NULL);

    pthread_mutex_lock(&mutex_init);
    this->url = url;
    pthread_create(&thread_init, NULL, __init, this);
//    initInternal(url);
}

void *__play(void *data) {
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
        onError(CODE_ERROR_COMMON, "cannot play,because init failed");
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

    LOGI(">>>player play...and streamDecoder count:%d", streamDecoders.size());
    stateChanged(PLAYING);

    //decoding
    while (state == PLAYING) {
        AVPacket *packet = av_packet_alloc();
        int result = av_read_frame(avFormatContext, packet);
        if (result < 0) {
            //the end
            LOGI(">>>preload completed!!!");
            break;
        }

        IStreamDecoder *streamDecoder = streamDecoders[packet->stream_index];
        if (streamDecoder != NULL) {//only process support type
            streamDecoder->enqueue(packet);
        } else {
            av_free(packet);
        }
    }
}

void RobinPlayer::pause() {
    LOGI(">>>player pause...");
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
    LOGI(">>>player resume...");
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
    LOGI(">>>player stop...");
    this->url = NULL;
    if (state == PLAYING || state == PAUSED) {
        stateChanged(STOPED);
        if (avFormatContext != NULL) {
            avformat_free_context(avFormatContext);
            avFormatContext = NULL;
        }
        for (IStreamDecoder *decoder : streamDecoders) {
            if (decoder != NULL) {
                decoder->stop();
                free(decoder);
            }
        }

        streamDecoders.clear();
        pthread_mutex_destroy(&mutex_init);
        pthread_cond_destroy(&cond_init_over);
    }
}

void RobinPlayer::release() {
    LOGI(">>>player release...");
    stop();
    stateChanged(NOT_INIT);
    for (IStreamDecoder *decoder : streamDecoders) {
        if (decoder != NULL) {
            decoder->release();
        }

        decoder = NULL;
    }
}

void RobinPlayer::initInternal(const char *url) {
    LOGI(">>>player init...%s at thread:%d", url, (unsigned int) pthread_self());
    stateChanged(INITING);
    try {
        //open the input
        int result = avformat_open_input(&avFormatContext, url, NULL, NULL);
        if (result < 0) {
            onError(CODE_ERROR_OPEN_INPUT_FAILED, av_err2str(result));
            goto initFailed;
        }
        LOGI(">>>TEST:will start find stream0");
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

        LOGI(">>>TEST:will start find stream1");
        //find stream types
        std::vector<IStreamDecoder *> decoders(avFormatContext->nb_streams);
        streamDecoders = decoders;

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
                onWarn(CODE_WARN_NOT_FOUND_STREAM_DECODER, "cannot found video stream decoder");
//                streamDecoders[i] = new VideoStreamDecoder(codecContext);
            } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
                //to decode audio
                LOGI(">>>init audio stream decoder...");
                streamDecoders[i] = new AudioStreamDecoder(codecContext);
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
    pthread_cond_signal(&cond_init_over);
    pthread_mutex_unlock(&mutex_init);
}

void RobinPlayer::seekTo(int seconds) {
    if (state != PLAYING && state != PAUSED && state != INITED) {
        return;
    }
    if (avFormatContext != NULL && &streamDecoders != NULL) {

        for (int i = 0; i < streamDecoders.size(); i++) {
//            IStreamDecoder *decoder = streamDecoders[i];
//            if (decoder != NULL) { //说明该流在播放
//
//            }
            avformat_seek_file(avFormatContext, i, INT64_MIN, seconds * AV_TIME_BASE, INT64_MAX, 0);
        }
    }
}

void RobinPlayer::onError(int code, const char *msg) {
    LOGE(">>>onError:%d,%s", code, msg);
}

void RobinPlayer::onWarn(int code, const char *msg) {
    LOGW(">>>onWarn:%d,%s", code, msg);
}

void RobinPlayer::stateChanged(PLAYER_STATE oldState, PLAYER_STATE state) {
    this->state = state;
    if (oldState != state) {
        JavaBridge::getInstance()->onPlayStateChanged(oldState, state);
        LOGI(">>>state changed:%d to %d", oldState, state);
    }
}

void RobinPlayer::stateChanged(PLAYER_STATE state) {
    stateChanged(this->state, state);
}

