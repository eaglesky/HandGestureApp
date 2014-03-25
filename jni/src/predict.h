/*
 * Copyright (C) 2011 http://www.csie.ntu.edu.tw/~cjlin/libsvm/
 * Ported by likunarmstrong@gmail.com
 */

#ifndef PREDICT_H
#define PREDICT_H

int predict(float **values, int **indices, int rowNum, int colNum, int isProb,
        const char *modelFile, int *labels, double* prob_estimates);

#endif
