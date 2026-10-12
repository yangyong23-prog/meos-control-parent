package com.persagy.apccontrol.basic.model.vo;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管控空间实体类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/7 19:51
 */
@Getter
@Setter
@ToString
public class DemandControlFloorVO implements Comparable<DemandControlFloorVO> {

    @Schema(description = "子节点")
    private List<DemandControlSpaceVO> children;

    @Schema(description = "楼层序列ID")
    private Integer floorSequenceId;

    private String objType;

    private String floorId;

    private String floorLocalId;

    private String floorLocalName;

    private String buildingId;

    private String buildingLocalId;

    private String buildingLocalName;

    private BigDecimal avgData;

    @Schema(description = "当前楼层是否异常")
    private Boolean abnormalStatus;

    @Override
    public int compareTo(@NotNull DemandControlFloorVO o) {
        if (this.floorSequenceId != null && o.getFloorSequenceId() != null) {
            return this.floorSequenceId.compareTo(o.getFloorSequenceId());
        }
        return -1;
    }

    @Override
    public int hashCode() {
        if (StrUtil.isNotBlank(floorId)) {
            return floorId.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DemandControlFloorVO && StrUtil.isNotBlank(floorId)) {
            return this.floorId.equals(((DemandControlFloorVO) obj).getFloorId());
        }
        return super.equals(obj);
    }

}
