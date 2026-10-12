package com.persagy.apccontrol.basic.model.vo.log;


import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行日志VO
 */
@Data
public class RunningLogVO {
    
    
    @Schema(description = "日志类型 1:自动/2人工/3 报警")
    private RunningLogTypeEnum type;
    
    @Schema(description = "操作对象id")
    private String objectId;
    
    @Schema(description = "操作对象名称")
    private String objectName;
    
    @Schema(description = "对象类型")
    private String classCode;
    
    @Schema(description = "反馈状态(-1控制结果未知/1成功/2失败/3超时/4终止/5异常)")
    private RunningLogSourceStateEnum state;
    
    @Schema(description = "操作关键字")
    private String keyWord;
    
    @Schema(description = "操作人名称(默认'系统')")
    private String createUser;
    
    @Schema(description = "数据时间")
    private LocalDateTime dataTime;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "生效开始时间")
    private LocalDateTime effectStartTime;
    
    @Schema(description = "生效结束时间")
    private LocalDateTime effectEndTime;
    
    @Schema(description = "所属于系统 ID")
    private String systemId;
    
    @Schema(description = "系统名称")
    private String systemName;
    
    @Schema(description = "系统编码")
    private String systemClassCode;
    
    @Schema(description = "日志一级分类")
    private NowRunningLogCategory firstLevelCategory;
    
    @Schema(description = "日志一级分类名称")
    private String firstLevelCategoryName;
    
    @Schema(description = "日志二级分类")
    private String secondLevelCategory;
    
    @Schema(description = "日志二级分类名称")
    private String secondLevelCategoryName;
    
    @Schema(description = "追踪ID")
    private String traceId;
    
    @Schema(description = "信息点编码")
    private String infoCode;
    
    @Schema(description = "扩展信息")
    private JSONObject content;
    
    @Schema(description = "操作内容")
    private String contentDesc;
    
    /**
     * 兼容之前数据结构
     *
     * @return
     */
    public String getCategory() {
        return secondLevelCategoryName;
    }
}
