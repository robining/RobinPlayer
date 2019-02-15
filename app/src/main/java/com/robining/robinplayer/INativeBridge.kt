package com.robining.robinplayer

interface INativeBridge {
    fun onPlayStateChanged(oldState: Int, newState: Int)
}