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
    RTMP *rtmp= NULL;
    BlockedQueue<RTMPPacket*> packetQueue;
    pthread_t threadSend;
    bool isRunning = false;
    void startSendLoop();
    static void *_startSendLoop(void* data);
    uint32_t startTime = 0;
public:
    RobinPusher();
    ~RobinPusher();
    void connect(const char* url);
    void close();
    void pushAudio(char* data,int length);
    void pushSpsAndPps(char* sps,int spsLength,char* pps,int ppsLength);
};


#endif //ROBINPLAYER_ROBINPUSHER_H
