LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := native_activity
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	/home/allen/AndroidStudioProjects/native-activity/openCVSamplenativeactivity/src/main/jni/native.cpp \
	/home/allen/AndroidStudioProjects/native-activity/openCVSamplenativeactivity/src/main/jni/Android.mk \
	/home/allen/AndroidStudioProjects/native-activity/openCVSamplenativeactivity/src/main/jni/Application.mk \

LOCAL_C_INCLUDES += /home/allen/AndroidStudioProjects/native-activity/openCVSamplenativeactivity/src/main/jni
LOCAL_C_INCLUDES += /home/allen/AndroidStudioProjects/native-activity/openCVSamplenativeactivity/src/debug/jni

include $(BUILD_SHARED_LIBRARY)
