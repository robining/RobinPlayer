//
// Created by Alpha on 2018/8/21.
//

#ifndef FFMPEG_LOG_H
#define FFMPEG_LOG_H
#include <android/log.h>
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"player",__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"player",__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,"player",__VA_ARGS__)
#endif //FFMPEG_LOG_H
