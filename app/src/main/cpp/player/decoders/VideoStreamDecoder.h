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
    pthread_t playerThread;
    static void *__internalPlayVideo(void *data);
    void playFrames();
};


#endif //ROBINPLAYER_VIDEOSTREAMDECODER_H
