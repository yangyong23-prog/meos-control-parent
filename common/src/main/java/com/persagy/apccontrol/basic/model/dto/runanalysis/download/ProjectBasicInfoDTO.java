package com.persagy.apccontrol.basic.model.dto.runanalysis.download;

import lombok.Builder;
import lombok.Data;

/**
 * 项目基础信息DTO
 *
 * @author: system
 * @create: 2026-03-17
 */
@Data
@Builder
public class ProjectBasicInfoDTO {

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 市内区域
     */
    private String urbanZone;
}
