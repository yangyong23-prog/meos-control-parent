package com.persagy.apccontrol.common.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 手动通过 excel 发数
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Data
public class FeedbackInfoMsgMqttExcel {

    /** 集团编码 */
    @ExcelProperty(index = 0)
    private String groupCode;

    /** 项目id */
    @ExcelProperty(index = 1)
    private String projectId;

    /***对象实例Id */
    @ExcelProperty(index = 2)
    private String objectId;
    /*** 对象类编码 */
    @ExcelProperty(index = 3)
    private String classCode;
    /*** 消息点编码 */
    @ExcelProperty(index = 4)
    private String infoCode;
    /***上报时间 yyyyyMMddhhmmss */
    @ExcelProperty(index = 5)
    private String dataTime;
    /***孪生值  */
    @ExcelProperty(index = 6)
    private String dataValue;

    /***控制值 开始时间  */
    @ExcelProperty(index = 7)
    private String startTime;
    /***控制值 结束时间(执行未完成为空)  */
    @ExcelProperty(index = 8)
    private String endTime;

    /***  数据状态 反馈值：数据状态 1-正常，2-异常   控制值状态：  0:开始执行，1:执行中，2完成，3:保护期*/
    @ExcelProperty(index = 9)
    private Integer dataStatus;
    /*** 反馈值  效标志 0-有效 1-超时无效 */
    @ExcelProperty(index = 10)
    private Integer invalid;

    @ExcelProperty(index = 11)
    private Integer forced;
    /*** 时间常量 */
    @ExcelProperty(index = 12)
    private Integer dataIndate;

    @ExcelProperty(index = 13)
    private String dateTimeFromDate;

    @ExcelProperty(index = 14)
    private String timeRange;

    /***类型 0 反馈值 1 设置值*/
    @ExcelProperty(index = 15)
    private Integer type;

}