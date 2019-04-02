#include "face_sdk.h"

#ifdef _WIN32

#include<windows.h>

#define SLEEP(a) Sleep(a)
#define CLOCK() clock()

#else
#include <unistd.h>
#define SLEEP(a) usleep(a*1000)
#define CLOCK() clock()/1000
#endif

using namespace cv;
using namespace std;

bool isNum(string str) {
    stringstream sin(str);
    double d;
    char c;
    if (!(sin >> d))
        return false;
    if (sin >> c)
        return false;
    return true;
}

int startsWith(const string &s, const string &sub) {
    return s.find(sub) == 0 ? 1 : 0;
}

int endsWith(const string &s, const string &sub) {
    return s.rfind(sub) == (s.length() - sub.length()) ? 1 : 0;
}

void detImg(char *embeddingPath, char *landmarkPath, const string &imgPath) {
    try {

        Face::User *users;
        int size;
        // 加载人脸库 embedding.dat
        load_face_users(embeddingPath, users, size);

        // ########### 打印 ##############
        cout << size << endl;
        for (int i = 0; i < size; i++) {
            cout << "id:" << users[i].id << endl;
            cout << "name:" << users[i].name << endl;
            vector<float> feature(users[i].embedding, users[i].embedding + 128);
/*            cout << "embedding:" << endl;
            for (auto &f:feature) {
                cout << f << ",";
            }*/
            cout << endl;
        }
        // ########### 打印 ##############

        // ########### 设置模型参数 ##############
        if (face_model_conf(new float[3]{0.8f, 0.8f, 0.7f}, 60) != FACE_SDK_STATUS_OK)
            throw runtime_error("config err.");
        // ########### 设置模型参数 ##############

        // ########### 初始化模型 ##############
        if (face_model_init(landmarkPath, 2) != FACE_SDK_STATUS_OK) throw runtime_error("model init err.");
        // ########### 初始化模型 ##############

        // ########### 读取本地图片 ##############
        Mat img1 = imread(imgPath);
        // ########### 读取本地图片 ##############

        // ########### 声明变量 ##############
        std::vector<FACE_BOX> faceBoxes;
        std::vector<int> indexes;
        std::vector<float> scores;
        float threshold = 0.55f;
        // ########### 声明变量 ##############

        // ########### 对比人脸 ##############
        int64 start = CLOCK();
        if (face_detect_and_compare(img1, faceBoxes, users, size, indexes, scores, threshold) ==
            FACE_SDK_STATUS_OK) {
            int64 end = CLOCK();
            // ########### 对比人脸 ##############

            // ########### 解析结果画框 ##############
            int boxSize = faceBoxes.size();

            for (int i = 0; i < boxSize; ++i) {
                auto box = faceBoxes[i];
                auto index = indexes[i];
                auto score = scores[i];

                rectangle(img1, Rect(box.x, box.y, box.width, box.height), Scalar(255, 0, 0));

                cout << "score:" << score
                     << " ,name:" << users[index].name
                     << ",time:" << (end - start) << "ms"
                     << endl;
            }
            // ########### 解析结果画框 ##############

            if (boxSize == 0) cout << "not found." << endl;
        } else {
            cout << "not found." << endl;
        }

        imshow("img1", img1);
    } catch (const std::exception &e) {
        cerr << e.what() << endl;
    }
    waitKey();
}

int fps() {
    static int fps = 0;
    static int lastTime = CLOCK(); // ms
    static int frameCount = 0;

    ++frameCount;

    int curTime = CLOCK();
    if (curTime - lastTime > 1000) // 取固定时间间隔为1秒
    {
        fps = frameCount;
        frameCount = 0;
        lastTime = curTime;
    }
    return fps;
}

