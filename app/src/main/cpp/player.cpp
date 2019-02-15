#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "player/IPlayer.h"
#include "player/RobinPlayer.h"
//---------------------------------------------------inner impl----------------------------------------------------------
JavaVM *javaVM;
IPlayer *player = new RobinPlayer();

extern "C" JNIEXPORT jint
JNICALL
JNI_OnLoad(JavaVM *jvm, void *args){
    javaVM = jvm;
    return JNI_VERSION_1_6;
}

//---------------------------------------------------external interface---------------------------------------------------
extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_RobinPlayer_nativeInit(
        JNIEnv *env,
        jobject jobj,
        jstring path) {
    player->init(env->GetStringUTFChars(path, 0));
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_RobinPlayer_nativePlay(
        JNIEnv *env,
        jobject jobj) {
    player->play();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_RobinPlayer_nativePause(
        JNIEnv *env,
        jobject jobj) {
    player->pause();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_RobinPlayer_nativeResume(
        JNIEnv *env,
        jobject jobj) {
    player->resume();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_RobinPlayer_nativeStop(
        JNIEnv *env,
        jobject jobj) {
    player->stop();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_RobinPlayer_nativeDestroy(
        JNIEnv *env,
        jobject jobj) {
    player->release();
}