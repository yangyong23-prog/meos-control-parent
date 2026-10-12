package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 冷源策略实现
 *
 * @author LiuYang
 * @since 5.241101.0
 */
@Component(RunningMonitorColdSourceStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorColdSourceStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorColdSourceStrategy";

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.COLD_SOURCE;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.FIELD;
    }


    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumnWithSimple(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        //过滤温度实例化的空间
        return filterInstantiateRuleToIntersection(spaces, Collections.singletonList(InfoCodeEnum.SPACE_TDB.getCode()));

    }

    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Collections.emptyList();
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "运行检测-冷源策略";
    }
}
