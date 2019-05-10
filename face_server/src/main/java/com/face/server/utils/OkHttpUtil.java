package com.face.server.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>类描述:基于okhttp的http客户端</p>
 *
 * @author xingdl@hundata.com
 * @version v1.0
 * @copyright www.hundata.com
 * @date 2017-06-03 下午2:12
 */
@Slf4j
public class OkHttpUtil {
    private static OkHttpClient client;
    private static MediaType jsonType = MediaType.parse("application/json;charset=utf-8");

    static {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager).connectTimeout(30, TimeUnit.SECONDS);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            client = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get请求
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String get(String url, Map<String, Object> params, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (params != null) {
            url += "?" + getParamFormat(params);
            builder.url(url);
        }
        Request request = null;
        if (headers != null) {
            headers.forEach((k, v) -> {
                builder.header(k, v);
            });
        }
        request = builder.build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.error("请求失败");
            return null;
        }
        return response.body().string();
    }

    /**
     * pot请求
     *
     * @param url
     * @param headers
     * @return
     */
    public static String postForm(String url, Map<String, Object> params, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder();
        FormBody formBody = getFormBody(params);
        builder.url(url).post(formBody);
        if (headers != null) {
            headers.forEach((k, v) -> {
                builder.header(k, v);
            });
        }
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.error("请求失败");
            return null;
        }
        return response.body().string();
    }

    /**
     * post发送json请求
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws Exception
     */
    public static String postJson(String url, Map<String, Object> params, Map<String, String> headers) throws Exception {
        Request.Builder builder = new Request.Builder();
        String data = JSON.toJSONString(params);
        if (headers != null) {
            headers.forEach((k, v) -> {
                builder.header(k, v);
            });
        }
        builder.url(url).post(RequestBody.create(jsonType, data));
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            log.error("请求失败");
            return null;
        }
        return response.body().string();
    }

    /**
     * get请求参数格式化
     *
     * @param params
     * @return
     */
    private static String getParamFormat(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        Iterator<Map.Entry<String, Object>> sets = params.entrySet().iterator();
        while (sets.hasNext()) {
            Map.Entry<String, Object> entry = sets.next();
            sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
            if (sets.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }

    private static FormBody getFormBody(Map<String, Object> params) {
        if (params == null)
            return null;
        FormBody.Builder builder = new FormBody.Builder();
        params.forEach((k, v) -> {
            builder.add(k, v.toString());
        });
        FormBody formBody = builder.build();
        return formBody;
    }

    public static void main(String[] args) throws IOException {
        /*String url = "https://api.hundata.com/v1/faceRecognitionfactory";
        Map<String,String> headers = new HashMap<>();
        headers.put("apkey","sfdsf");
        Map<String,Object> params = new HashMap<>();
        params.put("name","张建");
        params.put("idcard","140104197711112230");
        params.put("bankcard","6225880158494352");
        params.put("apiKey","hrt_nSU6Cajj");
        Map<String,Object> data = new HashMap<>();
        data.put("data", JSON.toJSONString(params));
        System.out.println(postForm(url,data,null));
        */

        String url = "http://localhost/api/faceLog/countNew/4";
        Map<String, Object> params = new HashMap<>();
        params.put("page", "0");
        params.put("size", "10");
        params.put("sort", "id,desc");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTU1NzMwMjczMywiaWF0IjoxNTU3Mjk1NTMzfQ.imkM946gsoKDsxVov5QqnjHiefBnKzj8NJHJYM0380Jrj9_5x9kStPilPPch8UueM7h63SJS7ibvQz2hOPd8Kw");
        System.out.println(get(url, params, headers));

    }

}
