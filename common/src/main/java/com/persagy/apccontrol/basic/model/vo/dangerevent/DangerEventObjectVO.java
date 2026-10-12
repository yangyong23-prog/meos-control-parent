package com.persagy.apccontrol.basic.model.vo.dangerevent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wxf
 * @date 18:14 2025/2/12
**/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "风险事件对象")
public class DangerEventObjectVO {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "风险事件")
    private List<DangerEventVO> dangerEvents;
}
