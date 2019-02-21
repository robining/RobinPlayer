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

        if (jniEnv != mainJniEnv) { //不是在主线程
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

void JavaBridge::onError(int code, const char *message) {
    if (isInited()) {
        JNIEnv *jniEnv = getJniEnv();
        jclass cls = jniEnv->GetObjectClass(this->javaBridgeObject);
        jmethodID method = jniEnv->GetMethodID(cls, "onReceivedError", "(ILjava/lang/String;)V");
        jniEnv->CallVoidMethod(javaBridgeObject, method, code, jniEnv->NewStringUTF(message));

        if (jniEnv != mainJniEnv) { //不是在主线程
            this->javaVM->DetachCurrentThread();
        }
    }
}

void JavaBridge::onWarn(int code, const char *message) {
    if (isInited()) {
        JNIEnv *jniEnv = getJniEnv();
        jclass cls = jniEnv->GetObjectClass(this->javaBridgeObject);
        jmethodID method = jniEnv->GetMethodID(cls, "onReceivedWarn", "(ILjava/lang/String;)V");
        jniEnv->CallVoidMethod(javaBridgeObject, method, code, jniEnv->NewStringUTF(message));

        if (jniEnv != mainJniEnv) { //不是在主线程
            this->javaVM->DetachCurrentThread();
        }
    }
}

void JavaBridge::onGotTotalDuration(double duration) {
    if (isInited()) {
        JNIEnv *jniEnv = getJniEnv();
        jclass cls = jniEnv->GetObjectClass(this->javaBridgeObject);
        jmethodID method = jniEnv->GetMethodID(cls, "onReceivedTotalDuration", "(D)V");
        jniEnv->CallVoidMethod(javaBridgeObject, method, duration);

        if (jniEnv != mainJniEnv) { //不是在主线程
            this->javaVM->DetachCurrentThread();
        }
    }
}

void JavaBridge::onProgressChanged(double progress) {
    if (isInited()) {
        JNIEnv *jniEnv = getJniEnv();
        jclass cls = jniEnv->GetObjectClass(this->javaBridgeObject);
        jmethodID method = jniEnv->GetMethodID(cls, "onProgressChanged", "(D)V");
        jniEnv->CallVoidMethod(javaBridgeObject, method, progress);

        if (jniEnv != mainJniEnv) { //不是在主线程
            this->javaVM->DetachCurrentThread();
        }
    }
}

void JavaBridge::onPreloadProgressChanged(double progress) {
    if (isInited()) {
        JNIEnv *jniEnv = getJniEnv();
        jclass cls = jniEnv->GetObjectClass(this->javaBridgeObject);
        jmethodID method = jniEnv->GetMethodID(cls, "onPreloadProgressChanged", "(D)V");
        jniEnv->CallVoidMethod(javaBridgeObject, method, progress);

        if (jniEnv != mainJniEnv) { //不是在主线程
            this->javaVM->DetachCurrentThread();
        }
    }
}


