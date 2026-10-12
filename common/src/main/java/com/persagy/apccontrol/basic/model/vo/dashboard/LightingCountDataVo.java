package com.persagy.apccontrol.basic.model.vo.dashboard;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandStatusDTO;
import com.persagy.apccontrol.basic.model.enumtype.DemandSourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeControlDeviationStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeRunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingTreeObjectVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author : wangchenghong
 */
@Data
@Schema(description = "Dashboard照明-统计数据")
public class LightingCountDataVo {

    @Schema(description = "需求类模版型")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "需求一致性")
    private String demandConsistency;

    @Schema(description = "空间总数")
    private Integer totalSpaceCount;

    @Schema(description = "建筑列表")
    private List<BuildingCountVo> buildings;


    /**
     * 结果统计
     *
     * @param demandLightingTreeObjectVO  建筑楼层空间树
     * @param spaceId2circuitIdsMap       空间回路id对应关系
     * @param mapCircuitIdAndDemandStatus 回路需求状态
     * @param circuitIdAndInfoCodeValues  照明回路信息点
     * @author yuecaipu
     * @since V5_241201
     */
    public void resultCalculate(
            DemandLightingTreeObjectVO demandLightingTreeObjectVO,
            Map<String, Set<String>> spaceId2circuitIdsMap,
            Map<String, DemandStatusDTO> mapCircuitIdAndDemandStatus,
            Map<String, Map<String, Object>> circuitIdAndInfoCodeValues) {
        // 空间总数(项目)
        int totalSpaceCountWithProject = 0;
        // 干预空间总数(项目)
        int totalInterventionSpaceCountWithProject = 0;
        this.setBuildings(new ArrayList<>(demandLightingTreeObjectVO.getChildren().size()));
        for (DemandLightingTreeObjectVO build : demandLightingTreeObjectVO.getChildren()) {
            BuildingCountVo buildingCountVo = new BuildingCountVo(build.getId(), build.getLocalName(), new ArrayList<>());
            this.getBuildings().add(buildingCountVo);
            for (DemandLightingTreeObjectVO floor : build.getChildren()) {
                FloorCountVo floorCountVo = new FloorCountVo();
                buildingCountVo.getFloors().add(floorCountVo);
                floorCountVo.setId(floor.getId());
                floorCountVo.setLocalName(floor.getLocalName());
                Map<DtpRwdClassCodeEnum, Set<String>> classCodeAndObjectIdsVo = new HashMap<>(2);
                classCodeAndObjectIdsVo.put(DtpRwdClassCodeEnum.SPACE, new HashSet<>());
                classCodeAndObjectIdsVo.put(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT, new HashSet<>());
                floorCountVo.setClassCodeAndObjectIds(classCodeAndObjectIdsVo);
                // 多开总数
                Set<String> totalMoreOpenCircuitIds = new HashSet<>();
                // 少开总数
                Set<String> totalMoreLessCircuitIds = new HashSet<>();
                // 实际开启总数
                Set<String> totalRealOpenCircuitIds = new HashSet<>();
                // 需求开启总数
                Set<String> totalDemandOpenCircuitIds = new HashSet<>();
                // 空间总数
                int totalSpaceCount = floor.getChildren().size();
                // 干预空间总数
                int totalInterventionSpaceCount = 0;
                // 需求干预空间总数
                int totalDemandInterventionSpaceCount = 0;
                // 手动照明回路服务空间总数
                int totalManualSpaceCount = 0;
                // 需求偏离空间数量
                int totalDemandDeviatedSpaceCount = 0;
                Map<String, DemandDeviatedSpacesVo> spaceId2DeviatedSpace = new HashMap<>(floor.getChildren().size());
                for (DemandLightingTreeObjectVO space : floor.getChildren()) {
                    String spaceId = space.getId();
                    Set<String> circuitIds = spaceId2circuitIdsMap.get(spaceId);
                    if (CollectionUtils.isEmpty(circuitIds)) {
                        continue;
                    }
                    classCodeAndObjectIdsVo.get(DtpRwdClassCodeEnum.SPACE).add(spaceId);
                    classCodeAndObjectIdsVo.get(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT).addAll(circuitIds);
                    // 空间是否需求偏离
                    boolean isDemandDeviated = false;
                    // 空间是否受人工干预
                    boolean isIntervened = false;
                    // 空间是否有手动控制回路
                    boolean isManual = false;
                    for (String circuitId : spaceId2circuitIdsMap.get(spaceId)) {
                        Map<String, Object> infoCodeAndValue = circuitIdAndInfoCodeValues.get(circuitId);
                        // 运行状态
                        DtpKnowledgeRunStatusEnum runStatusEnum = DtpKnowledgeRunStatusEnum.analyze(infoCodeAndValue);
                        // 控制偏差状态
                        DtpKnowledgeControlDeviationStatusEnum controlDeviationStatus =
                                DtpKnowledgeControlDeviationStatusEnum.analyze(infoCodeAndValue);
                        // 多开少开回路数量统计，空间需求偏离统计
                        if (Objects.nonNull(controlDeviationStatus)) {
                            switch (controlDeviationStatus) {
                                case OVER_OPENING:
                                case UNDER_OPENING:
                                    isDemandDeviated = true;
                                    DemandDeviatedSpacesVo deviatedSpaces = spaceId2DeviatedSpace.get(spaceId);
                                    if (null == deviatedSpaces) {
                                        deviatedSpaces = new DemandDeviatedSpacesVo(spaceId, space.getLocalName(), 0, 0, 0, 0, 0, 0);
                                        spaceId2DeviatedSpace.put(spaceId, deviatedSpaces);
                                    }
                                    if (controlDeviationStatus == DtpKnowledgeControlDeviationStatusEnum.OVER_OPENING) {
                                        totalMoreOpenCircuitIds.add(circuitId);
                                        deviatedSpaces.setTotalMoreOpenCount(deviatedSpaces.getTotalMoreOpenCount() + 1);
                                    } else if (controlDeviationStatus == DtpKnowledgeControlDeviationStatusEnum.UNDER_OPENING) {
                                        totalMoreLessCircuitIds.add(circuitId);
                                        deviatedSpaces.setTotalLessOpenCount(deviatedSpaces.getTotalLessOpenCount() + 1);
                                    }
                                default:
                            }
                        }
                        DemandStatusDTO demandStatusDTO = mapCircuitIdAndDemandStatus.get(circuitId);
                        if (null != demandStatusDTO) {
                            DemandSourceEnum demandSource = demandStatusDTO.getDemandSource();
                            DemandDeviatedSpacesVo deviatedSpaces = spaceId2DeviatedSpace.get(spaceId);
                            if (DemandSourceEnum.DEMAND_INTERVENTION.equals(demandSource)) {
                                isIntervened = true;
                                if (null != deviatedSpaces) {
                                    deviatedSpaces.setTotalDemandInterventionCount(deviatedSpaces.getTotalDemandInterventionCount() + 1);
                                }
                            } else if (DemandSourceEnum.MANUAL_CONTROL.equals(demandSource)) {
                                isManual = true;
                                if (null != deviatedSpaces) {
                                    deviatedSpaces.setTotalManualCount(deviatedSpaces.getTotalManualCount() + 1);
                                }
                            }
                            if (DemandLightingCircuitSettingModeEnum.OPEN.equals(demandStatusDTO.getSettingMode())) {
                                totalDemandOpenCircuitIds.add(circuitId);
                                if (null != deviatedSpaces) {
                                    deviatedSpaces.setTotalDemandOpenCount(deviatedSpaces.getTotalDemandOpenCount() + 1);
                                }
                            }
                        }
                        if (runStatusEnum == DtpKnowledgeRunStatusEnum.RUNNING) {
                            totalRealOpenCircuitIds.add(circuitId);
                            DemandDeviatedSpacesVo deviatedSpaces = spaceId2DeviatedSpace.get(spaceId);
                            if (null != deviatedSpaces) {
                                deviatedSpaces.setTotalRealOpenCount(deviatedSpaces.getTotalRealOpenCount() + 1);
                            }
                        }
                    }
                    if (isDemandDeviated) {
                        totalDemandDeviatedSpaceCount++;
                    }
                    if (isIntervened) {
                        totalDemandInterventionSpaceCount++;
                    }
                    if (isManual) {
                        totalManualSpaceCount++;
                    }
                    if (isManual || isIntervened) {
                        totalInterventionSpaceCount++;
                    }
                }
                floorCountVo.setTotalDemandOpenCount(totalDemandOpenCircuitIds.size());
                floorCountVo.setTotalRealOpenCount(totalRealOpenCircuitIds.size());
                floorCountVo.setTotalInterventionSpaceCount(totalInterventionSpaceCount);
                floorCountVo.setTotalDemandInterventionSpaceCount(totalDemandInterventionSpaceCount);
                floorCountVo.setTotalManualSpaceCount(totalManualSpaceCount);
                floorCountVo.setTotalMoreLessCount(totalMoreLessCircuitIds.size());
                floorCountVo.setTotalMoreOpenCount(totalMoreOpenCircuitIds.size());
                floorCountVo.setTotalDemandDeviatedSpaceCount(totalDemandDeviatedSpaceCount);
                floorCountVo.setDemandConsistency(demandConsistency(totalSpaceCount, totalDemandDeviatedSpaceCount));
                floorCountVo.setDemandDeviatedSpaces(new ArrayList<>(spaceId2DeviatedSpace.values()));

                totalSpaceCountWithProject += totalSpaceCount;
                totalInterventionSpaceCountWithProject += totalDemandDeviatedSpaceCount;
            }
        }

        this.setTotalSpaceCount(totalSpaceCountWithProject);
        this.setDemandConsistency(demandConsistency(totalSpaceCountWithProject, totalInterventionSpaceCountWithProject));
    }

    /**
     * 需求一致性计算
     *
     * @param totalSpaceCount               空间总数
     * @param totalDemandDeviatedSpaceCount 需求偏离空间总数
     * @return {@link String}
     * @author yuecaipu
     * @since V5_241201
     */
    private static String demandConsistency(double totalSpaceCount, double totalDemandDeviatedSpaceCount) {
        if (0 == totalSpaceCount) {
            return "--";
        } else {
            // 保留两位小数
            BigDecimal demandConsistency = new BigDecimal(1 - totalDemandDeviatedSpaceCount / totalSpaceCount).setScale(2, RoundingMode.HALF_DOWN);
            return String.valueOf(demandConsistency);
        }
    }
}
