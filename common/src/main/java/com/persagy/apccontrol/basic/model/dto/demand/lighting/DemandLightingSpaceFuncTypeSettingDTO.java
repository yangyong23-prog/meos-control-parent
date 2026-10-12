package com.persagy.apccontrol.basic.model.dto.demand.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-空间类型配置-功能类型配置DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求管理-照明需求-空间类型配置-功能类型配置DTO")
public class DemandLightingSpaceFuncTypeSettingDTO implements BaseCheckParamProvider {

    @Schema(description = "当前配置ID值")
    private String configId;

    @Schema(description = "空间功能类型")
    @NotEmpty(message = "{key_da2f29612b404d2d9503d93e5ecbadeb}")
    private List<BaseEnumVO> spaceFuncTypes;

    @Schema(description = "照明回路功能类型配置")
    @Valid
    private List<DemandLightingFuncTypeSettingDTO> funcTypeSettings;

    @Override
    public void checkAndSet() {
        if (CollectionUtils.isNotEmpty(funcTypeSettings)) {
            funcTypeSettings.forEach(DemandLightingFuncTypeSettingDTO::checkAndSet);
            if (checkRepeat(funcTypeSettings.stream()
                    .map(DemandLightingFuncTypeSettingDTO::getFuncType)
                    .collect(Collectors.toList()))) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_f740a7e44db74d06b19b06c642705ea1"));
            }
        }
    }
}
