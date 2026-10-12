package com.persagy.apccontrol.basic.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩工具类，使用GZIP进行压缩和解压
 *
 * @author muanfu
 * @date 2025/02/10
 */
public class CompressUtil {

    /**
     * 压缩
     *
     * @param str str
     * @return {@link String } 压缩后的Base64字符串
     */
    @SneakyThrows
    public static String compress(@NotBlank String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {

            gzipOS.write(str.getBytes(Charset.defaultCharset()));  // 将字符串写入GZIP流
            gzipOS.finish();                      // 确保数据全部写入
            // 将压缩后的字节数组转为Base64字符串（便于存储/传输）
            byte[] compressedBytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(compressedBytes);
        }
    }

    /**
     * 解压
     *
     * @param compressedStr 压缩后的Base64字符串
     * @return {@link String } 解压的字符串
     */
    @SneakyThrows
    public static String decompress(@NotBlank String compressedStr) {
        if (StringUtils.isBlank(compressedStr)) {
            return compressedStr;
        }

        try {
            // Base64解码
            byte[] compressedBytes = Base64.getDecoder().decode(compressedStr);

            try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedBytes);
                 GZIPInputStream gzipIS = new GZIPInputStream(bis);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipIS.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);  // 读取解压后的数据
                }

                return bos.toString(Charset.defaultCharset().name());   // 转为字符串
            }
        } catch (Exception e) {
            //如果解压失败，返回原字符串
            return compressedStr;
        }

    }

}
