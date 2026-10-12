package com.persagy.apccontrol.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.config.listener.BusinessRetryListener;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.*;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeConfigResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeTemplateTypeResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeConfigQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheInfoCodeTemplateRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.converter.InfoCodeConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.repository.InfoCodeRepository;
import com.persagy.apccontrol.basic.structure.translation.TranslationKit;
import com.persagy.apccontrol.basic.task.BaseTask;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.excel.ExcelUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.service.InfoCodeImportService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 信息点配置导入服务实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeImportServiceImpl
        implements InfoCodeImportService, BizComponent<InfoCodeImportService> {

    private final InfoCodeConverter infoCodeConverter;
    private final InfoCodeTemplateTypeResource infoCodeTemplateTypeResource;
    private final InfoCodeConfigResource infoCodeConfigResource;
    private final InfoCodeRepository infoCodeRepository;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final ObjectMapper objectMapper;
    private final RedisCacheTemplate redisCacheTemplate;
    private final CacheInfoCodeTemplateRepository cacheInfoCodeTemplateRepository;
    private final CacheRwdDomainService cacheRwdDomainService;

    @Override
    public R<ObjectNode> parseWebConfig(@NonNull MultipartFile file) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        try (InputStream inputStream = file.getInputStream();
             ExcelReader reader = ExcelUtil.getReader(inputStream)) {
            List<Sheet> sheets = reader.getSheets();
            if (CollectionUtils.isEmpty(sheets)) {
                return R.success(null);
            }
            for (Sheet sheet : sheets) {
                List<Row> rows = ExcelUtils.analyzeRows(sheet, 0);
                if (CollectionUtils.isEmpty(rows)) {
                    continue;
                }
                List<String> heads = new ArrayList<>();
                Row headRow = rows.get(0);
                for (int i = headRow.getFirstCellNum(); i < headRow.getLastCellNum(); i++) {
                    String cellValue = ExcelUtils.analyzeValueByString(headRow, i);
                    if (StringUtils.isBlank(cellValue)) {
                        continue;
                    }
                    heads.add(cellValue);
                }
                ArrayNode arrayNode = objectMapper.createArrayNode();
                for (int i = 1; i < rows.size(); i++) {
                    Row row = rows.get(i);
                    ObjectNode itemNode = objectMapper.createObjectNode();
                    int size = Math.min(row.getLastCellNum(), heads.size());
                    for (int j = row.getFirstCellNum(); j < size; j++) {
                        String cellValue = ExcelUtils.analyzeValueByString(row, j);
                        if (StringUtils.isBlank(cellValue)) {
                            continue;
                        }
                        String head = heads.get(j);
                        itemNode.put(head, cellValue);
                    }
                    arrayNode.add(itemNode);
                }
                objectNode.set(sheet.getSheetName(), arrayNode);
            }
        } catch (IOException e) {
            logProjectInstance().error("文件解析失败", e);
            throw new ApplicationException(ErrorCodeConstants.USER_ERROR_A0402.getCode(),
                    e.getLocalizedMessage(), LocaleMessageUtil.getMessage("key_a1e92efd2da847f598847f47df3f8625"));
        }
        return R.success(objectNode);
    }

    @Override
    public R<InfoCodeGroupImportContext> importGroupSheet(
            @NonNull MultipartFile file, boolean checkGroupCodeInConfig) {
        this.clearPublicContext();
        InfoCodeGroupImportContext context = new InfoCodeGroupImportContext(file, checkGroupCodeInConfig);
        if (context.isFailed()) {
            return R.success(context.toErrorOutPutContext());
        }

        // 检查所有分组编码在信息点配置中的定义
        if (context.isCheckGroupCodeInConfig()) {
            checkAllGroupCodeDefinedByInfoCodeConfig(context);
            if (context.isFailed()) {
                return R.success(context.toErrorOutPutContext());
            }
        }

        // 新增Sheet分组配置
        infoCodeRepository.addSheetGroupConfig(context.listTopConfigContexts(),
                context.mapGroupCodeAndConfigContext(), null);

        return R.success(context.toOutPutContext());
    }

    @Override
    public R<Map<String, Collection<String>>> importTableSheet(
            @NonNull List<MultipartFile> files, boolean skipNotDefineInfoCode) {
        this.clearPublicContext();
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        List<InfoCodeImportContext> successContexts = new ArrayList<>();
        Set<Long> replacedTemplateTypeIds = new LinkedHashSet<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            InfoCodeImportContext context = new InfoCodeImportContext(file, skipNotDefineInfoCode);
            if (context.isFailed()) {
                result.put(fileName, context.getErrorMessages());
                continue;
            }

            // 检查唯一键
            checkUniqueKey(context);
            if (context.isFailed()) {
                result.put(fileName, context.getErrorMessages());
                continue;
            }

            // 检查数据字典设备类编码是否有效
            checkKnowledgeClassCodeValidAndSetObjectType(context);
            if (context.isFailed()) {
                result.put(fileName, context.getErrorMessages());
                continue;
            }
            // 检查数据字典信息点是否有效
            checkKnowledgeInfoCodeValid(context);
            if (context.isFailed()) {
                result.put(fileName, context.getErrorMessages());
                continue;
            }

            // 新增Sheet表配置
            replacedTemplateTypeIds.addAll(this.infoCodeRepository.addSheetConfig(context));
            result.put(fileName, Lists.newArrayList(HttpStatus.OK.getReasonPhrase()));
            successContexts.add(context);
        }
        this.clearInfoCodeConfigRedisCache();

        // 触发跨库缓存重建
        Map<String, String> failedProjects = this.triggerCacheRebuild(
                successContexts, replacedTemplateTypeIds);
        if (MapUtils.isNotEmpty(failedProjects)) {
            result.put("_cacheRebuildFailures", failedProjects.values());
        }

        return R.success(result);
    }

    @Override
    public boolean del(@NonNull InfoCodeTemplateTypeRemoveDaoParam param) {
        this.clearPublicContext();
        Set<Long> deletedTemplateTypeIds = this.infoCodeRepository.delSheetConfig(param);
        if (CollectionUtils.isEmpty(deletedTemplateTypeIds)) {
            return false;
        }
        this.clearInfoCodeConfigRedisCache();
        Map<String, String> failedProjects = this.triggerCacheRebuild(
                Collections.emptyList(), deletedTemplateTypeIds);
        if (MapUtils.isNotEmpty(failedProjects)) {
            this.logProjectInstance().warn("部分项目缓存清理失败",
                    Pair.of("failedProjects", failedProjects));
        }
        return true;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> translateInfoCodeName(@Nullable HttpServletResponse response) throws IOException {
        this.clearPublicContext();
        List<InfoCodeConfig> configs = this.queryNeedReplaceI18nInfoCodeConfigs();
        if (CollectionUtils.isEmpty(configs)) {
            return Collections.emptyMap();
        }
        Set<String> infoCodeNames = new HashSet<>();
        for (InfoCodeConfig config : configs) {
            String infoCodeName = config.getInfoCodeName();
            String infoCodeAliasName = config.getInfoCodeAliasName();

            infoCodeNames.add(infoCodeName);
            if (StringUtils.isNotBlank(infoCodeAliasName)) {
                infoCodeNames.add(infoCodeAliasName);
            }
        }
        // QPS限制10, 当前限制8避免限流, 同时加入重试机制托底
        RateLimiter rateLimiter = RateLimiter.create(8);
        Map<String, String> chineseNameAndEnglishName = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String infoCodeName : infoCodeNames) {
            futures.add(CompletableFuture.runAsync(() -> {
                rateLimiter.acquire();
                RetryTemplateBuilder retryTemplateBuilder = new RetryTemplateBuilder();
                // 重试间隔5秒, 每次递增2倍, 最大不超过60秒
                retryTemplateBuilder.exponentialBackoff(5000, 2, 60000);
                // 重试5次
                retryTemplateBuilder.maxAttempts(5);
                // 遍历堆栈异常
                retryTemplateBuilder.retryOn(Exception.class);
                retryTemplateBuilder.traversingCauses();
                // 自定义重试监听
                retryTemplateBuilder.withListener(new BusinessRetryListener(String.format("翻译信息点名称(%s)", infoCodeName)));
                RetryTemplate retryTemplate = retryTemplateBuilder.build();
                retryTemplate.execute(retryContext -> {
                    String englishName = TranslationKit.BAIDU.translate(infoCodeName);
                    if (StringUtils.isBlank(englishName)) {
                        logProjectInstance().error("翻译失败", Pair.of("infoCodeName", infoCodeName));
                        throw new ApplicationException(ErrorCodeConstants.USER_ERROR_A0500, "翻译失败");
                    }
                    chineseNameAndEnglishName.put(infoCodeName, englishName);
                    return null;
                });
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        if (CollectionUtils.isNotEmpty(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        // 数据库仅支持最多64个字符, 超过截断处理
        int infoCodeNameLimit = 64;
        if (Objects.nonNull(response)) {
            ExcelWriter writer = ExcelUtil.getWriter(true);
            writer.renameSheet("中英文对照表");
            writer.write(chineseNameAndEnglishName.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Collator.getInstance(Locale.CHINA)))
                    .map(item -> {
                        Map<String, String> result = new LinkedHashMap<>();
                        result.put("原始信息点名称", item.getKey());
                        result.put("英文信息点原始名称", item.getValue());
                        result.put("英文信息点入库名称", item.getValue().length() > infoCodeNameLimit
                                ? item.getValue().substring(0, infoCodeNameLimit)
                                : item.getValue());
                        return result;
                    }).collect(Collectors.toList()), true);
            writer.setColumnWidth(0, 65);
            writer.setColumnWidth(1, 150);
            writer.setColumnWidth(2, 75);
            for (int i = 0; i < writer.getRowCount(); i++) {
                if (i == 0) {
                    writer.setRowHeight(0, 30);
                    continue;
                }
                writer.setRowHeight(i, 20);
            }
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=".concat(
                    URLEncoder.encode("中英文对照表.xlsx", StandardCharsets.UTF_8.name())));
            CountingOutputStream countingOutputStream = new CountingOutputStream(response.getOutputStream());
            writer.flush(countingOutputStream);
            response.setContentLengthLong(countingOutputStream.getByteCount());
        }
        return chineseNameAndEnglishName;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Collection<String>> importInfoCodeName(MultipartFile file) {
        this.clearPublicContext();
        try (InputStream inputStream = file.getInputStream();
             ExcelReader reader = ExcelUtil.getReader(inputStream)) {
            List<Sheet> sheets = reader.getSheets();
            if (CollectionUtils.isEmpty(sheets)) {
                return R.successMulti(null);
            }
            List<Row> rows = ExcelUtils.analyzeRows(sheets.get(0), 1);
            if (CollectionUtils.isEmpty(rows)) {
                return R.successMulti(null);
            }
            List<String> errorMessages = new ArrayList<>();
            Map<String, String> originAndFinalInfoCodeName = new LinkedHashMap<>();
            for (Row row : rows) {
                int rowNum = row.getRowNum();
                String originalInfoCodeName = ExcelUtils.analyzeValueByString(row, 0);
                String finalInfoCodeName = ExcelUtils.analyzeValueByString(row, 2);
                if (StringUtils.isBlank(originalInfoCodeName)) {
                    logProjectInstance().error("原始信息点名称为空",
                            Pair.of("originalInfoCodeName", originalInfoCodeName));
                    errorMessages.add(String.format("[%d列-%d行] 原始信息点名称为空", 1, rowNum + 1));
                }
                if (StringUtils.isBlank(finalInfoCodeName)) {
                    logProjectInstance().error("英文信息点名称为空",
                            Pair.of("finalInfoCodeName", finalInfoCodeName));
                    errorMessages.add(String.format("[%d列-%d行] 英文信息点名称为空", 3, rowNum + 1));
                } else if (finalInfoCodeName.length() > 64) {
                    logProjectInstance().error("英文信息点名称长度超过64个字符",
                            Pair.of("finalInfoCodeName", finalInfoCodeName));
                    errorMessages.add(String.format("[%d列-%d行] 英文信息点名称长度超过64个字符", 3, rowNum + 1));
                }
                originAndFinalInfoCodeName.put(originalInfoCodeName, finalInfoCodeName);
            }
            if (CollectionUtils.isNotEmpty(errorMessages)) {
                return R.successMulti(errorMessages);
            }
            List<InfoCodeConfig> configs = this.queryNeedReplaceI18nInfoCodeConfigs();
            if (CollectionUtils.isEmpty(configs)) {
                return R.successMulti(null);
            }
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (InfoCodeConfig config : configs) {
                futures.add(CompletableFuture.runAsync(() -> {
                    Long configId = config.getId();
                    String infoCodeName = config.getInfoCodeName();
                    String infoCodeAliasName = config.getInfoCodeAliasName();

                    String finalInfoCodeName = originAndFinalInfoCodeName.get(infoCodeName);
                    String finalInfoCodeAliasName = originAndFinalInfoCodeName.get(infoCodeAliasName);
                    infoCodeRepository.updateInfoCodeNameByIds(Sets.newHashSet(configId),
                            finalInfoCodeName, finalInfoCodeAliasName);
                }, BusinessThreadPoolExecutor.ttlExecutor()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            this.clearInfoCodeConfigRedisCache();
            return R.successMulti(Collections.emptyList());
        } catch (IOException e) {
            logProjectInstance().error("文件解析失败", e);
            throw new ApplicationException(ErrorCodeConstants.USER_ERROR_A0402.getCode(),
                    e.getLocalizedMessage(), "文件解析失败");
        }
    }

    /**
     * 触发跨库缓存重建
     * <p>
     * 公共库操作完成后，遍历所有集团和项目执行缓存清理及重建。集团、项目上下文由
     * {@link BaseTask} 在每次执行前设置，不能使用导入请求自身的集团或项目上下文。
     *
     * @param contexts                 成功导入的配置上下文
     * @param deletedTemplateTypeIds   被替换或删除的旧模板类型ID
     * @return 失败项目信息（key: groupCode:projectId, value: 错误信息）
     * @since v7.260701.0
     */
    private @NonNull Map<String, String> triggerCacheRebuild(
            @NonNull List<InfoCodeImportContext> contexts,
            @NonNull Set<Long> deletedTemplateTypeIds) {
        CacheRebuildExecutor executor = new CacheRebuildExecutor(contexts, deletedTemplateTypeIds);
        return executor.execute();
    }

    /**
     * 清理公共信息点配置对应的 Redis 缓存。
     *
     * <p>Key 包含集团和项目维度，使用统一前缀模糊删除可以一次覆盖全部项目，避免依赖
     * 当前 HTTP 请求中不存在的项目上下文。</p>
     *
     * @since v7.260701.0
     */
    private void clearInfoCodeConfigRedisCache() {
        try {
            this.redisCacheTemplate.fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX
                    + "*" + InfoCodeGateway.CACHE_KEY_TEMPLATE + "*");
            this.redisCacheTemplate.fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX
                    + "*" + InfoCodeGateway.CACHE_KEY_TEMPLATE_OBJECT + "*");
            // 清除老版本独立缓存 key。
            this.redisCacheTemplate.fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX
                    + "*" + InfoCodeGateway.CACHE_KEY_TEMPLATE_OBJECT_DEPRECATED + "*");
            this.redisCacheTemplate.fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX
                    + "*" + InfoCodeGateway.CACHE_KEY_TEMPLATE_OBJECT_IOT_RULE + "*");
        } catch (Exception e) {
            // 公共库是配置权威数据，Redis 清理失败只记录，避免已提交的配置被返回为失败。
            this.logInstance().error("清理信息点配置 Redis 缓存失败，公共库数据已提交", e);
        }
    }

    /**
     * 清除信息点配置公共库操作不应使用的集团和项目上下文。
     *
     * <p>HTTP 入口由控制器在事务代理执行前清理；非事务服务入口再次调用本方法，
     * 避免直接调用时把残留路由值传给后续仓储。</p>
     *
     * @since v7.260701.0
     */
    private void clearPublicContext() {
        ContextUtil.setTenant(null);
        ContextUtil.setGroupCode(null);
        ContextUtil.setProjectId(null);
    }

    /**
     * 查现需要替换国际化的信息点配置
     *
     * @return 需要替换国际化的信息点配置
     * @since 7.250902.0
     */
    private List<InfoCodeConfig> queryNeedReplaceI18nInfoCodeConfigs() {
        // 查询非IOT信息点类型
        InfoCodeConfigQueryDaoParam queryDaoParam = new InfoCodeConfigQueryDaoParam();
        queryDaoParam.addColumns(BaseAssignIdDO::getId,
                InfoCodeConfig::getInfoCodeName,
                InfoCodeConfig::getInfoCodeAliasName);
        return infoCodeRepository.list(queryDaoParam);
    }

    /**
     * 检查所有分组编码在信息点配置中的定义
     *
     * @param context 上下文
     * @since 5.240702.0
     */
    private void checkAllGroupCodeDefinedByInfoCodeConfig(@NonNull InfoCodeGroupImportContext context) {
        Set<String> allGroupCodes = context.getConfigContexts().stream()
                .map(InfoCodeGroupConfigImportContext::getGroupCode)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(allGroupCodes)) {
            return;
        }
        List<InfoCodeConfig> invalidConfigs =
                infoCodeConfigResource.listInvalidByGroupCode(allGroupCodes);
        if (CollectionUtils.isEmpty(invalidConfigs)) {
            return;
        }
        Map<Long, List<InfoCodeConfig>> templateTypeIdAndInfoCode = invalidConfigs.stream()
                .collect(Collectors.groupingBy(InfoCodeConfig::getRelTemplateTypeId));
        List<InfoCodeTemplateType> dbTemplateTypes = infoCodeTemplateTypeResource
                .listByIdIgnore(templateTypeIdAndInfoCode.keySet());
        boolean failed = false;
        for (InfoCodeTemplateType dbTemplateType : dbTemplateTypes) {
            Long templateTypeId = dbTemplateType.getId();
            RunningMonitorTypeEnum runningMonitorType = dbTemplateType.getRunningMonitorType();
            InfoCodeTemplateTypeEnum templateType = dbTemplateType.getTemplateType();
            String infoCodeFilter = dbTemplateType.getInfoCodeFilter();

            List<InfoCodeConfig> infoCodeConfigs = templateTypeIdAndInfoCode
                    .getOrDefault(templateTypeId, Collections.emptyList());
            for (InfoCodeConfig infoCodeConfig : infoCodeConfigs) {
                String infoCode = infoCodeConfig.getInfoCode();
                String relGroupCode = infoCodeConfig.getRelGroupCode();
                logProjectInstance().error("信息点配置中存在未定义的分组编码", Arrays.asList(
                        Pair.of("runningMonitorType", runningMonitorType),
                        Pair.of("templateType", templateType),
                        Pair.of("infoCodeFilter", infoCodeFilter),
                        Pair.of("infoCode", infoCode),
                        Pair.of("undefinedGroupCode", relGroupCode)
                ));
                context.getErrorMessages().add(String.format(LocaleMessageUtil.getMessage("key_667c2ab8c1034c9a9e3b4400ed59ebe1"),
                        runningMonitorType, templateType, infoCodeFilter, infoCode, relGroupCode));
                failed = true;
            }
        }
        if (failed) {
            context.setFailed(true);
        }
    }

    /**
     * 检查唯一标识
     *
     * @param context 上下文
     * @since 7.250802.0
     */
    private void checkUniqueKey(@NonNull InfoCodeImportContext context) {
        RunningMonitorTypeEnum runningMonitorType = context.getRunningMonitorType();
        RunningMonitorSecondTypeEnum runningMonitorSecondType = context.getRunningMonitorSecondType();
        List<InfoCodeSheetImportContext> sheetContexts = context.getSheetContexts();
        InfoCodeTemplateTypeEnum templateType = context.getTemplateType();
        if (CollectionUtils.isEmpty(sheetContexts)) {
            return;
        }
        boolean failed = false;
        Set<String> importClassCodes = new HashSet<>();
        Map<String, InfoCodeSheetImportContext> uniqueKeyAndSheetContext = new LinkedHashMap<>();
        for (InfoCodeSheetImportContext sheetContext : sheetContexts) {
            String uniqueKey = sheetContext.getUniqueKey();
            String classCode = sheetContext.getClassCode();

            importClassCodes.add(classCode);
            if (StringUtils.isBlank(uniqueKey)) {
                continue;
            }
            if (uniqueKeyAndSheetContext.containsKey(uniqueKey)) {
                InfoCodeSheetImportContext repeatSheetContext = uniqueKeyAndSheetContext.get(uniqueKey);
                logProjectInstance().error("模版唯一标识重复", Arrays.asList(
                        Pair.of("uniqueKey", uniqueKey),
                        Pair.of("classCode1", repeatSheetContext.getClassCode()),
                        Pair.of("classCode2", sheetContext.getClassCode())
                ));
                sheetContext.getErrorMessages().add(String.format("模版唯一标识重复 [Sheet1: %s, Sheet2: %s]",
                        repeatSheetContext.getClassCode(), sheetContext.getClassCode()));
                failed = true;
                continue;
            }
            uniqueKeyAndSheetContext.put(uniqueKey, sheetContext);
        }
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setRunningMonitorType(runningMonitorType);
        queryDaoParam.setRunningMonitorSecondType(runningMonitorSecondType);
        queryDaoParam.setTemplateType(templateType);
        queryDaoParam.setExcludeClassCodes(importClassCodes);
        List<InfoCodeTemplateType> existsTemplateTypes = infoCodeTemplateTypeResource.list(queryDaoParam);
        if (CollectionUtils.isNotEmpty(existsTemplateTypes)) {
            Map<String, InfoCodeTemplateType> existsUniqueKeyAndTemplateType = new HashMap<>();
            for (InfoCodeTemplateType existsTemplateType : existsTemplateTypes) {
                InfoCodeTemplateTypeModel templateTypeModel = infoCodeConverter
                        .toTemplateTypeModel(existsTemplateType);
                // 公共库上下文不存在项目缓存表, 强制实时解析跳过数据库与Redis快照复用; 唯一键校验须基于最新解析结果
                InfoCodeFilterValueObject filterValueObject = templateTypeModel.parseFilterValueObject(true);
                if (Objects.nonNull(filterValueObject)) {
                    String uniqueKey = filterValueObject.getUniqueKey();
                    if (StringUtils.isNotBlank(uniqueKey)) {
                        existsUniqueKeyAndTemplateType.put(uniqueKey, existsTemplateType);
                    }
                }
            }
            for (Map.Entry<String, InfoCodeSheetImportContext> entry : uniqueKeyAndSheetContext.entrySet()) {
                String uniqueKey = entry.getKey();
                InfoCodeSheetImportContext sheetContext = entry.getValue();
                InfoCodeTemplateType existsTemplateType = existsUniqueKeyAndTemplateType.get(uniqueKey);
                if (Objects.isNull(existsTemplateType)) {
                    continue;
                }
                logProjectInstance().error("模版唯一标识已存在", Arrays.asList(
                        Pair.of("uniqueKey", uniqueKey),
                        Pair.of("classCode1", existsTemplateType.getClassCode()),
                        Pair.of("classCode2", sheetContext.getClassCode())
                ));
                sheetContext.getErrorMessages().add(String.format("模版唯一标识已存在 [Sheet1: %s, Sheet2: %s]",
                        existsTemplateType.getClassCode(), sheetContext.getClassCode()));
                failed = true;
            }
        }
        if (failed) {
            context.setFailed(true);
        }
    }

    /**
     * 检查数据字典设备类编码是否有效并设置对象类型
     *
     * @param context 上下文
     * @since 5.240602.0
     */
    private void checkKnowledgeClassCodeValidAndSetObjectType(@NonNull InfoCodeImportContext context) {
        List<InfoCodeSheetImportContext> sheetContexts = context.getSheetContexts();
        if (CollectionUtils.isEmpty(sheetContexts)) {
            return;
        }
        Set<String> classCodes = sheetContexts.stream()
                .map(InfoCodeSheetImportContext::getClassCode)
                .collect(Collectors.toSet());
        boolean failed = false;
        Map<String, DtpKnowledgeObjectClassCodeResponse> classCodeAndDefine =
                dtpKnowledgeClientService.mapClassCodeAndDefine(true, classCodes);
        for (InfoCodeSheetImportContext sheetContext : sheetContexts) {
            String sheetName = sheetContext.getSheet().getSheetName();
            String classCode = sheetContext.getClassCode();
            DtpKnowledgeObjectClassCodeResponse classCodeDefine = classCodeAndDefine.get(classCode);
            if (Objects.isNull(classCodeDefine)) {
                logProjectInstance().error("设备类编码未在数据字典定义", Arrays.asList(
                        Pair.of("sheetName", sheetName),
                        Pair.of("classCode", classCode)
                ));
                sheetContext.getErrorMessages().add(String.format(LocaleMessageUtil.getMessage("key_115a1eece19a434c84806cab50ca1ca6"), sheetName));
                failed = true;
                continue;
            }
            sheetContext.setObjectType(BaseEnum.analyze(classCodeDefine.getObjType(), DtpRwdObjectTypeEnum.class));
        }
        if (failed) {
            context.setFailed(true);
        }
    }

    /**
     * 检查数据字典信息点是否有效
     *
     * @param context 上下文
     * @since 5.240602.0
     */
    private void checkKnowledgeInfoCodeValid(@NonNull InfoCodeImportContext context) {
        boolean skipNotDefineInfoCode = context.isSkipNotDefineInfoCode();
        List<InfoCodeSheetImportContext> sheetContexts = context.getSheetContexts();
        if (CollectionUtils.isEmpty(sheetContexts)) {
            return;
        }
        Set<String> classCodes = sheetContexts.stream()
                .map(InfoCodeSheetImportContext::getClassCode)
                .collect(Collectors.toSet());
        boolean failed = false;
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeAndDefine = dtpKnowledgeClientService
                .mapClassCodeInfoCodeAndDefine(false, true, classCodes);
        for (InfoCodeSheetImportContext sheetContext : context.getSheetContexts()) {
            String sheetName = sheetContext.getSheet().getSheetName();
            String classCode = sheetContext.getClassCode();
            List<InfoCodeConfigImportContext> configContexts = sheetContext.getConfigContexts();
            if (sheetContext.isFailed()) {
                failed = true;
            }
            if (CollectionUtils.isEmpty(configContexts)) {
                continue;
            }
            Map<String, DtpKnowledgeInfoCodeDTO> infoCodeDefine = classCodeInfoCodeAndDefine
                    .getOrDefault(classCode, Collections.emptyMap());
            Iterator<InfoCodeConfigImportContext> iterator = configContexts.iterator();
            // 配置上下文是否任意一个失败
            boolean configContextAnyFailed = false;
            while (iterator.hasNext()) {
                InfoCodeConfigImportContext configContext = iterator.next();
                int rowNum = configContext.getRow().getRowNum();
                InfoCodeTypeEnum infoCodeType = configContext.getInfoCodeType();
                String infoCode = configContext.getInfoCode();
                if (configContext.isFailed()) {
                    configContextAnyFailed = true;
                }
                if (infoCodeType.isKnowledgeInfoCode() && !infoCodeDefine.containsKey(infoCode)) {
                    logProjectInstance().error("信息点编码未在数据字典定义", Arrays.asList(
                            Pair.of("sheetName", sheetName),
                            Pair.of("rowNum", rowNum),
                            Pair.of("infoCode", infoCode)
                    ));
                    if (skipNotDefineInfoCode) {
                        iterator.remove();
                        continue;
                    }
                    configContext.getErrorMessages().add(String.format(LocaleMessageUtil.getMessage("key_d7f4c70986794a4d8a53244cbb1c51ad"), sheetName, rowNum + 1, infoCode));
                    configContext.setFailed(true);
                    configContextAnyFailed = true;
                }
            }
            if (configContextAnyFailed) {
                sheetContext.setFailed(true);
                failed = true;
            }
        }
        if (failed) {
            context.setFailed(true);
        }
    }

    /**
     * 缓存重建执行器
     * <p>
     * 内部类，继承 BaseTask，用于执行跨库缓存重建
     *
     * @author JiYinchuan
     * @since 7.250401.0
     */
    private class CacheRebuildExecutor extends BaseTask {

        /**
         * 成功导入的配置上下文。
         *
         * @since v7.260701.0
         */
        private final @NonNull List<InfoCodeImportContext> contexts;

        /**
         * 本次公共库替换或删除前的模板类型ID。
         *
         * @since v7.260701.0
         */
        private final @NonNull Set<Long> deletedTemplateTypeIds;

        /**
         * 项目级缓存处理失败信息。当前遍历为同步执行，使用有序容器便于排查。
         *
         * @since v7.260701.0
         */
        private final @NonNull Map<String, String> failedProjects = new LinkedHashMap<>();

        /**
         * 构造器
         *
         * @param contexts               成功导入的上下文列表
         * @param deletedTemplateTypeIds 被替换或删除的旧模板类型ID
         * @since v7.260701.0
         */
        public CacheRebuildExecutor(
                @NonNull List<InfoCodeImportContext> contexts,
                @NonNull Set<Long> deletedTemplateTypeIds) {
            super();
            this.contexts = contexts;
            this.deletedTemplateTypeIds = deletedTemplateTypeIds;
        }

        /**
         * 执行缓存重建
         *
         * @return 失败项目信息
         * @since v7.260701.0
         */
        public @NonNull Map<String, String> execute() {
            if (CollectionUtils.isEmpty(this.contexts)
                    && CollectionUtils.isEmpty(this.deletedTemplateTypeIds)) {
                return this.failedProjects;
            }
            String originalTenant = ContextUtil.getTenant();
            String originalGroupCode = ContextUtil.getGroupCode();
            String originalProjectId = ContextUtil.getProjectId();
            Map<String, String> originalMdcContext = MDC.getCopyOfContextMap();
            // 公共配置接口不能让调用方传入的路由上下文影响“查询全部集团、全部项目”。
            ContextUtil.setTenant(null);
            ContextUtil.setGroupCode(null);
            ContextUtil.setProjectId(null);
            // 这里必须使用同步遍历并等待全部项目结束；异步提交后立即返回会让公共库已更新而
            // 项目缓存尚未处理完，且会误以为所有集团项目都已完成。
            try {
                super.handlerProject(null, null, project -> {
                    this.executeProject(ContextUtil.getGroupCode(), project);
                    return true;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                super.logProjectInstance().error("遍历集团项目缓存时线程中断", e);
            } finally {
                // BaseTask 会在遍历中切换上下文，调用结束后恢复进入公共接口时的线程上下文。
                ContextUtil.setTenant(originalTenant);
                ContextUtil.setGroupCode(originalGroupCode);
                ContextUtil.setProjectId(originalProjectId);
                if (MapUtils.isEmpty(originalMdcContext)) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(originalMdcContext);
                }
            }
            return this.failedProjects;
        }

        /**
         * 执行单个项目的缓存重建
         *
         * @param groupCode 集团编码
         * @param project   项目
         * @since v7.260701.0
         */
        private void executeProject(
                @NonNull String groupCode,
                @NonNull ProjectDTO project) {
            try {
                if (CollectionUtils.isNotEmpty(this.deletedTemplateTypeIds)) {
                    InfoCodeImportServiceImpl.this.cacheInfoCodeTemplateRepository
                            .del(this.deletedTemplateTypeIds);
                }
                // 根据导入上下文构造查询参数，逐个重建
                for (InfoCodeImportContext context : this.contexts) {
                    InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
                    queryDaoParam.setRunningMonitorType(context.getRunningMonitorType());
                    queryDaoParam.setRunningMonitorSecondType(context.getRunningMonitorSecondType());
                    queryDaoParam.setTemplateType(context.getTemplateType());
                    // 设置设备类编码列表
                    Set<String> classCodes = context.getSheetContexts().stream()
                            .map(InfoCodeSheetImportContext::getClassCode)
                            .collect(Collectors.toSet());
                    queryDaoParam.setClassCodes(classCodes);
                    InfoCodeImportServiceImpl.this.cacheInfoCodeTemplateRepository
                            .rebuildByParam(queryDaoParam);

                    // FIXED_OBJECT + DEFAULT 配置影响机房安全策略的 roomFuncType 过滤逻辑, 需要联动重建机房安全物理世界缓存
                    if (context.getRunningMonitorSecondType() == RunningMonitorSecondTypeEnum.FIXED_OBJECT
                            && context.getTemplateType() == InfoCodeTemplateTypeEnum.DEFAULT) {
                        InfoCodeImportServiceImpl.this.cacheRwdDomainService
                                .buildCache(RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY);
                    }
                }
                super.logProjectInstance(project.getProjectId()).info("项目缓存重建成功",
                        Pair.of("projectId", project.getProjectId()));
            } catch (Exception e) {
                String failureKey = groupCode + ":" + project.getProjectId();
                this.failedProjects.put(failureKey, e.getMessage());
                super.logProjectInstance(project.getProjectId()).error("项目缓存重建失败", e);
            }
        }

        @Override
        public String logTag() {
            return "信息点配置-缓存重建执行器";
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-导入服务";
    }
}
