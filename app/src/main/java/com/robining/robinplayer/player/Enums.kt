package com.robining.robinplayer.player

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