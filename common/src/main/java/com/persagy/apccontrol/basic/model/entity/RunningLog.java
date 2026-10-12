package com.persagy.apccontrol.basic.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.service.runninglog.RunningLogTemplateConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

/**
 * 感知运行日志表
 *
 * @TableName running_log
 */
@Data
@TableName(value = "running_log", autoResultMap = true)
@FieldNameConstants
public class RunningLog {
    /**
     * 主键 id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 项目 ID
     */
    private String projectId;
    
    /**
     * 日志类型 1:自动/2人工/3 报警
     */
    private RunningLogTypeEnum type;
    
    /**
     * 操作对象id
     */
    private String objectId;
    
    /**
     * 操作对象名称
     */
    private String objectName;
    
    /**
     * 反馈状态(-1控制结果未知/0未完成/1成功/2失败/3超时/4终止/5异常)
     */
    private RunningLogSourceStateEnum state;
    
    /**
     * 操作关键字
     */
    private String keyWord;
    
    /**
     * 操作人名称(默认'系统')
     */
    private String createUser;
    
    /**
     * 数据时间
     */
    private LocalDateTime dataTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 操作内容
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject content;
    
    /**
     * 对象类型
     */
    private String classCode;
    
    /**
     * 所属于系统 ID
     */
    private String systemId;
    
    @Schema(description = "系统名称")
    private String systemName;
    
    @Schema(description = "系统编码")
    private String systemClassCode;
    
    /**
     * 日志一级分类
     */
    private NowRunningLogCategory firstLevelCategory;
    /**
     * 日志二级分类
     */
    private String secondLevelCategory;
    
    /**
     * 生效开始时间
     */
    private LocalDateTime effectStartTime;
    
    /**
     * 生效结束时间
     */
    private LocalDateTime effectEndTime;
    
    /**
     * 追踪ID
     */
    private String traceId;
    
    /**
     * 信息点编码
     */
    private String infoCode;
    
    /**
     * 信息点设定值
     */
    private String infoValue;
    
    /**
     * 来源数据类型(时间表等)
     */
    private RunningLogSourceTypeEnum sourceType;
    
    /**
     * 信息点定义 来源系统查询数据库不存储
     */
    @TableField(exist = false)
    private DtpKnowledgeInfoCodeDTO knowledgeInfoCode;
    
    /**
     * 构建二级分类
     * 通过规则 或者一级分类+ classCode 构建
     */
    public void buildSecondLevelCategory() {
        if (firstLevelCategory == null) {
            return;
        }
        if (StringUtils.isNotBlank(firstLevelCategory.getCodeRule())) {
            this.secondLevelCategory = RunningLogTemplateConfig.spelMapping(this, firstLevelCategory.getCodeRule());
            return;
        }
        NowRunningLogCategory secondLevel =
                NowRunningLogCategory.getSecondLevelCategoryByClassCode(firstLevelCategory.getCode(),
                        this.getClassCode());
        if (secondLevel == null) {
            return;
        }
        this.secondLevelCategory = secondLevel.getCode();
    }
    
    
}