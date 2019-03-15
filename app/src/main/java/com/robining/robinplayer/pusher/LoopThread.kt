package com.robining.robinplayer.pusher

abstract class LoopThread : Thread() {
    private var isClosed = false
    var isRunning = false
    override fun run() {
        super.run()
        isRunning = true
        onLoopStart()
        while (!isClosed) {
            if (!loopRun()) {
                break
            }
        }
        onLoopEnd()
        isRunning = false
    }

    abstract fun loopRun(): Boolean
    open fun onLoopEnd() {}
    open fun onLoopStart() {}

    fun exit() {
        isClosed = true
    }

    fun exitWithJoin() {
        exit()
        if (isRunning) {
            this.join()
        }
    }
}