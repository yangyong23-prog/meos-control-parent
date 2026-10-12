package com.persagy.apccontrol.basic.outside.dto;

import cn.hutool.core.date.DatePattern;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * @author zhangyanchang
 * @version 1.0
 * @title: QueryIPDataInPeriodDTO
 * @description: Never give up
 * @date: 2023/3/15 11:04
 */
@Data
public class QueryIPDataInPeriodDTO {

    private List<ObjectInfo> objects;
    /**
     * 查询时间点(为空时取最新)
     */
    private String ipTime;

    /**
     * 分精度数据类型，15min,1h,1d,1m
     */
    private String ipType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 数据状态标签(1-正常, 2-异常, 3-补录, 4-切变点)
     */
    private Integer dataStatus;

    /**
     * 如果分精度为日则填充今天时间
     *
     * @since 7.251201.0
     */
    public void fillTodyTimeIf1d() {
        if (!StringUtils.equals(this.ipType, DtpRwdPersistenceTimeTypeEnum.DAY.getValue())) {
            return;
        }
        if (!StringUtils.isAllBlank(this.startTime, this.endTime)) {
            return;
        }
        LocalDateTime today = DateUtils.now().toLocalDate().atStartOfDay();
        this.startTime = today.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
        this.endTime = today.plusDays(1).format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ObjectInfo {
        /**
         * 对象实例ID
         *
         * <p>分项为 [rwdId], 负载为 [id]
         */
        @NotBlank(message = "{key_849e6bb6ea174cd5b11b5bf4a5d4092e}")
        private String objectId;


        /**
         * 信息点编码列表
         */
        @Size(min = 1, message = "{key_8b4df484c4e64d24879c00c12d16cd67}")
        private List<String> infoCodes = new ArrayList<>();

    }

}
