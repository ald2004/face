//
// Created by wangxiaoming on 2019/3/19.
//

#ifndef FACE_FACE_SDK_H
#define FACE_FACE_SDK_H

#define FACE_SDK_VERSION 1.1
#define FACE_SHOW_LOG


#ifdef _WIN32
#   ifdef __cplusplus
#       define FACE_SDK_DLL_EXP __declspec(dllexport)
#   else
#       define FACE_SDK_DLL_EXP
#   endif
#else
#   define FACE_SDK_DLL_EXP
#endif

#define FACE_SDK_STATUS_OK 0 // 正常
#define FACE_SDK_STATUS_LICENSE_ERROR -1 // 授权错误

#define FACE_SDK_STATUS_NOT_IN_USER 201 // 未识别到脸

#define FACE_SDK_STATUS_ILLEGAL_PARAMETER 300 // 非法参数
#define FACE_SDK_STATUS_FACE_SIZE_TOO_SMALL 301 // 最小人脸尺寸必须 48像素

#define FACE_SDK_UNKNOWN_ERROR 400 // 未知
#define FACE_SDK_STATUS_NOT_INIT_ERROR 401 // 模型未初始化
#define FACE_SDK_STATUS_EMPTY_MAT_ERROR 402 // 空的图片
#define FACE_SDK_STATUS_EMPTY_USER_ERROR 403 // 空的图片

#define FACE_SDK_STATUS_NOT_FOUND_ERROR 404 // 找不到目录或文件
#define FACE_SDK_STATUS_IO_ERROR 405 // IO 异常 打开或写入文件失败

#define FACE_SDK_STATUS_MODEL_LOAD_ERROR 405 // 找不到目录或文件

#include <opencv2/opencv.hpp>
#include "User.h"

struct FACE_BOX {
    float score;
    int x;
    int y;
    int width;
    int height;
    float area;
//    float points[10];
};


#ifdef __cplusplus
extern "C" {
#endif
/**
 * 从图像创建 User 对象。用于从图片生成人脸库。
 * 建议在使用之前使用
 *
 * float threshold[3] = {0.6f, 0.7f, 0.8f};
 * face_model_conf(threshold,60);
 * 防止图片质量及尺寸旋转等问题 检测不到脸的情况。
 *
 * @param src 脸图片
 * @param user user对象
 * @param dst
 * @return
 */
FACE_SDK_DLL_EXP int face_embedding(cv::Mat &src, float embedding[128], cv::Mat &dst);

/**
 *
 * @param filepath
 * @param users
 * @param size
 * @return
 */
FACE_SDK_DLL_EXP int save_face_users(char *filepath, Face::User *users, int size);
/**
 * 从文件读取人脸库
 * @param filePath
 * @param users
 * @param size
 * @return
 */
FACE_SDK_DLL_EXP int load_face_users(char *filePath, Face::User *&users, int &size);

/**
 * 初始化模型
 * @param threadNum def 3
 * @return
 */
FACE_SDK_DLL_EXP int
face_model_init(int threadNum = 3);

/**
 * 设置
 *
 * @param threshold float threshold[3] = {0.9f, 0.9f, 0.99f};
 * @param minFaceSize
 * @return
 */
FACE_SDK_DLL_EXP int face_model_conf(float threshold[3], int minFaceSize = 60);

/**
 * 寻找人脸，未旋转矫正
 * @param src image
 * @param faceBoxes face box
 * @return
 */
FACE_SDK_DLL_EXP int face_detect(cv::Mat &src, std::vector<FACE_BOX> &faceBoxes);

/**
 *  图片与人脸库对比
 * @param src
 * @param users users
 * @param size  users 尺寸
 * @param index 返回值 users中的 序号
 * @param score 返回值 置信度
 * @param threshold  def 0.55 若低于此阈值  index = -1 ; score = -1
 * @return
 */
FACE_SDK_DLL_EXP int
face_compare(cv::Mat &src, Face::User *users, int size, int &index, double &score, float threshold = 0.55);

/**
 * 检测与人脸识别
 * @param src image
 * @param users user database
 * @param size  users size
 *
 * @param faceBoxes 返回值 face box
 * @param indexes 返回值 users中的 序号
 * @param scores  返回值 置信度
 * @param threshold def 0.55 若低于此阈值的 face box 会被过滤掉
 * @return
 */
FACE_SDK_DLL_EXP int
face_detect_and_compare(cv::Mat &src, std::vector<FACE_BOX> &faceBoxes, Face::User *users, int size,
                        std::vector<int> &indexes, std::vector<float> &scores,
                        float threshold = 0.55,
                        double scale = 1);

#ifdef __cplusplus
}
#endif
#endif //FACE_FACE_SDK_H
