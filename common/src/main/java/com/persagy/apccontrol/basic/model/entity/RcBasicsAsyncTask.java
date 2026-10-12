package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.task.SystemEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 基础任务表（配置定时任务）
 *
 * @TableName rc_basics_async_task
 */
@TableName(value = "rc_basics_async_task")
@Data
@EqualsAndHashCode(callSuper = true)
public class RcBasicsAsyncTask extends SynDTO {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 日志ID
     */
    @TableId
    private String id;
    /**
     * 项目ID
     */
    private String projectId;
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
     * 配置类型
     */
    private OperateTypeEnum type;
    /**
     * 执行系统 云端端还是本地某个系统
     */
    private SystemEnum sys;
    /**
     * 配置状态 默认生效 vaild
     */
    private StateTypeEnum taskSt;
    /**
     * 匹配表达式(后续扩展使用)
     */
    private String spEl;
    /**
     * 最新执行状态  init 初始化，fail 执行失败，succeed执行成功，underway执行中
     */
    private String executeSt;
    /**
     * 执行评率（分钟）默认 60分钟
     */
    private Integer timeFrequency;
    /**
     * 任务开始时间
     */
    private Date startTime;
    /**
     * 任务结束时间
     */
    private Date endTime;
    /**
     * 任务上下文
     */
    private String ctx;
    /**
     * 扩展信息
     */
    private String extendInfo;
    /**
     * 备注说明
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
     * 更新时间
     */
    private Date updateTime;
    /**
     * 同步时间 只有同步时间为空才进行同步
     */
    private Date synTime;
}