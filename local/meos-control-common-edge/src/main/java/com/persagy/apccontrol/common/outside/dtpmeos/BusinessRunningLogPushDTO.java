package com.persagy.apccontrol.common.outside.dtpmeos;

import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
 * 类业务运行日志推送DTO
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2025/4/21 9:45
 */
@Data
@FieldNameConstants
public class BusinessRunningLogPushDTO {
    
    @Schema(description = "链路追踪的UUID--控制消息使用")
    private String traceData;
    
    @Schema(description = "模块编码--来源于接口header")
    private String modelType;
    
    @Schema(description = "模块归类--如果是modelType为系统ID，这里是对应的系统类型编码")
    private String modelClass;
    
    @Schema(description = "操作人ID--来源于接口header")
    private String userId;
    
    @Schema(description = "操作人账号--来源于接口header")
    private String userName;
    
    @Schema(description = "数据来源")
    private RunningLogTypeEnum dataSource;
    
    @Schema(description = "数据赋予的模板")
    private RunningDataTargetEnum dataTarget;
    
    @Schema(description = "数据操作类型")
    private OperDataTypeEnum dataType;
    
    @Schema(description = "数据操作时间")
    private LocalDateTime dataTime;
    
    @Schema(description = "对象ID")
    private String objectId;
    
    @Schema(description = "对象类编码")
    private String classCode;
    
    @Schema(description = "信息点编码")
    private String infoCode;
    
    @Schema(description = "信息点名称")
    private String infoName;
    
    @Schema(description = "信息点的值")
    private String infoValue;
    
    @Schema(description = "操作前的孪生值")
    private String beforeValue;
    
    /**
     * 1-成功
     * 2-失败
     */
    @Schema(description = "结果")
    private Integer result = 1;
    
    @Schema(description = "操作原因下拉选项")
    private String reasonSource;
    
    @Schema(description = "具体的操作原因")
    private String reasonDetail;
    
    @Schema(description = "备注信息")
    private String remark;
    
    @Schema(description = "扩展信息")
    private ExtendInfo extendInfo;
    
    
    
    @Data
    public static class ExtendInfo {
        
        @Schema(description = "控制台类型")
        private ConsoleTypeEnum consoleType;
        
        @Schema(description = "冷机类型")
        private ChillerTypSub chillerType;
        
        @Schema(description = "冷机数量")
        private String chillerNum;
        
        @Schema(description = "生效开始日")
        private LocalDateTime startDateTime;
        
        @Schema(description = "生效结束日")
        private LocalDateTime endDateTime;
        
        @Schema(description = "日期生效类型")
        private EffectDateTypeEnum effectDateType;
        
        @Schema(description = "日期生效类型对应的生效数据")
        private String effectDateData;
        
        public String getConsoleTypeName() {
            if (consoleType != null) {
                return consoleType.getReasonPhrase();
            }
            return null;
        }
        
        public String getChillerTypeName() {
            if (chillerType != null) {
                return chillerType.getName();
            }
            return null;
        }
    }
    
    
}
