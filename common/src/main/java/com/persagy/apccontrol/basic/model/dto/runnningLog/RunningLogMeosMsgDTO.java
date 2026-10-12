package com.persagy.apccontrol.basic.model.dto.runnningLog;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 内部保存运行日志对象
 */
@Data
@ToString(callSuper = true)
public class RunningLogMeosMsgDTO {
    
    
    /**
     * 日志类型 1:自动/2人工
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
     * 构建人工类型需求任务
     *
     * @return
     */
    public static RunningLogMeosMsgDTO buildManual(String objectId, String objectName, String keyWord,
                                                   NowRunningLogCategory firstLevelCategory,
                                                   String secondLevelCategory, RunningLogSourceTypeEnum sourceType) {
        RunningLogMeosMsgDTO dto = new RunningLogMeosMsgDTO();
        dto.setType(RunningLogTypeEnum.MANUAL);
        dto.setObjectId(objectId);
        dto.setObjectName(objectName);
        dto.setState(RunningLogSourceStateEnum.SUCCEED);
        dto.setKeyWord(keyWord);
        dto.setFirstLevelCategory(firstLevelCategory);
        dto.setSecondLevelCategory(secondLevelCategory);
        dto.setSourceType(sourceType);
        return dto;
    }
    
    
    public JSONObject getContent() {
        if (content == null) {
            return new JSONObject();
        }
        return content;
    }
}
