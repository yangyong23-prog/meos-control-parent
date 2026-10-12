package com.persagy.apccontrol.basic.model.vo.infocode;

import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 趋势图通用
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/23 9:56
 * @since v5.240802.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoCodeFigureVO {

    private LocalDateTime time;

    private Object value;

    /**
     * 将map中的数据重组到list中
     *
     * @param ipValueGroupingByTimeMap iot实时数据
     * @param fiveMinutesWithTimeRange 五分钟精度数据
     * @param resultList               响应结果集
     */
    public static void mapToResultList(Map<String, String> ipValueGroupingByTimeMap,
                                 LinkedList<LocalDateTime> fiveMinutesWithTimeRange,
                                 List<InfoCodeFigureVO> resultList) {
        fiveMinutesWithTimeRange.forEach(x -> {
            String format = x.format(DateUtils.DTF_SDF_SECOND_UNSIGNED);
            String value = ipValueGroupingByTimeMap.getOrDefault(format, null);
            resultList.add(new InfoCodeFigureVO(x, value));
        });
    }

}
