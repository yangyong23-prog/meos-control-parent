package com.persagy.apccontrol.basic.model.param.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 照明-操作设定参数
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Schema(title = "照明-操作设定参数")
public class LightingOperatingParametersSetParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "空间ID, 优先级照明回路ID -> 照明控制模块ID -> 空间ID")
    private String spaceId;

    @Schema(description = "照明控制模块ID, 优先级照明回路ID -> 照明控制模块ID -> 空间ID")
    private String controllerId;

    @Schema(description = "照明回路ID, 优先级照明回路ID -> 照明控制模块ID -> 空间ID")
    private Set<String> circuitIds;

    @Schema(description = "修改信息")
    @NotEmpty(message = "{key_fd1b0f7900db4a7999e0704d4d943b5a}")
    private List<PointInfoDTO> pointInfos;

    @Override
    public void checkAndSet() {
        if (StringUtils.isBlank(spaceId) && StringUtils.isBlank(controllerId) && CollectionUtils.isEmpty(circuitIds)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_455693cdf8d94aa19174de41f410b3a6"));
        }
    }
}
