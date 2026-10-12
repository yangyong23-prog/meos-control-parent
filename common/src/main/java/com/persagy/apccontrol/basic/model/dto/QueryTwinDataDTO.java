package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 查询孪生dto
 *
 * @author yx
 * @date 2023/3/28
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "查询当前孪生dto")
public class QueryTwinDataDTO {

    @Schema(description = "对象信息点参数数组")
    private List<QueryTwinDataOnMomentDTO> objects;

    @Schema(description = "指定时间点")
    private String twinTime;

    @Schema(description = "数据单位")
    private String unit;

    public QueryTwinDataDTO(List<QueryTwinDataOnMomentDTO> objects) {
        this.objects = objects;
    }

}
