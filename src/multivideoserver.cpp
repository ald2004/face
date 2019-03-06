
#include "file_util.h"
#include "detect.h"
#include <recognize.h>
#include <opencv2/opencv.hpp>
#include <FacePreprocess.h>
#include "thread"
#include "socket.h"

#ifdef _WIN32

#include<windows.h>

#define SLEEP(a) Sleep(a )
#else
#include <unistd.h>
#define SLEEP(a) usleep(a*1000)
#endif


using namespace cv;
using namespace Face;
using namespace std;

static Mat dst;
static bool run = false;

void send(face::Socket socket, const char *buf, int len) {
    socket.Send(buf, len);
    SLEEP(100);
}

void openDoor(const string &ip, unsigned short port) {
    face::Socket sock;
    sock.Connect(ip, port);
    if (sock.IsConnected()) {
        send(sock, "\x55\xaa\x02\x80\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x00\x00", 20);
        send(sock, "\x55\xaa\x02\x80\x00\x00\x00\x00\x80\xed\xff\xff\x00\x00\x01\x00", 16);
        send(sock, "\x55\xaa\x02\x13\x00\x00\x00\x00\x00\x00\x00\x00\x30\x00\x02\x00", 16);
        send(sock, "\x55\xaa\x02\x13\x01\x00\x00\x00\x5c\x00\x00\x00\x00\x04\x03\x00", 16);
        send(sock, "\x55\xaa\x02\x13\x00\x00\x00\x00\x00\x00\x00\x00\x30\x00\x04\x00", 16);
        send(sock, "\x55\xaa\x02\x81\x00\x00\x00\x00\x00\x00\xff\xff\x00\x00\x05\x00", 16);
        send(sock, "\x55\xaa\x02\xe1\x01\x00\x00\x00\x00\x00\xff\xff\x00\x00\x06\x00", 16);
        send(sock, "\x55\xaa\x02\xe0\x00\x00\x00\x00\x00\x00\xff\xff\x00\x00\x07\x00", 16);
        send(sock, "\x55\xaa\x02\x81\x01\x00\x00\x00\x00\x00\xff\xff\x00\x00\x08\x00", 16);
        sock.Close();
    } else {
        cerr << "connected is false [" << ip << ":" << port << "]" << endl;
    }
}

void closeDoor(const string &ip, unsigned short port) {
    face::Socket sock;
    sock.Connect(ip, port);
    if (sock.IsConnected()) {
        send(sock, "\x55\xaa\x02\x80\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x00\x00", 20);
        send(sock, "\x55\xaa\x02\x80\x00\x00\x00\x00\x80\xed\xff\xff\x00\x00\x01\x00", 16);
        send(sock, "\x55\xaa\x02\x13\x00\x00\x00\x00\x00\x00\x00\x00\x30\x00\x02\x00", 16);
        send(sock, "\x55\xaa\x02\x13\x01\x00\x00\x00\x2a\x5e\x00\x00\x00\x04\x03\x00", 16);
        send(sock, "\x55\xaa\x02\x13\x00\x00\x00\x00\x00\x00\x00\x00\x30\x00\x04\x00", 16);
        send(sock, "\x55\xaa\x02\x81\x00\x00\x00\x00\x00\x00\xff\xff\x00\x00\x05\x00", 16);
        send(sock, "\x55\xaa\x02\xe1\x02\x00\x00\x00\x00\x00\xff\xff\x00\x00\x06\x00", 16);
        send(sock, "\x55\xaa\x02\xe0\x00\x00\x00\x00\x00\x00\xff\xff\x00\x00\x07\x00", 16);
        send(sock, "\x55\xaa\x02\x81\x01\x00\x00\x00\x00\x00\xff\xff\x00\x00\x08\x00", 16);
        sock.Close();
    } else {
        cerr << "connected is false [" << ip << ":" << port << "]" << endl;
    }
}

void doorOpenAndClose(const string &ip, unsigned short port, int waitTime) {
    if (!run) {
        run = true;
        openDoor(ip, port);
        SLEEP(waitTime);
        closeDoor(ip, port);
        run = false;
    }
}

