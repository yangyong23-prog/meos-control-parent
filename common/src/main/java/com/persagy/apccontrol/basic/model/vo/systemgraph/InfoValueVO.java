package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.cloud.commons.lang.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class InfoValueVO {

    @Schema(description = "信息点值")
    private String infoValue;
    @Schema(description = "图片链接")
    private String url;
    @Schema(description = "图片域名")
    private String host;
    @Schema(description = "文件名")
    private String filename;
    @Schema(description = "文件名")
    private String fileUrl;

    public String getFileUrl() {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(host)) {
            return null;
        }
        if (!host.endsWith("/")) {
            return host + "/" + url;
        }
        return host + url;
    }
}
