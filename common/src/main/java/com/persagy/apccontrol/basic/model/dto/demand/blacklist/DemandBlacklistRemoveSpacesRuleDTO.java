package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistSpacesRuleVO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
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
 * 移出空间规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "移出空间规则")
public class DemandBlacklistRemoveSpacesRuleDTO extends BasicsDemandBlacklistRuleDTO {

    @Schema(description = "移出空间ID")
    @NotNull(message = "{key_f6865b4f360043b4b6803e2e4b423701}")
    private List<DemandBlacklistSpacesRuleVO> spaces;

    /**
     * 获取规则
     *
     * @return 规则列表
     */
    public Set<String> rule() {
        return spaces.stream()
                .filter(space -> space != null && StringUtil.isNotBlank(space.getSpaceId()))
                .map(DemandBlacklistSpacesRuleVO::getSpaceId).collect(Collectors.toSet());
    }

    @Override
    public void fill() {
        if (CollectionUtils.isEmpty(spaces)) {
            return;
        }
        DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
        Map<String, String> spaceMap = dtpRwdClientService.mapObjectIdAndLocalName(true, rule());
        if (MapUtils.isNotEmpty(spaceMap)) {
            spaces.forEach(space -> space.setSpaceName(spaceMap.get(space.getSpaceId())));
        }
    }
}
