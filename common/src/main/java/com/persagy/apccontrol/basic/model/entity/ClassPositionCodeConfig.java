package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 设备半剖图配置
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Data
@TableName(value = "class_position_code_config", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ClassPositionCodeConfig extends SynDTO {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;

    /** 集团编码 */
    private String groupCode;

    /** 项目id */
    private String projectId;

    /** 设备类编码 */
    private String classCode;

    /** 部件编码 */
    private String positionCode;

    /** 部件名称 */
    private String positionName;

    /** 信息点编码 */
    private String infoCode;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

}