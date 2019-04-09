//
// Created by wangxiaoming on 2019/3/5.
//

//
// Created by Jack Yu on 23/03/2018.
//

#ifndef FACE_DEMO_FACEPREPROCESS_H
#define FACE_DEMO_FACEPREPROCESS_H

#include<opencv2/opencv.hpp>


namespace FacePreprocess {
    using namespace cv;
    using namespace std;

    Point2f DST_LEFT_EYE(35, 35);
    Point2f DST_RIGHT_EYE(77, 35);
    Point2f DST_NOSE(56, 55);
    Size DST_SIZE(112, 112);

    Point2f calcRotationPoint(Point2f point, Mat affine_matrix) {
        int x = static_cast<int>(affine_matrix.ptr<double>(0)[0] * (point.x) +
                                 affine_matrix.ptr<double>(0)[1] * (point.y) +
                                 affine_matrix.ptr<double>(0)[2]);
        int y = static_cast<int>(affine_matrix.ptr<double>(1)[0] * (point.x) +
                                 affine_matrix.ptr<double>(1)[1] * (point.y) +
                                 affine_matrix.ptr<double>(1)[2]);
        return Point2f(x, y);
    }

    Point2f calcRotationPoint(Point2f point, Point2f center, float angle) {
        //       (rx0,ry0)
        //       x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
        //       y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;
        auto radian = (float) (angle / 180.0 * CV_PI);
        return Point2f(
                ((center.x - point.x) * cos(radian)) - ((center.y - point.y) * sin(radian)) + center.x,
                ((center.x - point.x) * sin(radian)) - ((center.y - point.y) * cos(radian)) + center.y
        );
    }

    Point2f calcRotationPoint(const Point2f &point, float angle) {
        return calcRotationPoint(point, Point2f(0, 0), angle);
    }

    float calcRotationAngle(Point2f left, Point2d right) {
        double dy = (right.y - left.y);
        double dx = (right.x - left.x);
        double angle = atan2(dy, dx) * 180.0 / CV_PI; // Convert from radians to degrees.
        return static_cast<float>(angle);
    }

    void translateTransform(cv::Mat const &src, cv::Mat &dst, int dx, int dy) {
        CV_Assert(src.depth() == CV_8U);
        const int rows = src.rows;
        const int cols = src.cols;
        dst.create(rows, cols, src.type());
        Vec3b *p;
        for (int i = 0; i < rows; i++) {
            p = dst.ptr<Vec3b>(i);
            for (int j = 0; j < cols; j++) {
                //平移后坐标映射到原图像
                int x = j - dx;
                int y = i - dy;
                //保证映射后的坐标在原图像范围内
                if (x >= 0 && y >= 0 && x < cols && y < rows)
                    p[j] = src.ptr<Vec3b>(y)[x];
            }
        }
    }

    void translateTransformSize(cv::Mat const &src, cv::Mat &dst, int dx, int dy) {
        CV_Assert(src.depth() == CV_8U);
        const int rows = src.rows + abs(dy); //输出图像的大小
        const int cols = src.cols + abs(dx);
        dst.create(rows, cols, src.type());
        Vec3b *p;
        for (int i = 0; i < rows; i++) {
            p = dst.ptr<Vec3b>(i);
            for (int j = 0; j < cols; j++) {
                int x = j - dx;
                int y = i - dy;
                if (x >= 0 && y >= 0 && x < src.cols && y < src.rows)
                    p[j] = src.ptr<Vec3b>(y)[x];
            }
        }
    }

    /**
     * 测试点是否超出屏幕
     * @param point
     * @param matSize
     * @return
     */
    bool validatePoint(Point2f point, Mat mat) {
        return !(point.x < 0 || point.y < 0 || point.x > mat.cols || point.y > mat.rows);
    }

