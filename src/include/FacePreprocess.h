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

    void rotateAndCut(Mat &src, Mat &dst, Rect faceBox, float angle) {

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
                Point2f(faceBox.x + dx + faceBox.width, faceBox.y + dy + faceBox.height), affine_matrix);


        int x = (int) (std::min)(leftTop.x, leftBottom.x);
        int y = (int) (std::min)(leftTop.y, rightTop.y);
        int w = static_cast<int>((std::max)(rightTop.x, rightBottom.x) - x);
        int h = static_cast<int>((std::max)(leftBottom.y, rightBottom.y) - y);
        int maxWH = (std::max)(w, h);

        dst = Mat(dst, Rect(x, y, maxWH, maxWH));

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

    float calcRotationAngle(Point2f left, Point2d right) {
        double dy = (right.y - left.y);
        double dx = (right.x - left.x);
        double angle = atan2(dy, dx) * 180.0 / CV_PI; // Convert from radians to degrees.
        return static_cast<float>(angle);
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
