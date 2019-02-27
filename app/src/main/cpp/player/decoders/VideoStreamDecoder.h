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
    void playFrames();

private:
    pthread_t playerThread;
};


#endif //ROBINPLAYER_VIDEOSTREAMDECODER_H
