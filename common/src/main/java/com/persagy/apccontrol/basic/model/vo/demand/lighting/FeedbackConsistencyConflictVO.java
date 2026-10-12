package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 反馈调节条件一致性校验冲突VO
 *
 * @author JiYinchuan
 * @since 7.260801.0
 */
@Data
@NoArgsConstructor
@Schema(title = "反馈调节条件一致性校验冲突VO")
public class FeedbackConsistencyConflictVO {

    @Schema(description = "冲突回路ID")
    private String circuitId;

    @Schema(description = "冲突回路名称")
    private String circuitName;

    @Schema(description = "各空间配置详情")
    private List<SpaceFeedbackConfigVO> spaceConfigs;

    /**
     * 空间反馈调节配置VO
     */
    @Data
    @NoArgsConstructor
    @Schema(title = "空间反馈调节配置VO")
    public static class SpaceFeedbackConfigVO {

        @Schema(description = "空间实例ID")
        private String instanceId;

        @Schema(description = "实例类型")
        private DemandInstanceTypeEnum instanceType;

        @Schema(description = "空间名称")
        private String instanceName;

        @Schema(description = "反馈调节条件配置")
        private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting;

        /**
         * 构造
         *
         * @param instanceId           空间实例ID
         * @param instanceType         实例类型
         * @param instanceName         空间名称
         * @param feedbackAdjustSetting 反馈调节条件配置
         * @since 7.260801.0
         */
        public SpaceFeedbackConfigVO(String instanceId, DemandInstanceTypeEnum instanceType,
                                     String instanceName,
                                     DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting) {
            this.instanceId = instanceId;
            this.instanceType = instanceType;
            this.instanceName = instanceName;
            this.feedbackAdjustSetting = feedbackAdjustSetting;
        }
    }
}
