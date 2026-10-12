package com.persagy.apccontrol.basic.resource.demand.param.airconditioning;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

/**
 * 需求配置-历史空调需求查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigAirconditioningHistoryQueryDaoParam implements BaseCheckParamProvider {

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
     * 需求模版标签
     */
    private String demandTemplateTag;

    /**
     * 需求实例类型
     */
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例类型
     */
    private Collection<DemandInstanceTypeEnum> demandInstanceTypes;

    /**
     * 需求实例ID
     */
    private String demandInstanceId;

    /**
     * 需求实例ID
     */
    private Collection<String> demandInstanceIds;

    /**
     * 上级需求实例ID
     */
    private String parentDemandInstanceId;

    /**
     * 配置日期
     */
    private LocalDate configDate;

    /**
     * 配置日期
     */
    private Collection<LocalDate> configDates;

    /**
     * 配置日期, 前闭后开
     */
    private Pair<LocalDate, LocalDate> configDateRange;

    /**
     * 是否自定义配置
     */
    private Boolean isCustomizedSetting;

    /**
     * 是否为自定义管控空间
     */
    private Boolean isCustomControlSpace;

    /**
     * 是否被屏蔽
     */
    private Boolean isShielded;

    /**
     * 系统ID
     */
    private Set<String> systemIds;

    /**
     * 状态
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

    @Override
    public void checkAndSet() {
        if (ObjectUtils.allNull(configDate, configDates, configDateRange)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_7c4dab4dd1dd43faac2f70d2442bff8b"));
        }
    }
}
