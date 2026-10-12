package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


/**
 * 配置日志表
 *
 * @author yuyao
 * @TableName rc_basics_config_log
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "rc_basics_config_log")
@Data
@Slf4j
public class RcBasicsConfigLog extends SynDTO {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId
    private String id;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 配置类型
     */
    private BasicsConfigTypeEnum type;
    /**
     * 子配置类型
     */
    private String childType;
    /**
     * 配置状态(有效无效)
     */
    private StateTypeEnum st;
    /**
     * 建筑编码
     */
    private String architectureCode;
    /**
     * 楼层编码
     */
    private String floorCode;
    /**
     * 房间编码
     */
    private String roomCode;
    /**
     * 设备编码
     */
    private String facilityCode;

    /**
     * 系统编号
     */
    private String systemId;
    /**
     * 表达式
     */
    private String spEl;
    /**
     * 配置生效时间
     */
    private Date startTime;
    /**
     * 配置失效时间
     */
    private Date endTime;
    /**
     * 配置结果MD5(配置结果的MD5用于配置幂等判断)
     */
    private String md;
    /**
     * 配置结果
     */
    private String result;
    /**
     * 扩展信息(额外备用参数配置信息)
     */
    private String extendInfo;
    /**
     * 配置备注
     */
    private String memo;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 同步时间
     */
    private Date synTime;

    /**
     * 配置变更前配置ID
     */
    private String parentId;

}