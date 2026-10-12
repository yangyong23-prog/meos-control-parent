package com.persagy.apccontrol.basic.model.param.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量查询分精度数据 查询参数
 */
@Data
public class PeriodDataBatchQueryParam {
    @Schema(description = "对象 ID 集合")
    @NotBlank(message = "{key_c299fcc9abcc4b58b25b91d0b77ea79b}")
    private List<String> objectIds;

    @Schema(description = "信息点集合")
    @NotBlank(message = "{key_43db65edd8cc4710a83bb5aa4c383bd9}")
    private List<String> infoCodes;

    @Schema(description = "数据时间类型")
    @NotBlank(message = "{key_d1b122689769410f994476dd502aecde}")
    private String enumTime;

    @Schema(description = "开始时间")
    @NotBlank(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDateTime beginDate;

    @Schema(description = "结束时间")
    @NotBlank(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDateTime endDate;


}
