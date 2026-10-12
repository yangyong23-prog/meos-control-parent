package com.persagy.apccontrol.basic.model.context.airterminal;

import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalEquipmentTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalEquipmentStatusStatisticsQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalEquipmentTypeStatisticsVO;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 空调末端-设备状态统计上下文
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Accessors(chain = true)
public class AirTerminalEquipmentStatusStatisticsContext {

    // ------------------------- 参数 -------------------------

    /**
     * 查询参数
     */
    private AirTerminalEquipmentStatusStatisticsQueryParam param;

    // ------------------------- 过程 -------------------------

    // -------------------- 楼层 --------------------

    /**
     * 楼层列表
     */
    private List<DtpRwdFloorDTO> floors = Collections.emptyList();

    /**
     * 楼层ID -> 楼层对象
     */
    private Map<String, DtpRwdFloorDTO> floorIdAndData = Collections.emptyMap();

    /**
     * 楼层ID -> 关联设备
     */
    private Map<String, Set<DtpRwdObjectDTO>> floorIdAndEquipments = Collections.emptyMap();

    // -------------------- 设备 --------------------

    /**
     * 设备列表
     */
    private List<DtpRwdEquipmentDTO> equipments = Collections.emptyList();

    /**
     * 设备ID -> 设备标签
     */
    private Map<String, AirTerminalEquipmentTagDTO> equipmentIdAndTag = Collections.emptyMap();

    // -------------------- 建筑 --------------------

    /**
     * 楼层列表
     */
    private List<DtpRwdBuildingDTO> buildings = Collections.emptyList();

    /**
     * 建筑ID -> 关联楼层
     */
    private Map<String, TreeSet<DtpRwdFloorDTO>> buildingIdAndFloors = Collections.emptyMap();

    // ------------------------- 结果 -------------------------

    /**
     * 是否处理失败
     */
    private boolean isFailed = false;

    /**
     * 计算设备统计
     *
     * @param equipmentGroupStatistics 设备分组统计, 为空时实时统计
     * @return 设备统计
     * @since 5.240502.0
     */
    public AirTerminalEquipmentTypeStatisticsVO calculateEquipmentStatistics(
            @NonNull String floorId,
            @Nullable Map<String, AirTerminalEquipmentTypeStatisticsVO> equipmentGroupStatistics) {
        equipmentGroupStatistics = Optional.ofNullable(equipmentGroupStatistics)
                .orElseGet(() -> calculateEquipmentGroupStatistics(floorId));

        AirTerminalEquipmentTypeStatisticsVO result = new AirTerminalEquipmentTypeStatisticsVO().initCount();
        if (MapUtils.isEmpty(equipmentGroupStatistics)) {
            return result;
        }
        for (AirTerminalEquipmentTypeStatisticsVO statistics : equipmentGroupStatistics.values()) {
            result.setTotalCount(result.getTotalCount() + statistics.getTotalCount());
            result.setTotalOpenCount(result.getTotalOpenCount() + statistics.getTotalOpenCount());
            result.setTotalCloseCount(result.getTotalCloseCount() + statistics.getTotalCloseCount());
            result.setTotalUnknownCount(result.getTotalUnknownCount() + statistics.getTotalUnknownCount());
        }
        return result;
    }

    /**
     * 计算设备分组统计
     *
     * @param floorId 楼层ID
     * @return 设备类编码 -> 设备统计
     * @since 5.240502.0
     */
    public Map<String, AirTerminalEquipmentTypeStatisticsVO> calculateEquipmentGroupStatistics(@NonNull String floorId) {
        if (MapUtils.isEmpty(floorIdAndEquipments)) {
            return Maps.newHashMap();
        }
        Set<DtpRwdObjectDTO> relEquipments = floorIdAndEquipments.get(floorId);
        if (CollectionUtils.isEmpty(relEquipments)) {
            return Maps.newHashMap();
        }
        Map<String, AirTerminalEquipmentTypeStatisticsVO> result = new HashMap<>(16);
        for (DtpRwdObjectDTO reEquipment : relEquipments) {
            String equipmentId = reEquipment.getId();
            String classCode = reEquipment.getClassCode();

            AirTerminalEquipmentTypeStatisticsVO statistics = result.get(classCode);
            if (Objects.isNull(statistics)) {
                statistics = new AirTerminalEquipmentTypeStatisticsVO().initCount();
                statistics.setClassCode(classCode);
                statistics.setClassCodeName(BaseEnum.analyze(classCode, DtpRwdClassCodeEnum.class).getReasonPhrase());
                result.put(classCode, statistics);
            }
            statistics.setTotalCount(statistics.getTotalCount() + 1);

            AirTerminalEquipmentTagDTO equipmentTag = equipmentIdAndTag.get(equipmentId);
            if (Objects.isNull(equipmentTag) || Objects.isNull(equipmentTag.getRunStatus())) {
                statistics.setTotalUnknownCount(statistics.getTotalUnknownCount() + 1);
                continue;
            }
            RunStatusEnum runStatus = equipmentTag.getRunStatus();
            switch (runStatus) {
                case OPEN:
                    statistics.setTotalOpenCount(statistics.getTotalOpenCount() + 1);
                    break;
                case CLOSE:
                    statistics.setTotalCloseCount(statistics.getTotalCloseCount() + 1);
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(runStatus.getClass().getSimpleName(), runStatus);
            }
        }
        return result;
    }
}
