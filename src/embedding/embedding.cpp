//
// Created by wangxiaoming on 2019/2/27.
//


#include <opencv2/opencv.hpp>
#include "User.h"
#include "file_util.h"
#include "detect.h"
#include "recognize.h"


using namespace cv;
using namespace std;
using namespace Face;

int startsWith(const string &s, const string &sub) {
    return s.find(sub) == 0 ? 1 : 0;
}

int endsWith(const string &s, const string &sub) {
    return s.rfind(sub) == (s.length() - sub.length()) ? 1 : 0;
}

int main(int argc, char **argv) {
    // run  xxx/xxx.exe
    const string runPath = argv[0];
    // imgs
    const string imgPath = argv[1];
    // model
    const string tFaceModelDir = argv[2];

    cout << "runPath:" << runPath << endl;
    cout << "imgPath:" << imgPath << endl;
    cout << "tFaceModelDir:" << tFaceModelDir << endl;

    // 文件
    list<string> imgs;
    // 遍历目录文件
    listFiles(imgPath, &imgs);

    int len = imgs.size();
    User *users = (User *) malloc(len * sizeof(User));

    int detectThreadNum = 3, recognizeThreadNum = 3;
    Face::Detect *mDetect;
    Face::Recognize *mRecognize;

    mDetect = new Face::Detect(tFaceModelDir);
    mRecognize = new Face::Recognize(tFaceModelDir);

    mDetect->SetThreadNum(detectThreadNum);
    mRecognize->SetThreadNum(recognizeThreadNum);

    int index = 0;
    for (auto iter = imgs.begin(); iter != imgs.end(); ++iter, ++index) {
        string img = *iter;
        string filename = img.substr(imgPath.length() + 1, img.length());
        if (endsWith(filename, ".jpg")) {
            string name = filename.substr(0, filename.length() - 4);

            Mat mat = imread(img);

            vector<Face::Bbox> box;
            mDetect->SetMinFace(40);
            mDetect->start(ncnn::Mat::from_pixels(mat.data, ncnn::Mat::PIXEL_BGR, mat.cols, mat.rows), box);
            auto num_face = static_cast<int32_t>(box.size());
            if (num_face == 0) {
                cerr << filename << " -- no face!" << endl;
                abort();//结束
            } else if (num_face > 1) {
                cerr << filename << " -- some face!" << endl;
//                abort();//结束
            }

            for (int i = 0; i < 1; i++) {
                Rect rect(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);
                cv::Mat dst_roi = mat(rect);

                Mat dst_roi_dst;
                cv::resize(dst_roi, dst_roi_dst, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
                ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(dst_roi_dst.data, ncnn::Mat::PIXEL_BGR,
                                                                  dst_roi_dst.cols,
                                                                  dst_roi_dst.rows);
                vector<float> feature2;
                mRecognize->start(resize_mat_sub, feature2);
                imshow("1", dst_roi_dst);
                waitKey(1000);
            }


            Mat mat1_dst;
            cv::resize(mat, mat1_dst, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
            ncnn::Mat resize_mat = ncnn::Mat::from_pixels(mat1_dst.data, ncnn::Mat::PIXEL_BGR, mat1_dst.cols,
                                                          mat1_dst.rows);
            vector<float> feature;
            float embedding[128];
            mRecognize->start(resize_mat, feature);
            memcpy(embedding, &feature[0], sizeof(embedding));


            User user{name.c_str(), embedding};
            users[index] = user;
            cout << "name:" << user.name << endl;
        }
    }

    cout << endl;


    const char *filepath = "embedding.dat";

    save_user(filepath, users, len);
    User *users2 = (User *) malloc(len * sizeof(User));
    int len2 = read_users(filepath, users2);

    cout << "-------------------------------" << endl;
    for (int i = 0; i < len2; i++) {
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
