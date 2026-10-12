package com.persagy.apccontrol.basic.model.dto;

import lombok.*;

/**
 * 人工干预推送的实体类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/6/27 16:31
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInterventionDTO {

    private String groupCode;

    private String projectId;

    private String objectId;

    private String classCode;

    private String infoCode;

    // 截止时间，yyyyMMddHHmmss
    private String expireTime;

    // true-查询一次数据库
    private boolean dataBase;

    public ReleaseInterventionDTO(String groupCode, String projectId, String objectId, String classCode, String infoCode, boolean dataBase) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.objectId = objectId;
        this.classCode = classCode;
        this.infoCode = infoCode;
        this.dataBase = dataBase;
    }

}
