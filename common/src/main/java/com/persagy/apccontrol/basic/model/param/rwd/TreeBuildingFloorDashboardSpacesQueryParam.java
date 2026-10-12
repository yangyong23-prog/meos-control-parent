package com.persagy.apccontrol.basic.model.param.rwd;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * 建筑楼层空间 DashboardSpaces
 *
 */
@Data
@Schema(title = "建筑楼层空间查询参数")
public class TreeBuildingFloorDashboardSpacesQueryParam {
    
    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;
    
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;
    
    @Schema(description = "过滤建筑ID, 为空时不过滤")
    private Set<String> filterBuildingIds;

    @Schema(description = "过滤楼层ID, 为空时不过滤")
    private Set<String> filterFloorIds;

    @Schema(description = "过滤空间ID, 为空时不过滤")
    private Set<String> filerSpaceIds;
    
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
