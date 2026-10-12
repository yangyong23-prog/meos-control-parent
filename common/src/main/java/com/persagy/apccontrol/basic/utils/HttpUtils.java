package com.persagy.apccontrol.basic.utils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @Description
 * @Author feng
 * @Date 2018/7/19 12:10
 * @Version 1.0
 */
@Slf4j
public class HttpUtils {

    private final static PoolingHttpClientConnectionManager CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
    private final static CloseableHttpClient HTTP_CLIENT;
    private static final String UTF8 = "utf-8";

    static {
        // 设置整个连接池最大连接数 根据自己的场景决定
        CONNECTION_MANAGER.setMaxTotal(100);
        // 每个路由(网站)的最大连接数
        CONNECTION_MANAGER.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig.custom()
                // 连接建立时间，三次握手完成时间
                .setConnectTimeout(60000)
                // 数据传输过程中数据包之间间隔的最大时间
                .setSocketTimeout(300000)
                // httpClient使用连接池来管理连接，这个时间就是从连接池获取连接的超时时间
                .setConnectionRequestTimeout(30000).build();
        HTTP_CLIENT = HttpClients.custom().setConnectionManager(CONNECTION_MANAGER)
                .setDefaultRequestConfig(requestConfig).build();
    }

    private static boolean isValidUri(URI uri) {
        // 示例：禁止访问本地回环地址或私有 IP 地址
        String host = uri.getHost();
        if (host == null) {
            return false;
        }

        // 更完整的校验逻辑可根据业务需求扩展
        return !host.equals("localhost") &&
                !host.startsWith("127.") &&
                !host.matches("10\\.\\d+\\.\\d+\\.\\d+") &&
                !host.matches("192\\.168\\.\\d+\\.\\d+") &&
                !host.matches("172\\.(1[6-9]|2\\d|3[0-1])\\.\\d+\\.\\d+");
    }

    /**
     * GET 请求
     *
     * @param groupCode
     * @param projectId
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public static String httpGetRequest(String groupCode, String projectId, String url, Map<String, Object> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (CollectionUtil.isNotEmpty(params)) {
            for (Entry<String, Object> objectEntry : params.entrySet()) {
                if (objectEntry.getValue() != null) {
                    uriBuilder.setParameter(objectEntry.getKey(), String.valueOf(objectEntry.getValue()));
                }
            }
        }

        URI uri = uriBuilder.build();
        if (!isValidUri(uri)) {
            log.error("无效的URL [请求url]：【{}】", uri);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "无效的URL");
        }
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        if (StrUtil.isNotBlank(groupCode)) {
            httpGet.addHeader("groupCode", groupCode);
            httpGet.addHeader("group-code", groupCode);
        }
        if (StrUtil.isNotBlank(projectId)) {
            httpGet.addHeader("projectId", projectId);
            httpGet.addHeader("project-id", projectId);
        }
        httpGet.addHeader("token", "eyJhbGciOiJIUzI1NiJ9.eyJkZXZpY2VUeXBlIjoxLCJhdWQiOiJ5dXNodS1zYWFzLWNsaWVudCIsInN1YiI6Inl1c2h1LXVzZXIiLCJhY2NvdW50SWQiOiIxNTA5ODMyMjM3NjE3MTgwNjc0IiwiYWNjb3VudE5hbWUiOiIxODA4ODg4ODEwMCIsImFjY291bnRUeXBlIjoiMiIsImlzcyI6Inl1c2h1IiwianRpIjoiNTMyNjY5MDMtNWQ2Zi00MDUyLWJmNzYtMzQ2YTI2YTUzOTExIn0.wM1XXhdqjFxvkIu6xCEnlcINfzkvSEtEtLKlQtmdoKY");

        String result = sendRequest(httpGet);
        return result;
    }

    /**
     * 发数 post 请求
     *
     * @param groupCode
     * @param projectId
     * @param url
     * @param body
     * @param params
     * @return
     * @throws Exception
     */
    public static String httpPostRequest(String groupCode, String projectId, String url, Object body, Map<String, Object> params) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (CollectionUtil.isNotEmpty(params)) {
            for (Entry<String, Object> objectEntry : params.entrySet()) {
                if (objectEntry.getValue() != null) {
                    uriBuilder.setParameter(objectEntry.getKey(), String.valueOf(objectEntry.getValue()));
                }
            }
        }

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        if (body != null) {
            httpPost.setEntity(new StringEntity(JsonUtils.toJson(body), StandardCharsets.UTF_8));
        }

        if (StrUtil.isNotBlank(groupCode)) {
            httpPost.addHeader("groupCode", groupCode);
            httpPost.addHeader("group-code", groupCode);
        }
        if (StrUtil.isNotBlank(projectId)) {
            httpPost.addHeader("projectId", projectId);
            httpPost.addHeader("project-id", projectId);
        }
        httpPost.addHeader("token", "eyJhbGciOiJIUzI1NiJ9.eyJkZXZpY2VUeXBlIjoxLCJhdWQiOiJ5dXNodS1zYWFzLWNsaWVudCIsInN1YiI6Inl1c2h1LXVzZXIiLCJhY2NvdW50SWQiOiIxNTA5ODMyMjM3NjE3MTgwNjc0IiwiYWNjb3VudE5hbWUiOiIxODA4ODg4ODEwMCIsImFjY291bnRUeXBlIjoiMiIsImlzcyI6Inl1c2h1IiwianRpIjoiNTMyNjY5MDMtNWQ2Zi00MDUyLWJmNzYtMzQ2YTI2YTUzOTExIn0.wM1XXhdqjFxvkIu6xCEnlcINfzkvSEtEtLKlQtmdoKY");

        return sendRequest(httpPost);
    }

    private static String sendRequest(HttpUriRequest request) throws IOException {
        CloseableHttpResponse response = HTTP_CLIENT.execute(request);
        return EntityUtils.toString(response.getEntity(), UTF8);
    }

    public static String httpPostRequest(String url, InputStream is) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new InputStreamEntity(is));
        return sendRequest(httpPost);
    }

    public static String httpPostRequest(String url, File file) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new FileEntity(file));
        return sendRequest(httpPost);
    }

    public static byte[] httpGetFile(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
        return EntityUtils.toByteArray(response.getEntity());
    }

    public static String get(String url, Map<String, String> params) throws Exception {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, "utf-8"));
        }
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);

        //请求超时
        httpGet.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
        //读取超时
        httpGet.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201 && statusCode != 204) {
            httpGet.abort();
            log.error("post请求失败 [请求url]：【{}】[参数]:【{}】", url, params);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_c0846a1d257f4d7c98d97e17c193aba6") + url);
        }
        HttpEntity entity = response.getEntity();
        String result = null;
        if (entity != null) {
            result = EntityUtils.toString(entity, "utf-8");
        }
        EntityUtils.consume(entity);
        response.close();
        log.info("[请求url]：【{}】,[请求结果]:【{}】", url, result);
        return result;
    }

    // 返回到Web
    public static void writeToWeb(Workbook sheets, HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        try {
            response.setHeader("Content-Disposition", "attachment;fileName=" +
                    URLEncoder.encode(fileName, "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            sheets.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_681cc3f85bb04fbba6b93ab5aeb79aad"));
        }
    }

}
