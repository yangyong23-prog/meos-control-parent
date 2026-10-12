package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

/**
 * 需求模版日历查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandTemplateCalendarQueryDaoParam {

    /**
     * ID
     */
    private Long id;

    /**
     * ID
     */
    private Collection<Long> ids;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 需求类型
     */
    private DemandTypeEnum demandType;

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum demandTemplateType;

    /**
     * 日期
     */
    private LocalDate workDate;

    /**
     * 日期
     */
    private Collection<LocalDate> workDates;

    /**
     * 日期, 前闭后开
     */
    private Pair<LocalDate, LocalDate> workDateRange;

    /**
     * 是否自定义配置
     */
    private Boolean isCustomizedSetting;

    /**
     * 关联需求模版ID
     */
    private Long relDemandTemplateId;

    /**
     * 关联需求配置ID
     */
    private Collection<Long> relDemandTemplateIds;

    /**
     * 状态
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

    /**
     * 查询未来日期
     *
     * @return 当前实例
     * @since v6.0.0
     */
    public DemandTemplateCalendarQueryDaoParam queryTodayOrFutureWorkDate() {
        this.workDateRange = Pair.of(DateUtils.now().toLocalDate(), null);
        return this;
    }

    /**
     * 设置需求模版类型
     *
     * @param demandTemplateType 需求模版类型
     * @return 当前实例
     * @since v6.0.0
     */
    public DemandTemplateCalendarQueryDaoParam setDemandTemplateType(DemandTemplateTypeEnum demandTemplateType) {
        this.demandTemplateType = demandTemplateType;
        if (Objects.nonNull(demandTemplateType)) {
            this.demandType = demandTemplateType.toDemandTypeEnum();
        }
        return this;
    }
}
