package com.persagy.apccontrol.basic.model.vo.kitchen;

import com.persagy.apccontrol.basic.model.dto.kitchen.KitchenRangeHoodDemandTimeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 厨房排油烟需求时间VO
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Data
@Schema(title = "厨房排油烟需求时间VO")
public class KitchenRangeHoodDemandTimeVO {

    /**
     * 空间与其关联设备需求时间列表, 与入参结构一致、顺序一致
     *
     * @since v7.261001.0
     */
    @Schema(description = "空间与其关联设备需求时间列表, 与入参结构一致、顺序一致")
    private List<KitchenRangeHoodDemandTimeDTO> spaces;
}
