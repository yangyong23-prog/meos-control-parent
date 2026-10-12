package com.persagy.apccontrol.basic.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * dmp请求url参数对象
 * @author 叶丁辉
 * @date 2021/6/16
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DmpQueryStringsDto {

    /**
     * 应用id
     */
    private String appId;

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 方案id
     */
    private String schemeId;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * userId，字典数据使用
     */
    private String userId;


    public DmpQueryStringsDto setGroupCode(boolean condition, String groupCode) {
        if (condition) {
            return setGroupCode(groupCode);
        }
        return this;
    }

    public DmpQueryStringsDto setProjectId(boolean condition, String projectId) {
        if (condition) {
            return setProjectId(projectId);
        }
        return this;
    }
}
