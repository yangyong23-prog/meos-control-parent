package com.persagy.apccontrol.basic.model.vo.passengerflow;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 客流首页统计
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/21 17:44
 * @since v5.240802.0
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PassengerFlowHomeStatistics {

    /**
     * 对象id
     */
    private String objectId;

    /**
     * 对象名称
     */
    private String objectName;

    /**
     * 入
     */
    private Long inPeopleFlow;

    /**
     * 出
     */
    private Long outPeopleFlow;

    /**
     * 客流人数（累计进入人次-累计离开人次，任一侧无数据为null）
     */
    private Long netPeopleFlow;

    /**
     * 图
     */
    private List<TendencyFigure> graph;

    public static PassengerFlowHomeStatistics iotToStatistics(String objectId, String objectName,
                                                              List<InfoCodeValueVO> energyDataResponses,
                                                              Map<String, Map<LocalDateTime, Object>> infoCodeHoverMap) {
        PassengerFlowHomeStatistics homeStatistics = new PassengerFlowHomeStatistics();
        homeStatistics.setObjectId(objectId);
        homeStatistics.setObjectName(objectName);
        // 总值，不排除计算会出问题
        if (CollectionUtils.isNotEmpty(energyDataResponses)) {
            energyDataResponses.forEach(x -> {
                if (x.getInfoCode().equals(InfoCodeEnum.ACCM_IN_PEOPLE_FLOW.value())) {
                    homeStatistics.setInPeopleFlow(bigDecimalToLong(x.getInfoCodeValue()));
                } else {
                    homeStatistics.setOutPeopleFlow(bigDecimalToLong(x.getInfoCodeValue()));
                }
            });
        }
        // 客流人数：两侧均有值才计算差值，任一侧无数据则不计算（null，前端展示为--）
        if (Objects.nonNull(homeStatistics.getInPeopleFlow()) && Objects.nonNull(homeStatistics.getOutPeopleFlow())) {
            homeStatistics.setNetPeopleFlow(homeStatistics.getInPeopleFlow() - homeStatistics.getOutPeopleFlow());
        }
        // 用于存储合并后的对象
        LinkedHashMap<LocalDateTime, TendencyFigure> timeMap = new LinkedHashMap<>(32);
        if (MapUtils.isNotEmpty(infoCodeHoverMap)) {
            // 补齐24小时
            LocalDateTime startOfDay = LocalDateTime.of(DateUtils.now().toLocalDate(), LocalDateTime.MIN.toLocalTime());
            for (int i = 0; i < 24; i++) {
                timeMap.put(startOfDay, new TendencyFigure(null, null, null, startOfDay));
                startOfDay = startOfDay.plusHours(1);
            }
            infoCodeHoverMap.forEach((infoCode, timeValueMap) -> {
                timeValueMap.forEach((time, value) -> {
                    long infoCodeValue = 0L;
                    try {
                        double numberOfPeople = Double.parseDouble(String.valueOf(value));
                        infoCodeValue = Math.round(numberOfPeople);
                    } catch (Exception e) {
                        log.error("参数转换异常", e);
                    }
                    // 检查是否已经存在这个时间
                    if (timeMap.containsKey(time)) {
                        TendencyFigure existingObject = timeMap.get(time);
                        // 添加对应的值
                        if (InfoCodeEnum.ACCD_IN_PEOPLE_FLOW.value().equals(infoCode)) {
                            existingObject.setSpInPeopleFlow(infoCodeValue);
                        } else {
                            existingObject.setSpOutPeopleFlow(infoCodeValue);
                        }
                    }
                });
            });
            // 客流人数：每小时点两侧均有值才计算差值，任一侧无数据则不计算（null，前端断点展示为--）
            timeMap.values().forEach(tendencyFigure -> {
                if (Objects.nonNull(tendencyFigure.getSpInPeopleFlow()) && Objects.nonNull(tendencyFigure.getSpOutPeopleFlow())) {
                    tendencyFigure.setSpNetPeopleFlow(
                            tendencyFigure.getSpInPeopleFlow() - tendencyFigure.getSpOutPeopleFlow());
                }
            });
            homeStatistics.setGraph(new ArrayList<>(timeMap.values()));
        }
        return homeStatistics;
    }

    private static Long bigDecimalToLong(Object data) {
        try {
            double numberOfPeople = Double.parseDouble(String.valueOf(data));
            return Math.round(numberOfPeople);
        } catch (Exception e) {
            log.error("参数转换异常", e);
        }
        return 0L;
    }


}
