//
// Created by Robining on 2019/1/11.
//

#include "RobinPlayer.h"
#include "../AndroidLog.h"

extern "C" {
#include "../include/libavformat/avformat.h"
}

AVFormatContext *avFormatContext = NULL;

RobinPlayer::RobinPlayer() {
    av_register_all();
    avformat_network_init();
}

void onFailed(const char *title, const char *reason) {
    LOGE(">>>%s:%s", title, reason);
}

int RobinPlayer::init(const char *url) {
    LOGI(">>>player init...%s", url);
    try {
        int result = avformat_open_input(&avFormatContext, url, NULL, NULL);
        if (result < 0) {
            onFailed("open input", av_err2str(result));
            return ERROR_CODE_OPEN_INPUT_FAILED;
        }

        LOGI(">>>player open input success...");
    } catch (const char *reason) {
        LOGE(">>>%s", reason);
    }

}

void RobinPlayer::play() {
    LOGI(">>>player play...");
}

void RobinPlayer::pause() {
    LOGI(">>>player pause...");
}

void RobinPlayer::stop() {
    LOGI(">>>player stop...");
}

void RobinPlayer::release() {
    LOGI(">>>player release...");
}