    void faceAlign(Mat &src, Mat &dst, Rect faceBox, Face::Bbox box) {

        // 112 * 112 left eye (35,45)   right eye (77,45)  nose (56,65)
        Point2f leftEye(box.ppoint[0], box.ppoint[5]);
        Point2f rightEye(box.ppoint[1], box.ppoint[6]);
        Point2f nose(box.ppoint[2], box.ppoint[7]);

        float angle = calcRotationAngle(leftEye, rightEye);

        // full
        int maxBorder = (int) (max(src.cols, src.rows) * 1.414); // sqrt(2)*max
        int dx = (maxBorder - src.cols) / 2;
        int dy = (maxBorder - src.rows) / 2;
        copyMakeBorder(src, dst, dy, dy, dx, dx, BORDER_CONSTANT);
        //rotate
        Point2f center(dst.cols / 2.f, dst.rows / 2.f);

        Mat affine_matrix = getRotationMatrix2D(center, angle, 1.0);
        warpAffine(dst, dst, affine_matrix, dst.size());

        Point2f leftTop = calcRotationPoint(Point2f(faceBox.x + dx, faceBox.y + dy), affine_matrix);
        Point2f leftBottom = calcRotationPoint(Point2f(faceBox.x + dx, faceBox.y + dy + faceBox.height), affine_matrix);
        Point2f rightTop = calcRotationPoint(Point2f(faceBox.x + dx + faceBox.width, faceBox.y + dy), affine_matrix);
        Point2f rightBottom = calcRotationPoint(
                Point2f(faceBox.x + dx + faceBox.width, faceBox.y + dy + faceBox.height),
                affine_matrix);

        Point2f leftEyeRote = calcRotationPoint(Point2f(leftEye.x + dx, leftEye.y + dy), affine_matrix);
        Point2f rightEyeRote = calcRotationPoint(Point2f(rightEye.x + dx, rightEye.y + dy), affine_matrix);
        Point2f noseRote = calcRotationPoint(Point2f(nose.x + dx, nose.y + dy), affine_matrix);

        int x = (int) (std::min)(leftTop.x, leftBottom.x);
        int y = (int) (std::min)(leftTop.y, rightTop.y);
        int w = static_cast<int>((std::max)(rightTop.x, rightBottom.x) - x);
        int h = static_cast<int>((std::max)(leftBottom.y, rightBottom.y) - y);
        int maxWH = (std::max)(w, h);
        dst = Mat(dst, Rect(x, y, maxWH, maxWH));
        Point2f leftEyeRoteCut = Point2f(leftEyeRote.x - x, leftEyeRote.y - y);
        Point2f rightEyeRoteCut = Point2f(rightEyeRote.x - x, rightEyeRote.y - y);
        Point2f noseRoteCut = Point2f(noseRote.x - x, noseRote.y - y);

        if (!validatePoint(leftEye, src) || !validatePoint(rightEye, src) || !validatePoint(nose, src)) {
            return;
        }

        // to 112 * 112
        float imgScale = maxWH * 1.f / DST_SIZE.width;
        float faceXScale = abs(
                ((DST_LEFT_EYE * imgScale).x - (DST_RIGHT_EYE * imgScale).x) /
                (leftEyeRoteCut.x - rightEyeRoteCut.x));
        float faceYScale = abs(
                ((DST_LEFT_EYE * imgScale).y - (DST_NOSE * imgScale).y) /
                (leftEyeRoteCut.y - noseRoteCut.y));


        Mat output = Mat::zeros(maxWH, maxWH, src.type());
        cv::resize(dst, output, Size(static_cast<int>(dst.cols * faceXScale), static_cast<int>(dst.rows * faceXScale)),
                   0,
                   0, cv::INTER_CUBIC);
        Mat temp = Mat::zeros(output.cols * 4, output.rows * 4, dst.type());
        output.copyTo(
                temp(Rect(temp.cols / 2 - output.cols / 2, temp.rows / 2 - output.rows / 2, output.cols, output.rows))
        );

        int tran_x = (temp.cols / 2 - output.cols / 2) - (DST_LEFT_EYE.x * imgScale - leftEyeRoteCut.x * faceXScale);
        int tran_y = (temp.rows / 2 - output.rows / 2) - (DST_LEFT_EYE.y * imgScale - leftEyeRoteCut.y * faceXScale);

        if (temp.cols > tran_x || temp.rows > tran_y) return;

        temp(Rect(
                tran_x,
                tran_y,
                maxWH,
                maxWH
        )).copyTo(output);



        /* Point2f srcTri[3];
         srcTri[0] = leftEyeRoteCut;
         srcTri[1] = rightEyeRoteCut;
         srcTri[2] = noseRoteCut;

         Point2f dstTri[3];
         dstTri[0] = DST_LEFT_EYE * imgScale;
         dstTri[1] = DST_RIGHT_EYE * imgScale;
         dstTri[2] = DST_NOSE * imgScale;

         Mat warp_mat = getAffineTransform(srcTri, dstTri);
         warpAffine(dst, output, warp_mat, output.size());*/

        dst = output;

        /*cv::resize(output, output, DST_SIZE, 0, 0, cv::INTER_CUBIC);

        circle(output, DST_LEFT_EYE, 5, Scalar(0, 255, 0), -1);
        circle(output, DST_RIGHT_EYE, 5, Scalar(0, 255, 0), -1);
        circle(output, DST_NOSE, 5, Scalar(0, 255, 0), -1);


        circle(src, leftEye, 5, Scalar(0, 255, 0), -1);
        circle(src, rightEye, 5, Scalar(0, 255, 0), -1);
        circle(src, nose, 5, Scalar(0, 255, 0), -1);

        cout<< leftEye << endl;
        cout<< src.size << endl;

        imshow("1", src);
        imshow("2", output);*/
//        waitKey(10);
    }

