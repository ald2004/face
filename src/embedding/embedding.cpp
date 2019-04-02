//
// Created by wangxiaoming on 2019/2/27.
//


#include "file_util.h"
#include "detect.h"
#include "recognize.h"
#include <opencv2/opencv.hpp>
#include "FacePreprocess.h"

using namespace cv;
using namespace std;
using namespace Face;

void gray(Mat &src, Mat &dst) {
    cvtColor(src, dst, CV_BGR2GRAY);
    cvtColor(dst, dst, CV_GRAY2BGR);
}

int startsWith(const string &s, const string &sub) {
    return s.find(sub) == 0 ? 1 : 0;
}

int endsWith(const string &s, const string &sub) {
    return s.rfind(sub) == (s.length() - sub.length()) ? 1 : 0;
}


int main(int argc, char **argv) {
    std::vector<int> IMWRITE_PARAMS = {CV_IMWRITE_JPEG_QUALITY, 100};

    // run  xxx/xxx.exe
    const string runPath = argv[0];
    // imgs
    const string imgPath = argv[1];
    // model
    const string tFaceModelDir = argv[2];

    cout << "runPath:" << runPath << endl;
    cout << "imgPath:" << imgPath << endl;
    cout << "tFaceModelDir:" << tFaceModelDir << endl;

    // buffer
    list<string> imgs;
    // list file
    listFiles(imgPath, &imgs);
    int len = 0;

    vector<string> imgVector;
    for (const auto &img : imgs)
        if (endsWith(img.substr(imgPath.length() + 1, img.length()), ".jpg")) {
            len++;
            imgVector.push_back(img);
        }

    User *users = (User *) malloc(len * sizeof(User));

    int detectThreadNum = 3, recognizeThreadNum = 3;
    Face::Detect *mDetect;
    Face::Recognize *mRecognize;

    mDetect = new Face::Detect(tFaceModelDir);
    mRecognize = new Face::Recognize(tFaceModelDir);

//    float threshold[3] = {0.2f, 0.1f, 0.8f};
    float threshold[3] = {0.6f, 0.7f, 0.8f};;
//    mDetect->SetMinFace(40);
    mDetect->setThreshold(threshold);
    mDetect->SetThreadNum(detectThreadNum);
    mRecognize->SetThreadNum(recognizeThreadNum);

//#pragma omp parallel for
    for (int index = 0; index < imgVector.size(); ++index) {

        /* }
         for (auto iter = imgs.begin(); iter != imgs.end(); ++iter, ++index) {*/
//        string img = *iter;
        string img = imgVector[index];
        string filename = img.substr(imgPath.length() + 1, img.length());
        string name = filename.substr(0, filename.length() - 4);

        Mat mat = imread(img);

        vector<Face::Bbox> finalBbox;
        mDetect->start(ncnn::Mat::from_pixels(mat.data, ncnn::Mat::PIXEL_BGR2RGB, mat.cols, mat.rows), finalBbox);
        auto numFace = static_cast<int32_t>(finalBbox.size());
        if (numFace == 0) {
            cerr << filename << " -- no face!" << endl;
            continue;
//                exit(-1);
        } else if (numFace > 1) {
            cerr << filename << " -- many face!" << endl;
        }

        int maxIndex = 0;
        float maxScore = 0;
        for (int i = 0; i < numFace; i++) {
            if (finalBbox[i].score > maxScore) {
                maxScore = finalBbox[i].score;
                maxIndex = i;
            }
        }
        Face::Bbox box = finalBbox[maxIndex];
        Rect faceBox(box.x1, box.y1, box.x2 - box.x1, box.y2 - box.y1);


        Mat warp;
        FacePreprocess::faceAlign(mat, warp, faceBox, box);

        Face::Bbox it{};
        it.x2 = warp.cols;
        it.y2 = warp.rows;

        Mat warp1;
        cv::resize(warp, warp1, cv::Size(48, 48), 0, 0, cv::INTER_CUBIC);
        auto in = ncnn::Mat::from_pixels(warp1.data, ncnn::Mat::PIXEL_BGR2RGB, warp1.cols, warp1.rows);
        std::vector<Face::Bbox> bboxes = mDetect->ONet(in, it);
        if (bboxes.size() != 1) {
            cerr << filename << " -- the second stage did not find the face or many face!" << endl;
        } else {
            Rect faceBox1(bboxes[0].x1, bboxes[0].y1, bboxes[0].x2 - bboxes[0].x1, bboxes[0].y2 - bboxes[0].y1);
            warp = warp(faceBox1);
        }

        //灰度人脸
        gray(warp, warp);

        Mat dst_roi_dst;
        cv::resize(warp, dst_roi_dst,FacePreprocess::DST_SIZE, 0, 0, cv::INTER_CUBIC);

        ncnn::Mat resize_mat = ncnn::Mat::from_pixels(dst_roi_dst.data, ncnn::Mat::PIXEL_BGR2RGB, dst_roi_dst.cols,
                                                      dst_roi_dst.rows);
        vector<float> feature;
        float embedding[128];
        mRecognize->start(resize_mat, feature);
        memcpy(embedding, &feature[0], sizeof(embedding));

        User user{to_string(index).c_str(), name.c_str(), embedding};
        users[index] = user;
        cout << "name:" << user.name << endl;

        Face::createDir((imgPath + "/output").c_str());

        string outpath;
        outpath.append(imgPath);
        outpath.append("/output/");
        outpath.append(filename);
        imwrite(outpath, dst_roi_dst, IMWRITE_PARAMS);

    }

    cout << endl;


    const char *filepath = "embedding.dat";

    saveUsers(filepath, users, len);
    User *users2 = (User *) malloc(len * sizeof(User));
    int len2 = readUsers(filepath, users2);

    cout << "-------------------------------" << len << "," << len2 << "-------------------------------" << endl;
    for (int i = 0; i < len2; i++) {
        cout << "id:" << users2[i].id << endl;
        cout << "name:" << users2[i].name << endl;

        vector<float> feature(users2[i].embedding, users2[i].embedding + 128);
        cout << "embedding:" << endl;
        for (auto &f:feature) {
            cout << f << ",";
        }
        cout << endl;
    }
    return 0;
}
