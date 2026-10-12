package com.persagy.apccontrol.basic.service;

import java.io.File;
import java.io.InputStream;

public interface FileService {

    /**
     * 上传文件
     * @param is
     * @param fileId
     * @return
     * @throws Exception
     */
    String uploadFileToImageService(InputStream is, String fileId) throws Exception;

    /**
     * 上传文件
     * @param file
     * @param fileId
     * @return
     * @throws Exception
     */
    String uploadFileToImageService(File file, String fileId) throws Exception;

    /**
     * 下载文件
     * @param fileId
     * @return
     * @throws Exception
     */
    byte[] downloadFileFromImageService(String fileId) throws Exception;

    /**
     * 获取文件id
     * @param fileName
     * @param fileSuffix
     * @return
     */
    String initFileId(String fileName, String fileSuffix);
}
