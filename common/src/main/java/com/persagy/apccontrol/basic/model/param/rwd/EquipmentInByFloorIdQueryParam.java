package com.persagy.apccontrol.basic.model.param.rwd;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.Nullable;

import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 查询楼层下的设备参数
 *
 * @author LiuYang
 * @since v7.250302.0
 */
@Data
@Schema(title = "查询楼层下的设备参数")
public class EquipmentInByFloorIdQueryParam {

    @Schema(description = "楼层ID")
    @NotEmpty(message = "{key_4a7e14edef4d45f5a3c128903799b601}")
    private List<String> floorIds;

    @Schema(description = "对象类")
    @NotEmpty(message = "{key_70b2bf50925948bea9269c7ea5ca4131}")
    private List<String> classCodes;

    @Nullable
    @Schema(description = "是否过滤可监可控状态")
    private List<String> monitoringSurveillanceStatus;

    @Nullable
    @Schema(description = "查询字段, 为空时查询默认字段")
    private String[] withColumns;

    @Nullable
    @Schema(description = "附加字段")
    private String[] additionalWithColumns;

    public void checkAndSet() {
        if (CollectionUtils.isEmpty(floorIds)) {
            throw new ValidationException("{key_4a7e14edef4d45f5a3c128903799b601}");
        }
        if (CollectionUtils.isEmpty(classCodes)) {
            throw new ValidationException("{key_70b2bf50925948bea9269c7ea5ca4131}");
        }
    }

}
