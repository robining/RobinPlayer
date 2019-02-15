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

extern "C" {
#include "../include/libavformat/avformat.h"
#include "../include/libavcodec/avcodec.h"
#include "../include/libavutil/opt.h"
}
enum PLAYER_STATE{
    NOT_INIT,
    INITING,
    INITED,
    INIT_FAILED,
    PLAYING,
    PAUSED,
    STOPED
};
class RobinPlayer : public IPlayer {
private:
    AVFormatContext *avFormatContext = NULL;
    std::vector<IStreamDecoder *> streamDecoders;
    PLAYER_STATE state = NOT_INIT;
    pthread_mutex_t mutex_init;
    pthread_cond_t cond_init_over;
    /**
     * 播放器状态更改
     * @param oldState 上一次的状态
     * @param state 新的状态
     */
    void stateChanged(PLAYER_STATE oldState,PLAYER_STATE state);
    void stateChanged(PLAYER_STATE state);
    /**
     * 播放器发送错误，但是不可忽略
     * @param code 错误码
     * @param msg 错误消息
     */
    void onError(int code, const char* msg);
    /**
     * 播放器发送错误，但是可以忽略错误继续运行
     * @param code
     * @param msg
     */
    void onWarn(int code, const char* msg);
public:
    RobinPlayer();

    const char *url;
    pthread_t thread_init;
    pthread_t thread_play;

    void initInternal(const char *url);

    void init(const char *url);

    void playInternal();

    void play();

    void pause();

    void resume();

    void stop();

    void release();
};


#endif //ROBINPLAYER_ROBINPLAYER_H
