package com.persagy.apccontrol.basic.model.param.dashboard;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.validate.EnumValues;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 15:46
 */
@Data
@Accessors(chain = true)
@Schema(title = "Dashboard照明-查询统计参数")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardQueryCountParam {

    @NotNull(message = "{key_48ca0286ed224fb0b441e06559148e08}")
    @EnumValues(acceptedValues = {"PUBLIC_AREA_LIGHTING", "PARKING_LOT_LIGHTING"})
    private DemandTemplateTypeEnum templateType;

}
