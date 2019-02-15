//
// Created by Robining on 2019/2/15.
//

#include "JavaBridge.h"

JNIEnv *JavaBridge::getJniEnv() {
    if (isInited()) {
        pthread_t currentThread = pthread_self();
        bool nowIsMainThread = currentThread == mainThread;
        if (nowIsMainThread) {
            return mainJniEnv;
        } else {
            JNIEnv *jniEnv;
            this->javaVM->AttachCurrentThread(&jniEnv, NULL);
            return jniEnv;
        }
    } else {
        return NULL;
    }
}

void JavaBridge::onPlayStateChanged(PLAYER_STATE oldState, PLAYER_STATE newState) {
    if (isInited()) {
        JNIEnv *jniEnv = getJniEnv();
        jclass cls = jniEnv->GetObjectClass(this->javaBridgeObject);
        jmethodID method = jniEnv->GetMethodID(cls, "onPlayStateChanged", "(II)V");
        jniEnv->CallVoidMethod(javaBridgeObject, method, (int) oldState, (int) newState);

        if(jniEnv != mainJniEnv){ //不是在主线程
            this->javaVM->DetachCurrentThread();
        }
    }
}

bool JavaBridge::isInited() {
    return this->javaVM != NULL && mainJniEnv != NULL && javaBridgeObject != NULL &&
           mainThread != NULL;
}

JavaBridge *JavaBridge::getInstance() {
    static JavaBridge javaBridge;
    return &javaBridge;
}

void JavaBridge::init(JavaVM *javaVM, JNIEnv *mainJniEnv, jobject javaBridgeObject,
                      pthread_t mainThread) {
    this->javaVM = javaVM;
    this->mainJniEnv = mainJniEnv;
    this->javaBridgeObject = javaBridgeObject;
    this->mainThread = mainThread;
}



