package com.persagy.apccontrol.basic.model.vo.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 需求项目实例树VO
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求项目实例树VO")
public class DemandTreeInstanceProjectVO extends DtpRwdObjectDTO {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType = DemandInstanceTypeEnum.PROJECT;

    @Schema(description = "子集")
    private List<DemandTreeInstanceBuildingVO> children;

    /**
     * 创建空实例
     *
     * @param project 项目对象
     * @return 空实例
     * @since 5.241102.0
     */
    public static DemandTreeInstanceProjectVO ofEmpty(@NonNull ProjectDTO project) {
        DemandTreeInstanceProjectVO result = new DemandTreeInstanceProjectVO();
        result.setId(project.getProjectId());
        result.setLocalName(project.getProjectLocalName());
        result.setChildren(new ArrayList<>());
        return result;
    }

    /**
     * 创建建筑楼层空间树
     *
     * @param project   项目
     * @param buildings 建筑
     * @param floors    楼层
     * @param spaces    空间
     * @return 建筑楼层空间树
     * @since 5.241102.0
     */
    public static DemandTreeInstanceProjectVO of(
            @NonNull ProjectDTO project,
            @NonNull List<DtpRwdBuildingDTO> buildings,
            @NonNull List<DtpRwdFloorDTO> floors,
            @Nullable List<DtpRwdSpaceDTO> spaces) {
        DemandConverter converter = SpringUtil.getBean(DemandConverter.class);
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = floors.stream().collect(
                Collectors.groupingBy(DtpRwdFloorDTO::getBuildingId));
        List<DemandTreeInstanceBuildingVO> buildingVos = new ArrayList<>();
        for (DtpRwdBuildingDTO building : buildings) {
            DemandTreeInstanceBuildingVO buildingVo = converter.toTreeInstanceBuildingVo(building);

            List<DtpRwdFloorDTO> floorsInBuilding = buildingIdAndFloors.get(building.getId());
            if (CollectionUtils.isNotEmpty(floorsInBuilding)) {
                Map<String, List<DtpRwdSpaceDTO>> floorIdAndSpaces = Collections.emptyMap();
                if (CollectionUtils.isNotEmpty(spaces)) {
                    floorIdAndSpaces = spaces.stream().collect(Collectors.groupingBy(DtpRwdSpaceDTO::getFloorId));
                }
                List<DemandTreeInstanceFloorVO> floorVos = new ArrayList<>();
                for (DtpRwdFloorDTO floor : floorsInBuilding) {
                    DemandTreeInstanceFloorVO floorVo = converter.toTreeInstanceFloorVo(floor);

                    List<DtpRwdSpaceDTO> spacesInFloor = floorIdAndSpaces.get(floor.getId());
                    if (CollectionUtils.isNotEmpty(spacesInFloor)) {
                        floorVo.setChildren(spacesInFloor.stream()
                                .map(converter::toTreeInstanceSpaceVo)
                                .collect(Collectors.toList()));
                    }
                    floorVos.add(floorVo);
                }
                if (CollectionUtils.isNotEmpty(floorVos)) {
                    buildingVo.setChildren(floorVos);
                    buildingVos.add(buildingVo);
                }
            }
        }
        DemandTreeInstanceProjectVO result = new DemandTreeInstanceProjectVO();
        result.setId(project.getProjectId());
        result.setLocalName(project.getProjectLocalName());
        result.setChildren(buildingVos);
        return result;
    }
}
