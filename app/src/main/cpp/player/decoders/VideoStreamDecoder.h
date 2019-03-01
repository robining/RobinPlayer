//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_VIDEOSTREAMDECODER_H
#define ROBINPLAYER_VIDEOSTREAMDECODER_H

#include "IStreamDecoder.h"
extern "C"{
    #include <libavutil/imgutils.h>
    #include <libswscale/swscale.h>
};

class VideoStreamDecoder : public IStreamDecoder{
public:
    VideoStreamDecoder(AVStream* avStream,AVCodecContext* codecContext,SyncHandler* syncHandler);
    ~VideoStreamDecoder();
private:
    double defaultDelayTime = 0.04;
    pthread_t playerThread;
    bool supportDecodeByMediaCodec = false;
    static void *__internalPlayVideo(void *data);
    void playFrames();
    void processPacket(AVPacket* packet);
    AVBSFContext* initNativeSupportMediaCodec();
    AVBSFContext* avbsfContext = NULL;
    double getFrameDiffTime(AVFrame *avFrame, AVPacket *avPacket);
    double getDelayTime(double diff);
};


#endif //ROBINPLAYER_VIDEOSTREAMDECODER_H
