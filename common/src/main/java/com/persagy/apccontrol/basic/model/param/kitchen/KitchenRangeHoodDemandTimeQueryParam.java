package com.persagy.apccontrol.basic.model.param.kitchen;

import com.persagy.apccontrol.basic.model.dto.kitchen.KitchenRangeHoodDemandTimeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 厨房排油烟需求时间查询参数
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Data
@Schema(title = "厨房排油烟需求时间查询参数")
public class KitchenRangeHoodDemandTimeQueryParam {

    /**
     * 空间与其关联设备列表
     *
     * @since v7.261001.0
     */
    @NotEmpty(message = "{key_validation_spaces_not_empty}")
    @Valid
    @Schema(description = "空间与其关联设备列表")
    private List<KitchenRangeHoodDemandTimeDTO> spaces;
}
