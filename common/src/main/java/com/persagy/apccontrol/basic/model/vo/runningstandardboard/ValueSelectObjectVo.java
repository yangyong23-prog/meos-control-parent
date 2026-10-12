package com.persagy.apccontrol.basic.model.vo.runningstandardboard;

import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 16:38
 */
@Data
@Schema(description = "运行标准看板-价值选择配置")
public class ValueSelectObjectVo {

    @Schema(name = "价值选择类型")
    private String valueSelectType;

    @Schema(name = "价值选择区间配置")
    private List<ValueSelectConfigDto> valueSelectRange = new ArrayList<>();

    @Schema(name = "曲线图数据")
    private List<ValueSelectionTrendTableDto.ElecData> elecDataList = new ArrayList<>();

}
