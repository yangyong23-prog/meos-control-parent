package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * QueryWeatherDTO
 *
 * @author yx
 * @date 2023/3/29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
public class QueryWeatherDTO{
    /**
     * 城市code
     */
    private String cityCode;
    /**
     * ：weather,只返回A类数据; air,只返回B类数据; all, 返回A和B类数据
     */
    private String dataType;
}
