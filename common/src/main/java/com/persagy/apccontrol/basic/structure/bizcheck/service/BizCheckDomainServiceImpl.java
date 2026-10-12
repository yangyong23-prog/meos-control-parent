package com.persagy.apccontrol.basic.structure.bizcheck.service;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.convert.BizCheckConvert;
import com.persagy.apccontrol.basic.model.dto.StartEndDayDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.bizcheck.NotifyPeriodConfigParam;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.structure.bizcheck.context.BizCheckContext;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;
import com.persagy.apccontrol.basic.structure.bizcheck.notify.BizCheckNotifier;
import com.persagy.apccontrol.basic.structure.bizcheck.repository.BizCheckConfigRepository;
import com.persagy.apccontrol.basic.structure.bizcheck.repository.BizCheckResultRepository;
import com.persagy.apccontrol.basic.structure.bizcheck.strategy.BizCheckStrategy;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 14:26
 **/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BizCheckDomainServiceImpl implements BizCheckDomainService {
    private final BizCheckConfigRepository bizCheckConfigRepository;
    private final BizCheckResultRepository bizCheckResultRepository;
    private final ApplicationContext applicationContext;
    private final RedissonDistributedLockerService redissonDistributedLockerService;
    private final BizCheckConvert bizCheckConvert;


    @Override
    public Boolean saveConfig(BizCheckConfig config) {
        //校验param里的dateRangeConfig时间段不重复
        validateDateRangeConfig(config.getDateRangeConfig());
            
        config.setProjectId(ContextUtil.getProjectId());
        BizCheckConfig existConfig = getConfig(config.getBizCode());
        if (existConfig != null) {
            config.setId(existConfig.getId());
        }
        bizCheckConfigRepository.saveOrUpdate(config);
        return true;
    }

    @Override
    public BizCheckConfig getConfig(RunningMonitorTypeEnum bizCode) {
        return bizCheckConfigRepository.getByBizCode(bizCode);
    }

    // ==================== BizCheckResult相关方法 ====================

    @Override
    public Boolean saveResult(BizCheckResult result) {
        if (result == null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "检查结果不能为空");
        }
        
        // 设置项目ID
        result.setProjectId(ContextUtil.getProjectId());
        
        // 设置时间字段
        if (result.getCheckStartTime() == null) {
            result.setCheckStartTime(LocalDateTime.now());
        }
        
        if (result.getCheckStatus() == null) {
            result.setCheckStatus(BizCheckStatusEnum.PENDING);
        }
        
        if (result.getCheckType() == null) {
            result.setCheckType(BizCheckTypeEnum.AUTO);
        }
        
        return bizCheckResultRepository.saveOrUpdate(result);
    }

    @Override
    public void autoCheck() {
        logProjectInstance().info("开始执行自动检查");
        // 获取所有业务条线的检查配置
        List<BizCheckConfig> configs = bizCheckConfigRepository.getAll();
        configs.forEach(config -> {
            //判断当前时间是否在检查时间段内
            if (shouldPerformAutoCheck(config)) {
                performAutoCheckTask(config);
            }
        });
    }

    @Override
    public Boolean startCheck(BizCheckConfig bizCheckConfig, BizCheckTypeEnum checkType) {
        //获取所有检查结果，并检查状态
        List<BizCheckResult> results = bizCheckResultRepository.listByBizCode(bizCheckConfig.getBizCode());
        if (results != null && !results.isEmpty()) {
            for (BizCheckResult result : results) {
                if (result.getCheckStatus() == BizCheckStatusEnum.PENDING) {
                    logProjectInstance().info("检查跳过，正在检查中");
                    return true;
                }
            }
        }

        //删除旧的检查结果
        bizCheckResultRepository.deleteByBizCode(bizCheckConfig.getBizCode());

        // 获取所有业务检查策略
        Map<String, BizCheckStrategy> strategyMap = applicationContext.getBeansOfType(BizCheckStrategy.class);
        
        if (strategyMap.isEmpty()) {
            logProjectInstance().warn("未找到任何业务检查策略");
            return false;
        }
        
        logProjectInstance().info("开始执行业务检查，策略数量: " + strategyMap.size());
        
        // 创建检查上下文
        List<NotifyPeriodConfigParam> notifyConfigs = bizCheckConvert.stringToNotifyPeriodConfig(bizCheckConfig.getNotifyPeriodConfig());
        BizCheckContext context = BizCheckContext.builder()
                .bizCode(bizCheckConfig.getBizCode())
                .checkDate(LocalDate.now())
                .notifyPeriodConfigs(notifyConfigs)
                .build();

        // 遍历所有策略并执行检查
        for (Map.Entry<String, BizCheckStrategy> entry : strategyMap.entrySet()) {
            BizCheckStrategy strategy = entry.getValue();
            try {
                logProjectInstance().info("执行检查策略: " + strategy.getStrategyType().getValue());
                BizCheckResult result = new BizCheckResult();
                result.setCheckStatus(BizCheckStatusEnum.PENDING);
                result.setCheckType(checkType);
                result.setBizCode(bizCheckConfig.getBizCode());
                result.setCheckItemCode(strategy.getStrategyType());
                //保存
                saveResult(result);
                
                // 执行具体策略的检查
                BizCheckItemDTO checkResult = strategy.execute(context);
                result.setCheckStatus(checkResult.getStatus());
                result.setCheckResult(ObjectMapperUtils.writeValueAsString(checkResult.getCheckResult()));
                completeCheck(result);

                logProjectInstance().info("检查策略执行完成: " + strategy.getStrategyType().getValue());
                
            } catch (Exception e) {
                logProjectInstance().error("执行检查策略时发生异常: " + strategy.getStrategyType().getValue(), e);
                throw new RuntimeException(e);
            }
        }

        // 通知钩子：全部检查项入库后，触发异常通知（云端有实现，边端无实现自动跳过）
        Map<String, BizCheckNotifier> notifiers = applicationContext.getBeansOfType(BizCheckNotifier.class);
        for (BizCheckNotifier n : notifiers.values()) {
            try {
                n.notifyIfAbnormal(bizCheckConfig.getBizCode().getValue());
            } catch (Exception e) {
                logProjectInstance().error("BizCheck通知发送失败，不阻塞主流程. bizCode=" + bizCheckConfig.getBizCode().getValue(), e);
            }
        }
        
        return true;
    }

    @Override
    public Boolean completeCheck(BizCheckResult result) {
        result.setCheckEndTime(DateUtils.now());
        
        return bizCheckResultRepository.updateById(result);
    }

    @Override
    public List<BizCheckResult> getResults(RunningMonitorTypeEnum bizCode) {
        if (bizCode == null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "业务条线编码不能为空");
        }
        
        return bizCheckResultRepository.listByBizCode(bizCode);
    }

    /**
     *校日期范围配置，检查时间段是否重复
     * 
     * @param dateRangeConfig 日期范围配置列表
     * @throws BusinessException 当存在重复时间段时抛出异常
     */
    private void validateDateRangeConfig(String dateRangeConfig) {
        List<StartEndDayDTO> dateRangeConfigList = ObjectMapperUtils.readValueFailSafe(dateRangeConfig, new TypeReference<List<StartEndDayDTO>>() {});
        if (dateRangeConfigList == null || dateRangeConfigList.isEmpty()) {
            return;
        }
            
        //为Range对象用于重叠检查
        List<Range<ChronoLocalDate>> ranges = dateRangeConfigList.stream()
                .filter(Objects::nonNull)
                .map(this::convertToRange)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Range::getMinimum))
                .collect(Collectors.toList());
            
        //检查是否有重叠
        if (RangeUtils.isOverlapped(ranges)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "日期时间段存在重复");
        }
    }
        
    /**
     *将StartEndDayDTO转换为Range对象
     * 
     * @param dto StartEndDayDTO对象
     * @return Range对象，如果转换失败返回null
     */
    private Range<ChronoLocalDate> convertToRange(StartEndDayDTO dto) {
        if (dto == null || dto.getStartDay() == null || dto.getEndDay() == null) {
            return null;
        }
            
        try {
            LocalDate start = parseDate(dto.getStartDay());
            LocalDate end = parseDate(dto.getEndDay());
                
            //验证日期范围的有效性
            if (start.isAfter(end)) {
                throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(),
                    String.format("日期范围无效：开始日期[%s]不能晚于结束日期[%s]", dto.getStartDay(), dto.getEndDay()));
            }
                
            return Range.between(start, end);
        } catch (Exception e) {
            log.error("日期转换失败: startDay={}, endDay={}", dto.getStartDay(), dto.getEndDay(), e);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(),
                String.format("日期格式无效: startDay=%s, endDay=%s", dto.getStartDay(), dto.getEndDay()));
        }
    }
        
    /**
     * 判断当前日期是否在配置的日期范围内
     * 
     * @param currentDate 当前日期
     * @param dateRangeConfig 日期范围配置JSON字符串
     * @return 如果在范围内返回true，否则返回false
     */
    private boolean isCurrentDateInRange(LocalDate currentDate, String dateRangeConfig) {
        if (dateRangeConfig == null || dateRangeConfig.trim().isEmpty()) {
            // 如果没有配置日期范围
            return false;
        }
        
        try {
            List<StartEndDayDTO> dateRanges = ObjectMapperUtils.readValueFailSafe(dateRangeConfig, 
                new TypeReference<List<StartEndDayDTO>>() {});
            
            if (dateRanges == null || dateRanges.isEmpty()) {
                return false;
            }
            
            // 检查当前日期是否在任何一个配置的时间段内
            for (StartEndDayDTO range : dateRanges) {
                if (range != null && range.getStartDay() != null && range.getEndDay() != null) {
                    try {
                        LocalDate startDate = parseDate(range.getStartDay());
                        LocalDate endDate = parseDate(range.getEndDay());
                        
                        // 如果当前日期在该时间段内，返回true
                        if (!currentDate.isBefore(startDate) && !currentDate.isAfter(endDate)) {
                            return true;
                        }
                    } catch (Exception e) {
                        logProjectInstance().error("解析日期范围配置失败: ",
                             e, Pair.of("range", range));
                    }
                }
            }
            
            // 如果都不在任何配置的时间段内，返回false
            return false;
            
        } catch (Exception e) {
            logProjectInstance().error("检查日期范围配置时发生错误", e);
            return false;
        }
    }
    
    /**
     * 解析日期字符串
     *支持多种日期格式：yyyy-MM-dd, MM-dd
     * 
     * @param dateStr 日期字符串
     * @return LocalDate对象
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
            
        String trimmedDate = dateStr.trim();
            
        //尝解析完整日期格式 yyyy-MM-dd
        try {
            return LocalDate.parse(trimmedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            //忽，尝试其他格式
        }
            
        //尝解析月日格式 MM-dd
        try {
            String currentYear = String.valueOf(DateUtils.now().getYear());
            return LocalDate.parse(currentYear + "-" + trimmedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            //忽略，抛出最终异常
        }
            
        throw new IllegalArgumentException("不支持的日期格式: " + dateStr);
    }
    
    /**
     * 判断是否应该执行自动检查
     * 
     * @param config 业务检查配置
     * @return 是否应该执行自动检查
     */
    private boolean shouldPerformAutoCheck(BizCheckConfig config) {
        if (config == null) {
            return false;
        }
        
        // 检查基础配置
        if (!Boolean.TRUE.equals(config.getIsEnable()) || 
            !Boolean.TRUE.equals(config.getIsAutoCheck())) {
            return false;
        }
        
        LocalDateTime now = DateUtils.now();
        
        // 检查日期范围
        if (!isCurrentDateInRange(now.toLocalDate(), config.getDateRangeConfig())) {
            logProjectInstance().info("当前日期不在配置的检查日期范围内，已跳过检查");
            return false;
        }
        
        // 检查自动检查时间配置
        String autoCheckTimeStr = config.getAutoCheckTime();
        if (autoCheckTimeStr == null || autoCheckTimeStr.trim().isEmpty()) {
            logProjectInstance().warn("自动检查时间配置为空，跳过检查");
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行自动检查任务
     * 
     * @param config 业务检查配置
     */
    private void performAutoCheckTask(BizCheckConfig config) {
        try {
            LocalDateTime now = DateUtils.now();
            String autoCheckTimeStr = config.getAutoCheckTime();
            
            LocalTime autoCheckTimeLocal = LocalTime.parse(autoCheckTimeStr);
            LocalDateTime autoCheckTime = LocalDateTime.of(now.toLocalDate(), autoCheckTimeLocal);
            
            // 计算时间差值（秒）
            long timeDifferenceSeconds = ChronoUnit.SECONDS.between(autoCheckTime, now);
            logProjectInstance().info("触发自动检查任务，时间差值（秒）", Pair.of("timeDifferenceSeconds", timeDifferenceSeconds));

            // 如果时间差在60秒以内，则执行检查
            if (timeDifferenceSeconds < 60L && timeDifferenceSeconds >= 0) {
                logProjectInstance().info("触发自动检查任务", Pair.of("bizCode", config.getBizCode()));
                startCheck(config, BizCheckTypeEnum.AUTO);
            }else if(timeDifferenceSeconds >= 60L){
                List<BizCheckResult> bizCheckResults = bizCheckResultRepository.listByBizCode(config.getBizCode());
                if (CollectionUtil.isEmpty(bizCheckResults)) {
                    logProjectInstance().info("触发自动检查任务", Pair.of("bizCode", config.getBizCode()));
                    startCheck(config, BizCheckTypeEnum.AUTO);
                }else{
                    // 检查是否所有记录的 checkStartTime 都不是今天
                    LocalDate today = DateUtils.now().toLocalDate();
                    boolean allNotToday = bizCheckResults.stream()
                            .noneMatch(result -> result.getCheckStartTime() != null && 
                                    result.getCheckStartTime().toLocalDate().isEqual(today));
                                
                    if (allNotToday) {
                        logProjectInstance().info("所有检查记录的开始时间都不是今天，触发自动检查任务", 
                                Pair.of("bizCode", config.getBizCode()));
                        startCheck(config, BizCheckTypeEnum.AUTO);
                    }
                }
            }
            
        } catch (Exception e) {
            logProjectInstance().error("执行自动检查任务时发生错误", e);
        }
    }

    private static String getRedisLockKey(BizCheckConfig config) {
        return RedisKey.getBizCheckKey(ContextUtil.getProjectId(),config.getBizCode().getValue());
    }

    /**
     * 解析通知时段配置JSON字符串
     * 
     * @param notifyPeriodConfig JSON格式的通知时段配置
     * @return NotifyPeriodConfigParam列表
     */
    private List<NotifyPeriodConfigParam> parseNotifyPeriodConfig(String notifyPeriodConfig) {
        if (notifyPeriodConfig == null || notifyPeriodConfig.trim().isEmpty() || 
            "[]".equals(notifyPeriodConfig) || "null".equals(notifyPeriodConfig)) {
            return new ArrayList<>();
        }
        
        try {
            return ObjectMapperUtils.readValue(notifyPeriodConfig, 
                new TypeReference<List<NotifyPeriodConfigParam>>() {});
        } catch (Exception e) {
            logProjectInstance().error("解析通知时段配置失败: " + notifyPeriodConfig, e);
            return new ArrayList<>();
        }
    }

    @Override
    public String logTag() {
        return "可用性检查";
    }
}