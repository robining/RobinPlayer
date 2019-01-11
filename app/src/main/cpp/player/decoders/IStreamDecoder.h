//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_ISTREAMDECODER_H
#define ROBINPLAYER_ISTREAMDECODER_H


#include "../../AndroidLog.h"
extern "C"{
    #include "libavformat/avformat.h"
};

class IStreamDecoder {
public:
    virtual void enqueue(AVPacket *packet) = 0;
    virtual void pause() = 0;
    virtual void resume() = 0;
    virtual void stop() = 0;
    virtual void release() = 0;
};


#endif //ROBINPLAYER_ISTREAMDECODER_H
