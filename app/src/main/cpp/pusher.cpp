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
        jbyteArray data_,
        jint length) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    pusher->pushAudio(reinterpret_cast<char *>(data), length);
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_pusher_RobinPusher_nativePushVideo(
        JNIEnv *env,
        jobject jobj,
        jbyteArray data_,
        jint length,
        jboolean isKeyFrame) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    pusher->pushVideo(reinterpret_cast<char *>(data), length, isKeyFrame);
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_pusher_RobinPusher_nativePushSpsAndPps(
        JNIEnv *env,
        jobject jobj,
        jbyteArray sps_,
        jint spsLength,
        jbyteArray pps_,
        jint ppsLength) {

    jbyte *sps = env->GetByteArrayElements(sps_, NULL);
    jbyte *pps = env->GetByteArrayElements(pps_, NULL);
    pusher->pushSpsAndPps(reinterpret_cast<char *>(sps), spsLength, reinterpret_cast<char *>(pps),
                          ppsLength);
    env->ReleaseByteArrayElements(sps_, sps, 0);
    env->ReleaseByteArrayElements(pps_, pps, 0);


}