//
// Created by Robining on 2019/1/11.
//

#include "IStreamDecoder.h"

IStreamDecoder::IStreamDecoder(AVStream *stream, AVCodecContext *avCodecContext) {
    this->codecContext = avCodecContext;
    this->stream = stream;
    pthread_mutex_init(&mutexDecodePacket, NULL);
    pthread_cond_init(&condPacketQueueHaveData, NULL);
    pthread_mutex_init(&mutexDecodeFrame, NULL);
    pthread_cond_init(&condFrameQueueHaveFrame, NULL);
    pthread_cond_init(&condPacketBufferFulled, NULL);

    pthread_cond_init(&condIsSeeking, NULL);

    startLoopDecodeThread();
}

void *__loopDecodePacketQueue(void *data) {
    IStreamDecoder *obj = static_cast<IStreamDecoder *>(data);
    obj->processPacketQueue();
    pthread_exit(&obj->decodePacketThread);
}

void IStreamDecoder::startLoopDecodeThread() {
    pthread_create(&decodePacketThread, NULL, __loopDecodePacketQueue, this);
}

void IStreamDecoder::processPacketQueue() {
    if (isRunning) {
        return;
    }
    isRunning = true;
    LOGI(">>>start decoder...:%d", (unsigned int) pthread_self());
    while (isRunning) {
        //获取队列最前端的Packet
        pthread_mutex_lock(&mutexDecodePacket);
//        if (seeking) {
//            LOGI(">>>-------------decode packet,wait seek complete...");
//            pthread_cond_wait(&condIsSeeking, NULL);
//            LOGI(">>>-------------decode packet,seek completed...");
//        }
        if (packetQueue.size() == 0) {
            LOGI(">>>-------------decode packet,wait packet queue fill data...");
            pthread_cond_wait(&condPacketQueueHaveData, &mutexDecodePacket);
            LOGI(">>>-------------decode packet,packet queue filled data...");
        }
        AVPacket *packet = packetQueue.front();
        packetQueue.pop();

        if (packetQueue.size() <= 40) {
            pthread_cond_signal(&condPacketBufferFulled);
        }
        pthread_mutex_unlock(&mutexDecodePacket);
        LOGI(">>>ready to decode a packet...");

        int result = avcodec_send_packet(codecContext, packet);
        if (result < 0) {
            if (result == AVERROR_EOF) {//the end of file
                LOGE(">>>send packet completed!");
                continue;
            } else {//found a exception
                LOGE(">>>send packet:%s", av_err2str(result));
                continue;
            }
        }

        while (isRunning) {
            AVFrame *frame = av_frame_alloc();
            result = avcodec_receive_frame(codecContext, frame);
            if (result < 0) {
                if (result == AVERROR_EOF) {//the end of file
                    LOGE(">>>receive frame completed!");
                    break;
                } else {//found a exception
                    LOGE(">>>receive frame:%s", av_err2str(result));
                    break;
                }
            }

            if (seeking) {
                break; //丢弃当前播放的数据
            }

            if (framesQueue.size() > 40) {
                LOGI(">>>frame queue is full,wait frame queue pop...");
                pthread_cond_wait(&condFrameBufferFulled, NULL);
            }

            pthread_mutex_lock(&mutexDecodeFrame);
            framesQueue.push(frame);
            LOGI(">>>enqueue a frame...");
            pthread_cond_signal(&condFrameQueueHaveFrame);
            pthread_mutex_unlock(&mutexDecodeFrame);
        }


        LOGI(">>>decoded a packet...");
    }
}

void IStreamDecoder::enqueue(AVPacket *packet) {
    if (seeking) {
        return;
    }

    LOGI(">>>enqueue a packet");
    if (packetQueue.size() > 40) {
        LOGI(">>>packet queue is full,wait packet queue pop...");
        pthread_cond_wait(&condPacketBufferFulled, NULL);
    }
    pthread_mutex_lock(&mutexDecodePacket);
    packetQueue.push(packet);
    pthread_cond_signal(&condPacketQueueHaveData);
    pthread_mutex_unlock(&mutexDecodePacket);
}

AVFrame *IStreamDecoder::popFrame() {
    LOGI(">>>popframe start...at thread:%d", (unsigned int) pthread_self());
    pthread_mutex_lock(&mutexDecodeFrame);
    if (framesQueue.size() == 0) {
        pthread_cond_wait(&condFrameQueueHaveFrame, &mutexDecodeFrame);
    }
    AVFrame *frame = framesQueue.front();
    framesQueue.pop();

    if (framesQueue.size() <= 40) {
        pthread_cond_signal(&condFrameBufferFulled);
    }
    pthread_mutex_unlock(&mutexDecodeFrame);

    LOGI(">>>popframe end...");
    return frame;
}


void IStreamDecoder::start() {

}

void IStreamDecoder::pause() {
    isPaused = true;
}

void IStreamDecoder::resume() {
    isPaused = false;
}

void IStreamDecoder::stop() {
    isRunning = false;
}

void IStreamDecoder::release() {
    if (isRunning) {
        stop();
    }

    clearQueue();
}

IStreamDecoder::~IStreamDecoder() {
    clearQueue();

    pthread_mutex_destroy(&mutexDecodePacket);
    pthread_mutex_destroy(&mutexDecodeFrame);
    pthread_cond_destroy(&condPacketQueueHaveData);
    pthread_cond_destroy(&condFrameQueueHaveFrame);
    pthread_exit(&decodePacketThread);
}

void IStreamDecoder::clearQueue() {
    std::queue<AVPacket *> empty;
    std::swap(empty, packetQueue);
    std::queue<AVFrame *> emptyFrame;
    std::swap(emptyFrame, framesQueue);
    pthread_cond_signal(&condPacketBufferFulled);
    pthread_cond_signal(&condFrameBufferFulled);
}

void IStreamDecoder::changeSeekingState(bool isSeeking) {
    pthread_mutex_lock(&mutexDecodePacket);
    this->seeking = isSeeking;
    if (!isSeeking) {
//        pthread_cond_signal(&condIsSeeking);
    } else {
        clearQueue();
    }
    pthread_mutex_unlock(&mutexDecodePacket);
}