    void rotate(Mat &src, Mat &dst, float angle, bool isCut = true) {
        auto radian = (float) (angle / 180.0 * CV_PI);

        // full
        int maxBorder = (int) (max(src.cols, src.rows) * 1.414); // sqrt(2)*max
        int dx = (maxBorder - src.cols) / 2;
        int dy = (maxBorder - src.rows) / 2;
        copyMakeBorder(src, dst, dy, dy, dx, dx, BORDER_CONSTANT);
        //rotate
        Point2f center(dst.cols / 2.f, dst.rows / 2.f);
        Mat affine_matrix = getRotationMatrix2D(center, angle, 1.0);
        warpAffine(dst, dst, affine_matrix, dst.size());

        float sinVal = abs(sin(radian));
        float cosVal = abs(cos(radian));
        Size targetSize((int) (src.cols * cosVal + src.rows * sinVal),
                        (int) (src.cols * sinVal + src.rows * cosVal));
        int x = (dst.cols - targetSize.width) / 2;
        int y = (dst.rows - targetSize.height) / 2;
        Rect rect(x, y, targetSize.width, targetSize.height);

        if (isCut && (dst.rows > x + targetSize.width || dst.cols > y + targetSize.height)) {
            dst = Mat(dst, rect);
        }
    }


    float calcFaceHAngle(const Point2f &left, const Point2d &right, const Point2d &nose) {
        float angle = -calcRotationAngle(left, right);
        Point2f l = calcRotationPoint(left, angle);
        Point2f r = calcRotationPoint(right, angle);
        Point2f n = calcRotationPoint(nose, angle);

        double lr_w = abs(l.x - r.x);
        double ln_w = abs(l.x - n.x);
        double rn_w = abs(r.x - n.x);

//        cout << "lr_w:" << lr_w << endl;
//        cout << "ln_w:" << ln_w << endl;
//        cout << "rn_w:" << rn_w << endl;

        auto leftA = static_cast<float>(ln_w / lr_w) * 180 - 90;

        return leftA;
    }

    float calcFaceDefinition(const Mat &src) {
        Mat imageGrey;
        cvtColor(src, imageGrey, CV_RGB2GRAY);
        Mat imageSobel;
        Sobel(imageGrey, imageSobel, CV_16U, 1, 1);
        double meanValue = mean(imageSobel)[0];
        return static_cast<float>(meanValue);
    }

    float calcFaceScore(Mat &src, const Point2f &left, const Point2d &right, const Point2d &nose) {
        float faceHAngle = calcFaceHAngle(left, right, nose);
        float angle = static_cast<float>((std::max)(calcRotationAngle(left, right) / 90.0, 0.0));
        float angleScore = static_cast<float>((std::max)(abs(faceHAngle) / 90.0, 0.0));
        //  1-(math.tanh(4-2)+1)/2  Definition [0-4] 0.1-0.98
        float definition = 1 - (tanh(calcFaceDefinition(src) - 2) + 1) / 2;


        float score = 1;
        score -= angleScore * 0.4f;
        score -= angle * 0.1f;
        score -= definition * 0.7f;
        return score;
    }

    void warpAffineFace(Mat &src, Mat &dst, const Point2f &left, const Point2d &right) {

        float angle = calcRotationAngle(left, right);
//        cout << angle << endl;
        rotate(src, dst, angle);
    }


}
#endif //FACE_DEMO_FACEPREPROCESS_H
