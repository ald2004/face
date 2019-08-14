package com.face.sdk.main;

public class IOUUtil {

    public static float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = l1 > l2 ? l1 : l2;
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = r1 < r2 ? r1 : r2;
        return right - left;
    }

    public static float boxIntersection(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        float w = overlap(x1, w1, x2, w2);
        float h = overlap(y1, h1, y2, h2);
        if (w < 0 || h < 0) return 0;
        return w * h;
    }

    public static float boxUnion(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        float i = boxIntersection(x1, y1, w1, h1, x2, y2, w2, h2);
        float u = w1 * h1 + w2 * h2 - i;
        return u;
    }

    public static float boxIOU(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
        return boxIntersection(x1, y1, w1, h1, x2, y2, w2, h2) / boxUnion(x1, y1, w1, h1, x2, y2, w2, h2);

    }
}