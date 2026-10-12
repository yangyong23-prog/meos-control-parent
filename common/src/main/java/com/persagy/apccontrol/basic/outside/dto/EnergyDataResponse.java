package com.persagy.apccontrol.basic.outside.dto;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.DTF_SDF_SECOND_UNSIGNED;

/**
 * 能耗数据响应
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnergyDataResponse extends BaseResponse {

    /**
     * 对象主键
     */
    private String objectId;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 分精度类型
     */
    private String ipType;

    /**
     * 分精度时间 yyyyMMddHHmmss
     */
    private String ipTime;

    /**
     * 分精度值
     */
    private BigDecimal ipValue;

    /**
     * 数据状态标签(1-正常, 2-异常, 3-补录, 4-切变点)
     */
    private Integer dataStatus;


    public LocalDateTime toDataTime() {
        if (StringUtils.isBlank(ipTime)) {
            return null;
        }
        return LocalDateTime.parse(ipTime, DTF_SDF_SECOND_UNSIGNED);
    }

    public BigDecimal convertDataValue(Integer scale) {
        if (scale == null || ipValue == null) {
            return ipValue == null ? null : ipValue;
        }
        return ipValue.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public String convertDataValueStr(Integer scale) {
        BigDecimal bigDecimal = this.convertDataValue(scale);
        return bigDecimal == null ? null : bigDecimal.toPlainString();
    }

}
