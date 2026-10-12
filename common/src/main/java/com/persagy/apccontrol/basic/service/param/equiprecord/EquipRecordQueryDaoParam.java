package com.persagy.apccontrol.basic.service.param.equiprecord;

import com.persagy.apccontrol.basic.model.entity.ChangeContent;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 设备记录查询条件
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
@Accessors(chain = true)
public class EquipRecordQueryDaoParam {

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 系统ID
     */
    private String systemId;

    /**
     * 系统ID
     */
    private Collection<String> systemIds;

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 系统编码
     */
    private Collection<String> systemCodes;

    /**
     * 对象ID
     */
    private String objId;

    /**
     * 对象ID
     */
    private Set<String> objIds;

    /**
     * 执行状态
     */
    private Integer execStatus;

    /**
     * 执行状态
     */
    private List<Integer> execStatuses;

    /**
     * 变更内容
     */
    private ChangeContent changeContent;

    /**
     * 开始执行时间
     */
    private LocalDateTime beginExecuteTime;

    /**
     * 结束执行时间
     */
    private LocalDateTime endExecuteTime;


    /**
     * 开始执行时间
     */
    private LocalDateTime beginModifiedTime;

    /**
     * 结束执行时间
     */
    private LocalDateTime endModifiedTime;


    /**
     * 是否倒叙  0否  1 是  默认1
     */
    private Integer isDesc;

}
