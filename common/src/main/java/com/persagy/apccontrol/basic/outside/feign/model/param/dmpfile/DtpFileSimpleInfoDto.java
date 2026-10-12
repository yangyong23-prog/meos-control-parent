package com.persagy.apccontrol.basic.outside.feign.model.param.dmpfile;

import lombok.Data;

@Data
public class DtpFileSimpleInfoDto {
    /**
     * 文件ID
     */
    private String id;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 下载url
     */
    private String downloadUrl;
    /**
     * persis文件下载url
     */
    private String persisFileDownloadUrl;
}
