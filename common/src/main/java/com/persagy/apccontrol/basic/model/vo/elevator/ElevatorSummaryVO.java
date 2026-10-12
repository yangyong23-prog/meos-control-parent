package com.persagy.apccontrol.basic.model.vo.elevator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 直梯、扶梯数据统计
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/17 14:55
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ElevatorSummaryVO {

    @Schema(description = "总统计数")
    private Detail summary;

    @Schema(description = "客梯数")
    private Detail visitor;

    @Schema(description = "货梯数")
    private Detail freight;


    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {

        @Schema(description = "运行数量（非锁梯状态）")
        private Integer running;

        @Schema(description = "总梯数")
        private Integer total;

        @Schema(description = "统计信息点")
        private String infoCode;

    }

}
