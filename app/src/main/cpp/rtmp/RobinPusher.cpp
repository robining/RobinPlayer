//
// Created by Robining on 2019/3/14.
//

#include "RobinPusher.h"

RobinPusher::RobinPusher() {
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
}

void RobinPusher::connect(const char *url) {
    if (RTMP_IsConnected(rtmp)) {
        RTMP_Close(rtmp);
    }

    RTMP_SetupURL(rtmp, const_cast<char *>(url));
    RTMP_EnableWrite(rtmp);

    //连接服务器
    if (!RTMP_Connect(rtmp, nullptr)) {
        LOGE(">>>connect rtmp failed");
        return;
    }
    //连接流
    if (!RTMP_ConnectStream(rtmp, 0)) {
        LOGE(">>>connect rtmp stream failed");
        return;
    }

    LOGI(">>>connect rtmp stream success");
    startTime = RTMP_GetTime();
    pthread_create(&threadSend, NULL, _startSendLoop, this);
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
        RTMP_SendPacket(rtmp, packet, 1);
        LOGI(">>>send a packet,%d",packet->m_nTimeStamp);
        RTMPPacket_Free(packet);
    }
}

void RobinPusher::pushAudio(char *data, int length) {
    int bodySize = length + 2;
    auto *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, static_cast<uint32_t>(bodySize));
    RTMPPacket_Reset(packet);
    char *body = packet->m_body;
    body[0] = 0xAF;
    body[1] = 0x01;
    memcpy(&body[2], data, length);

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = bodySize;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    packetQueue.push(packet);
}
