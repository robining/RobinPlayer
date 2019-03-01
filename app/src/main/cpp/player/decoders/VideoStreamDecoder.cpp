//
// Created by Robining on 2019/1/11.
//
#include "VideoStreamDecoder.h"


VideoStreamDecoder::VideoStreamDecoder(AVStream *avStream, AVCodecContext *codecContext,
                                       SyncHandler *syncHandler)
        : IStreamDecoder(avStream, codecContext, syncHandler) {
    supportDecodeByMediaCodec = JavaBridge::getInstance()->initDecodeByMediaCodec(
            codecContext->codec_descriptor->name, codecContext->width, codecContext->height,codecContext->extradata_size,codecContext->extradata_size,codecContext->extradata,codecContext->extradata);
    if (supportDecodeByMediaCodec) {
        avbsfContext = initNativeSupportMediaCodec();
        if (avbsfContext == NULL) {
            supportDecodeByMediaCodec = false;
        }
    }
    LOGE(">>>GGG:是否支持硬解码:%d", supportDecodeByMediaCodec);
    if (!supportDecodeByMediaCodec) {
        JavaBridge::getInstance()->useYUVDecodeVideoMode();
        pthread_create(&playerThread, NULL, __internalPlayVideo, this);
    } else {
        JavaBridge::getInstance()->useMediaCodecDecodeVideoMode();
    }
}


void VideoStreamDecoder::processPacket(AVPacket *packet) {
    if (!supportDecodeByMediaCodec) {
        IStreamDecoder::processPacket(packet);
    } else {
        if (av_bsf_send_packet(avbsfContext, packet) < 0) {
            av_packet_free(&packet);
            return;
        }

        while (av_bsf_receive_packet(avbsfContext, packet) == 0) {
            if (syncHandler != NULL && syncHandler->audioClock != -1) {
                double diff = getFrameDiffTime(NULL,packet);
                double sleepTime = getDelayTime(diff);
                av_usleep(static_cast<unsigned int>(sleepTime * 1000000));
            }
            JavaBridge::getInstance()->decodeVideoByMediaCodec(packet->size, packet->data);
        }
        av_packet_free(&packet);
    }
}


void *VideoStreamDecoder::__internalPlayVideo(void *data) {
    VideoStreamDecoder *videoStreamDecoder = static_cast<VideoStreamDecoder *>(data);
    videoStreamDecoder->playFrames();
    pthread_t currentThread = pthread_self();
    pthread_exit(&currentThread);
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
            double diff = getFrameDiffTime(frame,NULL);
            double sleepTime = getDelayTime(diff);
            av_usleep(static_cast<unsigned int>(sleepTime * 1000000));
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

double VideoStreamDecoder::getFrameDiffTime(AVFrame *avFrame, AVPacket *avPacket) {

    double pts = 0;
    if(avFrame != NULL)
    {
        pts = av_frame_get_best_effort_timestamp(avFrame);
    }
    if(avPacket != NULL)
    {
        pts = avPacket->pts;
    }
    if(pts == AV_NOPTS_VALUE)
    {
        pts = 0;
    }
    pts *= av_q2d(stream->time_base);

    double diff = syncHandler->audioClock - pts;
    return diff;
}

double VideoStreamDecoder::getDelayTime(double diff) {
    double delayTime = 0;
    if(diff > 0.003)
    {
        delayTime = delayTime * 2 / 3;
        if(delayTime < defaultDelayTime / 2)
        {
            delayTime = defaultDelayTime * 2 / 3;
        }
        else if(delayTime > defaultDelayTime * 2)
        {
            delayTime = defaultDelayTime * 2;
        }
    }
    else if(diff < - 0.003)
    {
        delayTime = delayTime * 3 / 2;
        if(delayTime < defaultDelayTime / 2)
        {
            delayTime = defaultDelayTime * 2 / 3;
        }
        else if(delayTime > defaultDelayTime * 2)
        {
            delayTime = defaultDelayTime * 2;
        }
    }
    else if(diff == 0.003)
    {

    }
    if(diff >= 0.5)
    {
        delayTime = 0;
    }
    else if(diff <= -0.5)
    {
        delayTime = defaultDelayTime * 2;
    }

    if(fabs(diff) >= 10)
    {
        delayTime = defaultDelayTime;
    }
    return delayTime;
}

VideoStreamDecoder::~VideoStreamDecoder() {
    pthread_exit(&playerThread);
}

AVBSFContext *VideoStreamDecoder::initNativeSupportMediaCodec() {
    LOGE(">>>GGG:尝试初始化本地硬解码");
    const AVBitStreamFilter *streamFilter = NULL;
    if (strcasecmp(codecContext->codec_descriptor->name, "h264") == 0) {
        LOGE(">>>GGG:h264_mp4toannexb");
        streamFilter = av_bsf_get_by_name("h264_mp4toannexb");
    } else if (strcasecmp(codecContext->codec_descriptor->name, "h265") == 0) {

        LOGE(">>>GGG:hevc_mp4toannexb");
        streamFilter = av_bsf_get_by_name("hevc_mp4toannexb");
    }

    if (streamFilter == NULL) {
        LOGE(">>>GGG:没有找到streamFilter:%s", codecContext->codec_descriptor->name);
        return NULL;
    }
    AVBSFContext *context = NULL;
    if (av_bsf_alloc(streamFilter, &context) != 0) {
        LOGE(">>>GGG:av_bsf_alloc failed");
        //初始化失败
        return NULL;
    }

    if (avcodec_parameters_copy(context->par_in, stream->codecpar) < 0) {
        av_bsf_free(&context);
        LOGE(">>>GGG:avcodec_parameters_copy failed");
        return NULL;
    }
//
//    av_bsf_init(context);
    int result = av_bsf_init(context);
    if (result != 0) {
        LOGE(">>>GGG:av_bsf_init failed:%s",av_err2str(result));
        av_bsf_free(&context);
        return NULL;
    }

    context->time_base_in = stream->time_base;
    return context;
}
