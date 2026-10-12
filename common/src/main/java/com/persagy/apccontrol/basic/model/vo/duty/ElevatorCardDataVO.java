package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 给排水卡片数据
 * @author xiaofengwang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ElevatorCardDataVO extends BasicsChildCardDataVO {
    @Schema(description = "卡片名字")
    private String name;

    @Schema(description = "编码")
    private String classCode;

    @Schema(description = "电梯数据统计")
    private List<ChildCardDataVO> elevatorDataList;

    @Schema(description = "摄像头数据统计")
    private List<ChildCardDataVO> cameraDataList;
}
