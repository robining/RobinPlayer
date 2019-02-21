//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_VIDEOSTREAMDECODER_H
#define ROBINPLAYER_VIDEOSTREAMDECODER_H

#include "IStreamDecoder.h"

class VideoStreamDecoder : public IStreamDecoder{
public:
    VideoStreamDecoder(AVStream* avStream,AVCodecContext* codecContext) : IStreamDecoder(avStream,codecContext){}
};


#endif //ROBINPLAYER_VIDEOSTREAMDECODER_H
