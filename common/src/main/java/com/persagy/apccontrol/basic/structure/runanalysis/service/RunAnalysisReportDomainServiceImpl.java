package com.persagy.apccontrol.basic.structure.runanalysis.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.model.base.YesNoEnum;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.runanalysis.download.ProjectBasicInfoDTO;
import com.persagy.apccontrol.basic.model.dto.runanalysis.download.RunAnalysisReportDownloadDTO;
import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigGroupEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigSceneEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportStatusEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportChapterDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.RunAnalysisReportRemarkRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.repository.RunAnalysisReportRepository;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import com.persagy.apccontrol.basic.structure.runanalysis.util.RunAnalysisReportCsvExportUtil;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportDomainServiceImpl implements RunAnalysisReportDomainService {

    private final ApplicationContext applicationContext;
    private final RunAnalysisReportRepository runAnalysisReportRepository;
    private final RunAnalysisReportRemarkRepository runAnalysisReportRemarkRepository;
    private final ShellConfigService shellConfigService;
    @Resource
    private RedissonDistributedLockerService redissonDistributedLockerService;

    private static final String UNCHECK = "uncheck";

    private final RunAnalysisReportDownloadService runAnalysisReportDownloadService;
    @Override
    public String logTag() {
        return "运行分析-日报";
    }

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public Boolean build(@NonNull RunAnalysisReportBuildParam param) {
        String redisLockKey = getRedisLockKey(param);
        if (!redissonDistributedLockerService.tryLock(redisLockKey, TimeUnit.SECONDS, 0, -1)) {
            logProjectInstance().info("运行分析-日报-构建失败，正在构建中");
            return false;
        }

        try {
            return buildReport(param);
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-构建失败", e, Pair.of("report", param));
            return false;
        } finally {
            redissonDistributedLockerService.unlock(redisLockKey);
        }
    }

    /**
     * 构建运行分析报告
     *
     * @param param 报告构建参数
     * @return 构建是否成功
     */
    public Boolean buildReport(@NonNull RunAnalysisReportBuildParam param) throws ExecutionException, InterruptedException {
        logProjectInstance().info("运行分析-日报-开始构建", Pair.of("report", param));

        // 查询旧报告列表，记录ID用于复用
        RunAnalysisReport queryReport = new RunAnalysisReport();
        queryReport.setReportDate(param.getReportDate());
        queryReport.setReportType(param.getReportType());
        List<RunAnalysisReport> oldReports = runAnalysisReportRepository.getListByDayAndType(queryReport);
        
        // 建立旧报告ID映射：key为 chapter + objectId，value为旧报告ID
        Map<String, Long> oldReportIdMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(oldReports)) {
            for (RunAnalysisReport oldReport : oldReports) {
                String key = buildReportKey(oldReport.getReportChapter(), oldReport.getObjectId());
                oldReportIdMap.put(key, oldReport.getId());
            }
            logProjectInstance().info("运行分析-日报-记录旧报告ID", Pair.of("oldReportIdMap", oldReportIdMap));
        }

        //删除旧报告
        runAnalysisReportRepository.deleteByDay(param);

        // 1. 获取策略集合
        Collection<RunAnalysisReportStrategy> strategies = loadStrategies();
        if (CollectionUtil.isEmpty(strategies)) {
            logProjectInstance().info("运行分析-日报-构建失败，没有找到策略实现");
            return false;
        }

        // 2. 加载章节配置
        Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> configChapters = loadChapterConfigs();
        if (configChapters == null || configChapters.isEmpty()) {
            logProjectInstance().info("运行分析-日报-构建失败，没有章节壳配置");
            return false;
        }
        logProjectInstance().info("运行分析-日报-加载章节配置数量", Pair.of("count", configChapters.size()));

        for (RunAnalysisReportStrategy strategy : strategies) {
            if (strategy == null) {
                continue;
            }

            List<RunAnalysisReportChapterEnum> reportChapterList = param.getChapterList();
            if (CollectionUtil.isEmpty(reportChapterList) || reportChapterList.contains(strategy.getChapter())) {
                if (strategy.isSupport(param.getReportType())) {
                    try {
                        executeStrategyBuild(param, strategy, configChapters, oldReportIdMap);
                    } catch (Exception e) {
                        logProjectInstance().error("运行分析-日报-构建失败", e, Pair.of("report", param));
                    }
                }
            }
        }
        try {
            // 3. 所有章节构建完成后，生成CSV并发送到Dify
            runAnalysisReportDownloadService.sendCsvToDify(param.getReportDate(), param.getReportType());
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-CSV生成发送失败", e);
        }
        return true;
    }


    /**
     * 加载所有策略实现
     *
     * @return 策略集合
     */
    private Collection<RunAnalysisReportStrategy> loadStrategies() {
        try {
            Map<String, RunAnalysisReportStrategy> strategyBeans =
                    applicationContext.getBeansOfType(RunAnalysisReportStrategy.class);
            return strategyBeans != null ? strategyBeans.values() : Collections.emptyList();
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-加载策略失败", e);
            return Collections.emptyList();
        }
    }

    private Map<RunAnalysisReportChapterEnum, RunAnalysisReportStrategy> loadStrategyMap() {
        return loadStrategies().stream()
                .collect(Collectors.toMap(RunAnalysisReportStrategy::getChapter, strategy -> strategy));
    }

    /**
     * 加载章节配置
     *
     * @return 章节配置映射
     */
    private Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> loadChapterConfigs() {
        try {
            List<ShellConfigVO> shellConfigs = queryShellConfig(
                    Sets.newHashSet(ShellConfigGroupEnum.GROUP_REPORT_CHAPTER.getCode()));

            if (CollectionUtil.isEmpty(shellConfigs)) {
                return Collections.emptyMap();
            }

            return getConfigChapters(shellConfigs);
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-加载章节配置失败", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 执行策略构建
     *
     * @param param          构建参数
     * @param strategy       策略实现
     * @param configChapters 章节配置
     * @param oldReportIdMap 旧报告ID映射
     * @return 是否成功
     */
    public boolean executeStrategyBuild(
            @NonNull RunAnalysisReportBuildParam param,
            @NonNull RunAnalysisReportStrategy strategy,
            @NonNull Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> configChapters,
            @NonNull Map<String, Long> oldReportIdMap) throws ExecutionException, InterruptedException {

        RunAnalysisReportChapterEnum chapter = strategy.getChapter();
        if (chapter == null) {
            logProjectInstance().info("运行分析-日报-策略章节为空",
                    Pair.of("strategy", strategy.getClass().getSimpleName()));
            return false;
        }

        if (!configChapters.containsKey(chapter)) {
            logProjectInstance().info("运行分析-日报-章节配置不存在",
                    Pair.of("chapter", chapter.getValue()));
            return false;
        }

        try {
            logProjectInstance().info("运行分析-日报-开始构建章节",
                    Pair.of("chapter", chapter.getValue()));

            // 执行策略构建
            List<RunAnalysisReportConfig> configs = configChapters.get(chapter);
            RunAnalysisReportDataDto storeDto = new RunAnalysisReportDataDto();
            for (RunAnalysisReportConfig config : configs) {
                param.setObjectId(config.getObjectId());
                saveOrUpdateReport(param, strategy.getChapter(), storeDto, config, RunAnalysisReportStatusEnum.BUILDING, oldReportIdMap);
                try{
                    storeDto = strategy.buildWithConfig(param, config, configChapters);
                    saveOrUpdateReport(param, strategy.getChapter(), storeDto, config, RunAnalysisReportStatusEnum.CONFIRM, oldReportIdMap);
                }catch (Exception e){
                    logProjectInstance().error("运行分析-日报-构建失败", e, Pair.of("report", param));
                    saveOrUpdateReport(param, strategy.getChapter(), storeDto, config, RunAnalysisReportStatusEnum.FAILURE, oldReportIdMap);
                }

            }

            logProjectInstance().info("运行分析-日报-章节构建成功",
                    Pair.of("chapter", chapter.getValue()));
            return true;

        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-章节构建失败", e,
                    Arrays.asList(Pair.of("chapter", chapter.getValue()),
                            Pair.of("strategy", strategy.getClass().getSimpleName())));
            throw e;
        }
    }

    /**
     * 保存或更新报告
     *
     * @param param          构建参数
     * @param chapter        章节
     * @param storeDto       存储数据
     * @param config         章节配置
     * @param status         报告状态
     * @param oldReportIdMap 旧报告ID映射
     */
    public void saveOrUpdateReport(
            @NonNull RunAnalysisReportBuildParam param,
            @NonNull RunAnalysisReportChapterEnum chapter,
            RunAnalysisReportDataDto storeDto,
            @NonNull RunAnalysisReportConfig config,
            RunAnalysisReportStatusEnum status,
            @NonNull Map<String, Long> oldReportIdMap) {

        // 构建报告实体
        RunAnalysisReport reportEntity = buildReportEntity(param, chapter, storeDto, config, status);

        // 查询是否已存在
        RunAnalysisReport existingReport = runAnalysisReportRepository.getByDayAndTypeAndChapter(reportEntity);

        if (existingReport != null) {
            // 更新已有记录
            reportEntity.setId(existingReport.getId());
            runAnalysisReportRepository.updateById(reportEntity);
            logProjectInstance().info("运行分析-日报-更新报告",
                    Arrays.asList(Pair.of("chapter", chapter.getValue()),
                            Pair.of("reportId", existingReport.getId()),
                            Pair.of("time", reportEntity.getUpdateTime()),
                            Pair.of("status", status.getValue())));
        } else {
            // 尝试复用旧报告ID
            String key = buildReportKey(chapter, param.getObjectId());
            Long oldReportId = oldReportIdMap.get(key);
            if (oldReportId != null) {
                reportEntity.setId(oldReportId);
            }
                // 创建新记录
            runAnalysisReportRepository.save(reportEntity);
            logProjectInstance().info("运行分析-日报-创建报告",
                    Arrays.asList(Pair.of("chapter", chapter.getValue()),
                            Pair.of("time", reportEntity.getCreateTime()),
                            Pair.of("status", status.getValue())));
        }
    }

    /**
     * 构建报告实体对象
     *
     * @param param    构建参数
     * @param chapter  章节
     * @param storeDto 存储数据
     * @return 报告实体
     */
    private RunAnalysisReport buildReportEntity(
            @NonNull RunAnalysisReportBuildParam param,
            @NonNull RunAnalysisReportChapterEnum chapter,
            RunAnalysisReportDataDto storeDto,
            @NonNull RunAnalysisReportConfig config,
            RunAnalysisReportStatusEnum status) {

        RunAnalysisReport report = new RunAnalysisReport();
        report.setReportDate(param.getReportDate());
        report.setReportType(param.getReportType());
        report.setReportStatus(status);
        report.setReportChapter(chapter);
        report.setObjectId(param.getObjectId());
        report.setProjectId(ContextUtil.getProjectId());
        report.setChapterData(ObjectMapperUtils.writeValueAsStringFailSafe(storeDto));
        report.setChapterConfig(ObjectMapperUtils.writeValueAsStringFailSafe(config));
        report.setOperator(param.getOperator());

        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean buildAuto() {
        LocalDate buildDay = getBuildDay();
        if (buildDay == null) {
            return false;
        }

        RunAnalysisReportBuildParam param = new RunAnalysisReportBuildParam();
        param.setReportDate(buildDay);
        param.setReportType(RunAnalysisReportTypeEnum.DAILY);
        param.setReportStatus(RunAnalysisReportStatusEnum.CONFIRM);

        build(param);
        return null;
    }

    @Override
    public List<RunAnalysisReport> getList(RunAnalysisReportListParam param) {
        return runAnalysisReportRepository.getList(param);
    }

    @Override
    public List<RunAnalysisReport> getList(List<Long> idList) {
        List<RunAnalysisReport> runAnalysisReports = runAnalysisReportRepository.listByIds(idList);
        return Collections.emptyList();
    }

    @Override
    public List<RunAnalysisReportChapterDto> getChapterList(RunAnalysisReportQueryParam param) {
        List<RunAnalysisReport> chapterList = runAnalysisReportRepository.getChapterList(param);
        if (CollectionUtil.isEmpty(chapterList)) {
            logProjectInstance().info("运行分析-日报-获取失败，没有章节列表");
            return Collections.emptyList();
        }

        List<RunAnalysisReportChapterDto> chapters = new ArrayList<>();

        Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> configChapters = parseChapterConfigurations(chapterList);

        for (Map.Entry<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> entry : configChapters.entrySet()) {
            RunAnalysisReportChapterEnum chapter = entry.getKey();
            List<RunAnalysisReportConfig> configs = entry.getValue();

            for (RunAnalysisReportConfig config : configs) {
                // 添加空值检查和安全访问
                if (config != null) {
                    RunAnalysisReportChapterDto chapterDto = new RunAnalysisReportChapterDto();
                    chapterDto.setName(config.getName());
                    chapterDto.setSort(config.getSort());
                    chapterDto.setObjectId(config.getObjectId());
                    chapterDto.setCode(chapter.getValue());
                    chapterDto.setParentCode(config.getParent());
                    chapterDto.setId(config.getId());
                    chapters.add(chapterDto);
                }
            }
        }

        return chapters;
    }

    @Override
    public List<RunAnalysisReport> getDetail(RunAnalysisReportQueryParam param) {
        // 参数校验
        if (param == null) {
            logProjectInstance().warn("运行分析-日报-获取详情失败，参数为空");
            return Collections.emptyList();
        }

        List<RunAnalysisReportChapterParam> reportChapters = param.getReportChapters();
        if (CollectionUtil.isEmpty(reportChapters)) {
            logProjectInstance().info("运行分析-日报-获取详情失败，报告章节为空");
            return Collections.emptyList();
        }

        // 加载策略映射
        Map<RunAnalysisReportChapterEnum, RunAnalysisReportStrategy> strategyMap = loadStrategyMap();
        if (strategyMap == null || strategyMap.isEmpty()) {
            logProjectInstance().info("运行分析-日报-获取详情失败，策略映射为空");
            return Collections.emptyList();
        }

        List<RunAnalysisReport> reports = new ArrayList<>();

        // 顺序处理每个报告章节
        for (RunAnalysisReportChapterParam reportChapter : reportChapters) {
            if (reportChapter == null) {
                logProjectInstance().warn("运行分析-日报-报告章节参数为空，跳过处理");
                continue;
            }

            RunAnalysisReport queryParam = buildQueryParameter(param, reportChapter);
            RunAnalysisReport runAnalysisReport = runAnalysisReportRepository.getByDayAndTypeAndChapter(queryParam);
            if (runAnalysisReport != null) {
                RunAnalysisReportStrategy runAnalysisReportStrategy = strategyMap.get(queryParam.getReportChapter());
                if (runAnalysisReportStrategy != null) {
                    runAnalysisReportStrategy.processChapterData(runAnalysisReport);
                    // 获取备注
                    List<RunAnalysisReportRemark> remarkList = runAnalysisReportRemarkRepository.getRemarkList(Collections.singletonList(runAnalysisReport.getId()));
                    if (CollectionUtil.isNotEmpty(remarkList)) {
                        runAnalysisReport.setRemark(remarkList.get(0).getRemark());
                    }
                    reports.add(runAnalysisReport);
                } else {
                    logProjectInstance().warn("运行分析-日报-未找到对应章节策略",
                            Pair.of("chapter", queryParam.getReportChapter().getValue()));
                }
            } else {
                logProjectInstance().info("运行分析-日报-未找到对应报告数据",
                        Arrays.asList(Pair.of("chapter", queryParam.getReportChapter().getValue()),
                                Pair.of("objectId", reportChapter.getObjectId())));
            }
        }
        return reports;
    }

    @Override
    public List<RunAnalysisReport> getDetail(List<Long> idList) {
        List<RunAnalysisReport> runAnalysisReports = runAnalysisReportRepository.listByIds(idList);
        if (CollectionUtil.isEmpty(runAnalysisReports)) {
            logProjectInstance().info("运行分析-日报-获取详情失败，报告章节为空");
            return Collections.emptyList();
        }

        // 加载策略映射
        Map<RunAnalysisReportChapterEnum, RunAnalysisReportStrategy> strategyMap = loadStrategyMap();
        if (strategyMap == null || strategyMap.isEmpty()) {
            logProjectInstance().info("运行分析-日报-获取详情失败，策略映射为空");
            return Collections.emptyList();
        }

        List<RunAnalysisReport> reports = new ArrayList<>();

        for (RunAnalysisReport runAnalysisReport : runAnalysisReports) {
            if (runAnalysisReport != null) {
                RunAnalysisReportStrategy runAnalysisReportStrategy = strategyMap.get(runAnalysisReport.getReportChapter());
                if (runAnalysisReportStrategy != null) {
                    runAnalysisReportStrategy.processChapterData(runAnalysisReport);
                    // 获取备注
                    List<RunAnalysisReportRemark> remarkList = runAnalysisReportRemarkRepository.getRemarkList(Collections.singletonList(runAnalysisReport.getId()));
                    if (CollectionUtil.isNotEmpty(remarkList)) {
                        runAnalysisReport.setRemark(remarkList.get(0).getRemark());
                    }
                    reports.add(runAnalysisReport);
                } else {
                    logProjectInstance().warn("运行分析-日报-未找到对应章节策略",
                            Pair.of("chapter", runAnalysisReport.getReportChapter().getValue()));
                }
            } else {
                logProjectInstance().info("运行分析-日报-未找到对应报告数据",
                        Arrays.asList(Pair.of("chapter", runAnalysisReport.getReportChapter().getValue()),
                                Pair.of("objectId", runAnalysisReport.getObjectId())));
            }
        }
        return reports;
    }

    @Override
    public Boolean saveRemark(List<RunAnalysisReportRemarkParam> param) {
        List<RunAnalysisReportRemark> remarkList = new ArrayList<>();
        for (RunAnalysisReportRemarkParam remarkParam : param) {
            // 先根据 reportId 和 objectId 查询是否存在记录
            RunAnalysisReportRemark existingRemark = runAnalysisReportRemarkRepository
                    .getByReport(remarkParam);

            RunAnalysisReportRemark remark = new RunAnalysisReportRemark();
            remark.setReportId(remarkParam.getReportId());
            remark.setObjectId(remarkParam.getObjectId());
            remark.setCustomKey(remarkParam.getCustomKey());
            remark.setRemark(remarkParam.getRemark());
            remark.setProjectId(ContextUtil.getProjectId());
            if (existingRemark != null) {
                remark.setId(existingRemark.getId());
            }
            remarkList.add(remark);
        }
        return runAnalysisReportRemarkRepository.saveOrUpdateBatch(remarkList);
    }

    @Override
    public List<RunAnalysisReportRemark> getRemarkList(@NonNull List<Long> reportIdList) {
        return runAnalysisReportRemarkRepository.getRemarkList(reportIdList);
    }

    private List<ShellConfigVO> queryShellConfig(Set<String> groups) {
        QueryShellConfigParam queryShellConfigParam = new QueryShellConfigParam();
        queryShellConfigParam.setScene(ShellConfigSceneEnum.SCENE_REPORT.getCode());
        queryShellConfigParam.setProjectId(ContextUtil.getProjectId());
        queryShellConfigParam.setGroups(groups);
        return shellConfigService.query(ContextUtil.getGroupCode(), queryShellConfigParam);
    }

    private LocalDate getBuildDay() {
        // 1. 获取壳配置
        Map<String, String> shellConfigMap = getShellConfigMap();
        if (shellConfigMap == null) {
            return null;
        }

        // 2. 检查开关是否开启
        if (!isBuildSwitchEnabled(shellConfigMap)) {
            logProjectInstance().info("运行分析-日报-构建失败，时间开关未开启");
            return null;
        }

        LocalDate buildDay = calculateBuildDay(shellConfigMap);

        // 3. 检查当前时间是否在允许的时间范围内
        if (!isInValidTimeRange(shellConfigMap, buildDay)) {
            logProjectInstance().info("运行分析-日报-构建失败，不在时间范围内");
            return null;
        }

        // 4. 根据时间配置计算应构建哪一天的数据
        return buildDay;
    }

    /**
     * 获取壳配置映射
     *
     * @return 壳配置映射，如果获取失败则返回null
     */
    private Map<String, String> getShellConfigMap() {
        List<ShellConfigVO> shellConfigVOS = queryShellConfig(Sets.newHashSet(ShellConfigGroupEnum.GROUP_REPORT_BUILD.getCode()));
        if (CollectionUtil.isEmpty(shellConfigVOS)) {
            logProjectInstance().info("运行分析-日报-构建失败，没有时间壳配置");
            return null;
        }

        return shellConfigVOS.stream()
                .flatMap(shellConfig -> shellConfig.getGroups().stream())
                .flatMap(group -> group.getItems().stream())
                .filter(item -> item.getValue() != null)
                .collect(
                        Collectors.toMap(
                                item -> item.getCode(),
                                item -> item.getValue(),
                                (existing, replacement) -> replacement, // 如果有重复key，使用新值
                                HashMap::new
                        ));
    }

    /**
     * 检查构建开关是否启用
     *
     * @param shellConfigMap 壳配置映射
     * @return 开关是否启用
     */
    private boolean isBuildSwitchEnabled(Map<String, String> shellConfigMap) {
        String switchConfig = shellConfigMap.get(ShellConfigItemEnum.REPORT_BUILD_SWITCH.getCode());
        return switchConfig != null && YesNoEnum.YES.getValue().equalsIgnoreCase(switchConfig);
    }

    /**
     * 检查当前时间是否在有效时间范围内
     *
     * @param shellConfigMap 壳配置映射
     * @return 当前时间是否在有效范围内
     */
    private boolean isInValidTimeRange(Map<String, String> shellConfigMap,LocalDate day) {
        String timeRangeConfig = shellConfigMap.get(ShellConfigItemEnum.REPORT_BUILD_TIME_RANGE.getCode());
        if (timeRangeConfig == null) {
            logProjectInstance().info("运行分析-日报-构建失败，日期范围配置为空");
            return false;
        }

        try {
            JSONObject timeRangeObject = JSONObject.parseObject(timeRangeConfig);
            String startTimeStr = timeRangeObject.getString("startTime");
            String endTimeStr = timeRangeObject.getString("endTime");

            if (startTimeStr == null || endTimeStr == null) {
                logProjectInstance().info("运行分析-日报-构建失败，日期范围配置中的开始或结束时间为空");
                return false;
            }

            // 时间格式MM.DD
            LocalDate startTime = convertMonthDayToLocalDateTime(startTimeStr);
            LocalDate endTime = convertMonthDayToLocalDateTime(endTimeStr);

            // 比较now是否在时间范围内
            return !day.isBefore(startTime) && !day.isAfter(endTime);
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-解析日期范围配置失败", e,
                    Pair.of("timeRangeConfig", timeRangeConfig));
            return false;
        }
    }

    /**
     * 根据时间配置计算应构建哪一天的数据
     *
     * @param shellConfigMap 壳配置映射
     * @return 应构建的日期
     */
    private LocalDate calculateBuildDay(Map<String, String> shellConfigMap) {
        String timeConfig = shellConfigMap.get(ShellConfigItemEnum.REPORT_BUILD_TIME.getCode());
        if (timeConfig == null) {
            logProjectInstance().info("运行分析-日报-构建失败，时间配置为空");
            return null;
        }

        try {
            JSONObject timeObject = JSONObject.parseObject(timeConfig);
            Boolean acrossDay = timeObject.getBoolean("acrossDay");
            String timeStr = timeObject.getString("time");

            if (timeStr == null) {
                logProjectInstance().info("运行分析-日报-构建失败，时间配置中的时间字符串为空");
                return null;
            }

            LocalTime time = LocalTime.parse(timeStr);
            LocalDateTime now = DateUtils.now();
            LocalDateTime buildTime = LocalDateTime.of(now.toLocalDate(), time);

            // 获取now和buildTime的差值
            long between = ChronoUnit.SECONDS.between(buildTime, now);
            logProjectInstance().info("运行分析-日报-构建，时间差值", Pair.of("between", between));
            if (between < 60L && between >= 0L) {
                if (Boolean.TRUE.equals(acrossDay)) {
                    // 返回前一天
                    return now.toLocalDate().minusDays(1);
                }
                return now.toLocalDate();
            }else if (between > 60L) {
                LocalDate localDate = now.toLocalDate();
                if (Boolean.TRUE.equals(acrossDay)) {
                    // 返回前一天
                    localDate =  now.toLocalDate().minusDays(1);
                }

                RunAnalysisReport queryReport = new RunAnalysisReport();
                queryReport.setReportDate(localDate);
                queryReport.setReportType(RunAnalysisReportTypeEnum.DAILY);
                List<RunAnalysisReport> reports = runAnalysisReportRepository.getListByDayAndType(queryReport);

                if (CollectionUtil.isEmpty(reports)) {
                    // 没有报告，返回日期以触发补生成
                    logProjectInstance().info("运行分析-日报-检测到缺失报告，准备补生成",
                            Pair.of("repairDate", localDate));
                    return localDate;
                }
            }

            return null;
        } catch (Exception e) {
            logProjectInstance().error("运行分析-日报-解析时间配置失败", e,
                    Pair.of("timeConfig", timeConfig));
            return null;
        }
    }

    public static LocalDate convertMonthDayToLocalDateTime(String monthDayStr) {
        if (monthDayStr == null || monthDayStr.trim().isEmpty()) {
            log.warn("月份日期字符串为空，无法转换");
            return null;
        }

        try {
            // 解析月份和日期部分
            // 由于输入不包含年份，我们使用当前年份
            int currentYear = DateUtils.now().getYear();

            // 将字符串添加年份前缀后解析
            String fullDateString = currentYear + "-" + monthDayStr;
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern(DateUtils.SDF_DAY);

            return LocalDate.parse(fullDateString, fullFormatter);
        } catch (Exception e) {
            log.warn("解析月份日期字符串失败: {}", monthDayStr, e);
            return null;
        }
    }

    private static Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> getConfigChapters(List<ShellConfigVO> shellConfigs) {
        Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> configChapters = new HashMap<>();

        if (CollectionUtil.isEmpty(shellConfigs)) {
            return configChapters;
        }

        for (ShellConfigVO shellConfig : shellConfigs) {
            if (shellConfig == null || CollectionUtil.isEmpty(shellConfig.getGroups())) {
                continue;
            }

            for (ShellConfigGroup group : shellConfig.getGroups()) {
                if (group == null || CollectionUtil.isEmpty(group.getItems())) {
                    continue;
                }

                for (ShellConfigItem item : group.getItems()) {
                    if (item == null) {
                        continue;
                    }

                    String itemValue = item.getValue();
                    if (itemValue == null || itemValue.trim().isEmpty()) {
                        continue;
                    }
                    RunAnalysisReportChapterEnum chapter = RunAnalysisReportChapterEnum.analyze(item.getCode());
                    RunAnalysisReportConfig runAnalysisReportConfig = ObjectMapperUtils.readValueFailSafe(itemValue, chapter.getConfigModel());

                    // 检查checked状态并处理章节
                    if (runAnalysisReportConfig != null) {
                        String checkedValue = runAnalysisReportConfig.getChecked();
                        if (!UNCHECK.equals(checkedValue)) {
                                configChapters.computeIfAbsent(chapter, k -> new ArrayList<>()).add(runAnalysisReportConfig);
                        }
                    }
                }
            }
        }

        return configChapters;
    }

    private static String getRedisLockKey(RunAnalysisReportBuildParam param) {
        return RedisKey.getRunAnalysisReportKey(param.getReportDate(),param.getReportType().getValue(),ContextUtil.getProjectId());
    }

    /**
     * 构建查询参数对象
     *
     * @param param         查询参数
     * @param reportChapter 报告章节
     * @return 查询参数对象
     */
    private RunAnalysisReport buildQueryParameter(
            @NonNull RunAnalysisReportQueryParam param,
            @NonNull RunAnalysisReportChapterParam reportChapter) {

        RunAnalysisReport queryParam = new RunAnalysisReport();
        queryParam.setReportDate(param.getReportDate());
        queryParam.setReportType(param.getReportType());
        queryParam.setReportChapter(RunAnalysisReportChapterEnum.analyze(reportChapter.getChapterCode()));
        queryParam.setObjectId(reportChapter.getObjectId());

        return queryParam;
    }

    /**
     * 解析章节配置信息
     *
     * @param chapterList 章节列表
     * @return 解析后的配置映射
     */
    private Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> parseChapterConfigurations(
            List<RunAnalysisReport> chapterList) {

        Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> configChapters = new HashMap<>();

        for (RunAnalysisReport item : chapterList) {
            if (item != null && item.getReportChapter() != null && item.getChapterConfig() != null) {
                try {
                    RunAnalysisReportChapterEnum reportChapter = item.getReportChapter();
                    RunAnalysisReportConfig runAnalysisReportConfig = ObjectMapperUtils.readValue(item.getChapterConfig(), reportChapter.getConfigModel());
                    if (runAnalysisReportConfig != null) {
                        runAnalysisReportConfig.setId(item.getId());
                        configChapters.computeIfAbsent(item.getReportChapter(), k -> new ArrayList<>()).add(runAnalysisReportConfig);

                    }
                } catch (Exception e) {
                    logProjectInstance().warn("运行分析-日报-解析章节配置失败",
                            Arrays.asList(
                                    Pair.of("chapter", item.getReportChapter().getValue()),
                                    Pair.of("chapterConfig", item.getChapterConfig()),
                                    Pair.of("error", e.getMessage())
                            ));
                    // 如果解析失败，跳过该配置项
                }
            }
        }

        return configChapters;
    }

    /**
     * 构建报告唯一标识key
     *
     * @param chapter  章节枚举
     * @param objectId 对象ID
     * @return 唯一标识key
     */
    private String buildReportKey(RunAnalysisReportChapterEnum chapter, String objectId) {
        return chapter.getValue() + "_" + (objectId != null ? objectId : "null");
    }

    @Override
    public void downloadReport(RunAnalysisReportDownloadParam param, HttpServletResponse response) throws Exception {
        logProjectInstance().info("运行分析报告-开始下载", Pair.of("reportDate", param.getReportDate()));
        try {
            //获取数据
            RunAnalysisReportDownloadDTO runAnalysisReportDownloadDTO = runAnalysisReportDownloadService.generateDownloadData(param.getReportDate(), param.getReportType());
            // 使用EasyExcel分批生成CSV
            RunAnalysisReportCsvExportUtil.exportCsv(response, param, runAnalysisReportDownloadDTO);

        } catch (Exception e) {
            logProjectInstance().error("运行分析报告-下载失败", e);
            throw new Exception("报告生成失败：" + e.getMessage(), e);
        }
    }
}