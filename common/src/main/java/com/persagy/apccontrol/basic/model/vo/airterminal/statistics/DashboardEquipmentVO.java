package com.persagy.apccontrol.basic.model.vo.airterminal.statistics;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import lombok.Data;

import java.util.List;

/**
 * 空调末端Dashboard设备统计VO
 *
 * @author JiYinchuan
 * @since 7.251101.0
 */
@Data
public class DashboardEquipmentVO {

    /**
     * 建筑
     */
    private DtpRwdBuildingDTO building;

    /**
     * 楼层列表
     */
    private List<Floor> floors;

    @Data
    public static class Floor {

        /**
         * 楼层
         */
        private DtpRwdFloorDTO floor;

        /**
         * 楼层统计信息
         */
        private List<Statistic> statistics;

    }

    @Data
    public static class Statistic {

        /**
         * 模板名称
         */
        private String templateName;

        /**
         * 模板序号
         */
        private Integer templateSequence;

        /**
         * 设备总数
         */
        private Integer totalCount;

        /**
         * 设备开启数量
         */
        private Integer openCount;

    }
}
