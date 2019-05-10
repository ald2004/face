package com.face.sdk.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * project : eladmin
 * Code Create : 2019/5/9
 * Class : com.face.sdk.main.Test
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class Test {
    public Test() {
    }

    public static void main(String[] args) throws Exception {

        BufferedImage image = ImageIO.read(new File("D:\\projects\\java\\face-server\\eladmin\\imgs\\face\\job\\4\\255DC840F43D415381FC2EF465AA4E3B.jpg"));

        Graphics2D g = (Graphics2D) image.getGraphics();

        double[][] ppoints = new double[][]{{405.7119, 413.16895, 415.07373, 396.83643, 401.9834, 227.49414, 231.78027, 246.77148, 266.7461, 268.8379}};

        for (double[] ppoint : ppoints) {
            Point2D left = new java.awt.geom.Point2D.Double(ppoint[0], ppoint[5]);
            Point2D right = new java.awt.geom.Point2D.Double(ppoint[1], ppoint[6]);
            Point2D nose = new java.awt.geom.Point2D.Double(ppoint[2], ppoint[7]);

            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3.0f));
            for (int i = 0; i < 5; i++) {
                g.drawOval((int) ppoint[i], (int) ppoint[i + 5], 3, 3);
            }

            double angle = calcFaceHAngle(left, right, nose);
            System.out.println(angle);
        }
        File output = new File("test.png");
        ImageIO.write(image, "png", output);
        Desktop.getDesktop().open(output);

    }


    private static double calcRotationAngle(Point2D left, Point2D right) {
        double dy = (right.getY() - left.getY());
        double dx = (right.getX() - left.getX());
        return Math.atan2(dy, dx) * 180.0 / Math.PI;
    }

    private static double calcFaceHAngle(Point2D left, Point2D right, Point2D nose) {
        double angle = -calcRotationAngle(left, right);

        Point2D l = calcRotationPoint(left, nose, angle);
        Point2D r = calcRotationPoint(right, nose, angle);
        Point2D n = calcRotationPoint(nose, nose, angle);

        double lr_w = Math.abs(l.getX() - r.getX());
        double ln_w = Math.abs(l.getX() - n.getX());
        double rn_w = Math.abs(r.getX() - n.getX());

//        cout << "lr_w:" << lr_w << endl;
//        cout << "ln_w:" << ln_w << endl;
//        cout << "rn_w:" << rn_w << endl;

        return Math.max(0, Math.min(1, (ln_w / lr_w))) * 180 - 90;
    }

    private static Point2D calcRotationPoint(Point2D point, Point2D center, double angle) {
        //       (rx0,ry0)
        //       x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
        //       y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;
        double radian = (angle / 180.0 * Math.PI);
        return new java.awt.geom.Point2D.Double(
                ((center.getX() - point.getX()) * Math.cos(radian)) - ((center.getY() - point.getY()) * Math.sin(radian)) + center.getX(),
                ((center.getX() - point.getX()) * Math.sin(radian)) - ((center.getY() - point.getY()) * Math.cos(radian)) + center.getY()
        );
    }


}
