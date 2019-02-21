//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_AUDIOSTREAMDECODER_H
#define ROBINPLAYER_AUDIOSTREAMDECODER_H

#include "IStreamDecoder.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "soundtouch/SoundTouch.h"

extern "C" {
    #include <libswresample/swresample.h>
};
using namespace std;

class AudioStreamDecoder : public IStreamDecoder {
public:
    pthread_t playerThread;
    AudioStreamDecoder(AVStream* avStream,AVCodecContext *codecContext);
    ~AudioStreamDecoder();

    void initPlayer();

    void playFrame();

    void start();

    void pause();

    void resume();

    void stop();

    void release();

private:
    uint8_t* outBuffer = NULL;

    SLObjectItf engineObjItf = NULL;
    SLEngineItf engineItf = NULL;

    SLObjectItf outputMixObjItf = NULL;
    SLEnvironmentalReverbItf environmentalReverbItf = NULL;

    SLObjectItf playerObjItf = NULL;
    SLPlayItf playItf = NULL;

    SLAndroidSimpleBufferQueueItf androidSimpleBufferQueueItf = NULL;

    void sureSLResultSuccess(SLresult result, const char *message);
};


#endif //ROBINPLAYER_AUDIOSTREAMDECODER_H
