//
// Created by wangxiaoming on 2019/4/25.
//

#include <iostream>
#include "com_face_sdk_jni_FaceSDK.h"
#include "face_sdk.h"
#include <opencv2/opencv.hpp>
#include <detect.h>
#include <recognize.h>

#ifdef _WIN32

#include <windows.h>
#include <FacePreprocess.h>

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
    time_t licenseTime = 1600000000;
};

SDK sdk{};

void showLicense() {
    tm result_time{};
    LOCALTIME(&result_time, &sdk.licenseTime);

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

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    faceModelInit
 * Signature: (I)V
 */
JNIEXPORT jint JNICALL Java_com_face_sdk_jni_FaceSDK_faceModelInit
        (JNIEnv *, jobject, jint threadNum) {
    if (sdk.inited) {
        sdk.mRecognize->SetThreadNum(threadNum);
        sdk.mDetect->SetThreadNum(threadNum);
        return FACE_SDK_STATUS_OK;
    }
    showLicense();
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

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    faceModelConf
 * Signature: ([FI)I
 */
JNIEXPORT jint JNICALL Java_com_face_sdk_jni_FaceSDK_faceModelConf
        (JNIEnv *env, jobject, jfloatArray threshold_, jint minFaceSize) {
    CHECK_LICENSE()

    if (minFaceSize < 48)
        return FACE_SDK_STATUS_FACE_SIZE_TOO_SMALL;

    jfloat *threshold = env->GetFloatArrayElements(threshold_, nullptr);

    if (!(threshold[0] > 0 && threshold[0] <= 1) ||
        !(threshold[1] > 0 && threshold[1] <= 1) ||
        !(threshold[2] > 0 && threshold[2] <= 1)) {
        return FACE_SDK_STATUS_ILLEGAL_PARAMETER;
    }
    env->ReleaseFloatArrayElements(threshold_, threshold, 0);

    memcpy(sdk.threshold, threshold, sizeof(sdk.threshold));

    sdk.minFaceSize = minFaceSize;

    if (sdk.inited) {
        sdk.mDetect->setThreshold(sdk.threshold);
        sdk.mDetect->SetMinFace(sdk.minFaceSize);
    }
    return FACE_SDK_STATUS_OK;
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    faceDetect
 * Signature: ([BIILjava/util/ArrayList;)I
 */
JNIEXPORT jint JNICALL Java_com_face_sdk_jni_FaceSDK_faceDetect___3BIILjava_util_ArrayList_2
        (JNIEnv *env, jobject, jbyteArray faceDate_, jint rows_, jint cols_, jobject faceBoxes_) {
    CHECK_LICENSE()

    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    int rows = rows_;
    int cols = cols_;

    jbyte *faceDate = env->GetByteArrayElements(faceDate_, nullptr);
    auto *faceImageCharDate = (unsigned char *) faceDate;
//    Mat(int rows, int cols, int type, void* data, size_t step=AUTO_STEP);
    cv::Mat src(rows, cols, CV_8UC3, faceImageCharDate);

    ncnn::Mat ncnnData = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_BGR2RGB, cols, rows);


    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    std::vector<Face::Bbox> box;

    sdk.mDetect->start(ncnnData, box);

    auto num_face = static_cast<int32_t>(box.size());
    /* get the list class */
    jclass cls_list = env->GetObjectClass(faceBoxes_);
    if (cls_list == nullptr) {
        return FACE_SDK_UNKNOWN_ERROR;
    }
    jclass face_box_class = env->FindClass("com/face/sdk/jni/FACE_BOX");
    jmethodID face_box_init_method = (*env).GetMethodID(face_box_class, "<init>", "(FIIIIF[F[BIII[F)V");

    jmethodID jadd = (*env).GetMethodID(cls_list, "add", "(Ljava/lang/Object;)Z");

    for (int i = 0; i < num_face; i++) {
        FACE_BOX faceBox{};
        faceBox.x = box[i].x1;
        faceBox.y = box[i].y1;
        faceBox.width = box[i].x2 - box[i].x1;
        faceBox.height = box[i].y2 - box[i].y1;
        faceBox.score = box[i].score;
        faceBox.area = box[i].area;

        jfloatArray farr = env->NewFloatArray(10);
        env->SetFloatArrayRegion(farr, 0, 10, box[i].ppoint);


        cv::Rect rect(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);
        cv::Mat warp;

        FacePreprocess::faceAlign(src, warp, rect, box[i]);

        cv::Mat dst;
        cv::resize(warp, dst, FacePreprocess::DST_SIZE, 0, 0, cv::INTER_CUBIC);

        cv::Mat tmp;
        gray(dst, tmp);

        ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(tmp.data, ncnn::Mat::PIXEL_BGR2RGB,
                                                          tmp.cols,
                                                          tmp.rows);
        std::vector<float> feature;
        sdk.mRecognize->start(resize_mat_sub, feature);
        float embedding[128];
        memcpy(embedding, &feature[0], sizeof(float) * 128);

        jfloatArray jembedding = env->NewFloatArray(128);
        env->SetFloatArrayRegion(jembedding, 0, 128, embedding);

//        warp.data
        int datasize = warp.total() * warp.channels();
        jbyteArray jbyteArr = env->NewByteArray(datasize);
        jbyte *_data = new jbyte[datasize];

        for (int j = 0; j < datasize; j++) {
            _data[j] = warp.data[j];
        }

        env->SetByteArrayRegion(jbyteArr, 0, datasize, _data);


        jobject jfaceBox = (*env).NewObject(face_box_class, face_box_init_method,
                                            faceBox.score,
                                            faceBox.x,
                                            faceBox.y,
                                            faceBox.width,
                                            faceBox.height,
                                            faceBox.area,
                                            farr,
                                            jbyteArr,
                                            warp.rows,
                                            warp.cols,
                                            warp.channels(),
                                            jembedding
        );
        // (*env).SetFloatField(jfaceBox, jfieldID_score, faceBox.score);

        (*env).CallVoidMethod(faceBoxes_, jadd, jfaceBox);
    }

    env->ReleaseByteArrayElements(faceDate_, faceDate, 0);
    return FACE_SDK_STATUS_OK;
}


/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    sayHello
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_face_sdk_jni_FaceSDK_sayHello(JNIEnv *, jobject) {
    std::cout << "Say Hello!" << std::endl;
}
/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    showImg
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_face_sdk_jni_FaceSDK_showImg
        (JNIEnv *env, jobject, jbyteArray faceDate_, jint rows_, jint cols_) {

    int rows = rows_;
    int cols = cols_;

    jbyte *faceDate = env->GetByteArrayElements(faceDate_, nullptr);
    auto *faceImageCharDate = (unsigned char *) faceDate;
//    Mat(int rows, int cols, int type, void* data, size_t step=AUTO_STEP);
    cv::Mat mat(rows, cols, CV_8UC3, faceImageCharDate);

    cv::imshow("imshow", mat);
    cv::waitKey();
    env->ReleaseByteArrayElements(faceDate_, faceDate, 0);

}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    releaseVideo
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_face_sdk_jni_FaceSDK_releaseVideo
        (JNIEnv *, jobject, jlong videoHandle) {
    cv::VideoCapture videoCapture = *(cv::VideoCapture *) videoHandle;
    videoCapture.release();
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    isVideoOpened
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_face_sdk_jni_FaceSDK_isVideoOpened
        (JNIEnv *, jobject, jlong videoHandle) {
    cv::VideoCapture videoCapture = *(cv::VideoCapture *) videoHandle;
    return (jboolean) videoCapture.isOpened();
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    showMat
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_face_sdk_jni_FaceSDK_showMat
        (JNIEnv *, jobject, jlong matHandle) {
    cv::imshow(std::to_string(matHandle), *(cv::Mat *) matHandle);
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    waitKey
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_face_sdk_jni_FaceSDK_waitKey
        (JNIEnv *, jobject, jint delay) {
    return cv::waitKey(delay);
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    cloneMat
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_com_face_sdk_jni_FaceSDK_cloneMat
        (JNIEnv *, jobject, jlong matHandle, jlong cloneMatHandle) {
    (*((cv::Mat *) matHandle)).copyTo(*((cv::Mat *) cloneMatHandle));
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    releaseMat
 * Signature: (J)J
 */
JNIEXPORT void JNICALL Java_com_face_sdk_jni_FaceSDK_releaseMat
        (JNIEnv *, jobject, jlong matHandle) {
    ((cv::Mat *) matHandle)->release();
}



/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    VideoCapture
 * Signature: ([C)J
 */
JNIEXPORT jlong JNICALL Java_com_face_sdk_jni_FaceSDK_VideoCapture___3C
        (JNIEnv *env, jobject, jcharArray url_) {

    jchar *jurl = env->GetCharArrayElements(url_, nullptr);
    int len = env->GetArrayLength(url_);
    char *url = (char *) malloc(static_cast<size_t>(len + 1) * sizeof(char));
    //memcpy(url, jurl, static_cast<size_t>(len) * sizeof(char));
    for (int i = 0; i < len; i++) {
        url[i] = static_cast<char>(jurl[i]);
    }
    url[len] = '\0';
    auto *cap = new cv::VideoCapture();
//    std::cout << "len:" << len << " URL:" << url << " url_len:" << strlen(url) << std::endl;
    cap->open(url);
    env->ReleaseCharArrayElements(url_, jurl, 0);
    if (!cap->isOpened()) {
        cap->release();
        return -1L;
    }

    return (jlong) cap;
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    VideoCapture
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_face_sdk_jni_FaceSDK_VideoCapture__I
        (JNIEnv *, jobject, jint device) {
    auto *cap = new cv::VideoCapture();
    cap->open(device);
    if (!cap->isOpened()) {
        cap->release();
        return -1L;
    }
    return (jlong) cap;
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    createMat
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_face_sdk_jni_FaceSDK_createMat
        (JNIEnv *, jobject) {
    return (jlong) new cv::Mat();
}

/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    read
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_com_face_sdk_jni_FaceSDK_read
        (JNIEnv *, jobject, jlong videoHandle, jlong matHandle) {
    cv::VideoCapture videoCapture = *(cv::VideoCapture *) videoHandle;
//    std::cout << (videoCapture.isOpened() ? "True" : "False") << std::endl;
    if (!videoCapture.isOpened()) return -1L;
    auto *frame = (cv::Mat *) matHandle;
    videoCapture.read(*frame);
    return (jlong) (frame->empty() ? (cv::Mat *) -1L : frame);
}



/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    writeMat
 * Signature: (J[C)V
 */
JNIEXPORT jboolean JNICALL Java_com_face_sdk_jni_FaceSDK_writeMat
        (JNIEnv *env, jobject, jlong matHandle, jcharArray savePath_) {
    std::vector<int> IMWRITE_PARAMS = {CV_IMWRITE_JPEG_QUALITY, 100};
    auto *frame = (cv::Mat *) matHandle;
    jchar *jsavePath = env->GetCharArrayElements(savePath_, nullptr);
    int len = env->GetArrayLength(savePath_);
    char *savePath = (char *) malloc(static_cast<size_t>(len + 1) * sizeof(char));
    for (int i = 0; i < len; i++) {
        savePath[i] = static_cast<char>(jsavePath[i]);
    }
    savePath[len] = '\0';

    return (jboolean) cv::imwrite(savePath, *frame, IMWRITE_PARAMS);

}


/*
 * Class:     com_face_sdk_jni_FaceSDK
 * Method:    faceDetect
 * Signature: (JLjava/util/ArrayList;)I
 */
JNIEXPORT jint JNICALL Java_com_face_sdk_jni_FaceSDK_faceDetect__JLjava_util_ArrayList_2
        (JNIEnv *env, jobject, jlong handle, jobject faceBoxes_) {
    CHECK_LICENSE()

    if (!sdk.inited)
        return FACE_SDK_STATUS_NOT_INIT_ERROR;

    cv::Mat src = *(cv::Mat *) handle;
    ncnn::Mat ncnnData = ncnn::Mat::from_pixels(src.data, ncnn::Mat::PIXEL_BGR2RGB, src.cols, src.rows);
    if (src.empty())
        return FACE_SDK_STATUS_EMPTY_MAT_ERROR;

    std::vector<Face::Bbox> box;

    sdk.mDetect->start(ncnnData, box);

    auto num_face = static_cast<int32_t>(box.size());
    /* get the list class */
    jclass cls_list = env->GetObjectClass(faceBoxes_);
    if (cls_list == nullptr) {
        return FACE_SDK_UNKNOWN_ERROR;
    }
    jclass face_box_class = env->FindClass("com/face/sdk/jni/FACE_BOX");
    jmethodID face_box_init_method = (*env).GetMethodID(face_box_class, "<init>", "(FIIIIF[F[BIII[F)V");

    jmethodID jadd = (*env).GetMethodID(cls_list, "add", "(Ljava/lang/Object;)Z");

    for (int i = 0; i < num_face; i++) {
        FACE_BOX faceBox{};
        faceBox.x = box[i].x1;
        faceBox.y = box[i].y1;
        faceBox.width = box[i].x2 - box[i].x1;
        faceBox.height = box[i].y2 - box[i].y1;
        faceBox.score = box[i].score;
        faceBox.area = box[i].area;

        jfloatArray farr = env->NewFloatArray(10);
        env->SetFloatArrayRegion(farr, 0, 10, box[i].ppoint);

        cv::Rect rect(box[i].x1, box[i].y1, box[i].x2 - box[i].x1, box[i].y2 - box[i].y1);
        cv::Mat warp;

        FacePreprocess::faceAlign(src, warp, rect, box[i]);

        cv::Mat dst;
        cv::resize(warp, dst, FacePreprocess::DST_SIZE, 0, 0, cv::INTER_CUBIC);

        cv::Mat tmp;
        gray(dst, tmp);

        ncnn::Mat resize_mat_sub = ncnn::Mat::from_pixels(tmp.data, ncnn::Mat::PIXEL_BGR2RGB,
                                                          tmp.cols,
                                                          tmp.rows);
        std::vector<float> feature;
        sdk.mRecognize->start(resize_mat_sub, feature);
        float embedding[128];
        memcpy(embedding, &feature[0], sizeof(float) * 128);

        jfloatArray jembedding = env->NewFloatArray(128);
        env->SetFloatArrayRegion(jembedding, 0, 128, embedding);

//        warp.data
        int datasize = warp.total() * warp.channels();
        jbyteArray jbyteArr = env->NewByteArray(datasize);
        jbyte *_data = new jbyte[datasize];

        for (int j = 0; j < datasize; j++) {
            _data[j] = warp.data[j];
        }

        env->SetByteArrayRegion(jbyteArr, 0, datasize, _data);


        jobject jfaceBox = (*env).NewObject(face_box_class, face_box_init_method,
                                            faceBox.score,
                                            faceBox.x,
                                            faceBox.y,
                                            faceBox.width,
                                            faceBox.height,
                                            faceBox.area,
                                            farr,
                                            jbyteArr,
                                            warp.rows,
                                            warp.cols,
                                            warp.channels(),
                                            jembedding
        );
        // (*env).SetFloatField(jfaceBox, jfieldID_score, faceBox.score);

        (*env).CallVoidMethod(faceBoxes_, jadd, jfaceBox);
    }

    return FACE_SDK_STATUS_OK;
}