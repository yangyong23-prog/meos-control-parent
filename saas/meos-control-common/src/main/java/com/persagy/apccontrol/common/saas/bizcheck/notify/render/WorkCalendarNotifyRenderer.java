package com.persagy.apccontrol.common.saas.bizcheck.notify.render;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.WorkCalendarCheckDTO;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 工作日历检查结果渲染器
 *
 * @since 7.260601.0
 */
@Component
@Slf4j
public class WorkCalendarNotifyRenderer implements NotifyTextRenderer {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public BizCheckStrategyTypeEnum supports() {
        return BizCheckStrategyTypeEnum.WORK_CALENDAR;
    }

    @Override
    public List<String> render(BizCheckResultVO vo) {
        List<String> lines = new ArrayList<>();
        WorkCalendarCheckDTO dto = parseCheckResult(vo.getCheckResult());
        if (dto == null) {
            return lines;
        }

        // 1. 供冷系统服务空间营业时间
        if (dto.getSystemBusinessStatus() == BizCheckStatusEnum.EXCEPTION) {
            lines.add(renderSystemBusiness(dto));
        }

        // 2. 所属空调季节
        if (dto.getAirSeasonStatus() == BizCheckStatusEnum.EXCEPTION) {
            lines.add(renderAirSeason(dto));
        }

        // 3. 空调工况
        if (dto.getAcSeasonStatus() == BizCheckStatusEnum.EXCEPTION) {
            lines.add(renderAcSeason(dto));
        }

        return lines;
    }

    private String renderSystemBusiness(WorkCalendarCheckDTO dto) {
        List<String> parts = new ArrayList<>();
        if (dto.getDate2SystemBusiness() != null) {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            // 按当日、次日顺序遍历
            for (LocalDate date : new LocalDate[]{today, tomorrow}) {
                String dayLabel = date.equals(today) ? "当日" : "次日";
                List<WorkCalendarCheckDTO.SystemBusinessCheckDTO> systems = dto.getDate2SystemBusiness().get(date);
                if (systems == null || systems.isEmpty()) {
                    continue;
                }
                systems.stream()
                        .filter(s -> s.getObject() != null && s.getObject().getLocalName() != null)
                        .sorted(Comparator.comparing(s -> s.getObject().getLocalName(),
                                Comparator.nullsLast(Collator.getInstance(Locale.CHINA))))
                        .forEach(s -> {
                            String name = s.getObject().getLocalName();
                            String hours = renderBusinessHours(s);
                            parts.add(dayLabel + "-" + name );
                        });
            }
        }
        return "工作日历检查-供冷系统服务空间营业时间：" + String.join("、", parts);
    }

    private String renderBusinessHours(WorkCalendarCheckDTO.SystemBusinessCheckDTO system) {
        if (system.getSpaceBusinessHours() == null || system.getSpaceBusinessHours().isEmpty()) {
            return "未配置";
        }
        return system.getSpaceBusinessHours().stream()
                .map(range -> range.getMinimum().format(TIME_FORMATTER) + "-" + range.getMaximum().format(TIME_FORMATTER))
                .collect(Collectors.joining("、"));
    }

    private String renderAirSeason(WorkCalendarCheckDTO dto) {
        String today = renderAirSeason(dto.getDate2AirSeasonType(), LocalDate.now());
        String tomorrow = renderAirSeason(dto.getDate2AirSeasonType(), LocalDate.now().plusDays(1));
        return "工作日历检查-所属空调季节：当日-" + today + "；次日-" + tomorrow;
    }

    private String renderAcSeason(WorkCalendarCheckDTO dto) {
        String today = renderAcSeason(dto.getDate2AcSeasonType(), LocalDate.now());
        String tomorrow = renderAcSeason(dto.getDate2AcSeasonType(), LocalDate.now().plusDays(1));
        return "工作日历检查-空调工况：当日-" + today + "；次日-" + tomorrow;
    }

    private String renderAcSeason(java.util.Map<LocalDate, MidCalendarAcSeasonTypeEnum> dateMap, LocalDate date) {
        if (dateMap == null) {
            return "未配置";
        }
        MidCalendarAcSeasonTypeEnum value = dateMap.get(date);
        return value != null ? value.getReasonPhrase() : "未配置";
    }

    private String renderAirSeason(java.util.Map<LocalDate, MidCalendarAirSeasonTypeEnum> dateMap, LocalDate date) {
        if (dateMap == null) {
            return "未配置";
        }
        MidCalendarAirSeasonTypeEnum value = dateMap.get(date);
        return value != null ? value.getReasonPhrase() : "未配置";
    }

    private WorkCalendarCheckDTO parseCheckResult(JsonNode checkResult) {
        try {
            return ObjectMapperUtils.getObjectMapper().treeToValue(checkResult, WorkCalendarCheckDTO.class);
        } catch (Exception e) {
            log.error("解析WorkCalendarCheckDTO失败", e);
            return null;
        }
    }
}
