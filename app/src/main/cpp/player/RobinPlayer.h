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
public:
    RobinPlayer();

    int init(const char *url);

    void play();

    void pause();

    void resume();

    void stop();

    void release();
};


#endif //ROBINPLAYER_ROBINPLAYER_H
