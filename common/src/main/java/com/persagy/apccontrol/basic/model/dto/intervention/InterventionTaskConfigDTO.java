package com.persagy.apccontrol.basic.model.dto.intervention;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.intervention.InterventionExecuteTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.validate.utility.pattern.DatePattern;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "干预任务配置-配置DTO")
public class InterventionTaskConfigDTO extends InterventionTaskConfigBaseDTO {

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "配置对象")
    @NotNull(message = "{key_1d780ac83f774f41876d96220d328745}")
    @Valid
    private InterventionTaskInstanceDTO configObject;

    @Schema(description = "具体的干预配置")
    @NotNull(message = "{key_08089f5a72d54d469cd75eb38fc5d957}")
    private JsonNode configData;

    @Schema(description = "干预原因")
    private String reason;

    /**
     * 配置时间段
     * <hr/>
     * <p>照明配置说明</p>
     * <ul>
     *     <li><b>仅生效一次</b> - 开始时间和结束时间为执行时间</li>
     *     <li><b>重复多次</b> - 开始时间为重复日开始时间，结束时间为重复日结束时间</li>
     *     <li><b>立即执行</b> - 开始时间和结束时间为执行时间</li>
     *     <li><b>手动执行</b> - 开始时间为当前时间，结束时间为 9999-12-31 23:59:59</li>
     * </ul>
     */
    @Schema(description = "配置时间段")
    @NotNull(message = "{key_9e05ed972a3e4e72943fc10eefd93fd4}")
    private List<InterventionTaskTimeDTO> timeSlot;

    @Data
    @FieldNameConstants
    public static class Lighting {

        @Schema(description = "执行类型")
        private InterventionExecuteTypeEnum type;

        @Schema(description = "是否跨天")
        private Boolean acrossDay;

        @Schema(description = "具体配置")
        private Config config;

        /**
         * 所属实例，仅人工手动执行时传入
         */
        @Schema(description = "所属实例")
        private DemandInstanceValueObject belong;

        @Schema(description = "执行时间范围")
        private LinkedList<Range<ChronoLocalDateTime<?>>> executeTimeRanges;

        @JsonSetter(Fields.executeTimeRanges)
        public void deserializeExecuteTimeRanges(LinkedList<String> executeTimeRanges) {
            this.executeTimeRanges = RangeUtils.instanceByArray(executeTimeRanges, item ->
                    LocalDateTime.parse(item, DatePattern.NORM_DATETIME_FORMATTER), LinkedList::new);
        }

        @JsonGetter(Fields.executeTimeRanges)
        public LinkedList<String> serializeExecuteTimeRanges() {
            return RangeUtils.toString(this.executeTimeRanges, item ->
                    item.format(DatePattern.NORM_DATETIME_FORMATTER), LinkedList::new);
        }

        @Data
        public static class Config {

            @Schema(description = "照明回路 ID")
            private Set<String> circuitIds;

            @Schema(description = "执行模式")
            private DemandLightingCircuitSettingModeEnum settingMode;

        }
    }
}
