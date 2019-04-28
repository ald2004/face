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


#include <fstream>
#include <iostream>

#ifdef _WIN32

#include <direct.h>
#include <io.h>

#define ACCESS _access
#define MKDIR(a) _mkdir((a))
#else
#include <unistd.h>
#include <dirent.h>
#include <stdarg.h>
#include "sys/stat.h"
#define ACCESS access
#define MKDIR(a) mkdir((a),0755)
#endif


using namespace cv;
using namespace std;
std::vector<int> IMWRITE_PARAMS = {CV_IMWRITE_JPEG_QUALITY, 100};

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

#ifdef _WIN32

void GetStringSize(HDC hDC, const char *str, int *w, int *h) {
    SIZE size;
    GetTextExtentPoint32A(hDC, str, strlen(str), &size);
    if (w != 0) *w = size.cx;
    if (h != 0) *h = size.cy;
}

void putTextZH(Mat &dst, const char *str, Point org, Scalar color, int fontSize, const char *fn, bool italic,
               bool underline) {
    CV_Assert(dst.data != 0 && (dst.channels() == 1 || dst.channels() == 3));

    int x, y, r, b;
    if (org.x > dst.cols || org.y > dst.rows) return;
    x = org.x < 0 ? -org.x : 0;
    y = org.y < 0 ? -org.y : 0;

    LOGFONTA lf;
    lf.lfHeight = -fontSize;
    lf.lfWidth = 0;
    lf.lfEscapement = 0;
    lf.lfOrientation = 0;
    lf.lfWeight = 5;
    lf.lfItalic = italic;   //斜体
    lf.lfUnderline = underline; //下划线
    lf.lfStrikeOut = 0;
    lf.lfCharSet = DEFAULT_CHARSET;
    lf.lfOutPrecision = 0;
    lf.lfClipPrecision = 0;
    lf.lfQuality = PROOF_QUALITY;
    lf.lfPitchAndFamily = 0;
    strcpy_s(lf.lfFaceName, fn);

    HFONT hf = CreateFontIndirectA(&lf);
    HDC hDC = CreateCompatibleDC(0);
    HFONT hOldFont = (HFONT) SelectObject(hDC, hf);

    int strBaseW = 0, strBaseH = 0;
    int singleRow = 0;
    char buf[1 << 12];
    strcpy_s(buf, str);
    char *bufT[1 << 12];  // 这个用于分隔字符串后剩余的字符，可能会超出。
    //处理多行
    {
        int nnh = 0;
        int cw, ch;

        const char *ln = strtok_s(buf, "\n", bufT);
        while (ln != 0) {
            GetStringSize(hDC, ln, &cw, &ch);
            strBaseW = max(strBaseW, cw);
            strBaseH = max(strBaseH, ch);

            ln = strtok_s(0, "\n", bufT);
            nnh++;
        }
        singleRow = strBaseH;
        strBaseH *= nnh;
    }

    if (org.x + strBaseW < 0 || org.y + strBaseH < 0) {
        SelectObject(hDC, hOldFont);
        DeleteObject(hf);
        DeleteObject(hDC);
        return;
    }

    r = org.x + strBaseW > dst.cols ? dst.cols - org.x - 1 : strBaseW - 1;
    b = org.y + strBaseH > dst.rows ? dst.rows - org.y - 1 : strBaseH - 1;
    org.x = org.x < 0 ? 0 : org.x;
    org.y = org.y < 0 ? 0 : org.y;

    BITMAPINFO bmp = {0};
    BITMAPINFOHEADER &bih = bmp.bmiHeader;
    int strDrawLineStep = strBaseW * 3 % 4 == 0 ? strBaseW * 3 : (strBaseW * 3 + 4 - ((strBaseW * 3) % 4));

    bih.biSize = sizeof(BITMAPINFOHEADER);
    bih.biWidth = strBaseW;
    bih.biHeight = strBaseH;
    bih.biPlanes = 1;
    bih.biBitCount = 24;
    bih.biCompression = BI_RGB;
    bih.biSizeImage = strBaseH * strDrawLineStep;
    bih.biClrUsed = 0;
    bih.biClrImportant = 0;

    void *pDibData = 0;
    HBITMAP hBmp = CreateDIBSection(hDC, &bmp, DIB_RGB_COLORS, &pDibData, 0, 0);

    CV_Assert(pDibData != 0);
    HBITMAP hOldBmp = (HBITMAP) SelectObject(hDC, hBmp);

    //color.val[2], color.val[1], color.val[0]
    SetTextColor(hDC, RGB(255, 255, 255));
    SetBkColor(hDC, 0);
    //SetStretchBltMode(hDC, COLORONCOLOR);

    strcpy_s(buf, str);
    const char *ln = strtok_s(buf, "\n", bufT);
    int outTextY = 0;
    while (ln != 0) {
        TextOutA(hDC, 0, outTextY, ln, strlen(ln));
        outTextY += singleRow;
        ln = strtok_s(0, "\n", bufT);
    }
    uchar *dstData = (uchar *) dst.data;
    int dstStep = dst.step / sizeof(dstData[0]);
    unsigned char *pImg = (unsigned char *) dst.data + org.x * dst.channels() + org.y * dstStep;
    unsigned char *pStr = (unsigned char *) pDibData + x * 3;
    for (int tty = y; tty <= b; ++tty) {
        unsigned char *subImg = pImg + (tty - y) * dstStep;
        unsigned char *subStr = pStr + (strBaseH - tty - 1) * strDrawLineStep;
        for (int ttx = x; ttx <= r; ++ttx) {
            for (int n = 0; n < dst.channels(); ++n) {
                double vtxt = subStr[n] / 255.0;
                int cvv = vtxt * color.val[n] + (1 - vtxt) * subImg[n];
                subImg[n] = cvv > 255 ? 255 : (cvv < 0 ? 0 : cvv);
            }

            subStr += 3;
            subImg += dst.channels();
        }
    }

    SelectObject(hDC, hOldBmp);
    SelectObject(hDC, hOldFont);
    DeleteObject(hf);
    DeleteObject(hBmp);
    DeleteDC(hDC);
}

