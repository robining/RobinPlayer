//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_IPLAYER_H
#define ROBINPLAYER_IPLAYER_H

#define ERROR_CODE_OPEN_INPUT_FAILED -1

class IPlayer {
public:
    virtual int init(const char *url) = 0;

    virtual void play() = 0;

    virtual void pause() = 0;

    virtual void stop() = 0;

    virtual void release() = 0;
};

#endif //ROBINPLAYER_IPLAYER_H
