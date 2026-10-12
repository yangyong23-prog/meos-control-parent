package com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingCircuitValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 照明需求配置值对象
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigLightingValueObject extends BaseDemandConfig implements ValueObject, BaseCheckParamProvider {

    /**
     * 全局配置
     */
    @Valid
    private List<@NonNull DemandConfigLightingFuncTypeValueObject> globalSettings;

    // ------------------------- 以下仅配置保存和存储使用, 最终配置会解析到全局配置中 -------------------------

    /**
     * 空间ID和反馈调节配置, 仅存在空间反馈调节配置时存在
     */
    @Valid
    private Map<String, List<DemandConfigLightingFuncTypeValueObject>> instanceIdAndFeedbackAdjustSettings;

    @Override
    public void checkAndSet() {
        this.checkSettings(this.globalSettings);
        if (MapUtils.isNotEmpty(instanceIdAndFeedbackAdjustSettings)) {
            this.instanceIdAndFeedbackAdjustSettings.values().forEach(this::checkSettings);
        }
    }

    /**
     * 检查配置
     *
     * @param settings 配置
     * @since 7.250801.0
     */
    private void checkSettings(@Nullable Collection<DemandConfigLightingFuncTypeValueObject> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return;
        }
        List<DemandConfigLightingCircuitValueObject> allCircuitSettings = new ArrayList<>();
        List<DemandLightingCircuitGroupSettingDTO> allCircuitGroupSettings = new ArrayList<>();
        Iterator<DemandConfigLightingFuncTypeValueObject> iterator = settings.iterator();
        while (iterator.hasNext()) {
            DemandConfigLightingFuncTypeValueObject setting = iterator.next();
            if (setting.getSettingMode() == DemandLightingTypeSettingModeEnum.NOT_CONFIGURED) {
                iterator.remove();
                continue;
            }
            setting.checkAndSet();
            List<DemandConfigLightingCircuitValueObject> circuitSettings = setting.getCircuitSettings();
            List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = setting.getCircuitGroupSettings();
            if (CollectionUtils.isNotEmpty(circuitSettings)) {
                allCircuitSettings.addAll(circuitSettings);
            }
            if (CollectionUtils.isNotEmpty(circuitGroupSettings)) {
                allCircuitGroupSettings.addAll(circuitGroupSettings);
            }
        }
        if (CollectionUtils.isEmpty(settings)) {
            return;
        }
        this.checkCircuitSettings(allCircuitSettings);
        this.checkGroupSettings(allCircuitGroupSettings);
    }

    /**
     * 检查照明回路配置
     *
     * @param circuitSettings 照明回路配置
     * @since 7.250801.0
     */
    private void checkCircuitSettings(@Nullable List<DemandConfigLightingCircuitValueObject> circuitSettings) {
        if (CollectionUtils.isEmpty(circuitSettings)) {
            return;
        }
        Set<String> existsCircuitIds = new HashSet<>();
        for (DemandConfigLightingCircuitValueObject circuitSetting : circuitSettings) {
            String circuitId = circuitSetting.getCircuitId();
            if (existsCircuitIds.contains(circuitId)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_8d6b4361c08a42ac8d3489b1f9f8a5db"));
            }
            existsCircuitIds.add(circuitId);
        }
    }

    /**
     * 检查回路编组配置
     *
     * @param circuitGroupSettings 回路编组配置
     * @since 7.250801.0
     */
    private void checkGroupSettings(@Nullable List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings) {
        if (CollectionUtils.isEmpty(circuitGroupSettings)) {
            return;
        }
        Set<Long> groupIds = circuitGroupSettings.stream()
                .map(DemandLightingCircuitGroupSettingDTO::getGroupId)
                .collect(Collectors.toSet());
        DemandInstanceGroupQueryDaoParam queryDaoParam = new DemandInstanceGroupQueryDaoParam();
        queryDaoParam.setIds(groupIds);
        queryDaoParam.setQueryDetail(true);
        List<DemandInstanceGroupPO> groups = SpringUtil.getBean(DemandInstanceGroupResource.class).list(queryDaoParam);
        if (CollectionUtils.isEmpty(groups)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_4c95862972704281b7aa83d3f4843b99"));
        }
        if (groups.size() != groupIds.size()) {
            throw new ValidationException(String.format(LocaleMessageUtil.getMessage("key_7698cd5aa95845b5b6073bc5c455f8c1"), CollectionUtils.subtract(groupIds,
                    groups.stream().map(DemandInstanceGroupPO::getId).collect(Collectors.toSet()))));
        }
    }
}
