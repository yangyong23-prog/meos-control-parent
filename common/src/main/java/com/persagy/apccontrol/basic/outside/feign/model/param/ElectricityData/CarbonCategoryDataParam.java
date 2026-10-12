package com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CarbonCategoryDataParam {

    @Nullable
    @Schema(name = "城市编码")
    private String cityCode;

    @Nullable
    @Schema(name = "项目id")
    private String projectId;

    @Nullable
    @Schema(name = "集团编码")
    private String groupCode;

    @NotNull(message = "{key_242157c0c95c45b2a7682f0944857bd2}")
    private String categoryCode;

    /**
     * 开始时间, 格式yyyyMMddHHmmss
     */
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    @JsonFormat(pattern = DatePattern.PURE_DATETIME_PATTERN)
    private LocalDateTime beginTime;

    /**
     * 结束时间, 格式yyyyMMddHHmmss
     */
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    @JsonFormat(pattern = DatePattern.PURE_DATETIME_PATTERN)
    private LocalDateTime endTime;

    /**
     * AppId
     */
    private String appId = "meos";
}
