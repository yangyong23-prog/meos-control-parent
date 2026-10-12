package com.persagy.apccontrol.basic.model.vo.duty;


import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Objects;

/**
 * 安防卡片数据
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/4/18 10:28
 * @since 7.250402.0
 */
@Data
public class SecurityChildCardDataVO {

    @Schema(description = "卡片名字")
    private String name;
    @Schema(description = "卡片编码")
    private SecurityEquipmentTypeEnum nameCode;
    @Schema(description = "设备总数")
    private Integer equipmentTotal;
    @Schema(description = "运行总数")
    private Integer runStatusTotal;
    @Schema(description = "在线总数")
    private Integer onlineStatusTotal;
    @Schema(description = "布防数量")
    private Integer armingStatusTotal;

    public void setEquipmentTotal(Integer equipmentTotal) {
        if (Objects.isNull(this.equipmentTotal)) {
            this.equipmentTotal = 0;
        }
        this.equipmentTotal += equipmentTotal;
    }

    public void setRunStatusTotal(Integer runStatusTotal) {
        if (Objects.isNull(this.runStatusTotal)) {
            this.runStatusTotal = 0;
        }
        this.runStatusTotal += runStatusTotal;
    }

    public void setOnlineStatusTotal(Integer onlineStatusTotal) {
        if (Objects.isNull(this.onlineStatusTotal)) {
            this.onlineStatusTotal = 0;
        }
        this.onlineStatusTotal += onlineStatusTotal;
    }

    public void setArmingStatusTotal(Integer armingStatusTotal) {
        if (Objects.isNull(this.armingStatusTotal)) {
            this.armingStatusTotal = 0;
        }
        this.armingStatusTotal += armingStatusTotal;
    }

}
