package com.persagy.apccontrol.basic.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 *
 * @author JiYinchuan
 * @since v4.5.0
 */
public class ResponseUtils {

    /**
     * 向客户端输出文件响应信息
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param file     文件
     * @throws IOException 获取输出流失败
     * @since v4.5.0
     */
    public static void writeOfFile(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull File file) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        MediaType mediaType;
        String fileBaseName = FilenameUtils.getName(file.getAbsolutePath());

        ServletContext servletContext = request.getServletContext();
        String mineType = servletContext.getMimeType(fileBaseName);
        try {
            mediaType = MediaType.parseMediaType(mineType);
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        response.setContentType(mediaType.getType());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=".concat(URLEncoder.encode(fileBaseName, StandardCharsets.UTF_8.name())));
        response.setContentLengthLong(file.length());

        FileUtils.copyFile(file, response.getOutputStream());
    }
}
