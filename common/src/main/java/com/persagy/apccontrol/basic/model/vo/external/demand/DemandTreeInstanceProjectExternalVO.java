package com.persagy.apccontrol.basic.model.vo.external.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeInstanceBuildingVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeInstanceFloorVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeInstanceProjectVO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandTreeInstanceSpaceVO;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求项目实例树对外VO
 *
 * @author JiYinchuan
 * @since 5.241202.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求项目实例树对外VO")
public class DemandTreeInstanceProjectExternalVO extends DtpRwdObjectDTO {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType = DemandInstanceTypeEnum.PROJECT;

    @Schema(description = "子集")
    private List<DemandTreeInstanceBuildingExternalVO> children;

    /**
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since 5.241202.0
     */
    public static DemandTreeInstanceProjectExternalVO of(
            @NonNull DemandTreeInstanceProjectVO source) {
        DemandConverter converter = SpringUtil.getBean(DemandConverter.class);
        DemandTreeInstanceProjectExternalVO projectExternalVo = converter.toTreeInstanceProjectExternalVo(source);
        List<DemandTreeInstanceBuildingVO> buildings = source.getChildren();
        // 遍历建筑
        List<DemandTreeInstanceBuildingExternalVO> buildingVos = new ArrayList<>();
        projectExternalVo.setChildren(buildingVos);
        if (CollectionUtils.isEmpty(buildings)) {
            return projectExternalVo;
        }
        for (DemandTreeInstanceBuildingVO building : buildings) {
            DemandTreeInstanceBuildingExternalVO buildingExternalVo = converter.toTreeInstanceBuildingExternalVo(building);
            List<DemandTreeInstanceFloorVO> floors = building.getChildren();
            List<DemandTreeInstanceFloorExternalVO> floorVos = new ArrayList<>();
            buildingExternalVo.setChildren(floorVos);
            buildingVos.add(buildingExternalVo);
            if (CollectionUtils.isEmpty(floors)) {
                continue;
            }
            for (DemandTreeInstanceFloorVO floor : floors) {
                DemandTreeInstanceFloorExternalVO floorExternalVo =
                        converter.toTreeInstanceFloorExternalVo(floor);
                List<DemandTreeInstanceSpaceVO> spaces = floor.getChildren();
                List<DemandTreeInstanceSpaceExternalVO> spaceVos = new ArrayList<>();
                floorExternalVo.setChildren(spaceVos);
                floorVos.add(floorExternalVo);
                if (CollectionUtils.isEmpty(spaces)) {
                    continue;
                }
                for (DemandTreeInstanceSpaceVO space : spaces) {
                    spaceVos.add(converter.toTreeInstanceSpaceExternalVo(space));
                }
            }
        }
        return projectExternalVo;
    }
}
