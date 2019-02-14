//
// Created by Robining on 2019/1/11.
//

#ifndef ROBINPLAYER_VIDEOSTREAMDECODER_H
#define ROBINPLAYER_VIDEOSTREAMDECODER_H

#include "IStreamDecoder.h"

class VideoStreamDecoder : public IStreamDecoder{
public:
    VideoStreamDecoder(AVCodecContext* codecContext) : IStreamDecoder(codecContext){}
};


#endif //ROBINPLAYER_VIDEOSTREAMDECODER_H
