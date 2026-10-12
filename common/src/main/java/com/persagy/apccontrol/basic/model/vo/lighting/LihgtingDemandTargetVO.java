package com.persagy.apccontrol.basic.model.vo.lighting;

import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 照度需求目标VO
 *
 * @author JiYinchuan
 * @since 7.260101.0
 */
@Data
public class LihgtingDemandTargetVO {

    /**
     * 需求模版ID
     */
    private Long templateId;

    /**
     * 需求模版名称
     */
    private String templateName;

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 需求模版标签
     */
    private String templateTag;

    /**
     * 场景配置
     */
    private List<Scene> scenes;

    /**
     * 干预任务配置
     */
    private List<InterventionTaskConfigVO> tasks;

    /**
     * 功能类型配置
     */
    private List<TimeFuncTypeConfig> funcTypeConfigs;


    /**
     * 创建一个空的对象
     *
     * @return 空对象
     * @since 7.260101.0
     */
    public static LihgtingDemandTargetVO empty() {
        return new LihgtingDemandTargetVO();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeFuncTypeConfig {

        /**
         * 开始时间
         */
        private LocalDateTime beginDateTime;

        /**
         * 结束时间
         */
        private LocalDateTime endDateTime;

        /**
         * 功能类型配置
         */
        private List<FuncTypeConfig> funcTypeConfigs;

        /**
         * 是否存在场景配置
         */
        private Boolean hasSceneConfig;

        /**
         * 是否存在任务配置
         */
        private Boolean hasTaskConfig;

    }

    @Data
    public static class Scene {

        /**
         * 照明场景ID
         */
        private Long id;

        /**
         * 照明场景名称
         */
        private String name;

        /**
         * 开始时间
         */
        private LocalDateTime beginDateTime;

        /**
         * 结束时间
         */
        private LocalDateTime endDateTime;

        /**
         * 功能类型配置
         */
        private List<FuncTypeConfig> funcTypeConfigs;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FuncTypeConfig {

        /**
         * 功能类型
         */
        private String funcType;

        /**
         * 功能类型配置
         */
        private List<CircuitConfig> circuitSettings;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CircuitConfig {

            /**
             * 照明回路ID
             */
            private String circuitId;

            /**
             * 配置模式
             */
            private DemandLightingCircuitSettingModeEnum settingMode;

        }
    }
}