#endif

/**
  * @param folderPath
  * @param files  bufferList
  * @param depth
  */
void listFiles(const string &folderPath, list<string> *files, int depth) {
#ifdef _WIN32
    _finddata_t FileInfo{};
    string strfind = folderPath + "\\*";
    intptr_t Handle = _findfirst(strfind.c_str(), &FileInfo);

    if (Handle == -1L) {
        exit(-1);
    }
    do {
        string filename = (folderPath + "\\" + FileInfo.name);
        // has children
        if (FileInfo.attrib & _A_SUBDIR) {
            // curr dir & per dir
            if ((depth - 1 > 0) && (strcmp(FileInfo.name, ".") != 0) && (strcmp(FileInfo.name, "..") != 0)) {
                files->push_back(filename);
                listFiles(filename, files, depth - 1);
            }
        } else {
            files->push_back(filename);
        }
    } while (_findnext(Handle, &FileInfo) == 0);

    _findclose(Handle);
#else
    DIR *dir;
        struct dirent *entry;
        if ((dir = opendir(folderPath.c_str())) == NULL) {
            fprintf(stderr, "cannot open directory: %s\n", folderPath.c_str());
            return;
        }
        while ((entry = readdir(dir)) != NULL) {
            struct stat s{};
            string filename = (folderPath + "/" + entry->d_name);
            lstat(filename.c_str(), &s);
//            cout << filename << endl;
            if (S_ISDIR(s.st_mode)) {

                if (strcmp(".", entry->d_name) == 0 ||
                    strcmp("..", entry->d_name) == 0)
                    continue;

                if (depth - 1 > 0) {
                    files->push_back(filename);
                    listFiles(entry->d_name, files, depth - 1);
                }
            } else {
                files->push_back(filename);
            }
        }
        closedir(dir);
#endif
}

/**
   * @param pszDir
   * @return
   */
int createDir(const char *pszDir) {
    int iRet;
    //not exist
    iRet = ACCESS(pszDir, 0);
    if (iRet != 0) {
        iRet = MKDIR(pszDir);
        if (iRet != 0) {
            return -1;
        }
    }
    return 0;
}

