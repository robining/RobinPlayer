//
// Created by Robining on 2019/3/14.
//

#include "RobinPusher.h"

RobinPusher::RobinPusher() {
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
}

void RobinPusher::connect(const char *url) {
    if(RTMP_IsConnected(rtmp)){
        RTMP_Close(rtmp);
    }

    RTMP_SetupURL(rtmp, const_cast<char *>(url));
    RTMP_EnableWrite(rtmp);

    //连接服务器
    if(!RTMP_Connect(rtmp, nullptr)){
        LOGE(">>>connect rtmp failed");
        return;
    }
    //连接流
    if(!RTMP_ConnectStream(rtmp, 0)){
        LOGE(">>>connect rtmp stream failed");
        return;
    }

    LOGI(">>>connect rtmp stream success");
//    pthread_create(&threadSend, NULL, _startSendLoop, this);
}

void RobinPusher::close() {
    isRunning = false;
    packetQueue.clear();
    RTMP_Close(rtmp);
    RTMP_Free(rtmp);
}

RobinPusher::~RobinPusher() {

}

void *RobinPusher::_startSendLoop(void *data) {
    auto *pusher = static_cast<RobinPusher *>(data);
    pusher->startSendLoop();
    pthread_t self = pthread_self();
    pthread_exit(&self);
}

void RobinPusher::startSendLoop() {
    isRunning = true;
    while (isRunning) {
        RTMPPacket *packet = packetQueue.pop();
        LOGI(">>>send a packet");
        RTMPPacket_Free(packet);
    }
}
