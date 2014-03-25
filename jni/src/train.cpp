#include <jni.h>
#include <string.h>
#include <stdio.h>
#include "log.h"
#include "train.h"
#include "./svm/svm-train.h"

#define LOG_TAG "TRAIN"
#define PARA_LEN 12 // Max length for each parameter

int train(const char *trainingFile, int kernelType, int cost, float gamma,
        int isProb, const char *modelFile) {
    int cmdLen = 11;
    char *cmd[cmdLen];

    cmd[0] = "donotcare";

    cmd[1] = "-t";
    cmd[2] = (char *)calloc(PARA_LEN, sizeof(char));
    sprintf(cmd[2], "%d", kernelType); // Too lazy to implement a itoa ^_^

    cmd[3] = "-c";
    cmd[4] = (char *)calloc(PARA_LEN, sizeof(char));
    sprintf(cmd[4], "%d", cost);

    cmd[5] = "-g";
    cmd[6] = (char *)calloc(PARA_LEN, sizeof(char));
    sprintf(cmd[6], "%.2f", gamma);

    cmd[7] = "-b";
    cmd[8] = (char *)calloc(PARA_LEN, sizeof(char));
    sprintf(cmd[8], "%d", isProb);
	
    int len = strlen(trainingFile);
    cmd[9] = (char *)calloc(len+1, sizeof(char));
    strncpy(cmd[9], trainingFile, len);
    cmd[9][len] = '\0';

    len = strlen(modelFile);
    cmd[10] = (char *)calloc(len+1, sizeof(char));
    strncpy(cmd[10], modelFile, len);
    cmd[10][len] = '\0';

    int result = svmtrain(cmdLen, cmd);

    // Clean up space
    for (int i = 2; i < cmdLen; i += 2) {
        free(cmd[i]);
        cmd[i] = NULL;
    }
    free(cmd[9]);
    cmd[9] = NULL;

    return result;
}
