package com.persagy.apccontrol.basic.model.entity;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.utils.UserUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备冷热源切换记录表
 *
 * @TableName equip_record
 */
@TableName(value = "equip_record", autoResultMap = true)
@Data
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
public class EquipRecord implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 集团编码
     */
    @TableField(value = "group_code")
    private String groupCode;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 系统ID(无系统场景为空 如 公区照明系统 无系统 ID 承载)
     */
    @TableField(value = "system_id")
    private String systemId;

    /**
     * 系统编码(无系统场景为 类型编码:PUBLIC_AREA_LIGHTING 公区照明系统 DemandTemplateTypeEnum)
     */
    @TableField(value = "system_code")
    private String systemCode;

    /**
     * 设备对象ID
     */
    @TableField(value = "obj_id")
    private String objId;

    @TableField(value = "class_code", exist = false)
    private String classCode;

    /**
     * 变更内容
     */
    @TableField(value = "change_content", typeHandler = JacksonTypeHandler.class)
    private ChangeContent changeContent;

    /**
     * 执行状态
     *
     * 默认 -1 没有执行
     * 0：执行失败
     * 1：执行成功
     */
    @TableField(value = "exec_status")
    private Integer execStatus;

    /**
     * 创建人
     */
    @TableField(value = "creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "creation_time")
    private String creationTime;

    /**
     * 修改时间
     */
    @TableField(value = "modified_time")
    private String modifiedTime;

    /**
     * 修改人
     */
    @TableField(value = "modifier")
    private String modifier;

    /**
     * 扩展字段
     */
    @TableField(value = "extend", typeHandler = JacksonTypeHandler.class)
    private Object extend;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String infoCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Deprecated
    public EquipRecord(String groupCode, String projectId, String systemId, String systemCode, String objId, ChangeContent changeContent, String creator, String creationTime) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.systemId = systemId;
        this.systemCode = systemCode;
        this.objId = objId;
        this.changeContent = changeContent;
        this.creator = creator;
        this.creationTime = creationTime;
    }

    public EquipRecord(String groupCode, String projectId, String systemId, String systemCode, String objId, String classCode, ChangeContent changeContent, String creator, String creationTime) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.systemId = systemId;
        this.systemCode = systemCode;
        this.objId = objId;
        this.classCode = classCode;
        this.changeContent = changeContent;
        if (changeContent != null) {
            this.infoCode = changeContent.getInfoCode();
        }
        this.creator = creator;
        this.creationTime = creationTime;
    }

    @Deprecated
    public EquipRecord(String groupCode, String projectId, String systemId, String systemCode, String objId, ChangeContent changeContent) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.systemId = systemId;
        this.systemCode = systemCode;
        this.objId = objId;
        this.changeContent = changeContent;
    }

    public EquipRecord(String groupCode, String projectId, String systemId, String systemCode, String objId, String classCode, ChangeContent changeContent) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.systemId = systemId;
        this.systemCode = systemCode;
        this.objId = objId;
        this.classCode = classCode;
        this.changeContent = changeContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof EquipRecord)) return false;

        EquipRecord that = (EquipRecord) o;

        return new EqualsBuilder().append(getId(), that.getId()).append(getGroupCode(), that.getGroupCode()).append(getProjectId(), that.getProjectId()).append(getSystemId(), that.getSystemId()).append(getSystemCode(), that.getSystemCode()).append(getObjId(), that.getObjId()).append(getChangeContent(), that.getChangeContent()).append(getExecStatus(), that.getExecStatus()).append(getCreator(), that.getCreator()).append(getCreationTime(), that.getCreationTime()).append(getModifiedTime(), that.getModifiedTime()).append(getModifier(), that.getModifier()).append(getExtend(), that.getExtend()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).append(getGroupCode()).append(getProjectId()).append(getSystemId()).append(getSystemCode()).append(getObjId()).append(getChangeContent()).append(getExecStatus()).append(getCreator()).append(getCreationTime()).append(getModifiedTime()).append(getModifier()).append(getExtend()).toHashCode();
    }

    public void init() {
        String time = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);

        this.setModifier(UserUtils.getUserId());
        this.setCreator(UserUtils.getUserId());
        this.setModifiedTime(time);
        this.setCreationTime(time);
    }

    /**
     * 校验两个对象的 对象信息点 值是否一致
     *
     * @return
     */
    public boolean checkInfoCodeAndVal(EquipRecord er) {

        if (er == null) {
            return false;
        }

        return StringUtils.equals(this.getObjId(), er.getObjId())
                && StringUtils.equals(this.getChangeContent().getInfoCode(), er.getChangeContent().getInfoCode())
                && StringUtils.equals(this.getChangeContent().getInfoValue(), er.getChangeContent().getInfoValue())
                ;


    }

    /**
     * 获取对象信息点索引
     *
     * @return
     */
    public String getObjInfoIndexes() {
        return this.getObjId() + ":" + this.getChangeContent().getInfoCode();
    }


    public void addErrorExtend(String msg) {
        addErrorExtend(msg, null);
    }

    /**
     * 添加异常信息
     *
     * @param msg
     */
    public void addErrorExtend(String msg, Integer execStatus) {
        if (execStatus != null) {
            this.execStatus = execStatus;
        }
        if (StringUtils.isNotEmpty(msg)) {
            JSONObject json = new JSONObject();
            json.put("msg", msg);
            this.extend = json.toJSONString();
        }
    }

    public static String getErrorExtendByString(String msg) {
        if (StringUtils.isBlank(msg)) {
            return null;

        }
        JSONObject json = new JSONObject();
        json.put("msg", msg);
        return json.toJSONString();

    }

    @Override
    public String toString() {
        return "EquipRecord{" +
                "id=" + id +
                ", groupCode='" + groupCode + '\'' +
                ", projectId='" + projectId + '\'' +
                ", systemId='" + systemId + '\'' +
                ", systemCode='" + systemCode + '\'' +
                ", objId='" + objId + '\'' +
                ", changeContent=" + changeContent +
                ", execStatus=" + execStatus +
                ", creator='" + creator + '\'' +
                ", creationTime='" + creationTime + '\'' +
                ", modifiedTime='" + modifiedTime + '\'' +
                ", modifier='" + modifier + '\'' +
                ", extend=" + extend +
                '}';
    }
}