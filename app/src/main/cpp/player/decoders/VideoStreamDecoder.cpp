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

VideoStreamDecoder::VideoStreamDecoder(AVStream *avStream, AVCodecContext *codecContext)
        : IStreamDecoder(avStream, codecContext) {
    pthread_create(&playerThread, NULL, __playVideo, this);
}

void VideoStreamDecoder::playFrames() {
    while (isRunning) {
        AVFrame *frame = popFrame();
        AVFrame *yuv420pFrame;
        int frameWidth = codecContext->width;
        int frameHeight = codecContext->height;
        if (frame->format != AV_PIX_FMT_YUV420P) {
            LOGI(">>>sws to yuv420p");
            //若不是YUV420P格式转换为yuv420p格式
            yuv420pFrame = av_frame_alloc();
            int bufferSize = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, frameWidth,
                                                      frameHeight, 1);
            uint8_t *buffer = static_cast<uint8_t *>(av_malloc(bufferSize * sizeof(uint8_t)));
            av_image_fill_arrays(yuv420pFrame->data, frame->linesize, buffer, AV_PIX_FMT_YUV420P,
                                 frameWidth, frameHeight, 1);
            SwsContext *swsContext = sws_getContext(frameWidth, frameHeight,
                                                    codecContext->pix_fmt,
                                                    frameWidth, frameHeight, AV_PIX_FMT_YUV420P,
                                                    SWS_BICUBIC, NULL, NULL, NULL);
            if (!swsContext) {
                LOGI(">>>swsContext init failed:yuv420p");
                av_free(frame);
                av_free(yuv420pFrame);
                av_free(swsContext);
                continue;
            }
            sws_scale(swsContext,
                      frame->data, //为输入图像数据各颜色通道的buffer指针数组
                      frame->linesize,//为输入图像数据各颜色通道每行存储的字节数数组[每行]
                      0,//为从输入图像数据的第多少列开始逐行扫描，通常设为0；
                      frameHeight,//为需要扫描多少行，通常为输入图像数据的高度；
                      yuv420pFrame->data,
                      yuv420pFrame->linesize);
            av_free(swsContext);
        } else {
            LOGI(">>>ready to yuv420p");
            yuv420pFrame = frame;
        }

        JavaBridge::getInstance()->onPlayVideoFrame(frameWidth, frameHeight, yuv420pFrame->data[0], yuv420pFrame->data[1], yuv420pFrame->data[2]);

        av_free(yuv420pFrame);
        av_free(frame);
    }
}
