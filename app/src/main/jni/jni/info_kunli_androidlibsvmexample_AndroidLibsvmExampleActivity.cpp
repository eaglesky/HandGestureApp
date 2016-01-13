#include <jni.h>
#include <string.h>
#include "../src/log.h"
#include "../src/predict.h"
#include "../src/train.h"

namespace example {

static jint trainClassifier(JNIEnv *env, jobject obj, jstring trainingFileS,
        jint kernelType, jint cost, jfloat gamma, jint isProb, jstring modelFileS) {
    jboolean isCopy;
    const char *trainingFile = env->GetStringUTFChars(trainingFileS, &isCopy);
    const char *modelFile = env->GetStringUTFChars(modelFileS, &isCopy);

    int v = train(trainingFile, kernelType, cost, gamma, isProb, modelFile);

    env->ReleaseStringUTFChars(trainingFileS, trainingFile);
    env->ReleaseStringUTFChars(modelFileS, modelFile);

    return v;
}

static jint doClassification(JNIEnv *env, jobject obj, jobjectArray valuesArr,
        jobjectArray indicesArr, jint isProb, jstring modelFiles,
        jintArray labelsArr ,jdoubleArray probsArr) {
    jboolean isCopy;
    const char *modelFile = env->GetStringUTFChars(modelFiles, &isCopy);
    int *labels = env->GetIntArrayElements(labelsArr, NULL);
    double *probs = env->GetDoubleArrayElements(probsArr, NULL);

    int rowNum = env->GetArrayLength(valuesArr);
    jfloatArray dim = (jfloatArray)env->GetObjectArrayElement(valuesArr, 0);
    int colNum = env->GetArrayLength(dim);
    float **values = (float **)calloc(rowNum, sizeof(float *));
    int **indices = (int **)calloc(rowNum, sizeof(int *));

    for (int i = 0; i < rowNum; i++) {
        jfloatArray vrows = (jfloatArray)env->GetObjectArrayElement(valuesArr, i);
        jintArray irows = (jintArray)env->GetObjectArrayElement(indicesArr, i);
        jfloat *velement = env->GetFloatArrayElements(vrows, NULL);
        jint *ielement = env->GetIntArrayElements(irows, NULL);
        values[i] = (float *)calloc(colNum, sizeof(float));
        indices[i] = (int *)calloc(colNum, sizeof(int));
        for (int j = 0; j < colNum; j++) {
            values[i][j] = velement[j];
            indices[i][j] = ielement[j];
        }

        env->ReleaseFloatArrayElements(vrows, velement, JNI_ABORT);
        env->ReleaseIntArrayElements(irows, ielement, JNI_ABORT);
    }

    int r = predict(values, indices, rowNum, colNum, isProb, modelFile, labels, probs);

    for (int i = 0; i < rowNum; i++) {
        free(values[i]);
        free(indices[i]);
    }
    env->ReleaseIntArrayElements(labelsArr, labels, 0);
    env->ReleaseDoubleArrayElements(probsArr, probs, 0);
    env->ReleaseStringUTFChars(modelFiles, modelFile);
    return r;
}

static JNINativeMethod sMethods[] = {
    /* name, signature, funcPtr */
    {"doClassificationNative", "([[F[[IILjava/lang/String;[I[D)I",
        (void*)doClassification},
    {"trainClassifierNative", "(Ljava/lang/String;IIFILjava/lang/String;)I",
        (void*)trainClassifier},
};

static int jniRegisterNativeMethods(JNIEnv *env, const char *className,
        JNINativeMethod* Methods, int numMethods) {
    jclass clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, Methods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}


int register_Signal(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/example/newdemo/MainActivity",
            sMethods, sizeof(sMethods) / sizeof(sMethods[0]));
}

}
