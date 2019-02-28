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
    AudioStreamDecoder(AVStream *avStream, AVCodecContext *codecContext,SyncHandler* syncHandler);

    ~AudioStreamDecoder();

    void pause();

    void resume();

    void stop();

    void setAudioChannel(AUDIO_CHANNEL_TYPE channelType);

private:
    pthread_t playerThread;

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
    SAMPLETYPE *soundSampleInOutBuffer = NULL;

    bool readFinishedSoundSamples = true;
    int currentSoundSamples = 0;

    static void *__initPlayer(void *data);
    void initPlayer();

    void startPlay();

    void playFrame();

    int* readOneFrame();

    static void __bufferQueueCallback(SLAndroidSimpleBufferQueueItf bf, void *pContext);

    void sureSLResultSuccess(SLresult result, const char *message);
};


#endif //ROBINPLAYER_AUDIOSTREAMDECODER_H