void
recognize(Face::Detect *mDetect, Face::Recognize *mRecognize, User *users, int len, string ip, int port, int waitTime) {

    while (true) {

        if (dst.empty()) {
            continue;
        }
        try {

            int64 start = clock();
            vector<Face::Bbox> box;
            mDetect->start(ncnn::Mat::from_pixels(dst.data, ncnn::Mat::PIXEL_BGR2RGB, dst.cols, dst.rows), box);
            auto num_face = static_cast<int32_t>(box.size());

            for (int i = 0; i < num_face; i++) {

                Rect rect(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);
                cv::Mat dst_roi = dst(rect);

                Mat warp;
                Point2f left(box[i].ppoint[0], box[i].ppoint[5]);
                Point2f right(box[i].ppoint[1], box[i].ppoint[6]);
                Point2f nose(box[i].ppoint[2], box[i].ppoint[7]);
                FacePreprocess::warpAffineFace(dst_roi, warp, left, right);

                Mat dst_roi_dst;
                cv::resize(warp, dst_roi_dst, cv::Size(112, 112), 0, 0, cv::INTER_CUBIC);
                ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(dst_roi_dst.data, ncnn::Mat::PIXEL_BGR2RGB,
                                                                  dst_roi_dst.cols,
                                                                  dst_roi_dst.rows);


                float faceHAngle = FacePreprocess::calcFaceHAngle(left, right, nose);
                float faceScore = FacePreprocess::calcFaceScore(dst_roi_dst, left, right, nose);

//                cout << "faceHAngle:" << faceHAngle << "бу" << endl;
//                cout << "faceScore:" << faceScore << endl;

                vector<float> feature2;
                mRecognize->start(resize_mat_sub, feature2);
//                imshow("1", dst_roi_dst);
//            cout << Face::calculEuclidianDistance(feature1, feature2) << endl;
                int64 end = clock();

                string maxName;
                double max = -10;
                for (int j = 0; j < len; j++) {

                    vector<float> feature(users[j].embedding, users[j].embedding + 128);
                    double similar = Face::calculCosSimilar(feature, feature2);

                    if (similar > max) {
                        max = similar;
                        maxName = users[j].name;
                    }

                }

                if (max > 0.55) {
                    cout
                            << "name:" << maxName
                            << " similar:" << max
                            << " faceHAngle:" << faceHAngle << "бу"
                            << " faceScore:" << faceScore
                            << " time:" << (end - start) << "ms"
                            << endl;
                    thread t(doorOpenAndClose, ip, port, waitTime);
                    t.detach();
//                imshow("2", imread(imgPath + "/" + maxName + ".jpg"));
                }
                rectangle(dst, Point(box[i].x1, box[i].y1), Point(box[i].x2, box[i].y2), Scalar(225, 0, 225));
            }
        } catch (int x) {

        }
    }
}


int main(int argc, char **argv) {

//    string ip = "192.168.100.200";
//    string ip = "172.168.3.173";
//    unsigned short port = 5005;


    // run  xxx/xxx.exe
    const string runPath = argv[0];
    // imgs
    const string imgPath = argv[1];
    // model
    const string tFaceModelDir = argv[2];
    // embeddingPath
    const string embeddingPath = argv[3];

    const string video = argv[4];
    const string ip = argv[5];

    const string s_port = argv[6];
    istringstream isPort(s_port);
    int port;
    isPort >> port;

    const string s_waitTime = argv[7];
    istringstream isWaitTime(s_port);
    int waitTime;
    isWaitTime >> waitTime;

//    string video = "rtsp://admin:111111ab@192.168.100.251:554/h264/ch1/main/1";
    cout << "runPath:" << runPath << endl;
    cout << "imgPath:" << imgPath << endl;
    cout << "tFaceModelDir:" << tFaceModelDir << endl;
    cout << "embeddingPath:" << embeddingPath << endl;

    string tmp = (embeddingPath + "/embedding.dat");// ubuntu tmp var clear
    const char *fpath = tmp.c_str();

    long fileLength = getFileLength(fpath);

    if (fileLength == -1) {
        cerr << "embeddingPath [" << fpath << "] not found ." << endl;
        exit(-1);
    }

    int len = fileLength / sizeof(User);
    User *users = (User *) malloc(len * sizeof(User));
    int len2 = readUsers(fpath, users);

    cout << "-------------------------------" << len << "," << len2 << "-------------------------------" << endl;
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

//    int video = 0;
    auto *cap = new VideoCapture();
//    cap->open("rtsp://admin:111111ab@192.168.100.251:554/h264/ch1/main/1");
    cap->open(video);
//    VideoCapture cap(0);
    cout << cap->isOpened() << endl;
    if (!cap->isOpened()) {
        cout << "Failure to turn on video capture ." << endl;
        return -1;
    }
    Mat frame;
    bool first = true;
    bool stop = false;
    while (!stop) {

        if (!cap->isOpened() || !cap->read(frame)) {
            cap->release();
            cap->open(video);
            continue;
        }
//        dst = frame;
        frame.copyTo(dst);
//        cv::resize(frame, dst, cv::Size(640, 360), 0, 0, cv::INTER_CUBIC);
//        cv::cvtColor(frame, dst, COLOR_BGR2GRAY);

        if (first) {
            std::thread t(recognize, mDetect, mRecognize, users, len2, ip, port, waitTime);
            t.detach();
            first = false;
        }


        /* imshow("", dst);

         if ((cv::waitKey(2) & 0xEFFFFF) == 27)//esc
             stop = true;*/

    }

    return 0;
}