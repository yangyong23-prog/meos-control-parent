package com.persagy.apccontrol.basic.model.vo.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 需求管理建筑楼层空间树
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
@Schema(description = "需求管理空间功能类型树VO")
public class DemandTreeBuildingFloorSpaceVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "本地名称")
    private String localName;

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "下级对象")
    private List<DemandTreeBuildingFloorSpaceVO> children;

    // ------------------------- 楼层字段 -------------------------

    @Schema(description = "建筑ID")
    private String buildingId;

    @Schema(description = "楼层顺序")
    private Integer floorSequenceId;

    @Schema(description = "层高")
    private BigDecimal height;

    @Schema(description = "楼板标高")
    private BigDecimal floorElevation;

    // ------------------------- 建筑字段 -------------------------

    @Schema(description = "建筑顺序")
    private Integer buildingSequenceId;

    // ------------------------- 静态方法 -------------------------

    /**
     * 转换结果
     *
     * @param buildings 建筑
     * @param floors    楼层
     * @param spaces    空间
     * @return 结果
     * @since 7.250202.0
     */
    public static List<DemandTreeBuildingFloorSpaceVO> toResult(
            @NonNull List<DtpRwdBuildingDTO> buildings,
            @Nullable List<DtpRwdFloorDTO> floors,
            @Nullable List<DtpRwdSpaceDTO> spaces) {
        if (CollectionUtils.isEmpty(buildings)) {
            return Lists.newArrayList();
        }
        DemandConverter converter = SpringUtil.getBean(DemandConverter.class);
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = DtpRwdFloorDTO.mapBuildingIdAndFloors(floors);
        MultiKeyMap<String, List<DtpRwdSpaceDTO>> buildingFloorIdAndSpace = DtpRwdSpaceDTO.mapBuildingFloorIdAndSpace(spaces);
        List<DemandTreeBuildingFloorSpaceVO> buildingVos = new ArrayList<>();
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();

            List<DemandTreeBuildingFloorSpaceVO> floorVos = new ArrayList<>();
            List<DtpRwdFloorDTO> floorsInBuilding = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isNotEmpty(floorsInBuilding)) {
                for (DtpRwdFloorDTO floor : floorsInBuilding) {
                    String floorId = floor.getId();

                    List<DemandTreeBuildingFloorSpaceVO> spaceVos = new ArrayList<>();
                    List<DtpRwdSpaceDTO> spaceInFloor = buildingFloorIdAndSpace.get(buildingId, floorId);
                    if (CollectionUtils.isNotEmpty(spaceInFloor)) {
                        for (DtpRwdSpaceDTO space : spaceInFloor) {
                            spaceVos.add(converter.toTreeBuildingFloorSpaceVo(space));
                        }
                    }
                    DemandTreeBuildingFloorSpaceVO floorVo = converter.toTreeBuildingFloorSpaceVo(floor);
                    if (CollectionUtils.isNotEmpty(spaceVos)) {
                        floorVo.setChildren(spaceVos);
                    }
                    floorVos.add(floorVo);
                }
            }
            DemandTreeBuildingFloorSpaceVO buildingVo = converter.toTreeBuildingFloorSpaceVo(building);
            if (CollectionUtils.isNotEmpty(floorVos)) {
                buildingVo.setChildren(floorVos);
            }
            buildingVos.add(buildingVo);
        }
        return buildingVos;
    }
}