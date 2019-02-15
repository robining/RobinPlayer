//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_IPLAYER_H
#define ROBINPLAYER_IPLAYER_H

#define CODE_ERROR_COMMON -1
#define CODE_ERROR_OPEN_INPUT_FAILED -100
#define CODE_ERROR_FIND_STREAM_FAILED -200
#define CODE_ERROR_NOT_FOUND_AVALIABLE_STREAM -201
#define CODE_ERROR_NEED_CALL_INIT -300

#define CODE_WARN_DECODER_NOT_FOUND -1000
#define CODE_WARN_DECODER_PARAMETER_INIT_FAILED -1001
#define CODE_WARN_DECODER_OPEN_FAILED -1002
#define CODE_WARN_NOT_FOUND_STREAM_DECODER -2000

class IPlayer {
public:
    virtual void init(const char *url) = 0;

    virtual void play() = 0;

    virtual void pause() = 0;

    virtual void resume() = 0;

    virtual void stop() = 0;

    virtual void release() = 0;
};

#endif //ROBINPLAYER_IPLAYER_H
