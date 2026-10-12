package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumJobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 关系图异步任务表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value = "system_graph_async_job", autoResultMap = true)
public class SystemGraphAsyncJob extends BaseAssignIdDO implements Serializable {

    /**
     * 项目id
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 基准对象
     */
    @TableField(value = "system_object_id")
    private String systemObjectId;

    /**
     * 图类型
     */
    @TableField(value = "graph_type_code")
    private String graphTypeCode;

    /**
     * 模板oriId
     */
    @TableField(value = "template_ori_Id")
    private String templateOriId;

    @TableField(value = "not_edit")
    private Boolean notEdit;

    /**
     * 任务参数
     */
    @TableField(value = "job_param")
    private String jobParam;

    /**
     * 任务id
     */
    @TableField(value = "job_id")
    private String jobId;

    /**
     * 任务结果0:成功，1,失败，2,执行中
     */
    @TableField(value = "job_status")
    private EnumJobStatus jobStatus;

    /**
     * 任务消息
     */
    @TableField(value = "job_message")
    private String jobMessage;
}