void detImg(char *embeddingPath, const string &imgPath) {
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
        if (face_model_init(2) != FACE_SDK_STATUS_OK) throw runtime_error("model init err.");
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


        // ########### 缩放检测 ##############
        // ########### 缩放检测 ##############

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

                rectangle(img1, Rect(box.x, box.y, box.width, box.height), Scalar(0, 0, 255));

                cout << "[ x:" << box.x << ", y:" << box.y << ", w:" << box.width << ", h:" << box.height << " ] "
                     << "score:" << score
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

void detRtsp(char *embeddingPath, const string &url) {
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
        if (face_model_init(4) != FACE_SDK_STATUS_OK) throw runtime_error("model init err.");
        // ########### 初始化模型 ##############


        // ########### 打开摄像头 ##############
        auto *cap = new VideoCapture();
        if (isNum(url)) {
            istringstream isURL(url);
            int video;
            isURL >> video;
            cap->open(video);
            //cap->set(CV_CAP_PROP_FRAME_WIDTH, 640 * 2);
            //cap->set(CV_CAP_PROP_FRAME_HEIGHT, 360 * 2);
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


            if (face_detect_and_compare(img1, faceBoxes, users, size, indexes, scores, threshold, 1) ==
                FACE_SDK_STATUS_OK) {

                int64 end = CLOCK();
                // ########### 对比人脸 ##############

                // ########### 解析结果画框 ##############
                int boxSize = faceBoxes.size();

                for (int i = 0; i < boxSize; ++i) {
                    auto box = faceBoxes[i];
                    auto index = indexes[i];
                    auto score = scores[i];

                    rectangle(img1, Rect(box.x, box.y, box.width, box.height), Scalar(0, 0, 255));
#ifdef _WIN32
                    putTextZH(img1, users[index].name, Point(box.x, (std::max)(0, box.y - 30)), Scalar(128, 255, 100), 30,
                              "楷体", false,
                              false);
#endif
                    string outpath;
                    outpath.append("./imgs/");
                    outpath.append(users[index].name);
                    outpath.append("-");
                    outpath.append(to_string(score));
                    outpath.append(".jpg");
                    imwrite(outpath, img1, IMWRITE_PARAMS);

                    cout << "[ x:" << box.x << ", y:" << box.y << ", w:" << box.width << ", h:" << box.height << " ] "
                         << "score:" << score
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

void embeddingToFile(const string &faceImgPath) {

    // ########### 遍历目录下JPG文件 ##############
    // buffer
    list<string> imgs;
    // list file
    listFiles(faceImgPath, &imgs, 0);

    int len = 0;

    vector<string> imgVector;
    for (const auto &img : imgs) {
        if (endsWith(img.substr(faceImgPath.length() + 1, img.length()), ".jpg")) {
            len++;
            imgVector.push_back(img);
        }
    }
    // ########### 遍历目录JPG文件 ##############


    // ########### 根据文件个数开辟内存空间 ##############
    auto *users = (Face::User *) malloc(len * sizeof(Face::User));
    // ########### 根据文件个数开辟内存空间 ##############

    // ########### 设置模型参数 ##############
    if (face_model_conf(new float[3]{0.6f, 0.7f, 0.8f}, 60) != FACE_SDK_STATUS_OK)
        throw runtime_error("config err.");
    // ########### 设置模型参数 ##############

    // ########### 初始化模型 ##############
    if (face_model_init(4) != FACE_SDK_STATUS_OK) throw runtime_error("model init err.");
    // ########### 初始化模型 ##############

    // ########### 循环生成User ##############
    for (unsigned int index = 0; index < imgVector.size(); ++index) {

        // ########### 解析路径-文件名 ##############
        string img = imgVector[index];
        string filename = img.substr(faceImgPath.length() + 1, img.length());
        string name = filename.substr(0, filename.length() - 4);
        // ########### 解析路径-文件名 ##############

        // ########### 读取文件 ##############
        Mat mat = imread(img);
        // ########### 读取文件 ##############

        // ########### 生成向量 ##############
/*        char id[32] = "id";
        char name[64] = "name";*/
        float embedding[128];
        Mat dst;
        face_embedding(mat, embedding, dst);
        Face::User user{to_string(index).c_str(), name.c_str(), embedding};
        users[index] = user;
        // ########### 生成向量 ##############


        // ########### 在人脸库目录创建 output文件夹 并把处理后的脸放入此文件夹 ##############
        cout << "name:" << user.name << endl;
        createDir((faceImgPath + "/output").c_str());
        string outpath;
        outpath.append(faceImgPath);
        outpath.append("/output/");
        outpath.append(filename);
        imwrite(outpath, dst, IMWRITE_PARAMS);
        // ########### 在人脸库目录创建 output文件夹 并把处理后的脸放入此文件夹 ##############

    }
    // ########### 循环生成User ##############

    // ########### 将 ##############
    string outpath;
    outpath.append(faceImgPath);
    outpath.append("/embedding.dat");
    save_face_users(const_cast<char *>(outpath.c_str()), users, len);
    // ########### 循环生成User ##############
}

int main(int argc, char **argv) {

    char *embeddingPath = const_cast<char *>("./embedding.dat");

    if (argc < 2) {
        cerr << "no argv." << endl;
        return -1;
    }

    string imgPath = argv[1];

    if (startsWith(imgPath, "embedding://")) {
        // 生成 embedding.dat to imgPath
        embeddingToFile(imgPath.substr(12));
    } else if (startsWith(imgPath, "rtsp://")
               || isNum(imgPath)
               || endsWith(imgPath, ".mp4")
               || endsWith(imgPath, ".avi")) {
        const string &url = imgPath;
        // 视频实时识别
        detRtsp(embeddingPath, url);
    } else if (endsWith(imgPath, ".jpg")) {
        // 识别一张图片
        detImg(embeddingPath, imgPath);
    } else {
        cerr << "args can be [embedding://xxx] or [0] or [rtsp://xxxx] or [xxxxx.jpg] ." << endl;
        return -1;
    }

    return 0;
}

