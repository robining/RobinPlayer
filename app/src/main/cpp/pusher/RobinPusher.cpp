//
// Created by Robining on 2019/3/14.
//

#include <malloc.h>
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
    if (!RTMP_Connect(rtmp, NULL)) {
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
        LOGI(">>>send a packet,%d", packet->m_nTimeStamp);
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

void RobinPusher::pushSpsAndPps(char *sps, int spsLength, char *pps, int ppsLength) {
    int bodysize = spsLength + ppsLength + 16;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodysize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;

    int i = 0;

    body[i++] = 0x17;

    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    body[i++] = 0x01;
    body[i++] = sps[1];
    body[i++] = sps[2];
    body[i++] = sps[3];

    body[i++] = 0xFF;

    body[i++] = 0xE1;
    body[i++] = (spsLength >> 8) & 0xff;
    body[i++] = spsLength & 0xff;
    memcpy(&body[i], sps, spsLength);
    i += spsLength;

    body[i++] = 0x01;
    body[i++] = (ppsLength >> 8) & 0xff;
    body[i++] = ppsLength & 0xff;
    memcpy(&body[i], pps, ppsLength);

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = bodysize;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    packetQueue.push(packet);
}

void RobinPusher::pushVideo(char *data, int data_len, bool keyframe) {
    int bodysize = data_len + 9;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet, bodysize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;
    int i = 0;

    if (keyframe) {
        body[i++] = 0x17;
    } else {
        body[i++] = 0x27;
    }

    body[i++] = 0x01;
    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00;

    body[i++] = (data_len >> 24) & 0xff;
    body[i++] = (data_len >> 16) & 0xff;
    body[i++] = (data_len >> 8) & 0xff;
    body[i++] = data_len & 0xff;
    memcpy(&body[i], data, data_len);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodysize;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    packet->m_hasAbsTimestamp = 0;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    packetQueue.push(packet);
}
