package com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CarbonCategoryDataResponse extends BaseResponse {

    @Nullable
    @Schema(name = "城市编码")
    private String categoryCode;

    @Nullable
    @Schema(name = "项目id")
    private List<CarBonData> dataList;

    @Data
    public static class CarBonData{
        private LocalDateTime date;
        private List<CarbonDetailDTO> dataList;
    }
}
