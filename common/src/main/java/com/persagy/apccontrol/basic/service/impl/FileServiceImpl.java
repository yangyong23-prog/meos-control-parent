package com.persagy.apccontrol.basic.service.impl;

import com.persagy.apccontrol.basic.service.FileService;
import com.persagy.apccontrol.basic.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service("FileService")
@Scope("prototype")
public class FileServiceImpl implements FileService  {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    // 服务器地址
    @Value("${url.cloud-dtp-file-server:}")
    private String IMAGE_URL;
    // 服务器用户名
    @Value("${image.server.systemId:}")
    private String SYSTEM_ID;
    // 服务器密码
    @Value("${image.server.secret:}")
    private String SECRET;

    @Override
    public String uploadFileToImageService(InputStream is, String fileId) throws Exception {
        String url = assembleUploadUrl(fileId);
        logger.error("url:{}",url);
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                logger.error("up------------{}",e.getStackTrace());
            }
        }
        return HttpUtils.httpPostRequest(url, is);
    }

    public String uploadFileToImageService(File file, String fileId) throws Exception {
        String url = assembleUploadUrl(fileId);
        return HttpUtils.httpPostRequest(url, file);
    }

    public byte[] downloadFileFromImageService(String fileId) throws Exception {
        String url = assembleDownloadUrl(fileId);
        return HttpUtils.httpGetFile(url);
    }

    private String assembleUploadUrl(String fileId) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer("");
        sb.append(IMAGE_URL).append("/common/file_upload?&overwrite=true&systemId=").append(SYSTEM_ID).append("&secret=").append(SECRET)
                .append("&key=").append(URLEncoder.encode(fileId,"UTF-8"));
        return sb.toString();
    }

    private String assembleDownloadUrl(String fileId) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer("");
        sb.append(IMAGE_URL).append("/common/file_get?systemId=").append(SYSTEM_ID).append("&key=").append(URLEncoder.encode(fileId,"UTF-8"));
        return sb.toString();
    }

    @Override
    public String initFileId(String fileName, String fileSuffix) {
        long time = System.currentTimeMillis();
        String randomStr = Double.toString(Math.random()).substring(2);
        String key = time + randomStr;
        if (key.length() > 30) {
            key = key.substring(0, 30);
        } else {
            int middle = 30 - key.length();
            for (int i = 0; i < middle; i++) {
                key = key + "1";
            }
        }

        key += (fileName) + '.' + (fileSuffix);
        return key;
    }


}
