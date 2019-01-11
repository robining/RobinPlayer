//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_VIDEOSTREAMDECODER_H
#define ROBINPLAYER_VIDEOSTREAMDECODER_H

#include "IStreamDecoder.h"

class VideoStreamDecoder : public IStreamDecoder {
private:
    long packetSize = 0;
public:
    void enqueue(AVPacket *packet);

    void pause();

    void resume();

    void stop();

    void release();
};


#endif //ROBINPLAYER_VIDEOSTREAMDECODER_H
