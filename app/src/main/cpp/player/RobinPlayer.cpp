//
// Created by Robining on 2019/1/11.
//

#include "RobinPlayer.h"

RobinPlayer::RobinPlayer() {
    av_register_all();
    avformat_network_init();
}

void onFailed(const char *title, const char *reason) {
    LOGE(">>>%s:%s", title, reason);
}

int RobinPlayer::init(const char *url) {
    if(state != NOT_INIT){
        release();
    }

    LOGI(">>>player init...%s", url);
    try {
        //open the input
        int result = avformat_open_input(&avFormatContext, url, NULL, NULL);
        if (result < 0) {
            onFailed("open input", av_err2str(result));
            return ERROR_CODE_OPEN_INPUT_FAILED;
        }

        //find streams
        result = avformat_find_stream_info(avFormatContext, NULL);
        if (result < 0) {
            onFailed("find stream", av_err2str(result));
            return ERROR_CODE_FIND_STREAM_FAILED;
        }
        if (avFormatContext->nb_streams == 0) {//todo 此处应该根据播放模式判断，如果仅播放音频 就判断音频流之类的
            return ERROR_CODE_NOT_FOUND_AVALIABLE_STREAM;
        }

        //find stream types
        streamDecoders.reserve(avFormatContext->nb_streams);
        for (int i = 0; i < avFormatContext->nb_streams; i++) {
            AVStream *stream = avFormatContext->streams[i];
            AVCodecParameters *codecParameters = stream->codecpar;

            //find decoder
            AVCodec *codec = avcodec_find_decoder(codecParameters->codec_id);
            if (codec == NULL) {
                continue;
            }

            //init decoder parameters
            AVCodecContext *codecContext = avcodec_alloc_context3(codec);
            result = avcodec_parameters_to_context(codecContext, codecParameters);
            if (result < 0) {
                onFailed("decoder parameters to context failed", av_err2str(result));
                continue;
            }
//            av_opt_set_int(codecContext, "refcounted_frames", 1, 0);

            //open decoder
            result = avcodec_open2(codecContext, codec, NULL);
            if (result < 0) {
                onFailed("open decoder failed", av_err2str(result));
                continue;
            }

            //to decode
            if (codecParameters->codec_type == AVMEDIA_TYPE_VIDEO) {
                //to decode video
                streamDecoders[i] = new VideoStreamDecoder();
            } else if (codecParameters->codec_type == AVMEDIA_TYPE_AUDIO) {
                //to decode audio
                streamDecoders[i] = new AudioStreamDecoder();
            } else {
                //not support this codec_type at this time
            }
        }
    } catch (const char *reason) {
        LOGE(">>>%s", reason);
    }

    return 0;
}

void RobinPlayer::play() {
    LOGI(">>>player play...");
    state = PLAYING;
    //decoding
    while (state == PLAYING || state == PAUSED) {
        while (state == PAUSED){
        }

        if(state == PLAYING){
            AVPacket *packet = av_packet_alloc();
            int result = av_read_frame(avFormatContext, packet);
            if (result < 0) {
                //the end
                LOGI(">>>play completed!!!");
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
}

void RobinPlayer::pause() {
    LOGI(">>>player pause...");
    if(state == PLAYING){
        state = PAUSED;
        for(IStreamDecoder* decoder : streamDecoders){
            if(decoder != NULL){
                decoder->pause();
            }
        }
    }
}

void RobinPlayer::resume() {
    LOGI(">>>player resume...");
    if(state == PAUSED){
        state = PLAYING;
        for(IStreamDecoder* decoder : streamDecoders){
            if(decoder != NULL){
                decoder->resume();
            }
        }
    }
}

void RobinPlayer::stop() {
    LOGI(">>>player stop...");
    if(state == PLAYING || state == PAUSED){
        state = STOPED;
        for(IStreamDecoder* decoder : streamDecoders){
            if(decoder != NULL){
                decoder->stop();
            }
        }
    }
}

void RobinPlayer::release() {
    LOGI(">>>player release...");
    state = NOT_INIT;
    for(IStreamDecoder* decoder : streamDecoders){
        if(decoder != NULL){
            decoder->release();
        }

        decoder = NULL;
    }
}
