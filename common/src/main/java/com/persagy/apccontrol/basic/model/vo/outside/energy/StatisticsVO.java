package com.persagy.apccontrol.basic.model.vo.outside.energy;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "统计信息")
public class StatisticsVO {


    @Schema(description = "末端-设备组统计")
    private List<TerminalEquipGroupStatistics> terminalEquipGroupStatistics;

    @Schema(description = "末端-信息点统计")
    private TerminalInfoCodeStatistics terminalInfoCodeStatistics;


    @Schema(description = "照明-空间下照明设备统计")
    private List<LightingEquipInSpaceStatistics> lightingEquipInSpaceStatistics;


    @Schema(description = "照明-设备信息点统计")
    private List<LightingInfoCodeStatistics> lightingInfoCodeStatistics;

    @Data
    public static class TerminalInfoCodeStatistics {
        /**
         * 异常事件
         */
        @Schema(description = "异常事件")
        private long abnormalEventNum;
        /**
         * 控反不一致
         */
        @Schema(description = "离线设备")
        private long offEqNum;
        /**
         * 离线设备
         */
        @Schema(description = "控反不一致")
        private long oppositeNum;
    }

    @Data
    public static class TerminalEquipGroupStatistics {
        /**
         * 设备类型，新风机组，组合空调
         */
        @Schema(title = "设备类型，新风机组，组合空调111")
        private String classCode;

        @Schema(description = "设备类型名称")
        private String className;
        /**
         * 可监测总数，（暂时没有）
         */
        @Schema(description = "可监测总数，（暂时没有）")
        private long monitorTotal;
        /**
         * 运行总数
         */
        @Schema(description = "运行总数")
        private long runTotal;
        /**
         * 总数
         */
        @Schema(description = "总数")
        private long total;
    }

    @Data
    public static class LightingInfoCodeStatistics {
        /**
         * 异常事件
         */
        @Schema(description = "异常事件")
        private long abnormalEventNum;
        /**
         * 离线
         */
        @Schema(description = "离线")
        private long offEqNum;
        /**
         * 控反不一致
         */
        @Schema(description = "控反不一致")
        private long oppositeNum;
        /**
         * 空间类型
         * 公共照明 PUBLIC_AREA_LIGHTING
         * 停车场照明 PARKING_LOT_LIGHTING
         * 楼体照明 BUILDING_LIGHTING
         * 景观照明 LANDSCAPE_LIGHTING
         * 标识照明 SIGN_LIGHTING
         */
        @Schema(description = "空间类型，公共照明")
        private DemandTemplateTypeEnum spaceType;

        @Schema(description = "空间类型名称")
        private String spaceTypeName;
    }

    @Data
    public static class LightingEquipInSpaceStatistics {
        /**
         * 少开数量
         */
        @Schema(description = "少开数量")
        private long openLessNum;
        /**
         * 多开数量
         */
        @Schema(description = "多开数量")
        private long openMoreNum;
        /**
         * 开启数量
         */
        @Schema(description = "开启数量")
        private long openNum;
        /**
         * 总数
         */
        @Schema(description = "总数")
        private long total;
        /**
         * 运行模式
         */
        @Schema(description = "模版名称")
        private String templateName;

        @Schema(description = "模版标签")
        private String templateTag;
        /**
         * 空间类型
         * 公共照明 PUBLIC_AREA_LIGHTING
         * 停车场照明 PARKING_LOT_LIGHTING
         * 楼体照明 BUILDING_LIGHTING
         * 景观照明 LANDSCAPE_LIGHTING
         * 标识照明 SIGN_LIGHTING
         */
        @Schema(description = "空间类型")
        private DemandTemplateTypeEnum spaceType;
        @Schema(description = "空间类型名称")
        private String spaceTypeName;
    }
}
