//
// Created by Robining on 2019/3/15.
//
#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "pusher/RobinPusher.h"

RobinPusher *pusher = NULL;

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_pusher_RobinPusher_nativeConnectPusher(
        JNIEnv *env,
        jobject jobj,
        jstring url) {
    pusher = new RobinPusher();
    pusher->connect(env->GetStringUTFChars(url, JNI_FALSE));
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_pusher_RobinPusher_nativePushAudio(
        JNIEnv *env,
        jobject jobj,
        jbyteArray data,
        jint length) {
    pusher->pushAudio(reinterpret_cast<char *>(data), length);
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_pusher_RobinPusher_nativePushSpsAndPps(
        JNIEnv *env,
        jobject jobj,
        jbyteArray sps,
        jint spsLength,
        jbyteArray pps,
        jint ppsLength) {
    pusher->pushSpsAndPps(reinterpret_cast<char *>(sps), spsLength, reinterpret_cast<char *>(pps),
                          ppsLength);
}