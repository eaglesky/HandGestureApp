//
// Created by allen on 1/9/16.
//

#include "com_example_allen_helloworld_MyNDK.h"

JNIEXPORT jstring JNICALL Java_com_example_allen_helloworld_MyNDK_getMyString
        (JNIEnv* env, jobject) {
    return env->NewStringUTF("Yalun's jni call!");
}