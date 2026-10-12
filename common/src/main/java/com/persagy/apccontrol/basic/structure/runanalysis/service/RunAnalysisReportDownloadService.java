package com.persagy.apccontrol.basic.structure.runanalysis.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigByInfoCodeParam;
import com.persagy.apccontrol.basic.convert.RunAnalysisReportConvert;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.runanalysis.download.*;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportVO;
import com.persagy.apccontrol.basic.outside.dto.lighting.DifyDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DifyServerService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.outside.feign.service.impl.DtpRwdClientServiceImpl;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.airterminal.AnalysisAirConditionerSpaceService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerSize;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerTypeSub;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.WeatherDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun.CoolingSysRunDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun.SystemEnergyDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview.RunOverviewDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview.SystemRunningResultDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.EnvQualityConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.RunAnalysisReportRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl.RunAnalysisReportCoolingSysRun;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl.RunAnalysisReportEnvQuality;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl.RunAnalysisReportRunOverview;
import com.persagy.apccontrol.basic.structure.runanalysis.util.RunAnalysisReportCsvExportUtil;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import groovy.lang.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum.ALG_AIR_COOLING_TEMP_MAX_MIN;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportDownloadService implements BizComponent<RunAnalysisReportDownloadService> {

    private final MidPermissionClientService midPermissionClientService;
    private final RunAnalysisReportRepository runAnalysisReportRepository;
    private final MidCalendarClientService midCalendarClientService;
    private final RunAnalysisReportConvert runAnalysisReportConvert;
    private final ShellConfigService shellConfigService;
    private final AbstractIotDataCacheServer<?> abstractIotDataCacheServer;
    private final AnalysisAirConditionerSpaceService analysisAirConditionerSpacesService;
    private final RunAnalysisReportRunOverview runAnalysisReportRunOverview;
    private final RunAnalysisReportEnvQuality runAnalysisReportEnvQuality;
    private final RunAnalysisReportCoolingSysRun runAnalysisReportCoolingSysRun;
    private final DtpRwdClientServiceImpl dtpRwdClientService;
    private final DifyServerService dififyServerService;


    /**
     * 生成CSV并发送到Dify
     *
     * @param date       报告构建日期
     * @param reportType 运行分析报告类型
     */
    public RunAnalysisReportDownloadDTO generateDownloadData(LocalDate date, RunAnalysisReportTypeEnum reportType) throws Exception {
        logProjectInstance().info("运行分析-日报开始生成CSV", Pair.of("date", date));
        // 查询当天所有已完成的报告
        RunAnalysisReport queryReport = new RunAnalysisReport();
        queryReport.setReportDate(date);
        queryReport.setReportType(reportType);
        queryReport.setReportStatus(RunAnalysisReportStatusEnum.CONFIRM);
        Map<RunAnalysisReportChapterEnum, Long> chapterToIdMap = runAnalysisReportRepository.getListByDayAndType(queryReport)
                .stream().collect(Collectors.toMap(RunAnalysisReport::getReportChapter, RunAnalysisReport::getId, (k1, k2) -> k1));
        switch (reportType) {
            case DAILY:
                // 生成CSV下载相关数据
                RunAnalysisReportDownloadDTO runAnalysisReportDownloadDTO = generateCsvFromDailyReports(date, chapterToIdMap);
                logProjectInstance().info("运行分析-日报-CSV生成成功，：" + JSONObject.toJSONString(runAnalysisReportDownloadDTO));
                return runAnalysisReportDownloadDTO;
            case MONTHLY:
                break;
        }

        return new RunAnalysisReportDownloadDTO();
    }

    /**
     * 从报告数据生成CSV
     *
     * @param date           报告日期
     * @param chapterToIdMap 报告列表
     * @return CSV内容
     */
    private RunAnalysisReportDownloadDTO generateCsvFromDailyReports(LocalDate date, Map<RunAnalysisReportChapterEnum, Long> chapterToIdMap) throws Exception {
        RunAnalysisReportDownloadDTO runAnalysisReportDownloadDTO = new RunAnalysisReportDownloadDTO();

        // 1. 获取项目基础信息
        runAnalysisReportDownloadDTO.setProjectBasicInfo(generateProjectBasicInfo());

        // 2. 日汇总数据 （依赖概述数据）
        runAnalysisReportDownloadDTO.setDailySummaryData(generateDailySummary(date));

        // 3. 冷站日汇总数据
        runAnalysisReportDownloadDTO.setCoolingStationDailySummaryList(generateCoolingStationDailySummary(chapterToIdMap.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_RUN_OVERVIEW)));

        // 4. 冷站参考相似日能耗数据
        runAnalysisReportDownloadDTO.setCoolingStationSimilarDayList(generateSimilarDay(date));

        // 5. 逐15min冷站数据
        runAnalysisReportDownloadDTO.setCoolingStation15MinDataList(generateCoolingStation15Min(date, chapterToIdMap.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN)));

        // 6. 逐15min冷站服务空间的温度状态
        runAnalysisReportDownloadDTO.setServiceSpaceTempStatusList(generateServiceSpaceTempStatus(date, chapterToIdMap.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN), chapterToIdMap.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENV_QUALITY)));

        // 7. 逐15min冷机数据
        runAnalysisReportDownloadDTO.setChiller15MinDataList(generateChiller15Min(date, chapterToIdMap.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN)));

        // 8. 逐15min冷却塔风机数据
        runAnalysisReportDownloadDTO.setCoolingTowerFan15MinDataList(generateCoolingTowerFan15Min(date, chapterToIdMap.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN)));

        // 9. 室外天气数据
        runAnalysisReportDownloadDTO.setOutdoorWeatherDataList(generateOutdoorWeather(date));

        return runAnalysisReportDownloadDTO;
    }


    /**
     * 发送CSV到Dify
     *
     * @param reportType CSV内容
     * @param date       报告日期
     */
    public void sendCsvToDify(LocalDate date, RunAnalysisReportTypeEnum reportType) {
        try {
            // 查询数据
            RunAnalysisReportDownloadDTO runAnalysisReportDownloadDTO = generateDownloadData(date, reportType);
            String content = RunAnalysisReportCsvExportUtil.buildCsvString(runAnalysisReportDownloadDTO);
            logProjectInstance().info("运行分析-日报-CSV内容：" + content);
            logProjectInstance().info("运行分析-日报-准备发送CSV到Dify", Pair.of("date", date));
            // 推送
            dififyServerService.webhook(DifyDTO.builder().reportcsv(content).build());
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-发送CSV到Dify失败", e);
        }
    }


    /**
     * 生成项目基础信息CSV
     */
    private ProjectBasicInfoDTO generateProjectBasicInfo() {
        // 查询项目信息
        ProjectDTO project = midPermissionClientService.getProject(false, false, ContextUtil.getProjectId());
        return ProjectBasicInfoDTO.builder()
                .projectId(Optional.ofNullable(project).map(ProjectDTO::getProjectId).orElse(""))
                .projectName(Optional.ofNullable(project).map(ProjectDTO::getProjectLocalName).orElse(""))
                .province(Optional.ofNullable(project).map(ProjectDTO::getProvinceName).orElse(""))
                .city(Optional.ofNullable(project).map(ProjectDTO::getCityName).orElse(""))
                .urbanZone(Optional.ofNullable(project).map(ProjectDTO::getPartitionName).orElse(""))
                .build();
    }

    /**
     * 格式化日期为 YYYY/M/D
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.getYear() + "/" + (date.getMonthValue()) + "/" + (date.getDayOfMonth());
    }

    /**
     * 格式化 LocalTime 为 HH:mm
     *
     * @param time 时间
     * @return 格式化后的时间字符串
     */
    private String formatLocalTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    /**
     * BigDecimal四舍五入保留二位小数
     *
     * @param value 数值
     * @return 四舍五入保留二位小数后的数值，如果为null则返回null
     */
    private BigDecimal roundToTwoDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 格式化营业时间段
     *
     * @param businessTimeList 营业时间段列表
     * @return 格式化后的时间段字符串，例如：08:00-12:00、14:00-18:00
     */
    private String formatBusinessTimeRange(LinkedHashSet<Range<LocalTime>> businessTimeList) {
        if (CollectionUtils.isEmpty(businessTimeList)) {
            return "";
        }

        return businessTimeList.stream()
                .map(range -> {
                    LocalTime start = range.getMinimum();
                    LocalTime end = range.getMaximum();
                    return formatLocalTime(start) + "-" + formatLocalTime(end);
                })
                .collect(Collectors.joining("、"));
    }

    /**
     * 筛选营业时间段内的天气详情
     *
     * @param details          所有天气详情
     * @param businessTimeList 营业时间段列表
     * @return 营业时间段内的天气详情列表
     */
    private List<WeatherDTO.Detail> filterDetailsByBusinessTime(
            List<WeatherDTO.Detail> details,
            LinkedHashSet<Range<LocalTime>> businessTimeList) {

        if (CollectionUtils.isEmpty(details) || CollectionUtils.isEmpty(businessTimeList)) {
            return Collections.emptyList();
        }

        return details.stream()
                .filter(detail -> {
                    if (detail == null || detail.getTime() == null) {
                        return false;
                    }
                    LocalTime detailTime = detail.getTime().toLocalTime();
                    return businessTimeList.stream()
                            .anyMatch(range -> range != null
                                    && !detailTime.isBefore(range.getMinimum())
                                    && detailTime.isBefore(range.getMaximum()));
                })
                .collect(Collectors.toList());
    }


    /**
     * 获取营业时间
     *
     * @param reportId 报告id
     * @return 获取运行概述数据
     */
    private <T> T queryChapterDataByReportId(Long reportId, Class<T> clazz) {
        if (reportId == null) {
            return null;
        }
        List<RunAnalysisReport> runAnalysisReports = runAnalysisReportRepository.listByIds(Lists.newArrayList(reportId));
        List<RunAnalysisReportVO> runAnalysisReportVOList = runAnalysisReportConvert.toRunAnalysisReportVOList(Optional.ofNullable(runAnalysisReports).orElse(Lists.newArrayList()));
        if (CollectionUtils.isEmpty(runAnalysisReportVOList) || Objects.isNull(runAnalysisReportVOList.get(0).getChapterData())) {
            return null;
        }
        return ObjectMapperUtils.convertValue(runAnalysisReportVOList.get(0).getChapterData(), clazz);
    }

    private <T> T queryChapterConfigDataByReportId(Long reportId, Class<T> clazz) {
        if (reportId == null) {
            return null;
        }
        List<RunAnalysisReport> runAnalysisReports = runAnalysisReportRepository.listByIds(Lists.newArrayList(reportId));
        List<RunAnalysisReportVO> runAnalysisReportVOList = runAnalysisReportConvert.toRunAnalysisReportVOList(Optional.ofNullable(runAnalysisReports).orElse(Lists.newArrayList()));
        if (CollectionUtils.isEmpty(runAnalysisReportVOList) || Objects.isNull(runAnalysisReportVOList.get(0).getChapterConfig())) {
            return null;
        }
        return ObjectMapperUtils.convertValue(runAnalysisReportVOList.get(0).getChapterConfig(), clazz);
    }

    /**
     * 生成日汇总数据CSV
     *
     * @param date 报告日期
     * @return 日汇总数据DTO
     */
    private DailySummaryDataDTO generateDailySummary(LocalDate date) {
        // 查询天气，由于湿球温度、焓值 后开发需要重新查询
        WeatherDTO weatherDTO = runAnalysisReportRunOverview.queryWeather(date);
        // 查询营业时间
        LinkedHashSet<Range<LocalTime>> businessTimeList = getBusinessTimeByObjId(date);

        // 构建 DTO
        DailySummaryDataDTO dto = DailySummaryDataDTO.builder()
                .date(formatDate(date))
                .businessTimeRange(formatBusinessTimeRange(businessTimeList))
                .build();

        // 获取全天天气详情
        List<WeatherDTO.Detail> allDayDetails;

        if (CollectionUtils.isNotEmpty(weatherDTO.getOriginalStationDetails())) {
            // 取气象站的原始数据计算
            allDayDetails = weatherDTO.getOriginalStationDetails();
            // 填充全天天气数据（气象站原始数据，需从 Detail 列表自行计算统计值）
            if (CollectionUtils.isNotEmpty(allDayDetails)) {
                // 温度数据 - 从原始明细自行计算
                Range<BigDecimal> allDayTempRange = WeatherDTO.parseTemperatureRange(allDayDetails);
                dto.setAllDayAvgTemp(roundToTwoDecimal(WeatherDTO.parseTemperatureAverage(allDayDetails)));
                if (allDayTempRange != null) {
                    dto.setAllDayMaxTemp(roundToTwoDecimal(allDayTempRange.getMaximum()));
                    dto.setAllDayMinTemp(roundToTwoDecimal(allDayTempRange.getMinimum()));
                }

                // 湿度数据 - 从原始明细自行计算
                Range<BigDecimal> allDayHumidityRange = WeatherDTO.parseHumidityRange(allDayDetails);
                dto.setAllDayAvgHumidity(roundToTwoDecimal(WeatherDTO.parseHumidityAverage(allDayDetails)));
                if (allDayHumidityRange != null) {
                    dto.setAllDayMaxHumidity(roundToTwoDecimal(allDayHumidityRange.getMaximum()));
                    dto.setAllDayMinHumidity(roundToTwoDecimal(allDayHumidityRange.getMinimum()));
                }

                // 湿球温度和焓值 - 从原始明细自行计算
                dto.setAllDayAvgWetBulbTemp(roundToTwoDecimal(WeatherDTO.parseWetBulbTempAverage(allDayDetails)));
                dto.setAllDayAvgEnthalpy(roundToTwoDecimal(WeatherDTO.parseEnthalpyValueAverage(allDayDetails)));
            }

            // 筛选营业时段内的天气数据
            List<WeatherDTO.Detail> businessHoursDetails = filterDetailsByBusinessTime(allDayDetails, businessTimeList);

            // 填充营业时段内天气数据
            if (CollectionUtils.isNotEmpty(businessHoursDetails)) {
                // 温度数据 - 从原始明细自行计算
                BigDecimal businessAvgTemp = WeatherDTO.parseTemperatureAverage(businessHoursDetails);
                Range<BigDecimal> businessTempRange = WeatherDTO.parseTemperatureRange(businessHoursDetails);
                dto.setBusinessHoursAvgTemp(roundToTwoDecimal(businessAvgTemp));
                if (businessTempRange != null) {
                    dto.setBusinessHoursMaxTemp(roundToTwoDecimal(businessTempRange.getMaximum()));
                    dto.setBusinessHoursMinTemp(roundToTwoDecimal(businessTempRange.getMinimum()));
                }

                // 湿度数据 - 从原始明细自行计算
                BigDecimal businessAvgHumidity = WeatherDTO.parseHumidityAverage(businessHoursDetails);
                Range<BigDecimal> businessHumidityRange = WeatherDTO.parseHumidityRange(businessHoursDetails);
                dto.setBusinessHoursAvgHumidity(roundToTwoDecimal(businessAvgHumidity));
                if (businessHumidityRange != null) {
                    dto.setBusinessHoursMaxHumidity(roundToTwoDecimal(businessHumidityRange.getMaximum()));
                    dto.setBusinessHoursMinHumidity(roundToTwoDecimal(businessHumidityRange.getMinimum()));
                }

                // 湿球温度和焓值 - 从原始明细自行计算
                dto.setBusinessHoursAvgWetBulbTemp(roundToTwoDecimal(WeatherDTO.parseWetBulbTempAverage(businessHoursDetails)));
                dto.setBusinessHoursAvgEnthalpy(roundToTwoDecimal(WeatherDTO.parseEnthalpyValueAverage(businessHoursDetails)));
            }
        } else {
            allDayDetails = weatherDTO.getDetails();
            // 填充全天天气数据
            if (CollectionUtils.isNotEmpty(allDayDetails)) {
                // 温度数据 - 直接使用预计算值，四舍五入保留一位小数
                dto.setAllDayAvgTemp(roundToTwoDecimal(weatherDTO.getAvgTemperature()));
                dto.setAllDayMaxTemp(roundToTwoDecimal(weatherDTO.getMaxTemperature()));
                dto.setAllDayMinTemp(roundToTwoDecimal(weatherDTO.getMinTemperature()));

                // 湿度数据 - 直接使用预计算值，四舍五入保留一位小数
                dto.setAllDayAvgHumidity(roundToTwoDecimal(weatherDTO.getAvgHumidity()));
                dto.setAllDayMaxHumidity(roundToTwoDecimal(weatherDTO.getMaxHumidity()));
                dto.setAllDayMinHumidity(roundToTwoDecimal(weatherDTO.getMinHumidity()));

                // 湿球温度和焓值 - 使用静态方法计算，四舍五入保留一位小数
                dto.setAllDayAvgWetBulbTemp(roundToTwoDecimal(WeatherDTO.parseWetBulbTempAverage(allDayDetails)));
                dto.setAllDayAvgEnthalpy(roundToTwoDecimal(WeatherDTO.parseEnthalpyValueAverage(allDayDetails)));
            }

            // 筛选营业时段内的天气数据
            List<WeatherDTO.Detail> businessHoursDetails = filterDetailsByBusinessTime(allDayDetails, businessTimeList);

            // 填充营业时段内天气数据
            if (CollectionUtils.isNotEmpty(businessHoursDetails)) {
                // 温度数据 - 使用静态方法计算
                BigDecimal businessAvgTemp = WeatherDTO.parseTemperatureAverage(businessHoursDetails);
                Range<BigDecimal> businessTempRange = WeatherDTO.parseTemperatureRange(businessHoursDetails);
                dto.setBusinessHoursAvgTemp(roundToTwoDecimal(businessAvgTemp));
                if (businessTempRange != null) {
                    dto.setBusinessHoursMaxTemp(roundToTwoDecimal(businessTempRange.getMaximum()));
                    dto.setBusinessHoursMinTemp(roundToTwoDecimal(businessTempRange.getMinimum()));
                }

                // 湿度数据 - 使用静态方法计算
                BigDecimal businessAvgHumidity = WeatherDTO.parseHumidityAverage(businessHoursDetails);
                Range<BigDecimal> businessHumidityRange = WeatherDTO.parseHumidityRange(businessHoursDetails);
                dto.setBusinessHoursAvgHumidity(roundToTwoDecimal(businessAvgHumidity));
                if (businessHumidityRange != null) {
                    dto.setBusinessHoursMaxHumidity(roundToTwoDecimal(businessHumidityRange.getMaximum()));
                    dto.setBusinessHoursMinHumidity(roundToTwoDecimal(businessHumidityRange.getMinimum()));
                }

                // 湿球温度和焓值 - 使用静态方法计算
                dto.setBusinessHoursAvgWetBulbTemp(roundToTwoDecimal(WeatherDTO.parseWetBulbTempAverage(businessHoursDetails)));
                dto.setBusinessHoursAvgEnthalpy(roundToTwoDecimal(WeatherDTO.parseEnthalpyValueAverage(businessHoursDetails)));
            }
        }
        return dto;
    }


    /**
     * @author weiyizhong
     * @Company yushu.biz
     * 　　 * @date 2026/3/19 11:03
     * 　　 * @description: 查询工作历指定实例营业时间段
     */
    public LinkedHashSet<Range<LocalTime>> getBusinessTimeByObjId(LocalDate date) {
        MidCalendarQueryParam param = new MidCalendarQueryParam();
        param.setObjId(ContextUtil.getProjectId());
        MidCalendarOpenTimeResponse openTime = midCalendarClientService.getMidCalendarOpenTimeByDay(param, date);
        if (openTime == null) {
            return null;
        }
        LinkedHashSet<Range<LocalTime>> localTimeList = new LinkedHashSet<>();
        openTime.getTimeTable().forEach(item -> {
            if (StringUtils.isEmpty(item.getWorkTime())) {
                return;
            }
            String[] times = item.getWorkTime().split("-");
            if (times.length == 2) {
                localTimeList.add(Range.between(LocalTime.parse(times[0]), LocalTime.parse(times[1])));
            }
        });
        return localTimeList;
    }

    /**
     * 生成冷站日汇总数据
     */
    private List<CoolingStationDailySummaryDTO> generateCoolingStationDailySummary(Long reportId) {
        List<CoolingStationDailySummaryDTO> coolingStationDailySummaryDTOs = Lists.newArrayList();
        // 查询运行概述获取冷站数据
        RunOverviewDTO runOverviewDTO = queryChapterDataByReportId(reportId, RunOverviewDTO.class);
        if (runOverviewDTO == null || runOverviewDTO.getSystemRunningResults() == null) {
            return coolingStationDailySummaryDTOs;
        }
        List<SystemRunningResultDTO> systemRunningResults = runOverviewDTO.getSystemRunningResults();
        systemRunningResults.forEach(item -> {
            CoolingStationDailySummaryDTO coolingStationDailySummaryDTO = new CoolingStationDailySummaryDTO();
            coolingStationDailySummaryDTO.setSystemName(item.getLocalName());
            coolingStationDailySummaryDTO.setTemperatureSatisfactionRate(roundToTwoDecimal(item.getServiceSpaceSatisfyPercentage()));
            coolingStationDailySummaryDTO.setActualEnergyConsumption(roundToTwoDecimal(item.getDayAccElecConsum()));
            coolingStationDailySummaryDTOs.add(coolingStationDailySummaryDTO);
        });
        return coolingStationDailySummaryDTOs;
    }

    /**
     * 生成冷站参考相似日能耗数据
     */
    private List<CoolingStationSimilarDayDTO> generateSimilarDay(LocalDate date) {
        List<CoolingStationSimilarDayDTO> result = Lists.newArrayList();

        // 查询该报告日期之前的历史已生成的报告数据，日期 -> reports数据
        List<RunAnalysisReport> runAnalysisReportList = runAnalysisReportRepository.getChapterDataListLeDate(date, RunAnalysisReportTypeEnum.DAILY, RunAnalysisReportChapterEnum.REPORT_CHAPTER_RUN_OVERVIEW);
        List<RunAnalysisReportVO> runAnalysisReportVOList = runAnalysisReportConvert.toRunAnalysisReportVOList(Optional.ofNullable(runAnalysisReportList).orElse(Lists.newArrayList()));
        Map<LocalDate, RunOverviewDTO> dateToReportMap = runAnalysisReportVOList
                .stream().filter(e -> Objects.nonNull(e.getChapterData())).collect(Collectors.toMap(
                        RunAnalysisReportVO::getReportDate,
                        item -> ObjectMapperUtils.convertValue(item.getChapterData(), RunOverviewDTO.class),
                        (k1, k2) -> k1
                ));

        // 当前日期的报告不存在则不用比较了，直接返回
        if (!dateToReportMap.containsKey(date)) {
            return result;
        }

        // 获取报告日的"平均温度"和"最高温度"
        RunOverviewDTO currentDayOverview = dateToReportMap.get(date);
        if (currentDayOverview == null || currentDayOverview.getWeather() == null) {
            return result;
        }

        BigDecimal currentAvgTemp = currentDayOverview.getWeather().getAvgTemperature();
        BigDecimal currentMaxTemp = currentDayOverview.getWeather().getMaxTemperature();

        // 当前日温度为空，无法找出相似日
        if (currentAvgTemp == null || currentMaxTemp == null) {
            return result;
        }

        // 获取当前报告中的冷站系统id to 名称
        Map<String, String> systemIdToNameMap = Maps.newHashMap();
        if (currentDayOverview.getSystemRunningResults() != null) {
            systemIdToNameMap = currentDayOverview.getSystemRunningResults().stream()
                    .collect(Collectors.toMap(SystemRunningResultDTO::getId, SystemRunningResultDTO::getLocalName));
        }

        if (MapUtils.isEmpty(systemIdToNameMap)) {
            return result;
        }

        // 找出相似日
        for (Map.Entry<LocalDate, RunOverviewDTO> entry : dateToReportMap.entrySet()) {
            LocalDate historyDate = entry.getKey();

            // 跳过当前日期
            if (historyDate.equals(date)) {
                continue;
            }

            RunOverviewDTO historyOverview = entry.getValue();
            WeatherDTO historyWeather = historyOverview.getWeather();

            // 历史日天气数据为空，跳过
            if (historyWeather == null) {
                continue;
            }

            BigDecimal historyAvgTemp = historyWeather.getAvgTemperature();
            BigDecimal historyMaxTemp = historyWeather.getMaxTemperature();

            // 任意一个值为空，此日期被排除
            if (historyAvgTemp == null || historyMaxTemp == null) {
                continue;
            }

            // 计算温度偏差
            BigDecimal avgTempDiff = currentAvgTemp.subtract(historyAvgTemp).abs();
            BigDecimal maxTempDiff = currentMaxTemp.subtract(historyMaxTemp).abs();

            // 相似日条件：平均温度偏差绝对值≤1℃ AND 最高温度偏差绝对值≤1℃
            if (avgTempDiff.compareTo(new BigDecimal("1")) <= 0 && maxTempDiff.compareTo(new BigDecimal("1")) <= 0) {
                // 找到相似日，为每个冷站系统生成数据
                List<SystemRunningResultDTO> historySystemResults = historyOverview.getSystemRunningResults();

                if (CollectionUtils.isNotEmpty(historySystemResults)) {
                    for (String systemId : systemIdToNameMap.keySet()) {
                        // 找到该历史日对应的系统数据
                        SystemRunningResultDTO systemResult = historySystemResults.stream()
                                .filter(s -> systemId.equals(s.getId()))
                                .findFirst()
                                .orElse(null);

                        CoolingStationSimilarDayDTO dto = CoolingStationSimilarDayDTO.builder()
                                .systemName(systemIdToNameMap.get(systemId))
                                .date(formatDate(historyDate))
                                .referenceEnergyValue(systemResult != null ? roundToTwoDecimal(systemResult.getDayAccElecConsum()) : null)
                                .allDayAvgTemp(roundToTwoDecimal(historyAvgTemp))
                                .allDayMaxTemp(roundToTwoDecimal(historyMaxTemp))
                                .allDayAvgHumidity(historyWeather.getAvgHumidity() != null ? roundToTwoDecimal(historyWeather.getAvgHumidity()) : null)
                                .build();

                        result.add(dto);
                    }
                } else {
                    // 如果历史日没有系统数据，仍然要为每个系统生成空能耗值
                    for (String systemName : systemIdToNameMap.values()) {
                        CoolingStationSimilarDayDTO dto = CoolingStationSimilarDayDTO.builder()
                                .systemName(systemName)
                                .build();
                        result.add(dto);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            // 表示完全没有相似日，数据就只有名称
            for (String systemName : systemIdToNameMap.values()) {
                CoolingStationSimilarDayDTO dto = CoolingStationSimilarDayDTO.builder()
                        .systemName(systemName)
                        .build();
                result.add(dto);

            }
        }

        // 按系统名称分组，对每个系统的相似日列表进行清洗
        Map<String, List<CoolingStationSimilarDayDTO>> systemNameToDtoMap = result.stream()
                .collect(Collectors.groupingBy(CoolingStationSimilarDayDTO::getSystemName));
        List<CoolingStationSimilarDayDTO> finalResult = Lists.newArrayList();

        systemNameToDtoMap.forEach((systemName, dtoList) -> {
            boolean hasValidData = dtoList.stream().anyMatch(e -> Objects.nonNull(e.getReferenceEnergyValue()));
            if (!hasValidData) {
                // 该系统下所有相似日均无参考能耗值，保留一条仅含系统名称的占位行
                finalResult.add(CoolingStationSimilarDayDTO.builder()
                        .systemName(systemName)
                        .build());
            } else {
                // 该系统存在有效的相似日数据，过滤掉无参考能耗值的行
                finalResult.addAll(dtoList.stream()
                        .filter(e -> Objects.nonNull(e.getReferenceEnergyValue()))
                        .collect(Collectors.toList()));
            }
        });


        // 按照日期升序
        finalResult.sort(Comparator.comparing(
                CoolingStationSimilarDayDTO::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return finalResult;
    }

    /**
     * 生成一天的96个15分钟时间点
     */
    private List<LocalDateTime> generate15MinTimePoints(LocalDate reportDate) {
        List<LocalDateTime> timePoints = Lists.newArrayList();
        LocalDateTime start = reportDate.atStartOfDay();
        for (int i = 0; i < 96; i++) {
            timePoints.add(start.plusMinutes(i * 15));
        }
        return timePoints;
    }

    /**
     * 格式化时间点为 YYYY/M/D HH:MM-HH:MM
     */
    private String formatTimePoint(LocalDateTime timePoint) {
        return String.format("%02d:%02d",
                timePoint.getHour(),
                timePoint.getMinute());
    }

    /**
     * 从数据Map中获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(
            Map<String, Map<LocalDateTime, Object>> infoCode2Time2Value,
            String infoCode,
            LocalDateTime timePoint) {
        if (infoCode2Time2Value == null) {
            return null;
        }
        Map<LocalDateTime, Object> time2Value = infoCode2Time2Value.get(infoCode);
        if (time2Value == null) {
            return null;
        }
        Object value = time2Value.get(timePoint);
        if (value == null) {
            return null;
        }
        try {
            return roundToTwoDecimal(new BigDecimal(value.toString()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从数据Map中获取字符串值
     */
    private String getStringValue(
            Map<String, Map<LocalDateTime, Object>> infoCode2Time2Value,
            String infoCode,
            LocalDateTime timePoint) {
        if (infoCode2Time2Value == null) {
            return null;
        }
        Map<LocalDateTime, Object> time2Value = infoCode2Time2Value.get(infoCode);
        if (time2Value == null) {
            return null;
        }
        Object value = time2Value.get(timePoint);
        return value != null ? value.toString() : null;
    }


    /**
     * 从min值映射中获取BigDecimal值
     */
    private BigDecimal getMinValueAsBigDecimal(Map<String, Integer> systemIdToMinValueMap, String systemId) {
        Integer minValue = systemIdToMinValueMap.get(systemId);
        return minValue != null ? new BigDecimal(minValue) : null;
    }

    /**
     * 生成逐15min冷站数据
     */
    private List<CoolingStation15MinDataDTO> generateCoolingStation15Min(LocalDate reportDate, Long reportId) {
        // 查询冷站系统运行数据
        CoolingSysRunDTO coolingSysRunDTO = queryChapterDataByReportId(reportId, CoolingSysRunDTO.class);
        if (coolingSysRunDTO == null || CollectionUtils.isEmpty(coolingSysRunDTO.getSystems())) {
            return Lists.newArrayList();
        }

        // 提取系统数据
        List<SystemEnergyDTO> systems = coolingSysRunDTO.getSystems();
        List<String> systemIds = systems.stream()
                .map(e -> e.getSystem().getId())
                .collect(Collectors.toList());

        Map<String, String> systemIdToNameMap = systems.stream()
                .collect(Collectors.toMap(
                        e -> e.getSystem().getId(),
                        e -> e.getSystem().getLocalName(),
                        (k1, k2) -> k1
                ));
        Map<String, String> systemIdToClassCodeMap = systems.stream()
                .collect(Collectors.toMap(
                        e -> e.getSystem().getId(),
                        e -> e.getSystem().getClassCode(),
                        (k1, k2) -> k1
                ));

        // 构建系统ID到冷冻水一次侧供水干管温度下限值的映射
        Map<String, Integer> systemIdToMinValueMap = Maps.newHashMap();
        for (String systemId : systemIds) {
            QueryShellConfigByInfoCodeParam param = new QueryShellConfigByInfoCodeParam();
            param.setObjectId(systemId);
            param.setProjectId(ContextUtil.getProjectId());
            param.setCode(ALG_AIR_COOLING_TEMP_MAX_MIN.getCode());
            ShellConfigItem shellConfigItem = shellConfigService.queryByInfoCode(ContextUtil.getGroupCode(), param);
            if (shellConfigItem != null && shellConfigItem.getValue() != null && StringUtils.isNotBlank(shellConfigItem.getValue())) {
                systemIdToMinValueMap.put(systemId, Integer.valueOf(shellConfigItem.getValue().split(",")[0]));
            } else {
                systemIdToMinValueMap.put(systemId, null);
            }
        }

        // 查询字典
        // 查询字典
        Map<String, List<InfoCodeDefineModel>> classCodeToFuncid = runAnalysisReportCoolingSysRun.queryClassCode2InfoCodeDefines(systems.stream().map(e -> e.getSystem().getClassCode()).distinct().collect(Collectors.toList()));


        // 定义要查询的信息点编码
        List<String> infoCodes = Arrays.asList(
                InfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode(),
                InfoCodeEnum.CHILL_WATER_DETA_TEMP.getCode(),
                InfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(),
                InfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP.getCode(),
                InfoCodeEnum.COOLING_TOWER_OUT_TEMP_SET.getCode(),
                InfoCodeEnum.COOL_WATER_MAIN_SUPPLY_TEMP.getCode(),
                InfoCodeEnum.COOL_WATER_MAIN_RETURN_TEMP.getCode(),
                InfoCodeEnum.COOL_WATER_DETA_TEMP_SET.getCode(),
                InfoCodeEnum.COOL_WATER_DETA_TEMP.getCode(),
                InfoCodeEnum.FREE_COOLING_SWITCH_SET.getCode(),
                InfoCodeEnum.COOLING_SUPPLY_SWITCH_SET.getCode()
        );

        // 查询IoT数据
        LocalDateTime startTime = DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE);
        LocalDateTime endTime = DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE);
        List<InfoCodeValueVO> infoCodeValueList = abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES,
                systemIds,
                infoCodes,
                startTime,
                endTime
        );

        // 将IoT数据转换为方便查询的Map结构
        Map<String, Map<String, Map<LocalDateTime, Object>>> systemId2InfoCode2Time2Value =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueList);

        // 生成所有时间点
        List<LocalDateTime> timePoints = generate15MinTimePoints(reportDate);

        // 组装数据
        List<CoolingStation15MinDataDTO> result = Lists.newArrayList();

        for (String systemId : systemIds) {

            for (LocalDateTime timePoint : timePoints) {
                String timePointStr = formatTimePoint(timePoint);

                String systemName = systemIdToNameMap.get(systemId);
                List<InfoCodeDefineModel> infoCodeDefineModels = classCodeToFuncid.get(systemIdToClassCodeMap.get(systemId));

                // 获取该系统该时间点的数据
                Map<String, Map<LocalDateTime, Object>> infoCode2Time2Value =
                        systemId2InfoCode2Time2Value.getOrDefault(systemId, Maps.newHashMap());

                CoolingStation15MinDataDTO dto = CoolingStation15MinDataDTO.builder()
                        .timePoint(timePointStr)
                        .systemName(systemName)
                        .chillWaterDeltaTempSet(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode(), timePoint))
                        .chillWaterDeltaTemp(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.CHILL_WATER_DETA_TEMP.getCode(), timePoint))
                        .primaryChillWaterSupplyTempLowerLimit(
                                getMinValueAsBigDecimal(systemIdToMinValueMap, systemId))
                        .primaryChillWaterMainSupplyTemp(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(), timePoint))
                        .primaryChillWaterMainReturnTemp(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP.getCode(), timePoint))
                        .coolingTowerOutTempSet(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.COOLING_TOWER_OUT_TEMP_SET.getCode(), timePoint))
                        .coolWaterMainSupplyTemp(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.COOL_WATER_MAIN_SUPPLY_TEMP.getCode(), timePoint))
                        .coolWaterMainReturnTemp(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.COOL_WATER_MAIN_RETURN_TEMP.getCode(), timePoint))
                        .coolWaterDeltaTempSet(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.COOL_WATER_DETA_TEMP_SET.getCode(), timePoint))
                        .coolWaterDeltaTemp(getBigDecimalValue(
                                infoCode2Time2Value, InfoCodeEnum.COOL_WATER_DETA_TEMP.getCode(), timePoint))
                        .freeCoolingSwitchSet(translateInfoCodeValue(
                                getStringValue(infoCode2Time2Value, InfoCodeEnum.FREE_COOLING_SWITCH_SET.getCode(), timePoint),
                                infoCodeDefineModels, InfoCodeEnum.FREE_COOLING_SWITCH_SET.getCode()))
                        .coolingSupplySwitchSet(translateInfoCodeValue(
                                getStringValue(infoCode2Time2Value, InfoCodeEnum.COOLING_SUPPLY_SWITCH_SET.getCode(), timePoint),
                                infoCodeDefineModels, InfoCodeEnum.COOLING_SUPPLY_SWITCH_SET.getCode()))
                        .build();

                result.add(dto);
            }
        }
        return result;
    }


    /**
     * 生成逐15min冷站服务空间的温度状态
     */
    private List<ServiceSpaceTempStatusDTO> generateServiceSpaceTempStatus(LocalDate date, Long reportId, Long envQualityReportId) {
        // 查询冷站系统运行数据
        CoolingSysRunDTO coolingSysRunDTO = queryChapterDataByReportId(reportId, CoolingSysRunDTO.class);
        if (coolingSysRunDTO == null || CollectionUtils.isEmpty(coolingSysRunDTO.getSystems())) {
            return Lists.newArrayList();
        }

        // 环境品质数据
        EnvQualityConfig envQualityDTO = queryChapterConfigDataByReportId(envQualityReportId, EnvQualityConfig.class);
        if (envQualityDTO == null) {
            return Lists.newArrayList();
        }

        // 构造参数
        Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData = Maps.newHashMap();
        allConfigData.put(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENV_QUALITY,
                Lists.newArrayList(envQualityDTO));

        // 查询空间
        Set<String> spaceIds = runAnalysisReportRunOverview.querySpaceIds(date, allConfigData);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }

        // 提取系统数据
        Map<String, String> systemIdToNameMap = coolingSysRunDTO.getSystems().stream()
                .collect(Collectors.toMap(
                        e -> e.getSystem().getId(),
                        e -> e.getSystem().getLocalName(),
                        (k1, k2) -> k1
                ));

        // 获取系统温度达标率和系统空间关系
        Tuple2<Map<String, Map<LocalDateTime, String>>, Map<String, Set<String>>> result =
                querySpaceTdbComplianceRate(date, spaceIds, systemIdToNameMap);
        Map<String, Map<LocalDateTime, String>> systemIdToTimeToTdbComplianceRateMap = result.getFirst();
        Map<String, Set<String>> systemIdToSpaceIds = result.getSecond();

        // 查询空间TDB数据
        Map<String, Map<LocalDateTime, String>> spaceId2DateTime2Tdb =
                runAnalysisReportEnvQuality.querySpaceTdb(date, spaceIds);


        // 获取季节配置（取第一个）
        Map<String, DemandConfigAirconditioningTdbQualityValueObject> spaceIdToDemandMap = runAnalysisReportEnvQuality.queryTdbQualityConfig(date, spaceIds);

        // 生成96个15分钟时间点
        List<LocalDateTime> timePoints = generate15MinTimePoints(date);

        List<ServiceSpaceTempStatusDTO> resultList = Lists.newArrayList();

        // 遍历每个系统
        for (Map.Entry<String, String> entry : systemIdToNameMap.entrySet()) {
            String systemId = entry.getKey();
            String systemName = entry.getValue();

            // 获取该系统下的空间ID集合
            Set<String> systemSpaceIds = systemIdToSpaceIds.getOrDefault(systemId, new HashSet<>());

            // 获取该系统的每小时温度达标率
            Map<LocalDateTime, String> timeToComplianceRate =
                    systemIdToTimeToTdbComplianceRateMap.getOrDefault(systemId, Maps.newHashMap());

            // 遍历每个时间点
            for (LocalDateTime timePoint : timePoints) {
                ServiceSpaceTempStatusDTO dto = calculateSpaceTempStatus(
                        timePoint,
                        systemName,
                        systemSpaceIds,
                        spaceId2DateTime2Tdb,
                        spaceIdToDemandMap,
                        timeToComplianceRate
                );
                resultList.add(dto);
            }
        }

        return resultList;
    }


    /**
     * 查询系统服务空间达标率
     */
    private Tuple2<Map<String, Map<LocalDateTime, String>>, Map<String, Set<String>>> querySpaceTdbComplianceRate(LocalDate date, Set<String> spaceIds, Map<String, String> systemIdToNameMap) {
        // 查询系统服务空间达标率
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        spaceQueryParam.setFilterSpaceServiceSystemIds(systemIdToNameMap.keySet());
        spaceQueryParam.setFilterObjectIds(spaceIds);
        List<DtpRwdSpaceDTO> serviceSystemSpaceIds = spaceQueryParam.queryObject(new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));
        // 找到系统下的空间实例
        Map<String, Set<String>> systemId2SpaceIds = DtpRwdObjectDTO.mapRelObjIdAndIds(serviceSystemSpaceIds);
        logProjectInstance().info("查询-系统服务空间", Arrays.asList(
                Pair.of("systemIds", systemIdToNameMap.keySet()),
                Pair.of("spaceIds", spaceIds),
                Pair.of("systemId2SpaceIds", systemId2SpaceIds)
        ));

        // 计算服务空间达标率
        Map<String, Map<LocalDateTime, String>> systemId2Hour2TdbComplianceRate = new ConcurrentHashMap<>();
        if (MapUtils.isNotEmpty(systemId2SpaceIds)) {
            // 查询系统下的空间达标率
            List<CompletableFuture<Void>> futures = systemId2SpaceIds.entrySet().stream()
                    .map(entry -> CompletableFuture.runAsync(() -> {
                        Map<String, Map<LocalDateTime, Tuple2<Integer, Integer>>> spaceId2Hour2TimeNumber2SatisfyTimeNumber =
                                this.querySpaceEnvironmentalQuality(date, entry.getValue());
                        // 生成所有时间点
                        List<LocalDateTime> timePoints = generate15MinTimePoints(date);
                        Map<LocalDateTime, Tuple2<Integer, Integer>> hour2TimeNumber2SatisfyTimeNumberTotal = Maps.newHashMap();

                        if (MapUtils.isNotEmpty(spaceId2Hour2TimeNumber2SatisfyTimeNumber)) {
                            Map<LocalDateTime, String> hour2ComplianceRate = new HashMap<>();
                            for (Map.Entry<String, Map<LocalDateTime, Tuple2<Integer, Integer>>> spaceEntry : spaceId2Hour2TimeNumber2SatisfyTimeNumber.entrySet()) {
                                // 空间下的时间段
                                Map<LocalDateTime, Tuple2<Integer, Integer>> hour2Stats = spaceEntry.getValue();
                                for (LocalDateTime timePoint : timePoints) {
                                    // 获取时刻对应的统计数据
                                    Tuple2<Integer, Integer> stats = hour2Stats.getOrDefault(timePoint, new Tuple2<>(null, null));
                                    Integer totalCount = stats.getFirst();
                                    Integer satisfyCount = stats.getSecond();
                                    // 统计数据
                                    Tuple2<Integer, Integer> orDefault = hour2TimeNumber2SatisfyTimeNumberTotal.getOrDefault(timePoint, new Tuple2<>(null, null));
                                    hour2TimeNumber2SatisfyTimeNumberTotal.put(timePoint,
                                            new Tuple2<>(Objects.isNull(totalCount) ? orDefault.getFirst() : (
                                                    Objects.isNull(orDefault.getFirst()) ? totalCount : totalCount + orDefault.getFirst()
                                            ),
                                                    Objects.isNull(satisfyCount) ? orDefault.getSecond() : (
                                                            Objects.isNull(orDefault.getSecond()) ? satisfyCount : satisfyCount + orDefault.getSecond()
                                                    )));
                                }
                            }
                            hour2TimeNumber2SatisfyTimeNumberTotal.forEach((hour, stats) -> {
                                Integer totalCount = stats.getFirst();
                                Integer satisfyCount = stats.getSecond();
                                if (Objects.nonNull(totalCount) && totalCount > 0 && Objects.nonNull(satisfyCount)) {
                                    BigDecimal rate = new BigDecimal(satisfyCount)
                                            .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                                            .multiply(new BigDecimal(100));
                                    hour2ComplianceRate.put(hour, rate.setScale(2, RoundingMode.HALF_UP).toString() + "%");
                                }
                            });
                            systemId2Hour2TdbComplianceRate.put(entry.getKey(), hour2ComplianceRate);
                        }
                    }, BusinessThreadPoolExecutor.ttlExecutor()))
                    .collect(Collectors.toList());
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (Exception e) {
                logProjectInstance().error("查询-服务空间达标率失败", e,
                        Pair.of("systemId2SpaceIds", systemId2SpaceIds));
            }
        }

        return new Tuple2<>(systemId2Hour2TdbComplianceRate, systemId2SpaceIds);
    }

    /**
     * 查询空间温度达标统计
     *
     * @param date
     * @param spaceIds
     * @return
     */
    private Map<String, Map<LocalDateTime, Tuple2<Integer, Integer>>> querySpaceEnvironmentalQuality(LocalDate date, Set<String> spaceIds) {
        // 查询空间对应的数据
        List<AnalysisAirConditionerSpace> analysisAirConditionerSpaces =
                analysisAirConditionerSpacesService.listByDayAndAbnormalAndTagType(
                        Lists.newArrayList(spaceIds),
                        null,
                        null,
                        date,
                        null,
                        RunningMonitorSecondTypeEnum.RUN_QUALIFY_TDB
                );

        if (CollectionUtils.isEmpty(analysisAirConditionerSpaces)) {
            return Maps.newHashMap();
        }

        // 计算每小时的满足率
        // 时刻取左边的时刻，比如："10:00,10:59": "APPROPRIATE" 则LocalDateTime=10:00
        // Tuple2<Integer,Integer> f0表示总的次数，f1表示满足的次数
        Map<String, Map<LocalDateTime, Tuple2<Integer, Integer>>> spaceId2Hour2TimeNumber2SatisfyTimeNumber =
                new ConcurrentHashMap<>();

        for (AnalysisAirConditionerSpace space : analysisAirConditionerSpaces) {
            String spaceId = space.getSpaceId();
            if (StringUtils.isBlank(spaceId)) {
                continue;
            }

            List<TimeTagPO> timeTags = space.getTimeTag();
            if (CollectionUtils.isEmpty(timeTags)) {
                continue;
            }

            Map<LocalDateTime, Tuple2<Integer, Integer>> hour2TimeNumber2SatisfyTimeNumber =
                    spaceId2Hour2TimeNumber2SatisfyTimeNumber.computeIfAbsent(
                            spaceId,
                            k -> new HashMap<>()
                    );

            for (TimeTagPO timeTag : timeTags) {
                if (timeTag == null || timeTag.getTime() == null) {
                    continue;
                }

                // 取左边界时刻
                LocalTime timePoint = timeTag.getTime().getMinimum();
                LocalDateTime dateTime = LocalDateTime.of(space.getDay(), timePoint);

                // 获取标签值判断是否满足
                String tagValue = timeTag.getTagKey();
                boolean isSatisfied = "APPROPRIATE".equals(tagValue);

                // 更新统计
                hour2TimeNumber2SatisfyTimeNumber.compute(dateTime, (k, existingTuple) -> {
                    if (existingTuple == null) {
                        return new Tuple2(1, isSatisfied ? 1 : 0);
                    } else {
                        return new Tuple2(
                                existingTuple.getFirst() + 1,
                                existingTuple.getSecond() + (isSatisfied ? 1 : 0)
                        );
                    }
                });
            }
        }
        return spaceId2Hour2TimeNumber2SatisfyTimeNumber;
    }

    /**
     * 计算单个时间点的空间温度状态
     *
     * @param timePoint            时间点
     * @param systemName           系统名称
     * @param spaceIds             空间ID集合
     * @param spaceId2DateTime2Tdb 空间TDB数据
     * @param spaceIdToDemandMap   季节温度标准配置
     * @param timeToComplianceRate 时间到达标率映射
     * @return 服务空间温度状态DTO
     */
    private ServiceSpaceTempStatusDTO calculateSpaceTempStatus(
            LocalDateTime timePoint,
            String systemName,
            Set<String> spaceIds,
            Map<String, Map<LocalDateTime, String>> spaceId2DateTime2Tdb,
            Map<String, DemandConfigAirconditioningTdbQualityValueObject> spaceIdToDemandMap,
            Map<LocalDateTime, String> timeToComplianceRate) {

        // 初始化计数器
        Integer tooColdCount = null;
        Integer slightlyColdCount = null;
        Integer comfortableCount = null;
        Integer slightlyHotCount = null;
        Integer tooHotCount = null;
        Integer noDataCount = null;

        // 遍历每个空间，判断温度状态
        for (String spaceId : spaceIds) {
            BigDecimal temperature = getTemperatureAtTime(spaceId, timePoint, spaceId2DateTime2Tdb);

            // 获取季节配置（取第一个）
            List<DemandConfigAirconditioningTdbSeasonValueObject> airConditioning = spaceIdToDemandMap.getOrDefault(spaceId, new DemandConfigAirconditioningTdbQualityValueObject())
                    .getSeasonQuality();
            DemandConfigAirconditioningTdbSeasonValueObject seasonQuality = null;
            if (CollectionUtils.isNotEmpty(airConditioning)) {
                seasonQuality = airConditioning
                        .stream()
                        .findFirst()
                        .orElse(new DemandConfigAirconditioningTdbSeasonValueObject());
            }

            // 获取温度标准
            // 过冷门限
            BigDecimal underCoolingLimit = seasonQuality != null ?
                    seasonQuality.getUnderCoolingSeasonLimitValue() : null;
            // 舒适区下限值
            BigDecimal lowerComfortLimit = seasonQuality != null ?
                    seasonQuality.getLowerComfortSeasonLimitValue() : null;
            // 舒适区上限值
            BigDecimal upperComfortLimit = seasonQuality != null ?
                    seasonQuality.getUpperComfortSeasonLimitValue() : null;
            // 过热门限
            BigDecimal overHeatingLimit = seasonQuality != null ?
                    seasonQuality.getOverHeatingSeasonLimitValue() : null;

            // 判断是否有有效温度标准
            boolean hasValidStandard = seasonQuality != null &&
                    underCoolingLimit != null &&
                    lowerComfortLimit != null &&
                    upperComfortLimit != null &&
                    overHeatingLimit != null;

            if (!hasValidStandard || temperature == null) {
                noDataCount = getCount(noDataCount);
            } else {
                int status = judgeTemperatureStatus(
                        temperature,
                        underCoolingLimit,
                        lowerComfortLimit,
                        upperComfortLimit,
                        overHeatingLimit
                );

                switch (status) {
                    case 0:
                        tooColdCount = getCount(tooColdCount);
                        break;
                    case 1:
                        slightlyColdCount = getCount(slightlyColdCount);
                        break;
                    case 2:
                        comfortableCount = getCount(comfortableCount);
                        break;
                    case 3:
                        slightlyHotCount = getCount(slightlyHotCount);
                        break;
                    case 4:
                        tooHotCount = getCount(tooHotCount);
                        break;
                    default:
                        noDataCount = getCount(noDataCount);
                        break;
                }
            }
        }
        int totalCount = spaceIds.size();

        // 获取该小时对应的温度满足率
        LocalDateTime hourTime = timePoint.withMinute(0).withSecond(0).withNano(0);
        String complianceRate = timeToComplianceRate.getOrDefault(hourTime, "");

        // 计算占比
        BigDecimal tooColdPercentage = calculatePercentage(tooColdCount, totalCount);
        BigDecimal slightlyColdPercentage = calculatePercentage(slightlyColdCount, totalCount);
        BigDecimal comfortablePercentage = calculatePercentage(comfortableCount, totalCount);
        BigDecimal slightlyHotPercentage = calculatePercentage(slightlyHotCount, totalCount);
        BigDecimal tooHotPercentage = calculatePercentage(tooHotCount, totalCount);
        BigDecimal noDataPercentage = calculatePercentage(noDataCount, totalCount);

        return ServiceSpaceTempStatusDTO.builder()
                .timePoint(formatTimePoint(timePoint))
                .systemName(systemName)
                .temperatureSatisfactionRate(parsePercentage(complianceRate))
                .tooColdSpaceCount(tooColdCount)
                .slightlyColdSpaceCount(slightlyColdCount)
                .comfortableSpaceCount(comfortableCount)
                .slightlyHotSpaceCount(slightlyHotCount)
                .tooHotSpaceCount(tooHotCount)
                .noDataSpaceCount(noDataCount)
                .totalSpaceCount(totalCount == 0 ? null : totalCount)
                .tooColdSpacePercentage(tooColdPercentage)
                .slightlyColdSpacePercentage(slightlyColdPercentage)
                .comfortableSpacePercentage(comfortablePercentage)
                .slightlyHotSpacePercentage(slightlyHotPercentage)
                .tooHotSpacePercentage(tooHotPercentage)
                .noDataSpacePercentage(noDataPercentage)
                .build();
    }

    private Integer getCount(Integer count) {
        if (Objects.nonNull(count)) {
            count++;
        } else {
            count = 1;
        }
        return count;
    }

    /**
     * 判断温度状态
     *
     * @param temperature       温度
     * @param underCoolingLimit 过冷门限
     * @param lowerComfortLimit 舒适区下限
     * @param upperComfortLimit 舒适区上限
     * @param overHeatingLimit  过过热门限
     * @return 0:过冷, 1:偏冷, 2:舒适, 3:偏热, 4:过热
     */
    private int judgeTemperatureStatus(
            BigDecimal temperature,
            BigDecimal underCoolingLimit,
            BigDecimal lowerComfortLimit,
            BigDecimal upperComfortLimit,
            BigDecimal overHeatingLimit) {

        if (temperature.compareTo(underCoolingLimit) < 0) {
            return 0; // 过冷
        } else if (temperature.compareTo(lowerComfortLimit) < 0) {
            return 1; // 偏冷
        } else if (temperature.compareTo(upperComfortLimit) <= 0) {
            return 2; // 舒适
        } else if (temperature.compareTo(overHeatingLimit) <= 0) {
            return 3; // 偏热
        } else {
            return 4; // 过热
        }
    }

    /**
     * 获取指定时间的空间温度
     *
     * @param spaceId              空间ID
     * @param timePoint            时间点
     * @param spaceId2DateTime2Tdb 空间T时间到温度映射
     * @return 温度值
     */
    private BigDecimal getTemperatureAtTime(
            String spaceId,
            LocalDateTime timePoint,
            Map<String, Map<LocalDateTime, String>> spaceId2DateTime2Tdb) {

        Map<LocalDateTime, String> dateTime2Tdb = spaceId2DateTime2Tdb.get(spaceId);
        if (MapUtils.isEmpty(dateTime2Tdb)) {
            return null;
        }

        String tdbValue = dateTime2Tdb.get(timePoint);
        if (StringUtils.isBlank(tdbValue)) {
            return null;
        }

        try {
            return new BigDecimal(tdbValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 计算占比
     *
     * @param count 数量
     * @param total 总数
     * @return 百分比，保留2位小数
     */
    private BigDecimal calculatePercentage(Integer count, Integer total) {
        if (Objects.isNull(total) || total == 0 || Objects.isNull(count)) {
            return null;
        }
        return new BigDecimal(count)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(total), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 解析百分比字符串为BigDecimal
     *
     * @param rateStr 百分比字符串，如 "95.00%"
     * @return 百分比值
     */
    private BigDecimal parsePercentage(String rateStr) {
        if (StringUtils.isBlank(rateStr)) {
            return null;
        }
        try {
            // 去掉%号
            String cleanRate = rateStr.replace("%", "").trim();
            return new BigDecimal(cleanRate);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 生成逐15min冷机数据
     */
    private List<Chiller15MinDataDTO> generateChiller15Min(LocalDate date, Long reportId) {
        List<Chiller15MinDataDTO> resultList = Lists.newArrayList();
        // 查询冷站系统运行数据
        CoolingSysRunDTO coolingSysRunDTO = queryChapterDataByReportId(reportId, CoolingSysRunDTO.class);
        if (coolingSysRunDTO == null || CollectionUtils.isEmpty(coolingSysRunDTO.getSystems())) {
            return resultList;
        }
        // 提取系统数据
        List<SystemEnergyDTO> systems = coolingSysRunDTO.getSystems();
        Map<String, String> systemIdToNameMap = systems.stream()
                .collect(Collectors.toMap(
                        e -> e.getSystem().getId(),
                        e -> e.getSystem().getLocalName(),
                        (k1, k2) -> k1
                ));
        // 查询系统对应的设备集合
        Map<String, List<DtpRwdObjectDTO>> systemId2EqObjectsMap = runAnalysisReportCoolingSysRun.querySystemId2LoadRateRelObjects(systemIdToNameMap.keySet());
        // 查询系统中的设备对应的的大小机类型
        Map<String, Map<ChillerTypeSub, Map<String, List<String>>>> systemId2ChillerTypeSubMapMap = Maps.newHashMap();
        systemId2EqObjectsMap.forEach((systemId, eqObjects) -> systemId2ChillerTypeSubMapMap.put(systemId, dtpRwdClientService.queryChillerGroupById(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), systemId, null)));

        // 查询字典
        Map<String, List<InfoCodeDefineModel>> classCodeToFuncid = runAnalysisReportCoolingSysRun.queryClassCode2InfoCodeDefines(systemId2EqObjectsMap.values().stream().flatMap(Collection::stream).map(DtpRwdObjectDTO::getClassCode).distinct().collect(Collectors.toList()));
        Map<String, String> eqIdToClassCode = systemId2EqObjectsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getClassCode));


        // 完善相关信息点iot数据的生成
        // 查询IoT数据
        LocalDateTime startTime = DateUtils.getDayBoundaryByDate(date, 0, Boolean.TRUE);
        LocalDateTime endTime = DateUtils.getDayBoundaryByDate(date, 1, Boolean.TRUE);
        List<InfoCodeValueVO> infoCodeValueList = abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES,
                systemId2EqObjectsMap.values().stream().flatMap(Collection::stream).map(DtpRwdObjectDTO::getId).distinct().collect(Collectors.toList()),
                Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode(), InfoCodeEnum.I_PLR.getCode()),
                startTime,
                endTime
        );
        // 将IoT数据转换为方便查询的Map结构
        Map<String, Map<String, Map<LocalDateTime, Object>>> systemId2InfoCode2Time2Value =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueList);

        // 生成所有时间点
        List<LocalDateTime> timePoints = generate15MinTimePoints(date);

        // 遍历每个系统
        systemIdToNameMap.forEach((systemId, systemName) -> {
            if (systemId2EqObjectsMap.containsKey(systemId)) {
                List<DtpRwdObjectDTO> dtpRwdObjectDTOS = systemId2EqObjectsMap.get(systemId);
                // 获取该系统的大小机类型映射
                Map<ChillerTypeSub, Map<String, List<String>>> chillerTypeSubMapMap = systemId2ChillerTypeSubMapMap.get(systemId);

                // 遍历该系统的每个设备
                for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                    // 获取设备的大小机类型
                    String equipmentSizeType = getEquipmentSizeType(dtpRwdObjectDTO.getId(), chillerTypeSubMapMap);

                    // 遍历所有时间点
                    for (LocalDateTime timePoint : timePoints) {
                        String timePointStr = formatTimePoint(timePoint);

                        // 获取运行状态
                        Object runStatusObj = systemId2InfoCode2Time2Value
                                .getOrDefault(dtpRwdObjectDTO.getId(), Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.RUN_STATUS.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        String runStatus = runStatusObj != null ? runStatusObj.toString() : null;
                        // 根据classCodeToFuncid 中得字典翻译，比如：runStatus=0 翻译成关,根据dataSources翻译
                        String runStatusValue = classCodeToFuncid.getOrDefault(eqIdToClassCode.get(dtpRwdObjectDTO.getId()), Collections.emptyList())
                                .stream()
                                .filter(e -> e.getCode().equals(InfoCodeEnum.RUN_STATUS.getCode()))
                                .map(InfoCodeDefineModel::getDataSources)
                                .findFirst()
                                .orElse(Lists.newArrayList()).stream().filter(e -> e.getCode().equals(runStatus)).findFirst().orElse(new InfoCodeDataSourceDefineModel()).getName();

                        // 获取运行负载率
                        Object iPLRObj = systemId2InfoCode2Time2Value
                                .getOrDefault(dtpRwdObjectDTO.getId(), Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.I_PLR.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        BigDecimal iPLR = iPLRObj != null ? roundToTwoDecimal(new BigDecimal(iPLRObj.toString())) : null;

                        Chiller15MinDataDTO chiller15MinDataDTO = Chiller15MinDataDTO.builder()
                                .timePoint(timePointStr)
                                .systemName(systemName)
                                .equipmentName(dtpRwdObjectDTO.getLocalName())
                                .runStatus(runStatusValue)
                                .iPLR(iPLR)
                                .equipmentSizeType(equipmentSizeType)
                                .build();

                        resultList.add(chiller15MinDataDTO);
                    }
                }
            } else {
                resultList.add(Chiller15MinDataDTO.builder()
                        .systemName(systemName)
                        .build());
            }
        });
        // 处理多中机逻辑
        List<String> equipmentSizeTypes = resultList.stream().filter(e -> e.getEquipmentSizeType() != null)
                .filter(
                        e ->
                                e.getEquipmentSizeType().equalsIgnoreCase(ChillerSize.M1.getCode())
                                        || e.getEquipmentSizeType().equalsIgnoreCase(ChillerSize.M2.getCode())
                                        || e.getEquipmentSizeType().equalsIgnoreCase(ChillerSize.M3.getCode())
                ).map(Chiller15MinDataDTO::getEquipmentSizeType)
                .distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(equipmentSizeTypes) && equipmentSizeTypes.size() > 1) {
            // 处理中得情况
            resultList.forEach(e -> {
                String equipmentSizeType = e.getEquipmentSizeType();
                if (StringUtils.isNotEmpty(equipmentSizeType)) {
                    ChillerSize chillerSize = ChillerSize.getByCode(equipmentSizeType);
                    if (Objects.nonNull(chillerSize) && equipmentSizeTypes.contains(equipmentSizeType)) {
                        switch (chillerSize) {
                            case M1:
                                e.setEquipmentSizeType(chillerSize.getName() + "1机");
                                break;
                            case M2:
                                e.setEquipmentSizeType(chillerSize.getName() + "2机");
                                break;
                            case M3:
                                e.setEquipmentSizeType(chillerSize.getName() + "3机");
                                break;
                        }
                    } else if (Objects.nonNull(chillerSize)) {
                        e.setEquipmentSizeType(chillerSize.getName() + "机");
                    }
                }
            });
        } else {
            resultList.forEach(e -> {
                String equipmentSizeType = e.getEquipmentSizeType();
                if (StringUtils.isNotEmpty(equipmentSizeType)) {
                    ChillerSize chillerSize = ChillerSize.getByCode(equipmentSizeType);
                    if (Objects.nonNull(chillerSize)) {
                        e.setEquipmentSizeType(chillerSize.getName() + "机");
                    }
                }

            });
        }

        return resultList;
    }

    /**
     * 根据设备 ID 获取大小机类型
     *
     * @param eqId                 设备 ID
     * @param chillerTypeSubMapMap 冷机类型映射
     * @return 大小机类型字符串
     */
    private String getEquipmentSizeType(String eqId, Map<ChillerTypeSub, Map<String, List<String>>> chillerTypeSubMapMap) {
        if (chillerTypeSubMapMap == null || chillerTypeSubMapMap.isEmpty()) {
            return null;
        }

        // 遍历冷机类型，查找设备所在的类型
        for (Map.Entry<ChillerTypeSub, Map<String, List<String>>> entry : chillerTypeSubMapMap.entrySet()) {
            ChillerTypeSub chillerTypeSub = entry.getKey();
            Map<String, List<String>> typeMap = entry.getValue();

            // 遍历类型下的设备组
            for (String type : typeMap.keySet()) {
                List<String> eqIds = typeMap.get(type);
                if (eqIds != null && eqIds.contains(eqId)) {
                    // 根据枚举返回对应的中文名称
                    return type;
                }
            }
        }

        return null;
    }

    /**
     * 生成逐 15min 冷却塔风机数据
     */
    private List<CoolingTowerFan15MinDataDTO> generateCoolingTowerFan15Min(LocalDate date, Long reportId) {
        List<CoolingTowerFan15MinDataDTO> resultList = Lists.newArrayList();
        // 查询冷站系统运行数据
        CoolingSysRunDTO coolingSysRunDTO = queryChapterDataByReportId(reportId, CoolingSysRunDTO.class);
        if (coolingSysRunDTO == null || CollectionUtils.isEmpty(coolingSysRunDTO.getSystems())) {
            return resultList;
        }
        // 提取系统数据
        List<SystemEnergyDTO> systems = coolingSysRunDTO.getSystems();
        Map<String, String> systemIdToNameMap = systems.stream()
                .collect(Collectors.toMap(
                        e -> e.getSystem().getId(),
                        e -> e.getSystem().getLocalName(),
                        (k1, k2) -> k1
                ));
        // 查询系统对应的冷却塔风机设备集合
        Map<String, List<DtpRwdObjectDTO>> systemId2EqObjectsMap = runAnalysisReportCoolingSysRun.querySystemId2LoadRateRelObjects(systemIdToNameMap.keySet(), Lists.newArrayList(DtpRwdClassCodeEnum.ACCCOF.getValue()));

        // 查询字典 - 用于翻译运行状态和运行档位
        Map<String, List<InfoCodeDefineModel>> classCodeToFuncid = runAnalysisReportCoolingSysRun.queryClassCode2InfoCodeDefines(systemId2EqObjectsMap.values().stream().flatMap(Collection::stream).map(DtpRwdObjectDTO::getClassCode).distinct().collect(Collectors.toList()));
        Map<String, String> eqIdToClassCode = systemId2EqObjectsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getClassCode));

        // 查询 IoT 数据 - 冷却塔风机的信息点：runStatus(运行状态), freq(运行频率), equipFreqSet(运行频率设定), fanGear(运行档位), equipGearSet(运行档位设定)
        LocalDateTime startTime = DateUtils.getDayBoundaryByDate(date, 0, Boolean.TRUE);
        LocalDateTime endTime = DateUtils.getDayBoundaryByDate(date, 1, Boolean.TRUE);
        List<InfoCodeValueVO> infoCodeValueList = abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES,
                systemId2EqObjectsMap.values().stream().flatMap(Collection::stream).map(DtpRwdObjectDTO::getId).distinct().collect(Collectors.toList()),
                Lists.newArrayList(
                        InfoCodeEnum.RUN_STATUS.getCode(),      // 运行状态
                        InfoCodeEnum.FREQ.getCode(),       // 运行频率（freq）
                        InfoCodeEnum.EQUIP_FREQ_SET.getCode(),  // 运行频率设定
                        InfoCodeEnum.FAN_GEAR.getCode(),  // 运行档位（需要从字典中查找对应的编码）
                        InfoCodeEnum.EQUIP_GEAR_SET.getCode()   // 运行档位设定
                ),
                startTime,
                endTime
        );
        // 将 IoT 数据转换为方便查询的 Map 结构：objectId -> infoCode -> time -> value
        Map<String, Map<String, Map<LocalDateTime, Object>>> objectId2InfoCode2Time2Value =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueList);

        // 生成所有时间点
        List<LocalDateTime> timePoints = generate15MinTimePoints(date);

        // 遍历每个系统
        systemIdToNameMap.forEach((systemId, systemName) -> {
            if (systemId2EqObjectsMap.containsKey(systemId)) {
                List<DtpRwdObjectDTO> dtpRwdObjectDTOS = systemId2EqObjectsMap.get(systemId);

                // 遍历该系统的每个设备
                for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
                    String eqId = dtpRwdObjectDTO.getId();
                    String classCode = eqIdToClassCode.get(eqId);
                    List<InfoCodeDefineModel> infoCodeDefines = classCodeToFuncid.getOrDefault(classCode, Collections.emptyList());

                    // 遍历所有时间点
                    for (LocalDateTime timePoint : timePoints) {
                        String timePointStr = formatTimePoint(timePoint);

                        // 获取运行状态并翻译
                        Object runStatusObj = objectId2InfoCode2Time2Value
                                .getOrDefault(eqId, Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.RUN_STATUS.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        String runStatus = runStatusObj != null ? runStatusObj.toString() : null;
                        // 根据 classCodeToFuncid 中的字典翻译运行状态
                        String runStatusValue = translateInfoCodeValue(runStatus, infoCodeDefines, InfoCodeEnum.RUN_STATUS.getCode());

                        // 获取运行频率
                        Object freqObj = objectId2InfoCode2Time2Value
                                .getOrDefault(eqId, Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.FREQ.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        BigDecimal freq = freqObj != null ? roundToTwoDecimal(new BigDecimal(freqObj.toString())) : null;

                        // 获取运行频率设定
                        Object equipFreqSetObj = objectId2InfoCode2Time2Value
                                .getOrDefault(eqId, Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.EQUIP_FREQ_SET.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        BigDecimal equipFreqSet = equipFreqSetObj != null ? roundToTwoDecimal(new BigDecimal(equipFreqSetObj.toString())) : null;

                        // 获取运行档位并翻译
                        Object fanGearObj = objectId2InfoCode2Time2Value
                                .getOrDefault(eqId, Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.FAN_GEAR.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        String fanGear = fanGearObj != null ? fanGearObj.toString() : null;
                        String fanGearValue = translateInfoCodeValue(fanGear, infoCodeDefines, InfoCodeEnum.FAN_GEAR.getCode());

                        // 获取运行档位设定并翻译
                        Object equipGearSetObj = objectId2InfoCode2Time2Value
                                .getOrDefault(eqId, Maps.newHashMap())
                                .getOrDefault(InfoCodeEnum.EQUIP_GEAR_SET.getCode(), Maps.newHashMap())
                                .get(timePoint);
                        String equipGearSet = equipGearSetObj != null ? equipGearSetObj.toString() : null;
                        String equipGearSetValue = translateInfoCodeValue(equipGearSet, infoCodeDefines, InfoCodeEnum.EQUIP_GEAR_SET.getCode());

                        CoolingTowerFan15MinDataDTO dto = CoolingTowerFan15MinDataDTO.builder()
                                .timePoint(timePointStr)
                                .systemName(systemName)
                                .equipmentName(dtpRwdObjectDTO.getLocalName())
                                .runStatus(runStatusValue)
                                .freq(roundToTwoDecimal(freq))
                                .equipFreqSet(roundToTwoDecimal(equipFreqSet))
                                .fanGear(fanGearValue)
                                .equipGearSet(equipGearSetValue)
                                .build();

                        resultList.add(dto);
                    }
                }
            } else {
                // 系统下没有设备，添加空数据
                resultList.add(CoolingTowerFan15MinDataDTO.builder()
                        .systemName(systemName)
                        .build());
            }
        });

        return resultList;
    }

    /**
     * 根据字典翻译信息点值
     *
     * @param value           原始值
     * @param infoCodeDefines 信息点定义列表
     * @param infoCode        信息点编码
     * @return 翻译后的值
     */
    private String translateInfoCodeValue(String value, List<InfoCodeDefineModel> infoCodeDefines, String infoCode) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        InfoCodeDefineModel infoCodeDefine = infoCodeDefines.stream()
                .filter(e -> e.getCode().equals(infoCode))
                .findFirst()
                .orElse(null);

        if (infoCodeDefine == null || CollectionUtils.isEmpty(infoCodeDefine.getDataSources())) {
            return value;
        }

        return infoCodeDefine.getDataSources().stream()
                .filter(e -> e.getCode().equals(value))
                .findFirst()
                .map(InfoCodeDataSourceDefineModel::getName)
                .orElse(value);
    }

    /**
     * 生成室外天气数据
     */
    private List<OutdoorWeatherDataDTO> generateOutdoorWeather(LocalDate date) {
        List<OutdoorWeatherDataDTO> resultList = Lists.newArrayList();

        // 查询天气，由于湿球温度、焓值 后开发需要重新查询
        WeatherDTO weatherDTO = runAnalysisReportRunOverview.queryWeather(date);

        // 获取天气详情列表（15 分钟精度数据）
        List<WeatherDTO.Detail> details;
        if (CollectionUtils.isNotEmpty(weatherDTO.getOriginalStationDetails())) {
            details = weatherDTO.getOriginalStationDetails();
            if (CollectionUtils.isEmpty(details)) {
                return resultList;
            }
            // 生成每个时间点
            // 生成所有时间点
            List<LocalDateTime> timePoints = generate15MinTimePoints(date);

            // 构建小时到天气详情的映射（小时精度数据）
            Map<LocalDateTime, WeatherDTO.Detail> hourToDetailMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(details)) {
                for (WeatherDTO.Detail detail : details) {
                    if (detail != null && detail.getTime() != null) {
                        hourToDetailMap.put(detail.getTime(), detail);
                    }
                }
            }
            // 遍历每个时间点，组装数据
            for (LocalDateTime timePoint : timePoints) {
                // 获取该时间点对应的小时数据
                WeatherDTO.Detail hourDetail = hourToDetailMap.get(timePoint);
                OutdoorWeatherDataDTO dto = OutdoorWeatherDataDTO.builder()
                        .timePoint(formatTimePoint(timePoint))
                        .temperature(hourDetail != null ? roundToTwoDecimal(hourDetail.getTemperature()) : null)
                        .humidity(hourDetail != null ? roundToTwoDecimal(hourDetail.getHumidity()) : null)
                        .wetBulbTemp(hourDetail != null ? roundToTwoDecimal(hourDetail.getWetBulbTemp()) : null)
                        .enthalpy(hourDetail != null ? roundToTwoDecimal(hourDetail.getEnthalpyValue()) : null)
                        .build();

                resultList.add(dto);
            }
        } else {
            details = weatherDTO.getDetails();
            if (CollectionUtils.isEmpty(details)) {
                return resultList;
            }
            // 生成每个时间点
            // 生成所有时间点
            List<LocalDateTime> timePoints = generate15MinTimePoints(date);

            // 构建小时到天气详情的映射（小时精度数据）
            Map<Integer, WeatherDTO.Detail> hourToDetailMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(details)) {
                for (WeatherDTO.Detail detail : details) {
                    if (detail != null && detail.getTime() != null) {
                        hourToDetailMap.put(detail.getTime().getHour(), detail);
                    }
                }
            }
            // 遍历每个时间点，组装数据
            for (LocalDateTime timePoint : timePoints) {
                // 获取该时间点对应的小时数据
                Integer hour = timePoint.getHour();
                WeatherDTO.Detail hourDetail = hourToDetailMap.get(hour);
                OutdoorWeatherDataDTO dto = OutdoorWeatherDataDTO.builder()
                        .timePoint(formatTimePoint(timePoint))
                        .temperature(hourDetail != null ? roundToTwoDecimal(hourDetail.getTemperature()) : null)
                        .humidity(hourDetail != null ? roundToTwoDecimal(hourDetail.getHumidity()) : null)
                        .wetBulbTemp(hourDetail != null ? roundToTwoDecimal(hourDetail.getWetBulbTemp()) : null)
                        .enthalpy(hourDetail != null ? roundToTwoDecimal(hourDetail.getEnthalpyValue()) : null)
                        .build();

                resultList.add(dto);
            }
        }


        return resultList;
    }

    @Override
    public String logTag() {
        return "日报下载数据组装";
    }
}
