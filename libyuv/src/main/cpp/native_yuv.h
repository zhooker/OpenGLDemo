/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_onzhou_opengles_color_NativeColorRenderer */

#ifndef NATIVE_YUV_H
#define NATIVE_YUV_H
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL yuv2rgb(JNIEnv *, jclass, jstring, jint, jint, jint, jobject);

JNIEXPORT jbyteArray JNICALL convertYuv2Rgb(JNIEnv *, jclass , jbyteArray , jint , jint );

JNIEXPORT void JNICALL yuv2Surface(JNIEnv *, jclass, jbyteArray, jint, jint, jobject);

#ifdef __cplusplus
}
#endif
#endif