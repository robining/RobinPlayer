//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_ROBINPLAYER_H
#define ROBINPLAYER_ROBINPLAYER_H

#include "IPlayer.h"
class RobinPlayer : public IPlayer {
public:
    RobinPlayer();
    int init(const char* url);
    void play();
    void pause();
    void stop();
    void release();
};


#endif //ROBINPLAYER_ROBINPLAYER_H
