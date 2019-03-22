//
// Created by wangxiaoming on 2019/3/19.
//

#include <iostream>
#include "face_sdk.h"
#include <recognize.h>
#include <detect.h>

#include "ldmarkmodel.h"
#include <FacePreprocess.h>
#include "file_util.h"

struct SDK {
    Face::Detect *mDetect;
    Face::Recognize *mRecognize;
    int threadNum = 3;
    const char *tFaceModelDir = "";
    bool inited = false;
    ldmarkmodel modelt;
    float threshold[3] = {0.9f, 0.9f, 0.99f};
    int minFaceSize = 60;

    void gray(cv::Mat &src, cv::Mat &dst) {
        cvtColor(src, dst, CV_BGR2GRAY);
        cvtColor(dst, dst, CV_GRAY2BGR);
    }
};

SDK sdk{};

#ifdef __cplusplus
extern "C" {
#endif
void hello(const char *str) {
    std::cout << str << std::endl;
}
int face_model_init(char *ldmarkmodelPath, int threadNum) {
    int detectThreadNum = threadNum, recognizeThreadNum = threadNum;
    sdk.threadNum = threadNum;
    sdk.mDetect = new Face::Detect(sdk.tFaceModelDir);
    sdk.mRecognize = new Face::Recognize(sdk.tFaceModelDir);

    sdk.mDetect->SetThreadNum(detectThreadNum);
    sdk.mDetect->setThreshold(sdk.threshold);
    sdk.mDetect->SetMinFace(sdk.minFaceSize);

    sdk.mRecognize->SetThreadNum(recognizeThreadNum);

    if (!load_ldmarkmodel(ldmarkmodelPath, sdk.modelt)) {
        return FACE_SDK_STATUS_MODEL_LOAD_ERROR;
    }
    sdk.inited = true;
    return FACE_SDK_STATUS_OK;
}

int face_model_conf(float threshold[3], int minFaceSize) {

    if (minFaceSize < 48)
        return FACE_SDK_STATUS_FACE_SIZE_TOO_SMALL;

    if (!(threshold[0] > 0 && threshold[0] <= 1) ||
        !(threshold[1] > 0 && threshold[1] <= 1) ||
        !(threshold[2] > 0 && threshold[2] <= 1)) {
        return FACE_SDK_STATUS_ILLEGAL_PARAMETER;
    }

    memcpy(sdk.threshold, threshold, sizeof(sdk.threshold));
    sdk.minFaceSize = minFaceSize;

    if (sdk.inited) {
        sdk.mDetect->setThreshold(sdk.threshold);
        sdk.mDetect->SetMinFace(sdk.minFaceSize);
    }
    return FACE_SDK_STATUS_OK;
}

int load_face_users(char *filePath, Face::User *&users, int &size) {

    long fileLength = Face::getFileLength(filePath);

    if (fileLength == -1) {
//        cerr << "embeddingPath [" << filePath << "] not found ." << endl;
        return FACE_SDK_STATUS_NOT_FOUND_ERROR;
    }

    int len = fileLength / sizeof(Face::User);
    users = ((Face::User *) malloc(len * sizeof(Face::User)));
    size = readUsers(filePath, users);

    return FACE_SDK_STATUS_OK;
}

int face_detect(cv::Mat &src, std::vector<FACE_BOX> &faceBoxes) {

    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    std::vector<Face::Bbox> box;
    sdk.mDetect->start(ncnn::Mat::from_pixels(src.data, ncnn::Mat::PIXEL_BGR2RGB, src.cols, src.rows), box);

    auto num_face = static_cast<int32_t>(box.size());
    for (int i = 0; i < num_face; i++) {
        FACE_BOX faceBox{};
        faceBox.x = box[i].x1;
        faceBox.y = box[i].y1;
        faceBox.width = box[i].x2 - box[i].x1;
        faceBox.height = box[i].y2 - box[i].y1;
        faceBox.score = box[i].score;
        faceBox.area = box[i].area;
        faceBoxes.push_back(faceBox);
    }

    return FACE_SDK_STATUS_OK;
}
int face_compare(cv::Mat &src, Face::User *users, int size, int &index, double &score, float threshold) {
    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    if (size <= 0)
        return FACE_SDK_STATUS_EMPTY_USER_ERROR;


    //灰度人脸
    sdk.gray(src, src);

    cv::Mat dst_roi_dst;
    cv::resize(src, dst_roi_dst, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
    ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(dst_roi_dst.data, ncnn::Mat::PIXEL_BGR2RGB,
                                                      dst_roi_dst.cols,
                                                      dst_roi_dst.rows);
    std::vector<float> feature2;
    sdk.mRecognize->start(resize_mat_sub, feature2);

    index = 0;
    score = -10;
    for (int j = 0; j < size; j++) {
        std::vector<float> feature(users[j].embedding, users[j].embedding + sizeof(users[j].embedding) / sizeof(float));
        double similar = Face::calculCosSimilar(feature, feature2);
        if (similar > score) {
            score = similar;
            index = j;
        }

    }
    if (score > threshold) {
        return FACE_SDK_STATUS_OK;
    } else {
        index = -1;
        score = -1;
        return FACE_SDK_STATUS_NOT_IN_USER;
    }


}
int face_detect_and_compare(cv::Mat &src, std::vector<FACE_BOX> &faceBoxes, Face::User *users, int size,
                            std::vector<int> &indexes, std::vector<float> &scores,
                            float threshold) {
    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    if (size <= 0)
        return FACE_SDK_STATUS_EMPTY_USER_ERROR;


    std::vector<Face::Bbox> boxes;
    sdk.mDetect->start(ncnn::Mat::from_pixels(src.data, ncnn::Mat::PIXEL_BGR2RGB, src.cols, src.rows), boxes);

    auto num_face = static_cast<int32_t>(boxes.size());

    for (int i = 0; i < num_face; i++) {
        FACE_BOX faceBox{};
        Face::Bbox box = boxes[i];
        faceBox.x = box.x1;
        faceBox.y = box.y1;
        faceBox.width = box.x2 - box.x1;
        faceBox.height = box.y2 - box.y1;
        faceBox.score = box.score;
        faceBox.area = box.area;

        cv::Rect rect(box.x1, box.y1, box.x2 - box.x1, box.y2 - box.y1);

        cv::Mat warp;
        cv::Point2f left(box.ppoint[0], box.ppoint[5]);
        cv::Point2f right(box.ppoint[1], box.ppoint[6]);

        float angle = FacePreprocess::calcRotationAngle(left, right);
        FacePreprocess::rotateAndCut(src, warp, rect, angle);

        Face::Bbox it{};
        it.x2 = warp.cols;
        it.y2 = warp.rows;

        cv::Mat warp1;
        cv::resize(warp, warp1, cv::Size(48, 48), 0, 0, cv::INTER_CUBIC);
        auto in = ncnn::Mat::from_pixels(warp1.data, ncnn::Mat::PIXEL_BGR2RGB, warp1.cols, warp1.rows);
        std::vector<Face::Bbox> bboxes = sdk.mDetect->ONet(in, it);
        if (bboxes.size() != 1) {

        } else {
            cv::Rect rect1(bboxes[0].x1, bboxes[0].y1, bboxes[0].x2 - bboxes[0].x1, bboxes[0].y2 - bboxes[0].y1);
            warp = warp(rect1);
        }
        // 最大相似度
        double score = 0;
        // 匹配到最大相似度的序号
        int index = 0;

        int result = face_compare(warp, users, size, index, score, threshold);

        if (result != FACE_SDK_STATUS_OK) {
            continue;
        }

        indexes.push_back(index);
        scores.push_back(static_cast<float &&>(score));
        faceBoxes.push_back(faceBox);
    }

    return FACE_SDK_STATUS_OK;
}
#ifdef __cplusplus
}


#endif

