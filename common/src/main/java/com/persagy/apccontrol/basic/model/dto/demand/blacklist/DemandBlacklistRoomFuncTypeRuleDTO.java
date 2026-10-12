package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistRoomFuncTypeRuleVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 空间功能类型规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "空间功能类型规则")
public class DemandBlacklistRoomFuncTypeRuleDTO extends BasicsDemandBlacklistRuleDTO {

    @Schema(description = "空间功能类型")
    @NotNull(message = "{key_da2f29612b404d2d9503d93e5ecbadeb}")
    private List<DemandBlacklistRoomFuncTypeRuleVO> roomFuncTypes;

    /**
     * 获取规则
     *
     * @return 规则列表
     */
    public Set<String> rule() {
        return roomFuncTypes.stream()
                .filter(obj -> obj != null && StringUtil.isNotBlank(obj.getRoomFuncType()))
                .map(DemandBlacklistRoomFuncTypeRuleVO::getRoomFuncType).collect(Collectors.toSet());
    }

    @Override
    public void fill() {
        if (CollectionUtils.isEmpty(roomFuncTypes)) {
            return;
        }
        DtpKnowledgeClientService dtpKnowledgeClientService = SpringUtil.getBean(DtpKnowledgeClientService.class);
        DtpKnowledgeInfoCodeDTO infoCode = dtpKnowledgeClientService.getInfoCode(false, DtpRwdClassCodeEnum.SPACE.value(), InfoCodeEnum.ROOM_FUNC_TYPE.getCode());
        if (infoCode == null) {
            return;
        }
        Map<String, String> dataSourceCodeAndName = infoCode.toDataSourceCodeAndName();
        if (MapUtils.isEmpty(dataSourceCodeAndName)) {
            return;
        }
        roomFuncTypes.forEach(obj -> obj.setRoomFuncTypeName(dataSourceCodeAndName.get(obj.getRoomFuncType())));
    }
}
