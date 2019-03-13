//
// Created by wangxiaoming on 2019/3/8.
//

#include <recognize.h>
#include <detect.h>
#include "test.h"
#include <opencv2/opencv.hpp>

#include "ldmarkmodel.h"

#ifdef _WIN32

#include<windows.h>
#include <FacePreprocess.h>

#define SLEEP(a) Sleep(a)
#define GET_CURR_TIME() clock()
#else
#include <unistd.h>
#define SLEEP(a) usleep(a*1000)
#define GET_CURR_TIME() clock()/1000
#endif

using namespace cv;
using namespace std;
Face::Detect *mDetect;
Face::Recognize *mRecognize;
int threadNum;
const char *tFaceModelDir;

/**
 * 初始化模型
 */
void init() {
    int detectThreadNum = threadNum, recognizeThreadNum = threadNum;
    mDetect = new Face::Detect(tFaceModelDir);
    mRecognize = new Face::Recognize(tFaceModelDir);
    mDetect->SetThreadNum(detectThreadNum);
    mRecognize->SetThreadNum(recognizeThreadNum);
}

/**
 * 两张人脸比较相似度
 * @param img1 人脸1
 * @param img2 人脸2
 */
void testRecognize(Mat img1, Mat img2) {

    cv::resize(img1, img1, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
    ncnn::Mat resize_mat1 = ncnn::Mat::from_pixels(img1.data, ncnn::Mat::PIXEL_BGR2RGB, img1.cols,
                                                   img1.rows);
    cv::resize(img2, img2, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
    ncnn::Mat resize_mat2 = ncnn::Mat::from_pixels(img2.data, ncnn::Mat::PIXEL_BGR2RGB, img2.cols,
                                                   img2.rows);

    vector<float> feature1, feature2;
    mRecognize->start(resize_mat1, feature1);
    mRecognize->start(resize_mat2, feature2);


    cout << "embedding1:" << endl;
    for (auto &f:feature1) {
        cout << f << ",";
    }
    cout << endl;
    cout << "embedding2:" << endl;
    for (auto &f:feature1) {
        cout << f << ",";
    }
    cout << endl;

    double similar = Face::calculCosSimilar(feature1, feature2);
    double distance = Face::calculEuclidianDistance(feature1, feature2);
    cout << "similar :" << similar << endl;
    cout << "distance:" << distance << endl;
}

vector<Face::Bbox> mtcnn(Mat dst) {
    vector<Face::Bbox> box;
    mDetect->start(ncnn::Mat::from_pixels(dst.data, ncnn::Mat::PIXEL_BGR2RGB, dst.cols, dst.rows), box);
    return box;
}

void testLandmark(std::string modelFilePath) {
    ldmarkmodel modelt;
    while (!load_ldmarkmodel(modelFilePath, modelt)) {
        std::cout << "文件打开错误，请重新输入文件路径." << std::endl;
        std::cin >> modelFilePath;
    }

    cv::VideoCapture mCamera(0);
//    cv::VideoCapture mCamera("rtsp://admin:111111ab@192.168.100.251:554/h264/ch1/main/1");
    if (!mCamera.isOpened()) {
        std::cout << "Camera opening failed..." << std::endl;
        system("pause");
        return;
    }
    cv::Mat Image;
    cv::Mat current_shape;
    int start = GET_CURR_TIME();
    for (;;) {
        mCamera >> Image;

        int end = GET_CURR_TIME();
        bool isDetFace = false;
        if (end - start > 2000) {
            start = end;
            isDetFace = true;
        }

        vector<Face::Bbox> box = mtcnn(Image);
        auto num_face = static_cast<int32_t>(box.size());
//            cout << "mtcnn:" << (end - start) << "ms" << endl;

        for (int i = 0; i < num_face; i++) {

            Rect faceBox(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);

            modelt.track(Image(faceBox), current_shape);
//            modelt.track(Image, current_shape, faceBox, isDetFace);
            cv::Vec3d eav;
            modelt.EstimateHeadPose(current_shape, eav);
            modelt.drawPose(Image, current_shape, 50);

            std::cout << "俯仰 : " << eav[0] << "  侧脸 :" << eav[1] << "  旋转 :" << eav[2] << std::endl;

            int numLandmarks = current_shape.cols / 2;
            for (int j = 0; j < numLandmarks; j++) {
                int x = current_shape.at<float>(j);
                int y = current_shape.at<float>(j + numLandmarks);
                std::stringstream ss;
                ss << j;
//            cv::putText(Image, ss.str(), cv::Point(x, y), 0.5, 0.5, cv::Scalar(0, 0, 255));
                cv::circle(Image, cv::Point(x + faceBox.x, y + faceBox.y), 2, cv::Scalar(0, 0, 255), -1);
            }
        }
        cv::imshow("Camera", Image);
        if (27 == cv::waitKey(5)) {
            mCamera.release();
            cv::destroyAllWindows();
            break;
        }
    }

    system("pause");
}


int main(int argc, char **argv) {
    threadNum = 3;
    tFaceModelDir = "../model/face";
    init();
//    testLandmark("./landmark.bin");
    ldmarkmodel modelt;
    load_ldmarkmodel("../model/face/landmark.bin", modelt);

    cv::VideoCapture mCamera(0);
//    cv::VideoCapture mCamera("rtsp://admin:111111ab@192.168.100.251:554/h264/ch1/main/1");
    if (!mCamera.isOpened()) {
        std::cout << "Camera opening failed..." << std::endl;
        system("pause");
        return 0;
    }
    cv::Mat img1, img2, img3;
    cv::Mat current_shape;
    int start = GET_CURR_TIME();
    for (;;) {
        mCamera >> img1;
        img1.copyTo(img2);
        img1.copyTo(img3);

        vector<Face::Bbox> box1, box2, box3;
        mDetect->start2(ncnn::Mat::from_pixels(img1.data, ncnn::Mat::PIXEL_BGR2RGB, img1.cols, img1.rows), box1,
                        box2, box3);

        auto arr = new vector<Face::Bbox>[3]{box1, box2, box3};
        auto arr1 = new cv::Mat[3]{img1, img2, img3};
        for (int j = 0; j < 3; j++) {
            Mat img = arr1[j];
            vector<Face::Bbox> box = arr[j];
            auto num_face = static_cast<int32_t>(box.size());
            for (int i = 0; i < num_face; i++) {
                Rect faceBox(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);

                /*Point[] points = new Point[]{
                        new Point(faceInfo[5 + 14 * i], faceInfo[10 + 14 * i]),
                        new Point(faceInfo[6 + 14 * i], faceInfo[11 + 14 * i]),
                        new Point(faceInfo[7 + 14 * i], faceInfo[12 + 14 * i]),
                        new Point(faceInfo[8 + 14 * i], faceInfo[13 + 14 * i]),
                        new Point(faceInfo[9 + 14 * i], faceInfo[14 + 14 * i])
                };

                canvas.drawText("1", videoOverlap.getWidth() - (points[0].x) * widthScale, (points[0].y * heightScale), paint);
                paint.setColor(Color.parseColor("#AA0000"));
                canvas.drawText("2", videoOverlap.getWidth() - (points[1].x) * widthScale, (points[1].y * heightScale), paint);
                paint.setColor(Color.parseColor("#00AA00"));
                canvas.drawText("3", videoOverlap.getWidth() - (points[2].x) * widthScale, (points[2].y * heightScale), paint);
                paint.setColor(Color.parseColor("#0000AA"));
                canvas.drawText("4", videoOverlap.getWidth() - (points[3].x) * widthScale, (points[3].y * heightScale), paint);
                paint.setColor(Color.parseColor("#000000"));
                canvas.drawText("5", videoOverlap.getWidth() - (points[4].x) * widthScale, (points[4].y * heightScale), paint);
                paint.setColor(Color.parseColor("#009688"));*/

                /*cv::circle(img,
                           cv::Point(static_cast<int>(box[i].ppoint[5 - 5]), static_cast<int>(box[i].ppoint[10 - 5])),
                           3, cv::Scalar(255, 0, 0));
                cv::circle(img,
                           cv::Point(static_cast<int>(box[i].ppoint[6 - 5]), static_cast<int>(box[i].ppoint[11 - 5])),
                           3, cv::Scalar(255, 0, 0));
                cv::circle(img,
                           cv::Point(static_cast<int>(box[i].ppoint[7 - 5]), static_cast<int>(box[i].ppoint[12 - 5])),
                           3, cv::Scalar(255, 0, 0));
                cv::circle(img,
                           cv::Point(static_cast<int>(box[i].ppoint[8 - 5]), static_cast<int>(box[i].ppoint[13 - 5])),
                           3, cv::Scalar(255, 0, 0));
                cv::circle(img,
                           cv::Point(static_cast<int>(box[i].ppoint[9 - 5]), static_cast<int>(box[i].ppoint[14 - 5])),
                           3, cv::Scalar(255, 0, 0));*/

                if (j == 2) {


                    Mat warp;
                    Point2f left(box[i].ppoint[0], box[i].ppoint[5]);
                    Point2f right(box[i].ppoint[1], box[i].ppoint[6]);
                    Point2f nose(box[i].ppoint[2], box[i].ppoint[7]);
                    float angle = FacePreprocess::calcRotationAngle(left, right);
                    FacePreprocess::rotateAndCut(img, warp, faceBox, angle);

                    Face::Bbox it{};
                    it.x2 = warp.cols;
                    it.y2 = warp.rows;

                    Mat warp1;
                    cv::resize(warp, warp1, cv::Size(48, 48), 0, 0, cv::INTER_CUBIC);
                    auto in = ncnn::Mat::from_pixels(warp1.data, ncnn::Mat::PIXEL_BGR2RGB, warp1.cols, warp1.rows);
                    std::vector<Face::Bbox> bboxes = mDetect->ONet(in, it);

                    if (bboxes.size() != 1) {
                        continue;
                    }

                    Rect faceBox1(bboxes[0].x1, bboxes[0].y1, bboxes[0].x2 - bboxes[0].x1, bboxes[0].y2 - bboxes[0].y1);
                    warp = warp(faceBox1);
                    modelt.track(warp, current_shape);
                    cv::Vec3d eav;
                    modelt.EstimateHeadPose(current_shape, eav);
                    modelt.drawPose(img, current_shape, 50);
//                    modelt.drawPose(warp, current_shape, 50);

                    int numLandmarks = current_shape.cols / 2;
                    {
                        for (int j = 0; j < numLandmarks; j++) {
                            int x = current_shape.at<float>(j);
                            int y = current_shape.at<float>(j + numLandmarks);
                            std::stringstream ss;
                            ss << j;
                            //            cv::putText(Image, ss.str(), cv::Point(x, y), 0.5, 0.5, cv::Scalar(0, 0, 255));
                            cv::circle(warp, cv::Point(x, y), 2, cv::Scalar(0, 0, 255), -1);
                        }
                    }
                    cv::imshow("warp", warp);
                }
                cv::rectangle(img, faceBox, cv::Scalar(0, 0, 255));
            }

            cv::imshow(to_string(j), img);

        }
        if (27 == cv::waitKey(5)) {
            mCamera.release();
            cv::destroyAllWindows();
            break;
        }
    }


//    testRecognize(img1, img2);
    return 0;
}
