//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_AUDIOSTREAMDECODER_H
#define ROBINPLAYER_AUDIOSTREAMDECODER_H

#include "IStreamDecoder.h"

class AudioStreamDecoder : public IStreamDecoder {
private:
    long packetSize = 0;
public:
    void enqueue(AVPacket *packet);

    void pause();

    void resume();

    void stop();

    void release();
};


#endif //ROBINPLAYER_AUDIOSTREAMDECODER_H
