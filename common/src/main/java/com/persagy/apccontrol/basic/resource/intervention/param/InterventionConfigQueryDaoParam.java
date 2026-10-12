package com.persagy.apccontrol.basic.resource.intervention.param;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 干预配置查询参数
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@Accessors(chain = true)
public class InterventionConfigQueryDaoParam {

    /**
     * 干预ID
     */
    private Long id;

    /**
     * 干预ID
     */
    private Collection<Long> ids;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 运行监测二级类型
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 实例类型
     */
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例类型
     */
    private Collection<DemandInstanceTypeEnum> instanceTypes;

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 实例ID
     */
    private Collection<String> instanceIds;

    /**
     * 开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时间范围, 前闭后开
     */
    private Pair<LocalDateTime, LocalDateTime> timeRange;

    /**
     * 包含时间范围, 前闭后开
     */
    private Pair<LocalDateTime, LocalDateTime> timeContainRange;

    /**
     * 干预状态
     */
    private InterventionStatusEnum interventionStatus;

    /**
     * 获取项目ID
     *
     * @return 项目ID
     * @since 5.240702.0
     */
    public String getProjectId() {
        return StringUtils.defaultIfBlank(projectId, ContextUtil.getProjectId());
    }
}
