//
// Created by Robining on 2019/1/11.
//
#include "VideoStreamDecoder.h"

void *__playVideo(void *data) {
    VideoStreamDecoder *videoStreamDecoder = static_cast<VideoStreamDecoder *>(data);
    videoStreamDecoder->playFrames();
    pthread_t currentThread = pthread_self();
    pthread_exit(&currentThread);
}

VideoStreamDecoder::VideoStreamDecoder(AVStream *avStream, AVCodecContext *codecContext,
                                       SyncHandler *syncHandler)
        : IStreamDecoder(avStream, codecContext, syncHandler) {
    pthread_create(&playerThread, NULL, __playVideo, this);
}

void VideoStreamDecoder::playFrames() {
    int frameWidth = codecContext->width;
    int frameHeight = codecContext->height;
    SwsContext *swsContext = sws_getContext(frameWidth, frameHeight,
                                            codecContext->pix_fmt,
                                            frameWidth, frameHeight, AV_PIX_FMT_YUV420P,
                                            SWS_BICUBIC, NULL, NULL, NULL);
    if (!swsContext) {
        LOGI(">>>swsContext init failed:yuv420p");
        return;
    }

    uint8_t *buffer = NULL;

    while (isRunning) {
        AVFrame *frame = popFrame();
        //sync with audio clock
        //TODO 此同步应该放到scale转换之后去，因为转换过程也需要耗时
        if (syncHandler != NULL && syncHandler->audioClock != -1) {
            int64_t pts = av_frame_get_best_effort_timestamp(frame);
            if (pts == AV_NOPTS_VALUE) {
                pts = 0;
            }
            double clock = pts * av_q2d(stream->time_base);
            double diffWithAudioClock = clock - syncHandler->audioClock;
            if (diffWithAudioClock < 0) {
                diffWithAudioClock = 0;
            }

            LOGE(">>>video play:%f   audio play:%f,  need sleep:%f", clock, syncHandler->audioClock,
                 diffWithAudioClock);
            if(diffWithAudioClock > 1){ //如果大于一定时间 直接放弃该帧...（seek后可能会相差很多）
                av_frame_free(&frame);
                continue;
            }
            av_usleep(static_cast<unsigned int>(diffWithAudioClock * 1000000));
        }

        AVFrame *yuv420pFrame = NULL;
        bool scaled = false;
        if (frame->format != AV_PIX_FMT_YUV420P) {
            LOGI(">>>sws to yuv420p");
            //若不是YUV420P格式转换为yuv420p格式
            yuv420pFrame = av_frame_alloc();
            scaled = true;

            if (buffer == NULL) {
                int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, frameWidth,
                                                          frameHeight, 1);
                buffer = static_cast<uint8_t *>(av_malloc(bufferSize * sizeof(uint8_t)));
            }

            av_image_fill_arrays(yuv420pFrame->data, yuv420pFrame->linesize, buffer,
                                 AV_PIX_FMT_YUV420P,
                                 frameWidth, frameHeight, 1);
            sws_scale(swsContext,
                      frame->data, //为输入图像数据各颜色通道的buffer指针数组
                      frame->linesize,//为输入图像数据各颜色通道每行存储的字节数数组[每行]
                      0,//为从输入图像数据的第多少列开始逐行扫描，通常设为0；
                      frameHeight,//为需要扫描多少行，通常为输入图像数据的高度；
                      yuv420pFrame->data,
                      yuv420pFrame->linesize);
        } else {
            LOGI(">>>ready to yuv420p");
            yuv420pFrame = frame;
        }

        JavaBridge::getInstance()->onPlayVideoFrame(frameWidth, frameHeight, yuv420pFrame->data[0],
                                                    yuv420pFrame->data[1], yuv420pFrame->data[2]);

        if (frame != NULL) {
            av_frame_free(&frame);
        }

        if (scaled) {
            av_frame_free(&yuv420pFrame);
        }
    }

    if (buffer != NULL) {
        av_freep(&buffer);
    }
    sws_freeContext(swsContext);
}
