package com.persagy.apccontrol.basic.model.dto.demand.blacklist;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistInFloorRuleVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 所在楼层规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "所在楼层规则")
public class DemandBlacklistInFloorRuleDTO extends BasicsDemandBlacklistRuleDTO {

    @Schema(description = "所在楼层ID")
    @NotNull(message = "{key_a0927fca963c4305b6d16293cb4084f4}")
    private List<DemandBlacklistInFloorRuleVO> inFloors;

    /**
     * 获取规则
     *
     * @return 规则列表
     */
    public Set<String> rule() {
        return inFloors.stream()
                .filter(obj -> obj != null && StringUtil.isNotBlank(obj.getFloorId()))
                .map(DemandBlacklistInFloorRuleVO::getFloorId).collect(Collectors.toSet());
    }

    @Override
    public void fill() {
        if (CollectionUtils.isEmpty(inFloors)) {
            return;
        }
        DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);
        List<DtpRwdFloorDTO> dtpRwdFloorList = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdFloorDTO.Fields.buildingId).setId(rule()));
        if (CollectionUtils.isEmpty(dtpRwdFloorList)) {
            return;
        }
        Set<String> buildingIds = dtpRwdFloorList.stream().map(DtpRwdFloorDTO::getBuildingId).collect(Collectors.toSet());
        Map<String, DtpRwdFloorDTO> floorMap = dtpRwdFloorList.stream().collect(Collectors.toMap(DtpRwdFloorDTO::getId, Function.identity()));
        Map<String, String> buildingNameMap = dtpRwdClientService.mapObjectIdAndLocalName(false, buildingIds);
        inFloors.forEach(e -> {
            String floorId = e.getFloorId();
            if (floorMap.containsKey(floorId)) {
                DtpRwdFloorDTO floor = floorMap.get(floorId);
                e.setFloorLocalName(floor.getLocalName());
                e.setBuildingId(floor.getBuildingId());
                e.setBuildingLocalName(buildingNameMap.get(floor.getBuildingId()));
            }
        });
    }

}
