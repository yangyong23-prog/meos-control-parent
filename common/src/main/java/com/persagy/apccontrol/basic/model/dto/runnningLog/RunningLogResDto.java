package com.persagy.apccontrol.basic.model.dto.runnningLog;

import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RunningLogResDto extends SynDTO {

    private Long id;

    private String oriId;

    /**
     * 项目ID
     */
    private String projectId;

    private String systemId;
    /**
     * 对象id
     */
    private String objectId;

    /**
     * 对象名称
     */
    private String objectName;
    /**
     * 对象分类
     */
    private String objectCategory;
    /**
     * 对象类型
     */
    private String classCode;

    /**
     * 一级分类
     */
    private RunningLogCategoryEnum firstLevelCategory;

    /**
     * 二级分类
     */
    private RunningLogCategoryEnum secondLevelCategory;

    /**
     * 三级分类
     */
    private RunningLogCategoryEnum thirdLevelCategory;

    /**
     * 日志标识1,人工/2:自动
     */
    private Integer type;

    /**
     * 操作内容
     */
    private String content;

    /**
     * 操作详情
     */
    private List<String> detail;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectStartTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectEndTime;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 操作人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
