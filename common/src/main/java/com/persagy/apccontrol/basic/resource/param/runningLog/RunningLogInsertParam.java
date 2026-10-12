package com.persagy.apccontrol.basic.resource.param.runningLog;

import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogContentDto;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTemplateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunningLogInsertParam {

    /**
     * 项目ID
     */
    @NotBlank(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    private String systemId;
    /**
     * 对象id
     */
    @NotBlank(message = "{key_084485a601e5437688eeacf92ba08c45}")
    private String objectId;

    /**
     * 对象名称
     */
    @NotBlank(message = "{key_1cf17efd187445dfba4e7ad87f0f3bd7}")
    private String objectName;

    /**
     * 对象类型
     */
    private String classCode;
    /**
     * 对象分类
     */
    @NotNull(message = "{key_9bde932ce37b4bd897346a93ac9aa414}")
    private RunningLogObjectCategpryEnum objectCategory;

    /**
     * 三级分类
     */
    @NotNull(message = "{key_38649720e7604934be944b258dcd0889}")
    private RunningLogCategoryEnum thirdLevelCategory;

    /**
     * 日志标识1,人工/2:自动
     */
    @NotNull(message = "{key_6e42c9b85b2e4fcb9cfa5eff2dfd4f0e}")
    private RunningLogTypeEnum type;

    /**
     * 操作内容
     */
    @NotNull(message = "{key_93153fab3e40498b93f200665c4a0b32}")
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
    @NotBlank(message = "{key_96317460898b4c92872640af496a837d}")
    private String createUser;
    /**
     * 操作人
     */
    @NotBlank(message = "{key_dafb357a209547a7979f8177af99c3ae}")
    private String keyWord;

    /**
     * 时间
     */
    private LocalDateTime createTime;

    public RunningLogInsertParam(String projectId, String systemId, String objectId, String objectName, String classCode,
                                 RunningLogObjectCategpryEnum objectCategory, RunningLogCategoryEnum thirdLevelCategory, RunningLogTypeEnum type,
                                 String createUser, RunningLogTemplateEnum template, String...vars) {
        this.projectId = projectId;
        this.systemId = systemId;
        this.objectId = objectId;
        this.objectName = objectName;
        this.classCode = classCode;
        this.objectCategory = objectCategory;
        this.thirdLevelCategory = thirdLevelCategory;
        this.type = type;
        this.content = buildDto(template.getTemplate(), vars);
        this.keyWord = MessageFormat.format(template.getKeyWord(), vars);
        this.createUser = createUser;
    }

    public RunningLogInsertParam(String projectId, String systemId, String objectId, String objectName, String classCode,
                                 RunningLogObjectCategpryEnum objectCategory, RunningLogCategoryEnum thirdLevelCategory, RunningLogTypeEnum type,
                                 String createUser) {
        this.projectId = projectId;
        this.systemId = systemId;
        this.objectId = objectId;
        this.objectName = objectName;
        this.classCode = classCode;
        this.objectCategory = objectCategory;
        this.thirdLevelCategory = thirdLevelCategory;
        this.type = type;
        this.createUser = createUser;
    }

    public void buildRunningLogTemplate(RunningLogTemplateEnum template, String...vars) {
        this.content = buildDto(template.getTemplate(), vars);
        this.keyWord = MessageFormat.format(template.getKeyWord(), vars);
    }

    public static RunningLogContentDto buildDto(String template, String... vars) {
        RunningLogContentDto dto = new RunningLogContentDto();
        dto.setTemplate(template);
        dto.setVars(Arrays.asList(vars));
        dto.setContentDesc(MessageFormat.format(template, vars));
        return dto;
    }
}
