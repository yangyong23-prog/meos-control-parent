package com.persagy.apccontrol.common.saas.bizcheck.notify.render;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.DemandConfigCheckItemDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 空调运行标准检查结果渲染器（HVAC）
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
public class HvacOperationStandardNotifyRenderer implements NotifyTextRenderer {

    @Override
    public BizCheckStrategyTypeEnum supports() {
        return BizCheckStrategyTypeEnum.HVAC_OPERATION_STANDARD;
    }

    @Override
    public List<String> render(BizCheckResultVO vo) {
        List<String> lines = new ArrayList<>();
        DemandConfigCheckItemDTO dto = parseCheckResult(vo.getCheckResult());
        if (dto == null) {
            return lines;
        }

        // 运行模板异常
        if (dto.getTemplateStatus() == BizCheckStatusEnum.EXCEPTION) {
            lines.add("空调运行标准检查-运行模板：未配置");
        }

        // 温度门限值异常
        if (dto.getTemperatureThresholdStatus() == BizCheckStatusEnum.EXCEPTION) {
            lines.add("空调运行标准检查-温度门限值：未配置");
        }

        return lines;
    }

    private DemandConfigCheckItemDTO parseCheckResult(JsonNode checkResult) {
        try {
            return ObjectMapperUtils.getObjectMapper().treeToValue(checkResult, DemandConfigCheckItemDTO.class);
        } catch (Exception e) {
            log.error("解析DemandConfigCheckItemDTO失败", e);
            return null;
        }
    }
}
