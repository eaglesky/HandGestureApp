LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /home/allenchin/OpenCV-2.4.8-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := HandGestureApp
LOCAL_SRC_FILES := jni_process.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES += $(JNI_H_INCLUDE)

LOCAL_SRC_FILES := \
    jni/onload.cpp \
    jni/info_kunli_androidlibsvmexample_AndroidLibsvmExampleActivity.cpp \
    src/train.cpp \
    src/predict.cpp \
    src/svm/svm-train.cpp \
    src/svm/svm-predict.cpp \
    src/svm/svm.cpp



LOCAL_MODULE    := libsignal
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
