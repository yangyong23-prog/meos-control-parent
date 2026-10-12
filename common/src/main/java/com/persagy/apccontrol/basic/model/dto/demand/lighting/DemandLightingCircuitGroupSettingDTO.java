package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-回路分组配置DTO
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
@Schema(title = "需求管理-照明需求-回路分组配置DTO")
public class DemandLightingCircuitGroupSettingDTO implements BaseCheckParamProvider {

    /**
     * 编组ID
     */
    @NotNull(message = "{key_13d8a6853d1a468c9c0ad1d609fa96eb}")
    private Long groupId;

    /**
     * 类型配置模式
     *
     * <ul>
     *     <li><b>ALL_OPEN</b> - 全开</li>
     *     <li><b>ALL_CLOSE</b> - 全关</li>
     * </ul>
     */
    @NotNull(message = "{key_ca6220780644484bb984662750bce860}")
    private DemandLightingTypeSettingModeEnum settingMode;

    /**
     * 转换为编组ID和类型配置模式
     *
     * @param settings 类型配置
     * @return 编组ID-配置模式
     * @since 7.250602.0
     */
    public static Map<Long, DemandLightingTypeSettingModeEnum> toGroupIdAndSettingMode(
            @Nullable List<DemandLightingCircuitGroupSettingDTO> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return Collections.emptyMap();
        }
        return settings.stream().collect(Collectors.toMap(DemandLightingCircuitGroupSettingDTO::getGroupId,
                DemandLightingCircuitGroupSettingDTO::getSettingMode,
                (o1, o2) -> o1));
    }
}
