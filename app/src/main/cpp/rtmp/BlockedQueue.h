//
// Created by Robining on 2019/3/14.
//

#ifndef ROBINPLAYER_BLOCKQUEUE_H
#define ROBINPLAYER_BLOCKQUEUE_H

#include <queue>
#include <pthread.h>
using namespace std;

template <typename T>

class BlockedQueue {
private:
    pthread_mutex_t mutexOperate;
    pthread_cond_t condFulled;
    pthread_cond_t condIsEmpty;
    queue<T> innerQueue = queue<T>();
public:
    BlockedQueue();
    void push(T t);
    T pop();
    void clear();
};


template<typename T>
BlockedQueue<T>::BlockedQueue() {
    pthread_mutex_init(&mutexOperate, NULL);
    pthread_cond_init(&condFulled, NULL);
    pthread_cond_init(&condIsEmpty, NULL);
}

template<typename T>
void BlockedQueue<T>::push(T t) {
    pthread_mutex_lock(&mutexOperate);
    innerQueue.push(t);
    pthread_cond_signal(&condIsEmpty);
    pthread_mutex_unlock(&mutexOperate);
}

template<typename T>
T BlockedQueue<T>::pop() {
    pthread_mutex_lock(&mutexOperate);
    if (innerQueue.size() == 0) {
        pthread_cond_wait(&condIsEmpty, &mutexOperate);
    }
    T result = innerQueue.front();
    innerQueue.pop();
    pthread_mutex_unlock(&mutexOperate);
    return result;
}

template<typename T>
void BlockedQueue<T>::clear() {
    pthread_mutex_lock(&mutexOperate);
    queue<T> emptyFrame;
    swap(emptyFrame, innerQueue);
    pthread_mutex_unlock(&mutexOperate);
}
#endif //ROBINPLAYER_BLOCKQUEUE_H
