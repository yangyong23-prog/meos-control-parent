package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 给排水卡片数据
 * @author xiaofengwang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ElevatorChildCardDataVO extends ChildCardDataVO {
        @Schema(description = "名称")
        private String name;

        @Schema(description = "编码")
        private String classCode;

        @Schema(description = "类型")
        private String funcType;

        @Schema(description = "信息点")
        private String infoCode;
}
