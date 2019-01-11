//
// Created by Robining on 2019/1/11.
//

#include "VideoStreamDecoder.h"

void VideoStreamDecoder::enqueue(AVPacket *packet) {
    packetSize++;
    LOGI(">>>enqueue video packets:%ld", packetSize);
}

void VideoStreamDecoder::pause() {
}

void VideoStreamDecoder::resume() {
}

void VideoStreamDecoder::stop() {
}

void VideoStreamDecoder::release() {
}