package com.persagy.apccontrol.basic.model.vo.log;


import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运行日志VO
 */
@Data
public class RunningLogDownloadVO {
    
    @TableField(exist = false)
    @Excel(name = "日志分类", orderNum = "1")
    private String logCategory;
    
    @Excel(name = "日志标识", orderNum = "2")
    private String logType;
    
    @Schema(description = "操作人名称(默认'系统')")
    @Excel(name = "操作人", orderNum = "3")
    private String createUser;
    
    @Schema(description = "操作对象名称")
    @Excel(name = "操作对象", orderNum = "4")
    private String objectName;
    
    @Schema(description = "操作内容")
    @Excel(name = "操作内容", orderNum = "5")
    private String contentDesc;
    
    @Excel(name = "生效时间", orderNum = "6")
    @TableField(exist = false)
    private String effectTime;
    
    @Excel(name = "操作原因", orderNum = "7")
    private String reason;
    
    @Excel(name = "操作时间")
    @TableField(exist = false)
    private String logTime;
    
}
