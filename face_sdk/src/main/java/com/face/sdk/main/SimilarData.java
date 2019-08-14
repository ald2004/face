package com.face.sdk.main;

/**
 * project : face-web-server
 * Code Create : 2019/4/28
 * Class : com.face.sdk.main.SimilarData
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
public class SimilarData {
    public SimilarData() {
    }

    //陌生人
    static FaceUserEntity STRANGER = new FaceUserEntity(-1, 3, "陌生人", new float[128], "imgs", "imgs", "0000", "0000", "陌生人");

    double maxSimilar = -1;
    FaceUserEntity maxEntity = STRANGER;

    public SimilarData(double maxSimilar, FaceUserEntity maxEntity) {
        this.maxSimilar = maxSimilar;
        this.maxEntity = maxEntity;
    }
}
