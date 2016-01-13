/*
 * Copyright (C) 2011 http://www.csie.ntu.edu.tw/~cjlin/libsvm/
 * Ported by likunarmstrong@gmail.com
 */

#ifndef TRAIN_H
#define TRAIN_H

int train(const char *trainingFile, int kernelType, int cost,
        float gamma, int isProb, const char *modelFile);

#endif
