//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_ROBINPLAYER_H
#define ROBINPLAYER_ROBINPLAYER_H

#include "IPlayer.h"
#include "../AndroidLog.h"
#include "decoders/IStreamDecoder.h"
#include "decoders/AudioStreamDecoder.h"
#include "decoders/VideoStreamDecoder.h"
#include <vector>
#include "Constants.h"

extern "C" {
#include "../include/libavformat/avformat.h"
#include "../include/libavcodec/avcodec.h"
#include "../include/libavutil/opt.h"
}

class RobinPlayer : public IPlayer {
private:
    const char *url;
    pthread_t thread_init;
    pthread_t thread_play;

    AVFormatContext *avFormatContext = NULL;
    std::vector<IStreamDecoder *> streamDecoders;
    PLAYER_STATE state = NOT_INIT;
    pthread_mutex_t mutex_init;
    pthread_cond_t cond_init_over;
    SyncHandler *syncHandler = NULL;

    pthread_t threadSeeking = NULL;
    pthread_cond_t condSeeking;
    bool seeking = false;

    int seekTargetSeconds = 0;
    static void *__seek(void* data);
    void seekInternal(int seconds);
    static void *__init(void *data);
    void initInternal(const char *url);
    static void *__play(void *data);
    void playInternal();


    /**
     * 播放器状态更改
     * @param oldState 上一次的状态
     * @param state 新的状态
     */
    void stateChanged(PLAYER_STATE oldState, PLAYER_STATE state);

    void stateChanged(PLAYER_STATE state);

    /**
     * 播放器发送错误，但是不可忽略
     * @param code 错误码
     * @param msg 错误消息
     */
    void onError(int code, const char *msg);

    /**
     * 播放器发送错误，但是可以忽略错误继续运行
     * @param code
     * @param msg
     */
    void onWarn(int code, const char *msg);

public:
    RobinPlayer();

    void init(const char *url);

    void play();

    void pause();

    void resume();

    void stop();

    void release();

    void seekTo(int seconds);

    void setAudioChannel(AUDIO_CHANNEL_TYPE channelType);
};


#endif //ROBINPLAYER_ROBINPLAYER_H
