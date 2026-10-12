package com.persagy.apccontrol.common.saas.bizcheck.notify.render;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.SystemRelationshipDiagramCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.SystemRelationshipDiagramCheckPublishDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统关系图检查结果渲染器
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
public class SystemRelationshipDiagramNotifyRenderer implements NotifyTextRenderer {

    @Override
    public BizCheckStrategyTypeEnum supports() {
        return BizCheckStrategyTypeEnum.SYSTEM_RELATIONSHIP_DIAGRAM;
    }

    @Override
    public List<String> render(BizCheckResultVO vo) {
        SystemRelationshipDiagramCheckItemDTO dto = parseCheckResult(vo.getCheckResult());
        if (dto == null || dto.getPublishList() == null) {
            return Collections.emptyList();
        }

        String systems = dto.getPublishList().stream()
                .map(p -> p.getLocalName() + "（" + (p.isPublished() ? "已发布" : "未发布") + "）")
                .collect(Collectors.joining("、"));

        return Collections.singletonList("系统关系图检查-是否发布关系图：" + systems);
    }

    private SystemRelationshipDiagramCheckItemDTO parseCheckResult(JsonNode checkResult) {
        try {
            return ObjectMapperUtils.getObjectMapper().treeToValue(checkResult, SystemRelationshipDiagramCheckItemDTO.class);
        } catch (Exception e) {
            log.error("解析SystemRelationshipDiagramCheckItemDTO失败", e);
            return null;
        }
    }
}