void detRtsp(char *embeddingPath, char *landmarkPath, const string &url) {
    try {

        Face::User *users;
        int size;
        // 加载人脸库 embedding.dat
        load_face_users(embeddingPath, users, size);

        // ########### 打印 ##############
        cout << size << endl;
        for (int i = 0; i < size; i++) {
            cout << "id:" << users[i].id << endl;
            cout << "name:" << users[i].name << endl;
            vector<float> feature(users[i].embedding, users[i].embedding + 128);
/*            cout << "embedding:" << endl;
            for (auto &f:feature) {
                cout << f << ",";
            }*/
            cout << endl;
        }
        // ########### 打印 ##############

        // ########### 设置模型参数 ##############
        if (face_model_conf(new float[3]{0.8f, 0.8f, 0.7f}, 60) != FACE_SDK_STATUS_OK)
            throw runtime_error("config err.");
        // ########### 设置模型参数 ##############

        // ########### 初始化模型 ##############
        if (face_model_init(landmarkPath) != FACE_SDK_STATUS_OK) throw runtime_error("model init err.");
        // ########### 初始化模型 ##############


        // ########### 打开摄像头 ##############
        auto *cap = new VideoCapture();
        if (isNum(url)) {
            istringstream isURL(url);
            int video;
            isURL >> video;
            cap->open(video);
        } else {
            cap->open(url);
        }
        if (!cap->isOpened()) {
            cerr << "Failure to turn on video capture ." << endl;
            return;
        }
        // ########### 打开摄像头 ##############

        Mat frame;
        Mat img1;
        bool stop = false;
        int count = 0;
        while (!stop) {

            // ########### 摄像头意外断开重连 ##############
            if (!cap->isOpened() || !cap->read(frame)) {
                cap->release();
                if (isNum(url)) {
                    istringstream isURL(url);
                    int video;
                    isURL >> video;
                    cap->open(video);
                } else {
                    cap->open(url);
                }
                continue;
            }
//            if (count++ % 2 == 0) {
//                cap->grab();
//                continue;
//            }
            // ########### 摄像头意外断开重连 ##############

            // ########### 复制图片 ##############
            frame.copyTo(img1);
            // ########### 复制图片 ##############


            // ########### 声明变量 ##############
            std::vector<FACE_BOX> faceBoxes;
            std::vector<int> indexes;
            std::vector<float> scores;
            float threshold = 0.55f;
            // ########### 声明变量 ##############

            // ########### 对比人脸 ##############
            int64 start = CLOCK();
            if (face_detect_and_compare(img1, faceBoxes, users, size, indexes, scores, threshold) ==
                FACE_SDK_STATUS_OK) {
                int64 end = CLOCK();
                // ########### 对比人脸 ##############

                // ########### 解析结果画框 ##############
                int boxSize = faceBoxes.size();

                for (int i = 0; i < boxSize; ++i) {
                    auto box = faceBoxes[i];
                    auto index = indexes[i];
                    auto score = scores[i];

                    rectangle(img1, Rect(box.x, box.y, box.width, box.height), Scalar(255, 0, 0));

                    cout << "score:" << score
                         << " ,name:" << users[index].name
                         << ",time:" << (end - start) << "ms"
                         << endl;
                }
                // ########### 解析结果画框 ##############

//                if (boxSize == 0) cout << "not found." << endl;
            } else {
                //cout << "not found." << endl;
            }

            // ########### 显示FPS ##############
            putText(img1, "fps:" + to_string(fps()), cv::Point(10, 20), 1, 1, cv::Scalar(255, 255, 255));
            // ########### 显示FPS ##############
            // ########### 显示图片 ##############

            imshow("img1", img1);
            if ((cv::waitKey(2) & 0xEFFFFF) == 27)//esc 退出
                stop = true;
            // ########### 显示图片 ##############
        }

    } catch (const std::exception &e) {
        cerr << e.what() << endl;
    }


}

int main(int argc, char **argv) {

    char *embeddingPath = const_cast<char *>("./embedding.dat");
    char *landmarkPath = const_cast<char *>("./landmark.bin");

    if (argc < 2) {
        cerr << "no argv." << endl;
        return -1;
    }

    string imgPath = argv[1];

    if (startsWith(imgPath, "rtsp://") || isNum(imgPath) || endsWith(imgPath, ".mp4") | endsWith(imgPath, ".mp4")) {
        const string &url = imgPath;
        detRtsp(embeddingPath,
                landmarkPath,
                url);
    } else {
        detImg(embeddingPath,
               landmarkPath,
               imgPath);
    }


    return 0;
}

