package com.persagy.apccontrol.basic.model.dto.demand.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingSceneTimeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Objects;

/**
 * 照明需求管理-场景时段DTO
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "照明需求管理-场景时段DTO")
public class DemandLightingSceneTimeIntervalDTO implements BaseCheckParamProvider {

    public static final int MAX_INTERVAL_SIZE = 2;

    @Schema(description = "时间类型")
    @NotNull(message = "{key_5b4ebc01f5ac4199aebe316977616160}")
    private DemandLightingSceneTimeTypeEnum timeType;

    @Schema(description = "绝对时间, 当时间类型为 [ABSOLUTE] 存在")
    private LocalTime absoluteTime;

    @Schema(description = "偏移量")
    @Range(min = -60, max = 60, message = "{key_c7f5365da0404a00af8854a2a58162f8}")
    private Integer offsetMinutes;

    /**
     * 检查长度
     *
     * @param source 数据源
     * @since v5.8.0
     */
    public static void checkSize(Collection<DemandLightingSceneTimeIntervalDTO> source) {
        if (CollectionUtils.size(source) != MAX_INTERVAL_SIZE) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_45ab6d9c72024d7f967ad4414a26f4f3"));
        }
    }

    @Override
    public void checkAndSet() {
        if (timeType == DemandLightingSceneTimeTypeEnum.ABSOLUTE && Objects.isNull(absoluteTime)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_73766d377a1441b0b7bda388b226af0d"));
        }
    }
}
