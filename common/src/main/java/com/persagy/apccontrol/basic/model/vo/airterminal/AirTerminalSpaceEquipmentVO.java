package com.persagy.apccontrol.basic.model.vo.airterminal;

import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalEquipmentTagDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * 空调末端空间设备接口响应。
 *
 * <p>该响应只包含对象基础字段和当前接口需要的末端业务字段。模型视图字段由前端通过独立
 * 模型图接口获取，空间温度和二氧化碳规则标记也不在本接口返回。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Schema(title = "空调末端-空间设备VO")
public class AirTerminalSpaceEquipmentVO {

    /**
     * 空间列表。
     *
     * @since v7.260701.0
     */
    @Schema(description = "空间列表")
    private List<SpaceVO> spaces;

    /**
     * 设备列表。
     *
     * @since v7.260701.0
     */
    @Schema(description = "设备列表")
    private List<EquipmentVO> equipments;

    /**
     * 空调末端空间基础信息和业务状态。
     *
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Schema(title = "空调末端-空间设备接口空间VO")
    public static class SpaceVO extends DtpRwdObjectVO {

        /**
         * 空间功能类型。
         *
         * @since v7.260701.0
         */
        @Schema(description = "空间功能类型")
        private String roomFuncType;

        /**
         * 所在建筑 ID。
         *
         * @since v7.260701.0
         */
        @Schema(description = "建筑ID")
        private String buildingId;

        /**
         * 所在楼层 ID。
         *
         * @since v7.260701.0
         */
        @Schema(description = "楼层ID")
        private String floorId;

        /**
         * 是否无法监测。
         *
         * @since v7.260701.0
         */
        @Schema(description = "是否无法监测")
        private Boolean unableMonitor;

        /**
         * 是否受控制。
         *
         * @since v7.260701.0
         */
        @Schema(description = "是否受控制")
        private Boolean controlled;

        /**
         * 受控关联设备 ID。
         *
         * @since v7.260701.0
         */
        @Schema(description = "受控制关联设备ID")
        private Set<String> controlledRelEquipmentIds;

        /**
         * 空间标签。
         *
         * @since v7.260701.0
         */
        @Schema(description = "空间标签")
        private AirTerminalSpaceTagDTO tag;

    }

    /**
     * 空调末端设备基础信息和业务状态。
     *
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Schema(title = "空调末端-空间设备接口设备VO")
    public static class EquipmentVO extends DtpRwdObjectVO {

        /**
         * 所在楼层 ID。
         *
         * @since v7.260701.0
         */
        @Schema(description = "楼层ID")
        private String floorId;

        /**
         * 所在建筑 ID。
         *
         * @since v7.260701.0
         */
        @Schema(description = "建筑ID")
        private String buildingId;

        /**
         * 是否受控制。
         *
         * @since v7.260701.0
         */
        @Schema(description = "是否受控制")
        private Boolean controlled;

        /**
         * 设备标签。
         *
         * @since v7.260701.0
         */
        @Schema(description = "设备标签")
        private AirTerminalEquipmentTagDTO tag;

        /**
         * 监测监控状态。
         *
         * @since v7.260701.0
         */
        @Schema(description = "可监可控")
        private String monitoringSurveillanceStatus;

        /**
         * 空调机组控制组关联的空调机组 ID。
         *
         * @since v7.260701.0
         */
        @Schema(description = "空调机组控制组关联的空调机组ID")
        private Set<String> controlledAirHandlingUnitIds;
    }
}
