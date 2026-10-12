package com.persagy.apccontrol.basic.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 设备半剖图配置
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Data
public class ClassPositionCodeConfigExcel {

    /** 集团编码 */
    @ExcelProperty(index = 0)
    private String groupCode;

    /** 项目id */
    @ExcelProperty(index = 1)
    private String projectId;

    /** 设备类编码 */
    @ExcelProperty(index = 2)
    private String classCode;

    /** 部件名称 */
    @ExcelProperty(index = 3)
    private String positionName;

    /** 信息点编码 */
    @ExcelProperty(index = 4)
    private String infoCode;

}