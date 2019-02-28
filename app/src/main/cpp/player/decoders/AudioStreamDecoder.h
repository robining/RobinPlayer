//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_AUDIOSTREAMDECODER_H
#define ROBINPLAYER_AUDIOSTREAMDECODER_H

#include "IStreamDecoder.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "../../soundtouch/include/SoundTouch.h"

extern "C" {
#include "../../include/libswresample/swresample.h"
};
using namespace std;
using namespace soundtouch;

class AudioStreamDecoder : public IStreamDecoder {
public:
    pthread_t playerThread;

    AudioStreamDecoder(AVStream *avStream, AVCodecContext *codecContext,SyncHandler* syncHandler);

    ~AudioStreamDecoder();

    void initPlayer();

    void playFrame();

    void start();

    void pause();

    void resume();

    void stop();

    void release();

    void setAudioChannel(AUDIO_CHANNEL_TYPE channelType);

private:
    uint8_t *outBuffer = NULL;

    SLObjectItf engineObjItf = NULL;
    SLEngineItf engineItf = NULL;

    SLObjectItf outputMixObjItf = NULL;
    SLEnvironmentalReverbItf environmentalReverbItf = NULL;

    SLObjectItf playerObjItf = NULL;
    SLPlayItf playItf = NULL;

    SLMuteSoloItf muteSoloItf = NULL;

    SLAndroidSimpleBufferQueueItf androidSimpleBufferQueueItf = NULL;

    SoundTouch *soundTouch = NULL;
    SAMPLETYPE *soundSampleInBuffer = NULL;
    SAMPLETYPE *soundSampleOutBuffer = NULL;

    bool readFinishedSoundSamples = true;
    int currentSoundSamples = 0;

    int* readOneFrame();


    void sureSLResultSuccess(SLresult result, const char *message);
};


#endif //ROBINPLAYER_AUDIOSTREAMDECODER_H
