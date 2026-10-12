package com.persagy.apccontrol.common.saas.bizcheck.notify.render;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.UnitPriceConfigCheckItemDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 单价配置检查结果渲染器
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
public class UnitPriceNotifyRenderer implements NotifyTextRenderer {

    @Override
    public BizCheckStrategyTypeEnum supports() {
        return BizCheckStrategyTypeEnum.UNIT_PRICE_CONFIG;
    }

    @Override
    public List<String> render(BizCheckResultVO vo) {
        UnitPriceConfigCheckItemDTO dto = parseCheckResult(vo.getCheckResult());
        if (dto == null) {
            return Collections.emptyList();
        }

        String description = "数据缺失";
        if (dto.getResultDescription() != null && !dto.getResultDescription().isEmpty()) {
            description = dto.getResultDescription();
        }

        return Collections.singletonList("单价配置检查-当日电价数据：" + description);
    }

    private UnitPriceConfigCheckItemDTO parseCheckResult(JsonNode checkResult) {
        try {
            return ObjectMapperUtils.getObjectMapper().treeToValue(checkResult, UnitPriceConfigCheckItemDTO.class);
        } catch (Exception e) {
            log.error("解析UnitPriceConfigCheckItemDTO失败", e);
            return null;
        }
    }
}
