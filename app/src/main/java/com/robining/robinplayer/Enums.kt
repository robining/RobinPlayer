package com.robining.robinplayer

enum class AUDIO_CHANNEL {
    LEFT,
    NONE,
    RIGHT
}

enum class PLAYER_STATE {
    NOT_INIT,
    INITING,
    INITED,
    INIT_FAILED,
    PLAYING,
    PAUSED,
    STOPED
}