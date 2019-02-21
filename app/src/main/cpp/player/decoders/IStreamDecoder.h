//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_ISTREAMDECODER_H
#define ROBINPLAYER_ISTREAMDECODER_H


#include "../../AndroidLog.h"
#include <queue>
#include <pthread.h>
#include "../bridge/JavaBridge.h"

extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
};

class IStreamDecoder {
protected:
    std::queue<AVPacket *> packetQueue;
    std::queue<AVFrame *> framesQueue;
    AVCodecContext* codecContext;
private:
    bool isRunning = false;
    bool isPaused = false;
    bool seeking = false;
    pthread_mutex_t mutexDecodePacket;
    pthread_cond_t condPacketQueueHaveData;

    pthread_mutex_t mutexDecodeFrame;
    pthread_cond_t condFrameQueueHaveFrame;

    pthread_cond_t condPacketBufferFulled;
    pthread_cond_t condFrameBufferFulled;

    pthread_cond_t condIsSeeking;
    void startLoopDecodeThread();
public:
    pthread_t decodePacketThread;
    AVStream* stream;

    IStreamDecoder(AVStream* stream,AVCodecContext* avCodecContext);
    ~IStreamDecoder();

    AVFrame* popFrame();

    virtual void processPacketQueue();

    virtual void enqueue(AVPacket *packet);

    virtual void pause();

    virtual void resume();

    virtual void stop();

    virtual void release();

    virtual void start();

    void clearQueue();

    void changeSeekingState(bool isSeeking);
};


#endif //ROBINPLAYER_ISTREAMDECODER_H
