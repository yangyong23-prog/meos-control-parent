package com.persagy.apccontrol.basic.model.param.rwd;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.*;

/**
 * 建筑楼层空间功能类型空间树查询参数
 *
 * @author JiYinchuan
 * @since 7.250101.0
 */
@Data
@Schema(title = "建筑楼层空间功能类型空间树查询参数")
public class TreeBuildingFloorRoomFuncTypeSpaceQueryParam {

    @Schema(description = "过滤建筑ID, 为空时不过滤")
    private Set<String> filterBuildingIds;

    @Schema(description = "过滤楼层ID, 为空时不过滤")
    private Set<String> filterFloorIds;

    @Schema(description = "过滤空间功能类型, 为空时不过滤")
    private Set<String> filterRoomFuncTypes;

    @Schema(description = "过滤空间ID, 为空时不过滤")
    private Set<String> filerSpaceIds;

    @Schema(description = "是否查询空间 默认true")
    private boolean querySpace = true;

    @Schema(description = "实例类型 -> 查询字段, 为空时查询所有")
    private Map<InstanceTypeEnum, Set<String>> instanceTypeAndWithColumns;

    /**
     * 填充模版实例类型查询字段
     *
     * @since 7.250101.0
     */
    public void fillDefaultInstanceTypeAndWithColumns() {
        instanceTypeAndWithColumns = Objects.isNull(instanceTypeAndWithColumns)
                ? new HashMap<>() : instanceTypeAndWithColumns;
        Set<String> spaceWithColumns = Optional.ofNullable(instanceTypeAndWithColumns
                .get(InstanceTypeEnum.SPACE)).orElseGet(HashSet::new);
        Set<String> floorWithColumns = Optional.ofNullable(instanceTypeAndWithColumns
                .get(InstanceTypeEnum.FLOOR)).orElseGet(HashSet::new);
        Set<String> buildingWithColumns = Optional.ofNullable(instanceTypeAndWithColumns
                .get(InstanceTypeEnum.BUILDING)).orElseGet(HashSet::new);

        spaceWithColumns.add(DtpRwdObjectDTO.Fields.id);
        spaceWithColumns.add(DtpRwdObjectDTO.Fields.localName);
        spaceWithColumns.add(DtpRwdSpaceDTO.Fields.roomFuncType);
        spaceWithColumns.add(DtpRwdSpaceDTO.Fields.floorId);
        spaceWithColumns.add(DtpRwdSpaceDTO.Fields.buildingId);

        floorWithColumns.add(DtpRwdObjectDTO.Fields.id);
        floorWithColumns.add(DtpRwdObjectDTO.Fields.localName);
        floorWithColumns.add(DtpRwdFloorDTO.Fields.floorSequenceId);
        floorWithColumns.add(DtpRwdFloorDTO.Fields.buildingId);

        buildingWithColumns.add(DtpRwdObjectDTO.Fields.id);
        buildingWithColumns.add(DtpRwdObjectDTO.Fields.localName);
        buildingWithColumns.add(DtpRwdBuildingDTO.Fields.buildingSequenceId);

        instanceTypeAndWithColumns.put(InstanceTypeEnum.SPACE, spaceWithColumns);
        instanceTypeAndWithColumns.put(InstanceTypeEnum.FLOOR, floorWithColumns);
        instanceTypeAndWithColumns.put(InstanceTypeEnum.BUILDING, buildingWithColumns);
    }
}
