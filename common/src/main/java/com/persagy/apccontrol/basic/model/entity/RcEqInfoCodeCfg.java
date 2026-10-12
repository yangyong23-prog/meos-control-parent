package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 设备信息点配置 已经绑定的设备信息通过rc_info_code_cfg配置生成
 *
 * @TableName rc_eq_info_code_cfg
 */
@TableName(value = "rc_eq_info_code_cfg")
@Data
@EqualsAndHashCode(callSuper = true)
public class RcEqInfoCodeCfg extends SynDTO {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 配置ID
     */
    @TableId
    private String id;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 设备id
     */
    private String equipmentId;
    /**
     * 设备类
     */
    private String classCode;
    /**
     * 信息点
     */
    private String infoCode;
    /**
     * 信息点名称
     */
    private String name;
    /**
     * 信息点别名
     */
    private String alias;
    /**
     * 单位名称
     */
    private String unit;
    /**
     * 换算系数。页面展示的数值=采集数据*换算系数
     */
    private Double scale;
    /**
     * 展示位置，枚举类型。如页面展示的数值=采集数据*换算系数
     */
    private String displayPosition;
    /**
     * 最大值
     */
    private Integer max;
    /**
     * 最小值
     */
    private Integer min;
    /**
     * 展示顺序
     */
    private Integer ordered;
    /**
     * 参数类型：运行参数，设定参数
     * 枚举类型:ParameterTypeEnum
     */
    private String parameterType;
    /**
     * 扩展信息
     */
    private String extendInfo;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 同步时间
     */
    private Date sysTime;
    /**
     * 系统Id
     */
    private String systemId;
    /**
     * 分组信息
     */
    private String groupInfo;
    /**
     * 状态,枚举类型.
     */
    @TableField(value = "enum")
    private String enumTpye;
}