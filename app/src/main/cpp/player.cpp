#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "player/IPlayer.h"
#include "player/RobinPlayer.h"
#include "player/bridge/JavaBridge.h"

//---------------------------------------------------inner impl----------------------------------------------------------
IPlayer *player;
JavaVM *javaVM;
extern "C" JNIEXPORT jint
JNICALL
JNI_OnLoad(JavaVM *jvm, void *args) {
    javaVM = jvm;
    return JNI_VERSION_1_6;
}

//---------------------------------------------------external interface---------------------------------------------------
extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativeInit(
        JNIEnv *env,
        jobject jobj,
        jobject bridge) {
    JavaBridge::getInstance()->init(javaVM, env,
                                    env->NewGlobalRef(bridge), pthread_self());
    player = new RobinPlayer();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativePrepare(
        JNIEnv *env,
        jobject jobj,
        jstring path) {
    player->init(env->GetStringUTFChars(path, 0));
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativePlay(
        JNIEnv *env,
        jobject jobj) {
    player->play();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativePause(
        JNIEnv *env,
        jobject jobj) {
    player->pause();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativeResume(
        JNIEnv *env,
        jobject jobj) {
    player->resume();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativeStop(
        JNIEnv *env,
        jobject jobj) {
    player->stop();
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativeDestroy(
        JNIEnv *env,
        jobject jobj) {
    player->release();
    delete player;
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativeSeekTo(
        JNIEnv *env,
        jobject jobj,
        jint seconds) {
    player->seekTo(seconds);
}

extern "C" JNIEXPORT void
JNICALL
Java_com_robining_robinplayer_player_RobinPlayer_nativeSetAudioChannel(
        JNIEnv *env,
        jobject jobj,
        jint channel) {
    player->setAudioChannel(static_cast<AUDIO_CHANNEL_TYPE>(channel));
}