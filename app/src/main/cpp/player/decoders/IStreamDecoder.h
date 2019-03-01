//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_ISTREAMDECODER_H
#define ROBINPLAYER_ISTREAMDECODER_H


#include "../../AndroidLog.h"
#include <queue>
#include <pthread.h>
#include "../bridge/JavaBridge.h"
#include "SyncHandler.h"

extern "C" {
#include "../../include/libavformat/avformat.h"
#include "../../include/libavcodec/avcodec.h"
#include "../../include/libavutil/time.h"
};

class IStreamDecoder {
protected:
    AVStream* stream;
    std::queue<AVPacket *> packetQueue;
    std::queue<AVFrame *> framesQueue;
    AVCodecContext* codecContext;
    bool isRunning = false;
    bool isPaused = false;
    bool seeking = false;
    SyncHandler *syncHandler = NULL;

    AVFrame* popFrame();
    virtual void processPacketQueue();
    virtual void processPacket(AVPacket* packet);
private:
    const int MAX_QUEUE_SIZE = 50; //注意配置 否则可能会出现内存溢出
    pthread_t decodePacketThread;

    pthread_mutex_t mutexDecodePacket;
    pthread_cond_t condPacketQueueHaveData;

    pthread_mutex_t mutexDecodeFrame;
    pthread_cond_t condFrameQueueHaveFrame;

    pthread_cond_t condPacketBufferFulled;
    pthread_cond_t condFrameBufferFulled;

    pthread_cond_t condIsSeeking;
    pthread_cond_t condIsPaused;

    pthread_mutex_t mutexSeeking;
    void startLoopDecodeThread();
    static void *__loopDecodePacketQueue(void *data);
public:
    IStreamDecoder(AVStream* stream,AVCodecContext* avCodecContext,SyncHandler* syncHandler);
    ~IStreamDecoder();

    virtual void enqueue(AVPacket *packet);

    virtual void pause();

    virtual void resume();

    virtual void stop();

    virtual void start();

    void clearQueue();

    void changeSeekingState(bool isSeeking);
};


#endif //ROBINPLAYER_ISTREAMDECODER_H
