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


#ifdef _WIN32

#include<windows.h>

#define SLEEP(a) Sleep(a)
#define CLOCK() clock()
#define LOCALTIME(result_time, time_seconds) localtime_s(result_time,time_seconds)
#else
#include <unistd.h>
#define SLEEP(a) usleep(a*1000)
#define CLOCK() clock()/1000
#define LOCALTIME(time_seconds, result_time) localtime_r(result_time,time_seconds)
#endif

#define CHECK_LICENSE() if (sdk.licenseTime <= time(nullptr)) return FACE_SDK_STATUS_LICENSE_ERROR;

struct SDK {
    Face::Detect *mDetect;
    Face::Recognize *mRecognize;
    int threadNum = 3;
    const char *tFaceModelDir = "";
    bool inited = false;
//    ldmarkmodel modelt;
    float threshold[3] = {0.9f, 0.9f, 0.99f};
    int minFaceSize = 60;
    // 授权到期时间
    time_t licenseTime = 1555000000;

    void showLicense() {
        tm result_time{};
        LOCALTIME(&result_time, &licenseTime);

        printf("[face_sdk] license to %d-%02d-%02d %02d:%02d:%02d\n",
               result_time.tm_year + 1900,
               result_time.tm_mon + 1,
               result_time.tm_mday,
               result_time.tm_hour,
               result_time.tm_min,
               result_time.tm_sec);
    }

    void gray(cv::Mat &src, cv::Mat &dst) {
        cvtColor(src, dst, CV_BGR2GRAY);
        cvtColor(dst, dst, CV_GRAY2BGR);
    }
};

SDK sdk{};

#ifdef __cplusplus
extern "C" {
#endif
int face_model_init(/*char *ldmarkmodelPath,*/ int threadNum) {
    sdk.showLicense();
    CHECK_LICENSE();

    int detectThreadNum = threadNum, recognizeThreadNum = threadNum;
    sdk.threadNum = threadNum;
    sdk.mDetect = new Face::Detect(sdk.tFaceModelDir);
    sdk.mRecognize = new Face::Recognize(sdk.tFaceModelDir);

    sdk.mDetect->SetThreadNum(detectThreadNum);
    sdk.mDetect->setThreshold(sdk.threshold);
    sdk.mDetect->SetMinFace(sdk.minFaceSize);

    sdk.mRecognize->SetThreadNum(recognizeThreadNum);

    /*   if (!load_ldmarkmodel(ldmarkmodelPath, sdk.modelt)) {
           return FACE_SDK_STATUS_MODEL_LOAD_ERROR;
       }*/
    sdk.inited = true;
    return FACE_SDK_STATUS_OK;
}


int face_model_conf(float threshold[3], int minFaceSize) {
    CHECK_LICENSE();

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

int face_embedding(cv::Mat &src, float embedding[128], cv::Mat &dst) {
    CHECK_LICENSE();
    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    std::vector<Face::Bbox> finalBbox;
    sdk.mDetect->start(ncnn::Mat::from_pixels(src.data, ncnn::Mat::PIXEL_BGR2RGB, src.cols, src.rows), finalBbox);

    auto num_face = static_cast<int32_t>(finalBbox.size());

    if (num_face == 0)
        return FACE_SDK_STATUS_NOT_IN_USER;

    double maxScore = 0;
    int maxIndex = 0;

    for (int i = 0; i < num_face; i++) {
        if (finalBbox[i].score > maxScore) {
            maxScore = finalBbox[i].score;
            maxIndex = i;
        }
    }

    auto box = finalBbox[maxIndex];

    FACE_BOX faceBox{};


    faceBox.x = box.x1;
    faceBox.y = box.y1;
    faceBox.width = box.x2 - box.x1;
    faceBox.height = box.y2 - box.y1;
    faceBox.score = box.score;
    faceBox.area = box.area;

    cv::Rect rect(box.x1, box.y1, box.x2 - box.x1, box.y2 - box.y1);
    cv::Mat warp;
    FacePreprocess::faceAlign(src, warp, rect, box);

    cv::resize(warp, dst, FacePreprocess::DST_SIZE, 0, 0, cv::INTER_CUBIC);
    ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(dst.data, ncnn::Mat::PIXEL_BGR2RGB,
                                                      dst.cols,
                                                      dst.rows);
    std::vector<float> feature;
    sdk.mRecognize->start(resize_mat_sub, feature);

    memcpy(embedding, &feature[0], sizeof(float) * 128);

    return FACE_SDK_STATUS_OK;
}


int save_face_users(char *filepath, Face::User *users, int size) {
    CHECK_LICENSE();
    ofstream outfile(filepath, ios::binary);
    if (!outfile) {
        return FACE_SDK_STATUS_IO_ERROR;
    }
    for (int i = 0; i < size; i++) {
        Face::User user = users[i];
        outfile.write((char *) &user, sizeof(user));
    }
    outfile.close();
    return FACE_SDK_STATUS_OK;
}


int load_face_users(char *filePath, Face::User *&users, int &size) {
    CHECK_LICENSE();

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
    CHECK_LICENSE();

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
    CHECK_LICENSE();

    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    if (size <= 0)
        return FACE_SDK_STATUS_EMPTY_USER_ERROR;


    //灰度人脸
    sdk.gray(src, src);

    cv::Mat dst_roi_dst;
    cv::resize(src, dst_roi_dst, FacePreprocess::DST_SIZE, 0, 0, cv::INTER_CUBIC);
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
    CHECK_LICENSE();

    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    if (size <= 0)
        return FACE_SDK_STATUS_EMPTY_USER_ERROR;


    double scale = (640.0 / src.cols);
//    double scale = 1;
    cv::Mat tmp;
    cv::resize(src, tmp, cv::Size(static_cast<int>(src.cols * scale), static_cast<int>(src.rows * scale)), 0, 0,
               cv::INTER_CUBIC);

    std::vector<Face::Bbox> boxes;
    sdk.mDetect->start(ncnn::Mat::from_pixels(tmp.data, ncnn::Mat::PIXEL_BGR2RGB, tmp.cols, tmp.rows), boxes);

    auto num_face = static_cast<int32_t>(boxes.size());

//#pragma omp parallel for
    for (int i = 0; i < num_face; i++) {
        FACE_BOX faceBox{};
        Face::Bbox box = boxes[i];

        box.x1 /= scale;
        box.x2 /= scale;
        box.y1 /= scale;
        box.y2 /= scale;

        faceBox.x = box.x1;
        faceBox.y = box.y1;
        faceBox.width = box.x2 - box.x1;
        faceBox.height = box.y2 - box.y1;
        faceBox.score = box.score;
        faceBox.area = box.area;

        cv::Rect rect(box.x1, box.y1, box.x2 - box.x1, box.y2 - box.y1);

        cv::Mat warp;

//        int64 start = CLOCK();
        FacePreprocess::faceAlign(src, warp, rect, box);
//        int64 end = CLOCK();
//        cout << "faceAlign:time:" << (end - start) << endl;

        /*Face::Bbox it{};
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
        }*/

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

