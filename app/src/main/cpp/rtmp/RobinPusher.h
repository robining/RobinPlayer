//
// Created by Robining on 2019/3/14.
//

#ifndef ROBINPLAYER_ROBINPUSHER_H
#define ROBINPLAYER_ROBINPUSHER_H

extern "C" {
#include "../librtmp/rtmp.h"
};

#include "BlockedQueue.h"
#include "../AndroidLog.h"

class RobinPusher {
private:
    RTMP *rtmp= nullptr;
    BlockedQueue<RTMPPacket*> packetQueue;
    pthread_t threadSend;
    bool isRunning = false;
    void startSendLoop();
    static void *_startSendLoop(void* data);
public:
    RobinPusher();
    ~RobinPusher();
    void connect(const char* url);
    void close();
};


#endif //ROBINPLAYER_ROBINPUSHER_H
