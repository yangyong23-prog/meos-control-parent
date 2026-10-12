package com.persagy.apccontrol.basic.outside.feign.model.response.dtppersist.aggregate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 冷机开启台数统计响应
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpPersisWatchChillPumpStatisticsResponse extends BaseResponse {

    /**
     * 大机(泵)数量
     */
    @JsonAlias("Num")
    private Integer num;

    /**
     * 中机(泵)数量
     */
    @JsonAlias("M1Num")
    private Integer m1Num;

    /**
     * 小机(泵)数量
     */
    @JsonAlias("SNum")
    private Integer sNum;

}
