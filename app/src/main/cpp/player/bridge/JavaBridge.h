//
// Created by Robining on 2019/2/15.
//

#ifndef ROBINPLAYER_JAVABRIDGE_H
#define ROBINPLAYER_JAVABRIDGE_H

#include "../Constants.h"
#include <jni.h>
#include <cstdlib>
#include <pthread.h>
#include "../../AndroidLog.h"

class JavaBridge {
private:
    JavaVM* javaVM = NULL;
    JNIEnv* mainJniEnv = NULL;
    jobject javaBridgeObject = NULL;
    pthread_t mainThread = NULL;
    JNIEnv* getJniEnv();
    bool isInited();
public:
    static JavaBridge* getInstance();
    void init(JavaVM* javaVM,JNIEnv* mainJniEnv,jobject javaBridgeObject,pthread_t mainThread);
    void onPlayStateChanged(PLAYER_STATE oldState,PLAYER_STATE newState);
    void onError(int code, const char* message);
    void onWarn(int code, const char* message);
    void onGotTotalDuration(double duration);
    void onProgressChanged(double progress);
    void onPreloadProgressChanged(double progress);
};


#endif //ROBINPLAYER_JAVABRIDGE_H
