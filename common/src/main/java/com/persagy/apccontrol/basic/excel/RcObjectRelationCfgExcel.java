package com.persagy.apccontrol.basic.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 3D模型下关联设备-关系配置信息
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Data
public class RcObjectRelationCfgExcel {

    @ExcelProperty(index = 0)
    private String id;

    /** 集团编码 */
    @ExcelProperty(index = 1)
    private String groupCode;

    /** 项目id */
    @ExcelProperty(index = 2)
    private String projectId;

    /** 系统ID */
    @ExcelProperty(index = 3)
    private String systemId;

    /** 系统类型 */
    @ExcelProperty(index = 4)
    private String systemType;

    /** 主设备类编码-需要进行关联设备查询的主对象对应的设备类编码 */
    @ExcelProperty(index = 5)
    private String masterClassCode;

    /** 主设备类名称-需要进行关联设备查询的主对象对应的设备类名称 */
    @ExcelProperty(index = 6)
    private String masterClassName;

    /** 从设备类编码-关系对象的设备类编码 */
    @ExcelProperty(index = 7)
    private String slaveClassCode;

    /** 从设备类名称-关系对象的设备类名称 */
    @ExcelProperty(index = 8)
    private String slaveClassName;

    /** 图类型编码 */
    @ExcelProperty(index = 9)
    private String graphCode;

    /** 图类型名称 */
    @ExcelProperty(index = 10)
    private String graphName;

    /** 边类型编码 */
    @ExcelProperty(index = 11)
    private String relCode;

    /** 边类型名称 */
    @ExcelProperty(index = 12)
    private String relName;

    /** 分组名称-实指关系类型的名称 */
    @ExcelProperty(index = 13)
    private String groupName;

    /** 关系方向，主或从，from-主，to-从 */
    @ExcelProperty(index = 14)
    private String relDirection;

    /** 扩展信息 */
    @ExcelProperty(index = 15)
    private String extendInfo;

}