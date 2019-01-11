//
// Created by Robining on 2019/1/11.
//

#include "AudioStreamDecoder.h"

void AudioStreamDecoder::enqueue(AVPacket *packet) {
    packetSize++;
    LOGI(">>>enqueue audio packets:%ld", packetSize);
}

void AudioStreamDecoder::pause() {
}

void AudioStreamDecoder::resume() {
}

void AudioStreamDecoder::stop() {
}

void AudioStreamDecoder::release() {
}
