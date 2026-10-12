package com.persagy.apccontrol.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Description: 算法计算操作记录
 * @author: yancheng
 * @date: 2023/3/18
 * @version: V1.0
 */
@Data
@Schema(title = "算法计算操作记录")
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmCalculationVO {

    @Schema(description = "操作人")
    private String createUser;

    @Schema(description = "操作时间")
    private Date createTime;

    @Schema(description = "状态")
    private String status;
}
