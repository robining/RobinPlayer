//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_IPLAYER_H
#define ROBINPLAYER_IPLAYER_H

#define ERROR_CODE_OPEN_INPUT_FAILED -100

#define ERROR_CODE_FIND_STREAM_FAILED -200
#define ERROR_CODE_NOT_FOUND_AVALIABLE_STREAM -201

#define ERROR_CODE_DECODER_NOT_FOUND -300
#define ERRPR_CODE_DECODER_PARAMETER_INIT_FAILED -301
#define ERRPR_CODE_DECODER_OPEN_FAILED -302


class IPlayer {
public:
    virtual int init(const char *url) = 0;

    virtual void play() = 0;

    virtual void pause() = 0;

    virtual void resume() = 0;

    virtual void stop() = 0;

    virtual void release() = 0;
};

#endif //ROBINPLAYER_IPLAYER_H
