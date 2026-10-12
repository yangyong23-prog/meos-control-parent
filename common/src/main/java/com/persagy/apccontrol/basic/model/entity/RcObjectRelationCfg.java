package com.persagy.apccontrol.basic.model.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 3D模型下关联设备-关系配置信息
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Data
@TableName(value = "rc_object_relation_cfg", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class RcObjectRelationCfg extends SynDTO {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;

    /** 集团编码 */
    private String groupCode;

    /** 项目id */
    private String projectId;

    /** 系统ID */
    private String systemId;

    /** 系统类型 */
    private String systemType;

    /** 主设备类编码-需要进行关联设备查询的主对象对应的设备类编码 */
    private String masterClassCode;

    /** 主设备类名称-需要进行关联设备查询的主对象对应的设备类名称 */
    private String masterClassName;

    /** 从设备类编码-关系对象的设备类编码 */
    private String slaveClassCode;

    /** 从设备类名称-关系对象的设备类名称 */
    private String slaveClassName;

    /** 图类型编码 */
    private String graphCode;

    /** 图类型名称 */
    private String graphName;

    /** 边类型编码 */
    private String relCode;

    /** 边类型名称 */
    private String relName;

    /** 关系方向，主或从，from-主，to-从 */
    private String relDirection;

    /** 分组名称-实指关系类型的名称 */
    private String groupName;

    /** 扩展信息 */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject extendInfo;

    /** 创建时间 */
    private Date createTime;

    /** 同步时间 */
    private Date synTime;

}