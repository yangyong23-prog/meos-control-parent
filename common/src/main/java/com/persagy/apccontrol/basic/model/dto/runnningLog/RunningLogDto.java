package com.persagy.apccontrol.basic.model.dto.runnningLog;

import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RunningLogDto extends SynDTO {

    private Long id;

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
     * 对象类
     */
    private String classCode;

    /**
     * 对象名称
     */
    private String objectName;
    /**
     * 对象分类
     */
    private RunningLogObjectCategpryEnum objectCategory;

    /**
     * 一级分类
     */
    private RunningLogCategoryEnum firstLevelCategory;

    private String firstLevelCategoryName;

    /**
     * 二级分类
     */
    private RunningLogCategoryEnum secondLevelCategory;

    private String secondLevelCategoryName;

    /**
     * 三级分类
     */
    private RunningLogCategoryEnum thirdLevelCategory;

    private String thirdLevelCategoryName;

    /**
     * 日志标识1,人工/2:自动
     */
    private Integer type;

    /**
     * 操作内容
     */
    private RunningLogContentDto content;

    /**
     * 操作详情
     */
    private List<RunningLogContentDto> detail;

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

    public String getFirstLevelCategoryName() {
        return firstLevelCategory == null ? null : firstLevelCategory.getName();
    }

    public String getSecondLevelCategoryName() {
        return secondLevelCategory == null ? null : secondLevelCategory.getName();
    }

    public String getThirdLevelCategoryName() {
        return thirdLevelCategory == null ? null : thirdLevelCategory.getName();
    }

}
