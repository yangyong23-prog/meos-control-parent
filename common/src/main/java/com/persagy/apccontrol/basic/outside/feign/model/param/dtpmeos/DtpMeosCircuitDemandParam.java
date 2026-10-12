package com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 需求计算-手动触发照明需求计算参数
 *
 * @author JiYinchuan
 * @since 7.260101.0
 */
@Data
@NoArgsConstructor
public class DtpMeosCircuitDemandParam {

    /**
     * 需求模板类型
     */
    private @NonNull DemandTemplateTypeEnum templateType;

    /**
     * 计算时间，yyyyMMddHHmmss
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private @NonNull LocalDateTime time;

    /**
     * 是否强制下发回路设定
     */
    private @NonNull Boolean forcibleExecution;

    /**
     * 回路ID和设定map映射
     *
     * <p>为空时控制服务主动向需求管理获取临时控件自配置</p>
     * <p>不为空时按照入参进行直接临时控制</p>
     */
    private @Nullable Map<String, DemandLightingCircuitSettingModeEnum> circuitSettingModeMap;

}
