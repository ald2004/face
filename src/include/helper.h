#pragma once
#ifndef __HELPER_H_
#define __HELPER_H_

#include <iostream>
#include <iostream>
#include <fstream>

#include "opencv2/opencv.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/objdetect/objdetect.hpp"

#include "cereal/cereal.hpp"
#include "cereal/types/string.hpp"
#include "cereal/types/vector.hpp"
#include "cereal/archives/binary.hpp"
#include "cereal_extension/mat_cerealisation.hpp"

#include "modelcfg.h"

template<class T = int>
static cv::Rect_<T> get_enclosing_bbox(cv::Mat landmarks) {
    auto num_landmarks = landmarks.cols / 2;
    double min_x_val, max_x_val, min_y_val, max_y_val;
    cv::minMaxLoc(landmarks.colRange(0, num_landmarks), &min_x_val, &max_x_val);
    cv::minMaxLoc(landmarks.colRange(num_landmarks, landmarks.cols), &min_y_val, &max_y_val);
    double width = max_x_val - min_x_val;
    double height = max_y_val - min_y_val;
    return cv::Rect_<T>(min_x_val, min_y_val, width, height);
//    return cv::Rect_<T>(min_x_val, min_y_val, width, height);
}


/**
 * Performs an initial alignment of the model, by putting the mean model into
 * the center of the face box.
 *
 * An optional scaling and translation parameters can be given to generate
 * perturbations of the initialisation.
 *
 * Note 02/04/15: I think with the new perturbation code, we can delete the optional
 * parameters here - make it as simple as possible, don't include what's not needed.
 * Align and perturb should really be separate - separate things.
 *
 * @param[in] mean Mean model points.
 * @param[in] facebox A facebox to align the model to.
 * @param[in] scaling_x Optional scaling in x of the model.
 * @param[in] scaling_y Optional scaling in y of the model.
 * @param[in] translation_x Optional translation in x of the model.
 * @param[in] translation_y Optional translation in y of the model.
 * @return A cv::Mat of the aligned points.
 */
cv::Mat
align_mean(cv::Mat mean, cv::Rect facebox, float scaling_x = 1.0f, float scaling_y = 1.0f, float translation_x = 0.0f,
           float translation_y = 0.0f) {
    using cv::Mat;
    // Initial estimate x_0: Center the mean face at the [-0.5, 0.5] x [-0.5, 0.5] square (assuming the face-box is that square)
    // More precise: Take the mean as it is (assume it is in a space [-0.5, 0.5] x [-0.5, 0.5]), and just place it in the face-box as
    // if the box is [-0.5, 0.5] x [-0.5, 0.5]. (i.e. the mean coordinates get upscaled)
    Mat aligned_mean = mean.clone();
    Mat aligned_mean_x = aligned_mean.colRange(0, aligned_mean.cols / 2);
    Mat aligned_mean_y = aligned_mean.colRange(aligned_mean.cols / 2, aligned_mean.cols);
    aligned_mean_x = (aligned_mean_x * scaling_x + 0.5f + translation_x) * facebox.width + facebox.x;
    aligned_mean_y = (aligned_mean_y * scaling_y + 0.3f + translation_y) * facebox.height + facebox.y;
    return aligned_mean;
}

#endif
