package com.persagy.apccontrol.basic.structure.demand.model.scene.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.base.domain.Entity;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.identifier.DemandSceneIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Objects;

/**
 * 需求场景实体
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@NoArgsConstructor
public class DemandSceneEntity implements Entity<Long> {

    /**
     * 场景ID
     */
    private Long id;

    /**
     * 场景名称
     */
    private @NonNull String sceneName;

    /**
     * 开始时间
     */
    private @NonNull DemandTimeValueObject beginTime;

    /**
     * 结束时间
     */
    private @NonNull DemandTimeValueObject endTime;

    /**
     * 最终开始时间
     */
    private LocalDateTime realBeginTime;

    /**
     * 最终结束时间
     */
    private LocalDateTime realEndTime;

    /**
     * 需求模版类型
     */
    private @NonNull DemandTemplateTypeEnum demandTemplateType;

    /**
     * 需求模版标签
     */
    private @NonNull String demandTemplateTag;

    /**
     * 排序
     */
    private @NonNull Integer sequence;

    /**
     * 关联模版ID
     */
    private @NonNull Long relTemplateId;

    /**
     * 获取是否为当前场景
     *
     * @param time 时间
     * @return 是否为当前场景
     */
    @JsonIgnore
    public Boolean isCurrentScene(@NonNull LocalDateTime time) {
        Range<ChronoLocalDateTime<?>> timeRange = Range.between(this.realBeginTime,
                this.realEndTime.minus(1, LightingSceneDTO.SCENE_TIME_TYPE.convertTemporalUnit()));
        return timeRange.contains(LightingSceneDTO.SCENE_TIME_TYPE.truncate(time));
    }

    /**
     * 是否为无效场景
     *
     * @return 是否为无效场景
     * @since 7.251001.0
     */
    @JsonIgnore
    public boolean isInvalid() {
        return ObjectUtils.anyNull(this.realBeginTime, this.realEndTime);
    }

    /**
     * 创建场景实体
     *
     * @param scene 场景配置
     * @return 场景实体
     * @since 5.240902.0
     */
    public static DemandSceneEntity of(@NonNull DemandScene scene) {
        // DemandSceneIdentifier identifier = new DemandSceneIdentifier();
        // identifier.setSceneId(scene.getId());
        DemandSceneIdentifier identifier = new DemandSceneIdentifier();
        identifier.setSceneId(scene.getId());
        identifier.setTemplateType(scene.getDemandTemplateType());
        identifier.setTemplateTag(scene.getDemandTemplateTag());
        identifier.setRelTemplateId(scene.getRelTemplateId());

        // DemandSceneIdentifier identifier = new DemandSceneIdentifier();
        // identifier.setSceneId(scene.getId());
        /*
        DemandTimeIntervalValueObject timeInterval = new DemandTimeIntervalValueObject();
        timeInterval.setBeginTime(scene.getBeginTime());
        timeInterval.setEndTime(scene.getEndTime());
         */

        DemandSceneEntity result = new DemandSceneEntity();
        result.setId(scene.getId());
        result.setSceneName(scene.getSceneName());
        result.setBeginTime(scene.getBeginTime());
        result.setEndTime(scene.getEndTime());
        result.setDemandTemplateType(scene.getDemandTemplateType());
        result.setDemandTemplateTag(scene.getDemandTemplateTag());
        result.setSequence(scene.getSequence());
        result.setRelTemplateId(scene.getRelTemplateId());
        return result;
    }

    /**
     * 创建不带场景的实例
     *
     * @param template      模版VO
     * @param beginDateTime 开始时间
     * @param endDateTime   结束时间
     * @return 不带场景的实例
     * @since 7.250801.0
     */
    public static DemandSceneEntity withoutScene(
            @Nullable DemandTemplateVO template,
            @NonNull LocalDateTime beginDateTime,
            @NonNull LocalDateTime endDateTime) {
        DemandSceneEntity result = new DemandSceneEntity();
        result.setRealBeginTime(beginDateTime);
        result.setRealEndTime(endDateTime);
        if (Objects.nonNull(template)) {
            result.setDemandTemplateType(template.getTemplateType());
            result.setDemandTemplateTag(template.getTemplateTag());
            result.setRelTemplateId(template.getTemplateId());
        }
        return result;
    }
}
