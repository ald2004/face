
#include "file_util.h"
#include "detect.h"
#include <recognize.h>
#include <opencv2/opencv.hpp>

using namespace cv;
using namespace Face;
using namespace std;

int main(int argc, char **argv) {
    // run  xxx/xxx.exe
    const string runPath = argv[0];
    // imgs
    const string imgPath = argv[1];
    // model
    const string tFaceModelDir = argv[2];
    // embeddingPath
    const string embeddingPath = argv[3];

    cout << "runPath:" << runPath << endl;
    cout << "imgPath:" << imgPath << endl;
    cout << "tFaceModelDir:" << tFaceModelDir << endl;
    cout << "embeddingPath:" << embeddingPath << endl;


    const char *filepath = (embeddingPath + "/embedding.dat").c_str();

    int len = getFileLength(filepath) / sizeof(User);
    User *users = (User *) malloc(len * sizeof(User));
    int len2 = readUsers(filepath, users);

    cout << "-------------------------------" << endl;
    for (int i = 0; i < len2; i++) {
        cout << "name:" << users[i].name << endl;

        vector<float> feature(users[i].embedding, users[i].embedding + 128);
        cout << "embedding:" << endl;
        for (auto &f:feature) {
            cout << f << ",";
        }
        cout << endl;
    }

    int detectThreadNum = 3, recognizeThreadNum = 3;
    Face::Detect *mDetect;
    Face::Recognize *mRecognize;
    mDetect = new Face::Detect(tFaceModelDir);
    mRecognize = new Face::Recognize(tFaceModelDir);

    mDetect->SetThreadNum(detectThreadNum);
    mRecognize->SetThreadNum(recognizeThreadNum);

    cout << mDetect << endl;


//    VideoCapture cap("rtsp://admin:111111ab@192.168.100.252:554/h264/ch1/main/4");
    VideoCapture cap(0);
    if (!cap.isOpened()) {
        return -1;
    }
    Mat frame;
    Mat dst;

    bool stop = false;
    while (!stop || cap.isOpened()) {
        int64 start = clock();
        cap >> frame;
        vector<Face::Bbox> box;
        dst = frame;
        mDetect->start(ncnn::Mat::from_pixels(dst.data, ncnn::Mat::PIXEL_BGR, dst.cols, dst.rows), box);
        auto num_face = static_cast<int32_t>(box.size());

        int out_size = 1 + num_face * 14;
        int *faceInfo = new int[out_size];
        faceInfo[0] = num_face;
        for (int i = 0; i < num_face; i++) {
            faceInfo[14 * i + 1] = box[i].x1;//left
            faceInfo[14 * i + 2] = box[i].y1;//top
            faceInfo[14 * i + 3] = box[i].x2;//right
            faceInfo[14 * i + 4] = box[i].y2;//bottom
            for (int j = 0; j < 10; j++) {
                faceInfo[14 * i + 5 + j] = static_cast<int>(box[i].ppoint[j]);
            }

            Rect rect(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);
            cv::Mat dst_roi = dst(rect);

            Mat dst_roi_dst;
            cv::resize(dst_roi, dst_roi_dst, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
            ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(dst_roi_dst.data, ncnn::Mat::PIXEL_BGR, dst_roi_dst.cols,
                                                              dst_roi_dst.rows);
            vector<float> feature2;
            mRecognize->start(resize_mat_sub, feature2);
            imshow("1", dst_roi_dst);

//            copy(feature2.begin(), feature2.end(), ostream_iterator<float>(cout, ","));
//            cout << Face::calculEuclidianDistance(feature1, feature2) << endl;
            int64 end = clock();

            string maxName;
            double max = -10;
            for (int i = 0; i < len2; i++) {

                vector<float> feature(users[i].embedding, users[i].embedding + 128);
                double similar = Face::calculCosSimilar(feature, feature2);

                if (similar > max) {
                    max = similar;
                    maxName = users[i].name;
                }


            }

            if (max > 0.65) {
                cout << "name:" << maxName << " similar:" << max << " time:" << (end - start) << "ms" << endl;
//                imshow("2", imread(imgPath + "/" + maxName + ".jpg"));
            }
            rectangle(dst, Point(box[i].x1, box[i].y1), Point(box[i].x2, box[i].y2), Scalar(225, 0, 225));

        }

        imshow("", dst);
        if (waitKey(1) >= 0)
            stop = true;
    }

    return 0;
}