package com.persagy.apccontrol.basic.model.dto.lighting;

import cn.hutool.core.date.DatePattern;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarDemandResult;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Range;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 照明场景DTO
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "照明场景DTO")
public class LightingSceneDTO {

    /**
     * 场景时段类型
     */
    public static final DateTimeTypeEnum SCENE_TIME_TYPE = DateTimeTypeEnum.MINUTE;

    /**
     * 场景时间间隔
     */
    public static final int SCENE_TIME_AMOUNT = 5;

    @Schema(description = "照明场景ID")
    private Long sceneId;

    @Schema(description = "照明场景名称")
    private String sceneName;

    @Schema(description = "需求模版类型")
    private DemandTemplateTypeEnum demandTemplateType;

    @Schema(description = "需求模版标签")
    private String demandTemplateTag;

    @Schema(description = "开始时间")
    private LocalDateTime beginDateTime;

    @Schema(description = "结束时间")
    private LocalDateTime endDateTime;

    @Schema(description = "是否当前场景")
    private Boolean isCurrentScene;

    @Schema(description = "关联需求模版ID")
    private Long relDemandTemplateId;

    /**
     * 创建不带场景的实例
     *
     * @param template      模版VO
     * @param beginDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 不带场景的实例
     * @since 5.240402.0
     */
    public static LightingSceneDTO instanceWithoutScene(
            @Nullable DemandTemplateVO template,
            @NonNull LocalDateTime beginDateTime,
            @NonNull LocalDateTime endDateTime) {
        LightingSceneDTO result = new LightingSidebarDemandResult();
        result.setBeginDateTime(beginDateTime);
        result.setEndDateTime(endDateTime);
        if (Objects.nonNull(template)) {
            result.setDemandTemplateType(template.getTemplateType());
            result.setDemandTemplateTag(template.getTemplateTag());
            result.setRelDemandTemplateId(template.getTemplateId());
        }
        return result;
    }

    /**
     * 获取是否当前场景
     *
     * @return 是否当前场景
     * @since 5.240402.0
     */
    public Boolean getIsCurrentScene() {
        Range<ChronoLocalDateTime<?>> timeRange = Range.between(beginDateTime,
                endDateTime.minus(1, SCENE_TIME_TYPE.convertTemporalUnit()));
        return timeRange.contains(SCENE_TIME_TYPE.truncate(DateUtils.now()));
    }
    
    /**
     * 是否是指定时间的场景
     *
     * @param tiem
     * @return
     */
    public Boolean getIsCurrentScene(LocalDateTime tiem) {
        Range<ChronoLocalDateTime<?>> timeRange = Range.between(beginDateTime,
                endDateTime.minus(1, SCENE_TIME_TYPE.convertTemporalUnit()));
        return timeRange.contains(SCENE_TIME_TYPE.truncate(tiem));
    }
    
    
    
    /**
     * 获取幂等key
     *
     * @return
     */
    public String idempotentKey() {

        if (Objects.isNull(getBeginDateTime()) || Objects.isNull(getEndDateTime())) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
        }

        return getBeginDateTime().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)) + "-"
                + getEndDateTime().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
    }


    /**
     * 判断是否是空场景 对应时段没有配置
     *
     * @return
     */
    public Boolean isNoScene() {
        return sceneId == null;
    }
}
