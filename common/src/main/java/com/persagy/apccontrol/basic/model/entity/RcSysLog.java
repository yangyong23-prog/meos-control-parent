package com.persagy.apccontrol.basic.model.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.persagy.apccontrol.basic.model.dto.SynDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogSourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 系统日志表
 *
 * @TableName rc_sys_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "rc_sys_log", autoResultMap = true)
public class RcSysLog extends SynDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId
    private String id;
    private String groupCode;
    /**
     * 项目ID
     */
    private String projectId;

    private String systemId;
    /**
     * 系统类型
     */
    private DtpRwdSystemClassCodeEnum systemType;
    /**
     * 操作对象id
     */
    private String objectId;
    /**
     * 操作对象本地编码
     */
    private String localId;
    /**
     * 操作对象本地名称
     */
    private String localName;
    /**
     * 设备类编码
     */
    private String classCode;
    /**
     * 设备类名称
     */
    private String className;
    /**
     * 日志来源
     */
    private LogSourceEnum logSource;
    /**
     * 日志类型
     */
    private LogTypeEnum logType;
    /**
     * 数据类型
     * add 新增 delete 删除
     */
    private DataTypeEnum dataType;
    /**
     * 操作类型
     */
    private OperateTypeEnum operateType;
    /**
     * 操作原因
     */
    private String operateReason;
    /**
     * 文本内容，一般是前端展示需要
     */
    private String contentInfo;
    /**
     * 一般存操作结果
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject resultInfo;
    /**
     * 指令详情，json格式，字段随意，必须的为 infoCode、infoValue、infoName
     * [{
     * 	"infoCode": "manualAutoSet",
     * 	"infoValue": "0",
     * 	"infoName": "手自动设定"
     * }]
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONArray pointInfo;
    /**
     * 扩展信息
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private JSONObject extendInfo;
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
     * 接口ip地址
     */
    private String ip;

    // 0-手动，1-自动
    @TableField(exist = false)
    private String manualAutoStatus;

    // 失效时间  yyyyMMddHHmmss
    @TableField(exist = false)
    private String expireTime;

}