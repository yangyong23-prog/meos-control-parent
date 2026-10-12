package com.persagy.apccontrol.basic.model.dto.demand.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求配置-照明需求-配置数据
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求配置-照明需求-配置数据")
public class DemandConfigLightingDataDTO extends BaseDemandConfig
        implements BaseCheckParamProvider, InterventionConfigData {

    @Schema(description = "全局配置")
    @Valid
    private List<DemandLightingFuncTypeSettingDTO> globalSettings;

    @Schema(description = "空间类型配置")
    @Valid
    private List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings;

    /**
     * 新建当前实例
     *
     * @return 当前实例
     * @since 24Q1 T3S1-5.17.0
     */
    public static DemandConfigLightingDataDTO newInstance() {
        return new DemandConfigLightingDataDTO()
                .setGlobalSettings(Lists.newArrayList())
                .setSpaceSettings(Lists.newArrayList());
    }

    @Override
    public void checkAndSet() {
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            globalSettings.forEach(DemandLightingFuncTypeSettingDTO::checkAndSet);
            if (checkRepeat(globalSettings.stream()
                    .map(DemandLightingFuncTypeSettingDTO::getFuncType)
                    .collect(Collectors.toList()))) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_f740a7e44db74d06b19b06c642705ea1"));
            }
        }
        if (CollectionUtils.isNotEmpty(spaceSettings)) {
            spaceSettings.forEach(DemandLightingSpaceFuncTypeSettingDTO::checkAndSet);
            if (checkRepeat(spaceSettings.stream()
                    .flatMap(item -> item.getSpaceFuncTypes().stream())
                    .map(BaseEnumVO::getKey).collect(Collectors.toList()))) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_98e61cc4b6ff45ae88d525fed73fa6bc"));
            }
        }
    }
}
