package com.persagy.apccontrol.common.service.demand.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.param.demand.template.*;
import com.persagy.apccontrol.basic.model.vo.KeyValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateCalendarService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.*;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandCalendarConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTagEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.evnet.DemandTemplateChangeEvent;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.service.demand.DemandCalendarService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求管理-需求日历模版服务
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getTemplate</b> - 查询模版</li>
 *     <li><b>getDateSetting</b> - 根据日期查询日期配置</li>
 *     <li><b>listTemplate</b> - 查询模版列表</li>
 *     <li><b>listDateSetting</b> - 查询模版日期配置</li>
 *     <li><b>editTemplate</b> - 编辑模版</li>
 *     <li><b>clearCalendar</b> - 清除日历配置</li>
 *     <li><b>applyTemplateByDate</b> - 根据日期应用模版</li>
 *     <li><b>resetTemplateCalendarByTemplate</b> - 根据模版重置需求日历</li>
 *     <li><b>checkTemplateName</b> - 检查模版名称</li>
 *     <li><b>checkExistsDateSettingInFuture</b> - 检查未来日期是否存在配置</li>
 *     <li><b>initTemplate</b> - 初始化模版</li>
 * </ul>
 * <p>------------------------- 内部方法 -------------------------
 * <ul>
 *     <li><b>queryProjectCalendarDayDateTagOfAllowDay</b> - 查询允许时间范围内的项目工作历日期标签</li>
 *     <li><b>generateInitializedTemplateAddParams</b> - 生成初始化模版添加参数</li>
 *     <li><b>analyzeTemplateTagByCalendarDateTag</b> - 根据工作历日期标签解析模版标签</li>
 *     <li><b>saveCustomTemplate</b> - 保存自定义模版</li>
 *     <li><b>updateTemplates</b> - 更新模版</li>
 *     <li><b>applyTemplate</b> - 应用模版</li>
 *     <li><b>removeCustomTemplate</b> - 删除自定义模版</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandCalendarServiceImpl implements DemandCalendarService {

    private static final String NOT_FOUND_TEMPLATE_MESSAGE = LocaleMessageUtil.getMessage("key_818b608791ee4ff5bd430ef00b2bfa31");

    private final DemandCalendarConverter demandCalendarConverter;
    private final MidPermissionClientService midPermissionClientService;
    private final MidCalendarClientService midCalendarClientService;
    private final DemandTemplateService demandTemplateService;
    private final DemandTemplateCalendarService demandTemplateCalendarService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editTemplate(@NonNull DemandTemplateUpdateParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        List<DemandTemplateUpdateParam.Template> templates = param.getTemplates();
        Set<Long> toBeDelTemplateIds = param.getToBeDelTemplateIds();

        List<DemandTemplateUpdateParam.Template> addCustomTemplateParams = new ArrayList<>();
        Map<Long, DemandTemplateUpdateParam.Template> templateIdAndParam = new HashMap<>(16);
        for (DemandTemplateUpdateParam.Template template : templates) {
            Long templateId = template.getTemplateId();

            if (Objects.isNull(templateId)) {
                addCustomTemplateParams.add(template);
            } else {
                templateIdAndParam.put(templateId, template);
            }
        }
        return saveCustomTemplate(projectId, templateType, addCustomTemplateParams)
                | updateTemplates(projectId, templateType, templateIdAndParam)
                | removeCustomTemplate(projectId, templateType, toBeDelTemplateIds);
    }

    @Override
    public boolean delTemplate(@NonNull DemandTemplateRemoveParam param) {
        return removeCustomTemplate(ContextUtil.getProjectId(), param.getTemplateType(), param.getToBeDelTemplateIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearCalendar(@NonNull DemandTemplateClearCalendarParam param) {
        param.checkAndSet();
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        DemandTypeEnum demandType = templateType.toDemandTypeEnum();
        Boolean isReset = param.getIsReset();
        Set<LocalDate> dates = param.getDates();
        if (CollectionUtils.isEmpty(dates)) {
            return false;
        }

        DemandTemplateCalendarQueryDaoParam queryDaoParam = new DemandTemplateCalendarQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDemandType(demandType);
        queryDaoParam.setDemandTemplateType(templateType);

        queryDaoParam.setWorkDates(dates);
        // 删除所有模版工作历
        List<DemandTemplateCalendar> dbTemplateCalendars = demandTemplateCalendarService.list(projectId, queryDaoParam);
        demandTemplateCalendarService.removeByIdIgnore(dbTemplateCalendars.stream()
                .map(BaseAssignIdDO::getId).collect(Collectors.toList()));

        // 为重置时进行重新初始化所有模版
        if (BooleanUtils.isTrue(isReset)) {
            DemandTemplateQueryDaoParam templateQueryDaoParam = new DemandTemplateQueryDaoParam();
            templateQueryDaoParam.setDemandType(demandType);
            templateQueryDaoParam.setDemandTemplateType(templateType);
            templateQueryDaoParam.setIsEnabled(true);
            List<DemandTemplate> dbTemplates = demandTemplateService.list(templateQueryDaoParam);
            Set<Long> dbTemplateIds = dbTemplates.stream().map(BaseAssignIdDO::getId).collect(Collectors.toSet());
            // 初始化指定日期所有已启用的模版
            resetTemplateCalendarByTemplate(projectId, dbTemplateIds, dates, true);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyTemplateByDate(@NonNull DemandTemplateApplyByDateParam param) {
        param.checkAndSet();
        String projectId = param.getProjectId();
        Long templateId = param.getTemplateId();
        Set<LocalDate> dates = param.getDates();

        DemandTemplate dbTemplate = demandTemplateService.getByIdValidate(templateId, NOT_FOUND_TEMPLATE_MESSAGE);
        Boolean isEnabled = dbTemplate.getIsEnabled();
        if (BooleanUtils.isFalse(isEnabled)) {
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_2a5a2e02aa9742f4ac4f004a61d2aa80"));
        }
        return applyTemplate(projectId, templateId, dates,
                true, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetTemplateCalendarByTemplate(
            @NonNull String projectId,
            @Nullable Collection<Long> templateIds,
            @Nullable Set<LocalDate> dates,
            boolean resetCustomizedSetting) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        List<DemandTemplate> dbTemplates = demandTemplateService.listByIdValidate(
                templateIds, NOT_FOUND_TEMPLATE_MESSAGE);
        Map<DemandTemplateTypeEnum, Map<String, Long>> templateTypeAndTemplateTagId = new HashMap<>();
        dbTemplates.forEach(item -> MapExecutor.dynamicPut(templateTypeAndTemplateTagId, item.getDemandTemplateType(),
                item.getDemandTemplateTag(), item.getId(), HashMap::new));

        // 清空指定日期范围内所有模版日历
        clearTemplateCalendarOnAllowApplyDate(projectId, templateIds, dates, resetCustomizedSetting);

        // 根据工作历日期标签应用模版
        applyTemplateByCalendarDateTag(projectId, templateTypeAndTemplateTagId, dates, resetCustomizedSetting);
    }

    @Override
    public KeyValueVO checkTemplateName(@NonNull DemandTemplateNameCheckParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String templateName = param.getTemplateName();
        Long templateId = param.getTemplateId();
        Boolean isThrows = param.getIsThrows();

        int templateNameAllowLength = DemandTemplateNameCheckParam.TEMPLATE_NAME_ALLOW_LENGTH;
        if (templateName.length() > templateNameAllowLength) {
            String errorCode = ErrorCodeConstants.USER_ERROR_A0420.getCode();
            String errorMessage = String.format(LocaleMessageUtil.getMessage("key_a7aa76f0a8104afeaa056487daf3b1e1"), templateNameAllowLength);
            if (BooleanUtils.isTrue(isThrows)) {
                throw new BusinessException(errorCode, errorMessage);
            } else {
                return new KeyValueVO(errorCode, errorMessage);
            }
        }

        // 查询需求类型下所有模版
        List<DemandTemplate> dbTemplates = demandTemplateService.list(
                new DemandTemplateQueryDaoParam().setDemandTemplateType(templateType));
        for (DemandTemplate dbTemplate : dbTemplates) {
            // 当传了模版ID时, 代表编辑, 在校验时需要过滤当前数据
            if (Objects.nonNull(templateId) && Objects.equals(templateId, dbTemplate.getId())) {
                continue;
            }
            if (StringUtils.equals(templateName, dbTemplate.getDemandTemplateName())) {
                String errorCode = ErrorCodeConstants.USER_ERROR_A0402.getCode();
                String errorMessage = LocaleMessageUtil.getMessage("key_83a59dac3d66432e831bb494c3b58a60");
                if (BooleanUtils.isTrue(isThrows)) {
                    throw new BusinessException(errorCode, errorMessage);
                } else {
                    return new KeyValueVO(errorCode, errorMessage);
                }
            }
        }
        return new KeyValueVO(ErrorCodeConstants.SUCCESS.getCode(), ErrorCodeConstants.SUCCESS.getMessage());
    }

    @Override
    public KeyValueVO checkExistsDateSettingInFuture(
            @NonNull DemandTemplateExistsDateSettingInFutureCheckParam param) {
        String projectId = param.getProjectId();
        Long templateId = param.getTemplateId();
        Boolean isThrows = param.getIsThrows();

        DemandTemplate dbTemplate = demandTemplateService.getByIdValidate(templateId, NOT_FOUND_TEMPLATE_MESSAGE);

        DemandTemplateCalendarQueryDaoParam calendarQueryDaoParam =
                new DemandTemplateCalendarQueryDaoParam()
                        .queryTodayOrFutureWorkDate()
                        .setProjectId(projectId)
                        .setDemandType(dbTemplate.getDemandType())
                        .setDemandTemplateType(dbTemplate.getDemandTemplateType())
                        .setRelDemandTemplateId(dbTemplate.getId());
        List<DemandTemplateCalendar> futureTemplateCalendars =
                demandTemplateCalendarService.list(projectId, calendarQueryDaoParam);
        if (CollectionUtils.isNotEmpty(futureTemplateCalendars)) {
            String errorCode = ErrorCodeConstants.USER_ERROR_A0402.getCode();
            String errorMessage = LocaleMessageUtil.getMessage("key_2941e85138bb4a03838168af124b97e6");
            if (BooleanUtils.isTrue(isThrows)) {
                throw new BusinessException(errorCode, errorMessage);
            } else {
                return new KeyValueVO(errorCode, errorMessage);
            }
        }

        return new KeyValueVO(ErrorCodeConstants.SUCCESS.getCode(), ErrorCodeConstants.SUCCESS.getMessage());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<DemandTemplateTypeEnum, List<String>> initTemplate(@NonNull String projectId) {
        midPermissionClientService.getProject(false, true, projectId);

        Map<DemandTemplateTypeEnum, List<String>> templateTypeAndTags = new HashMap<>(16);
        for (DemandTemplateTypeEnum templateType : DemandTemplateTypeEnum.values()) {
            DemandTemplateQueryDaoParam queryDaoParam = new DemandTemplateQueryDaoParam()
                    .setDemandTemplateType(templateType).setSt(null);
            List<DemandTemplate> dbTemplates = demandTemplateService.list(queryDaoParam);

            // 待初始化的模版标签枚举
            List<String> initializedTags = new ArrayList<>(INITIALIZE_TAGS);
            for (DemandTemplate dbTemplate : dbTemplates) {
                String dbTemplateTag = dbTemplate.getDemandTemplateTag();
                // 如果查询出来的数据已经存在初始化模版类型, 则从待初始化模版类型中移除
                if (INITIALIZE_TAGS.contains(dbTemplateTag)) {
                    LogExecutor.project(projectId).info(log, logTag(), "已存在模版, 跳过初始化", Arrays.asList(
                            Pair.of("demandTemplateType", templateType),
                            Pair.of("dbTemplateTag", dbTemplateTag))
                    );
                    initializedTags.remove(dbTemplateTag);
                }
            }

            // 当无待初始化的模版标签, 则存在全部初始化模版, 直接返回
            if (CollectionUtils.isEmpty(initializedTags)) {
                continue;
            }

            LogExecutor.project(projectId).info(log, logTag(), "开始初始化模版", Arrays.asList(
                    Pair.of("demandTemplateType", templateType),
                    Pair.of("initializedTags", initializedTags))
            );
            List<DemandTemplateSaveDaoParam> initializedTemplateAddParams =
                    generateInitializedTemplateAddParams(templateType, initializedTags);
            demandTemplateService.add(projectId, initializedTemplateAddParams);

            // 新增了初始化模版后重新进行查询
            queryDaoParam.setDemandTemplateTags(initializedTags);
            queryDaoParam.setIsEnabled(true);
            queryDaoParam.setSt(StateTypeEnum.VALID);
            List<DemandTemplate> dbInitializedEnableTemplates = demandTemplateService.list(queryDaoParam);
            // 查询刚刚初始化的已启用模版并应用
            if (CollectionUtils.isNotEmpty(dbInitializedEnableTemplates)) {
                Set<Long> templateIds = dbInitializedEnableTemplates.stream()
                        .map(BaseAssignIdDO::getId).collect(Collectors.toSet());
                resetTemplateCalendarByTemplate(projectId, templateIds, null, false);
            }
            templateTypeAndTags.put(templateType, initializedTags);
        }
        return templateTypeAndTags;
    }

    /**
     * 清除过时的模版
     *
     * @param projectId              项目ID
     * @param deprecatedDemandType   过时的需求类型
     * @param deprecatedTemplateType 过时的模版类型
     * @since 24Q1 T3S1-5.17.0
     */
    private void clearDeprecatedTemplate(
            @NonNull String projectId,
            @Nullable String deprecatedDemandType,
            @Nullable String deprecatedTemplateType) {
        // 清除过时的模版类型
        demandTemplateService.lambdaUpdate()
                .eq(DemandTemplate::getProjectId, projectId)
                .eq(StringUtils.isNotBlank(deprecatedDemandType), DemandTemplate::getDemandType, deprecatedDemandType)
                .eq(StringUtils.isNotBlank(deprecatedTemplateType), DemandTemplate::getDemandTemplateType, deprecatedTemplateType)
                .remove();

        // 清除过时的模版日历
        demandTemplateCalendarService.lambdaUpdate()
                .eq(DemandTemplateCalendar::getProjectId, projectId)
                .eq(StringUtils.isNotBlank(deprecatedDemandType), DemandTemplateCalendar::getDemandType, deprecatedDemandType)
                .eq(StringUtils.isNotBlank(deprecatedTemplateType), DemandTemplateCalendar::getDemandTemplateType, deprecatedTemplateType)
                .remove();
    }

    /**
     * 根据工作历日期标签应用模版
     *
     * @param projectId                    项目ID
     * @param templateTypeAndTemplateTagId 模版类型 -> [模版标签 -> 模版ID]
     * @param dates                        待应用的日期, 为 {@literal null} 时则应用有效范围内所有未来日期
     * @param updateCustomizedSetting      是否更新自定义配置, 为 {@code false} 时则仅更新系统配置
     * @since v6.0.0
     */
    private void applyTemplateByCalendarDateTag(
            @NonNull String projectId,
            @NonNull Map<DemandTemplateTypeEnum, Map<String, Long>> templateTypeAndTemplateTagId,
            @Nullable Set<LocalDate> dates,
            boolean updateCustomizedSetting) {
        // 查询最新工作历标签并应用
        Map<Long, Set<LocalDate>> templateIdAndDates = new HashMap<>(64);
        List<MidCalendarDayDateTagResponse> dayDateTags = queryProjectCalendarDayDateTagOfAllowDay(projectId, dates);
        for (MidCalendarDayDateTagResponse dayDateTag : dayDateTags) {
            LocalDate date = dayDateTag.getCurrentDay().toLocalDate();
            List<String> templateTags = analyzeTemplateTagByCalendarDateTag(dayDateTag);

            // 当模版标签为自定义时直接跳过, 避免后续无用查询, 自定义标签不支持初始化
            if (CollectionUtils.isEmpty(templateTags) || Collections.disjoint(INITIALIZE_TAGS, templateTags)) {
                continue;
            }
            for (Map<String, Long> templateTagAndId : templateTypeAndTemplateTagId.values()) {
                for (String templateTag : templateTags) {
                    Long templateId = templateTagAndId.get(templateTag);
                    if (Objects.nonNull(templateId)) {
                        MapExecutor.dynamicPut(templateIdAndDates, templateId, date, HashSet::new);
                        break;
                    }
                }
            }
        }
        if (MapUtils.isNotEmpty(templateIdAndDates)) {
            templateIdAndDates.forEach((k, v) -> applyTemplate(projectId, k, v,
                    false, updateCustomizedSetting));
        }
    }

    /**
     * 清除允许应用日期内的模版日历
     *
     * @param projectId              项目ID
     * @param templateIds            待清除的模版ID
     * @param dates                  待清除的日期, 为空时则清除当天和所有未来日期
     * @param clearCustomizedSetting 是否清除自定义配置
     * @since v6.0.0
     */
    private void clearTemplateCalendarOnAllowApplyDate(
            @NonNull String projectId,
            @NonNull Collection<Long> templateIds,
            @Nullable Set<LocalDate> dates,
            boolean clearCustomizedSetting) {
        DemandTemplateCalendarQueryDaoParam queryDaoParam = new DemandTemplateCalendarQueryDaoParam()
                .setIsCustomizedSetting(clearCustomizedSetting ? null : false)
                .setRelDemandTemplateIds(templateIds);
        if (CollectionUtils.isNotEmpty(dates)) {
            queryDaoParam.setWorkDates(dates);
        } else {
            queryDaoParam.queryTodayOrFutureWorkDate();
        }
        List<DemandTemplateCalendar> existsTemplateCalendars = demandTemplateCalendarService.list(projectId, queryDaoParam);
        demandTemplateCalendarService.removeByIdIgnore(existsTemplateCalendars.stream()
                .map(BaseAssignIdDO::getId).collect(Collectors.toSet()));

        Set<Long> clearApplyTemplateIds = existsTemplateCalendars.stream()
                .map(DemandTemplateCalendar::getRelDemandTemplateId)
                .collect(Collectors.toSet());
        List<DemandTemplate> demandTemplates = demandTemplateService.listByIdIgnore(clearApplyTemplateIds);
        if (CollectionUtils.isNotEmpty(demandTemplates)) {
            Map<DemandTypeEnum, List<DemandTemplate>> demandTypeAndTemplates = demandTemplates.stream()
                    .collect(Collectors.groupingBy(DemandTemplate::getDemandType));
            for (Map.Entry<DemandTypeEnum, List<DemandTemplate>> entry : demandTypeAndTemplates.entrySet()) {
                notifyTemplateChanged(projectId, entry.getKey(), DemandTemplateChangeTypeEnum.APPLIED, entry.getValue(), null);
            }
        }
    }

    /**
     * 保存自定义模版
     *
     * @param projectId               项目ID
     * @param templateType            模版类型
     * @param addCustomTemplateParams 自定义模版新增参数
     * @return 是否成功
     * @since v6.0.0
     */
    private boolean saveCustomTemplate(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @Nullable List<DemandTemplateUpdateParam.Template> addCustomTemplateParams) {
        DemandTypeEnum demandType = templateType.toDemandTypeEnum();
        if (CollectionUtils.isEmpty(addCustomTemplateParams)) {
            return false;
        }
        List<DemandTemplateSaveDaoParam> saveDaoParams = new ArrayList<>();
        for (DemandTemplateUpdateParam.Template addCustomTemplateParam : addCustomTemplateParams) {
            String templateName = addCustomTemplateParam.getTemplateName();
            Boolean isEnabled = addCustomTemplateParam.getIsEnabled();

            DemandTemplateNameCheckParam nameCheckParam = new DemandTemplateNameCheckParam();
            nameCheckParam.setProjectId(projectId);
            nameCheckParam.setTemplateType(templateType);
            nameCheckParam.setTemplateName(templateName);
            nameCheckParam.setIsThrows(true);
            checkTemplateName(nameCheckParam);

            DemandTemplateSaveDaoParam saveDaoParam = new DemandTemplateSaveDaoParam();
            saveDaoParam.setDemandType(demandType);
            saveDaoParam.setDemandTemplateType(templateType);
            saveDaoParam.setDemandTemplateName(templateName);
            saveDaoParam.setIsEnabled(isEnabled);
            saveDaoParams.add(saveDaoParam);
        }
        List<DemandTemplate> demandTemplates = demandTemplateService.addCustom(projectId, saveDaoParams);
        notifyTemplateChanged(projectId, templateType.toDemandTypeEnum(),
                DemandTemplateChangeTypeEnum.INSERT, null, demandTemplates);
        return true;
    }

    /**
     * 更新模版
     *
     * @param projectId                项目ID
     * @param templateType             模版类型
     * @param templateIdAndUpdateParam 模版ID -> 更新参数
     * @return 是否成功
     * @since v6.0.0
     */
    private boolean updateTemplates(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Map<Long, DemandTemplateUpdateParam.Template> templateIdAndUpdateParam) {
        if (MapUtils.isEmpty(templateIdAndUpdateParam)) {
            return false;
        }
        List<DemandTemplate> dbTemplates = demandTemplateService.list(
                new DemandTemplateQueryDaoParam().setIds(templateIdAndUpdateParam.keySet()));
        List<DemandTemplateUpdateDaoParam> updateDaoParams = new ArrayList<>();
        for (DemandTemplate dbTemplate : dbTemplates) {
            Long templateId = dbTemplate.getId();
            String templateTag = dbTemplate.getDemandTemplateTag();
            DemandTemplateUpdateParam.Template editTemplateParam = templateIdAndUpdateParam.get(templateId);
            if (Objects.isNull(editTemplateParam)) {
                continue;
            }
            DemandTemplateUpdateDaoParam updateDaoParam = new DemandTemplateUpdateDaoParam().setId(templateId);
            // 当 [是否启用] 状态变更时同步初始化模版日历
            if (!Objects.equals(dbTemplate.getIsEnabled(), editTemplateParam.getIsEnabled())) {
                // 模版禁用时清空模版日历
                if (BooleanUtils.isFalse(editTemplateParam.getIsEnabled())) {
                    clearTemplateCalendarOnAllowApplyDate(projectId, Lists.newArrayList(templateId), null, true);
                }
                // 模版启用时应用模版ID
                if (BooleanUtils.isTrue(editTemplateParam.getIsEnabled())) {
                    Map<DemandTemplateTypeEnum, Map<String, Long>> templateTypeAndTemplateTagId = new HashMap<>();
                    MapExecutor.dynamicPut(templateTypeAndTemplateTagId,
                            templateType, templateTag, templateId, HashMap::new);
                    applyTemplateByCalendarDateTag(projectId, templateTypeAndTemplateTagId, null, false);
                }
            }
            // 非初始化模版时校验模版名称
            if (BooleanUtils.isNotTrue(dbTemplate.getIsInitialized())) {
                // 非初始化模版时校验模版名称
                if (StringUtils.isBlank(editTemplateParam.getTemplateName())) {
                    throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_12e4d31bd767464b9c84aa614b6c1ff3"));
                }
                DemandTemplateNameCheckParam nameCheckParam = new DemandTemplateNameCheckParam();
                nameCheckParam.setProjectId(projectId);
                nameCheckParam.setTemplateType(templateType);
                nameCheckParam.setTemplateName(editTemplateParam.getTemplateName());
                nameCheckParam.setTemplateId(templateId);
                nameCheckParam.setIsThrows(true);
                checkTemplateName(nameCheckParam);

                // 仅非初始化模版时可以修改模版名称
                updateDaoParam.setDemandTemplateName(editTemplateParam.getTemplateName());
            }
            updateDaoParam.setIsEnabled(editTemplateParam.getIsEnabled());
            updateDaoParams.add(updateDaoParam);
        }
        List<DemandTemplate> demandTemplates = demandTemplateService.edit(projectId, updateDaoParams);
        notifyTemplateChanged(projectId, templateType.toDemandTypeEnum(),
                DemandTemplateChangeTypeEnum.UPDATE, dbTemplates, demandTemplates);
        return true;
    }

    /**
     * 删除自定义模版
     *
     * @param projectId          项目ID
     * @param templateType       模版类型
     * @param toBeDelTemplateIds 待删除模版ID
     * @return 是否成功
     * @since v6.0.0
     */
    private boolean removeCustomTemplate(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Collection<Long> toBeDelTemplateIds) {
        if (CollectionUtils.isEmpty(toBeDelTemplateIds)) {
            return false;
        }
        List<DemandTemplate> dbTemplates = demandTemplateService.listByIdIgnore(toBeDelTemplateIds);
        for (DemandTemplate dbTemplate : dbTemplates) {
            String templateTag = dbTemplate.getDemandTemplateTag();
            if (INITIALIZE_TAGS.contains(templateTag)) {
                throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(),
                        String.format(LocaleMessageUtil.getMessage("key_73df645e9e044d0999d1c02c5cc14db9"), dbTemplate.getDemandTemplateName()));
            }
        }
        clearTemplateCalendarOnAllowApplyDate(projectId, toBeDelTemplateIds, null, true);
        List<DemandTemplate> toBeDelTemplates = demandTemplateService.list(
                new DemandTemplateQueryDaoParam().setIds(toBeDelTemplateIds));
        boolean result = demandTemplateService.removeByIdIgnore(toBeDelTemplateIds);
        notifyTemplateChanged(projectId, templateType.toDemandTypeEnum(),
                DemandTemplateChangeTypeEnum.REMOVE, toBeDelTemplates, null);
        return result;
    }

    /**
     * 通知模版变动消息
     *
     * @param dataType     数据操作类型
     * @param oldTemplates 操作前模版
     * @param newTemplates 操作后模版
     * @since 24Q1 T3S1-5.17.0
     */
    private void notifyTemplateChanged(
            @NonNull String projectId,
            @NonNull DemandTypeEnum demandType,
            @NonNull DemandTemplateChangeTypeEnum dataType,
            @Nullable Collection<DemandTemplate> oldTemplates,
            @Nullable Collection<DemandTemplate> newTemplates) {
        if (CollectionUtils.isEmpty(oldTemplates) && CollectionUtils.isEmpty(newTemplates)) {
            return;
        }
        Map<Long, DemandTemplate> oldTemplateIdAndData = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(oldTemplates)) {
            oldTemplateIdAndData = oldTemplates.stream().collect(Collectors.toMap(
                    BaseAssignIdDO::getId, Function.identity(), (o1, o2) -> o1));
        }
        Map<Long, DemandTemplate> newTemplateIdAndData = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(newTemplates)) {
            newTemplateIdAndData = newTemplates.stream().collect(Collectors.toMap(
                    BaseAssignIdDO::getId, Function.identity(), (o1, o2) -> o1));
        }
        Collection<Long> intersectionTemplateIds;
        if (Objects.isNull(oldTemplates)) {
            intersectionTemplateIds = newTemplateIdAndData.keySet();
        } else if (Objects.isNull(newTemplates)) {
            intersectionTemplateIds = oldTemplateIdAndData.keySet();
        } else {
            intersectionTemplateIds = CollectionUtils.intersection(
                    oldTemplateIdAndData.keySet(), newTemplateIdAndData.keySet());
        }
        for (Long intersectionTemplateId : intersectionTemplateIds) {
            DemandTemplateChangeEvent.Message message = new DemandTemplateChangeEvent.Message();
            message.setDataType(dataType);
            message.setOldTemplate(MapUtils.getObject(oldTemplateIdAndData, intersectionTemplateId));
            message.setNewTemplate(MapUtils.getObject(newTemplateIdAndData, intersectionTemplateId));
            applicationEventPublisher.publishEvent(new DemandTemplateChangeEvent(message));
        }
    }

    /**
     * 应用模版
     *
     * @param projectId               项目ID
     * @param templateId              模版ID
     * @param applyDates              应用日期
     * @param customizedSetting       是否为用户自定义配置, 为 {@code false} 时则代表为系统赋值
     * @param updateCustomizedSetting 是否强制更新自定义配置, 为 {@code false} 时则仅更新系统配置
     * @return 是否成功
     * @since v6.0.0
     */
    private boolean applyTemplate(
            @NonNull String projectId, @NonNull Long templateId,
            @NonNull Set<LocalDate> applyDates, boolean customizedSetting,
            boolean updateCustomizedSetting) {
        DemandTemplate dbTemplate = demandTemplateService.getByIdValidate(templateId, NOT_FOUND_TEMPLATE_MESSAGE);
        DemandTypeEnum demandType = dbTemplate.getDemandType();
        DemandTemplateTypeEnum templateType = dbTemplate.getDemandTemplateType();

        DemandTemplateCalendarQueryDaoParam queryDaoParam = new DemandTemplateCalendarQueryDaoParam();
        queryDaoParam.setDemandType(demandType);
        queryDaoParam.setDemandTemplateType(templateType);
        queryDaoParam.setWorkDates(applyDates);
        List<DemandTemplateCalendar> dbTemplateCalendars = demandTemplateCalendarService.list(projectId, queryDaoParam);
        Map<LocalDate, DemandTemplateCalendar> dateAndTemplateCalendar = dbTemplateCalendars.stream()
                .collect(Collectors.toMap(DemandTemplateCalendar::getWorkDate, Function.identity(), (o1, o2) -> o1));
        List<DemandTemplateCalendarUpdateDaoParam> updateDaoParams = new ArrayList<>();
        List<DemandTemplateCalendarSaveDaoParam> saveDaoParams = new ArrayList<>();
        for (LocalDate date : applyDates) {
            DemandTemplateCalendar templateCalendar = dateAndTemplateCalendar.get(date);
            if (Objects.isNull(templateCalendar)) {
                DemandTemplateCalendarSaveDaoParam saveDaoParam = new DemandTemplateCalendarSaveDaoParam();
                saveDaoParam.setDemandType(demandType);
                saveDaoParam.setDemandTemplateType(templateType);
                saveDaoParam.setWorkDate(date);
                saveDaoParam.setIsCustomizedSetting(customizedSetting);
                saveDaoParam.setRelDemandTemplateId(templateId);
                saveDaoParams.add(saveDaoParam);
                continue;
            }
            // 当不为自定义配置 (即为系统赋值时), 判断当前配置是否为自定义配置, 为自定义配置时跳过执行, 即自定义配置不进行系统覆盖
            if (!updateCustomizedSetting && BooleanUtils.isTrue(templateCalendar.getIsCustomizedSetting())) {
                continue;
            }
            DemandTemplateCalendarUpdateDaoParam updateDaoParam = new DemandTemplateCalendarUpdateDaoParam();
            updateDaoParam.setId(templateCalendar.getId());
            updateDaoParam.setIsCustomizedSetting(customizedSetting);
            updateDaoParam.setRelDemandTemplateId(templateId);
            updateDaoParams.add(updateDaoParam);
        }
        // 编辑需求模版日历
        Set<Long> toBeEditTemplateCalendarIds = updateDaoParams.stream()
                .map(DemandTemplateCalendarUpdateDaoParam::getId)
                .collect(Collectors.toSet());
        Set<Long> oldTemplateIds = demandTemplateCalendarService.listByIdIgnore(toBeEditTemplateCalendarIds).stream()
                .map(DemandTemplateCalendar::getRelDemandTemplateId)
                .collect(Collectors.toSet());
        List<DemandTemplate> oldTemplates = null;
        if (CollectionUtils.isNotEmpty(oldTemplateIds)) {
            oldTemplates = demandTemplateService.listByIdIgnore(oldTemplateIds);
        }
        boolean result = demandTemplateCalendarService.edit(projectId, updateDaoParams);
        Set<Long> newTemplateIds = updateDaoParams.stream()
                .map(DemandTemplateCalendarUpdateDaoParam::getRelDemandTemplateId)
                .collect(Collectors.toSet());
        List<DemandTemplate> newTemplates = null;
        if (CollectionUtils.isNotEmpty(newTemplateIds)) {
            newTemplates = demandTemplateService.listByIdIgnore(newTemplateIds);
        }
        notifyTemplateChanged(projectId, templateType.toDemandTypeEnum(),
                DemandTemplateChangeTypeEnum.APPLIED, oldTemplates, newTemplates);

        // 新增需求模版日历
        result = result | demandTemplateCalendarService.add(projectId, saveDaoParams);
        Set<Long> toBeAddApplyTemplateIds = saveDaoParams.stream()
                .map(DemandTemplateCalendarSaveDaoParam::getRelDemandTemplateId)
                .collect(Collectors.toSet());
        List<DemandTemplate> toBeAddApplyTemplates = demandTemplateService.listByIdIgnore(toBeAddApplyTemplateIds);
        notifyTemplateChanged(projectId, templateType.toDemandTypeEnum(),
                DemandTemplateChangeTypeEnum.APPLIED, null, toBeAddApplyTemplates);
        return result;
    }

    /**
     * 根据工作历日期标签解析模版标签
     *
     * @param dayDateTag 工作历日期标签
     * @return 需求模版标签
     * @since v6.0.0
     */
    private List<String> analyzeTemplateTagByCalendarDateTag(
            @Nullable MidCalendarDayDateTagResponse dayDateTag) {
        if (Objects.isNull(dayDateTag)) {
            return null;
        }
        List<String> tagList = dayDateTag.getTagList();
        // 工作历日期标签为空时直接跳过
        if (CollectionUtils.isEmpty(tagList)) {
            return null;
        }
        List<DemandTemplateTagEnum> templateTags = new ArrayList<>();
        for (String tag : tagList) {
            MidCalendarDateTagValueEnum dateTagValue = BaseEnum.analyze(tag,
                    MidCalendarDateTagValueEnum.class, false);
            DemandTemplateTagEnum templateTag = DemandTemplateTagEnum.create(dateTagValue);
            if (Objects.isNull(dateTagValue) || Objects.isNull(templateTag)) {
                continue;
            }
            templateTags.add(templateTag);
        }
        if (CollectionUtils.isEmpty(templateTags)) {
            return null;
        }
        // 按优先级排序
        templateTags.sort(Comparator.comparingInt(DemandTemplateTagEnum::getPriority));
        return templateTags.stream().map(DemandTemplateTagEnum::value).collect(Collectors.toList());
    }

    /**
     * 查询允许时间范围内的项目工作历日期标签
     *
     * <p>工作日/休息日/节假日/店庆日标签仅项目可配置, 故仅需查项目工作历日期标签
     *
     * @param dates     日期, 为空时则为有效范围内所有日期
     * @param projectId 项目ID
     * @return 工作历日期标签
     * @since v6.0.0
     */
    private List<MidCalendarDayDateTagResponse> queryProjectCalendarDayDateTagOfAllowDay(
            @NonNull String projectId, @Nullable Set<LocalDate> dates) {
        Range<ChronoLocalDate> allowApplyDateRange = DemandTemplateApplyByDateParam.allowApplyDateRange();
        LocalDate beginDate = LocalDate.from(allowApplyDateRange.getMinimum());
        LocalDate endDate = LocalDate.from(allowApplyDateRange.getMaximum());

        MidCalendarDayDateTagQueryParam queryParam = new MidCalendarDayDateTagQueryParam();
        queryParam.setObjId(projectId);
        queryParam.setBeginDate(beginDate);
        queryParam.setEndDate(endDate);
        List<MidCalendarDayDateTagResponse> dateTags = midCalendarClientService
                .listCalendarDayDateTag(projectId, queryParam);
        if (CollectionUtils.isNotEmpty(dates)) {
            dateTags.removeIf(item -> !dates.contains(item.getCurrentDay().toLocalDate()));
        }
        return dateTags;
    }

    /**
     * 生成初始化模版添加参数
     *
     * @param demandTemplateType 需求模版类型
     * @param initializedTags    初始化模版标签, 为 {@code null} 时默认为 {@link #INITIALIZE_TAGS}
     * @return 初始化模版添加参数
     * @since v6.0.0
     */
    private List<DemandTemplateSaveDaoParam> generateInitializedTemplateAddParams(
            @NonNull DemandTemplateTypeEnum demandTemplateType,
            @Nullable List<String> initializedTags) {
        initializedTags = Optional.ofNullable(initializedTags).orElse(INITIALIZE_TAGS);
        if (CollectionUtils.isEmpty(initializedTags)) {
            return Lists.newArrayList();
        }
        DemandTypeEnum demandTypeEnum = demandTemplateType.toDemandTypeEnum();

        List<DemandTemplateSaveDaoParam> result = new ArrayList<>();
        DemandTemplateSaveDaoParam saveTemplateDaoParam = new DemandTemplateSaveDaoParam();
        saveTemplateDaoParam.setDemandType(demandTypeEnum);
        saveTemplateDaoParam.setDemandTemplateType(demandTemplateType);
        saveTemplateDaoParam.setIsInitialized(true);

        for (String initializeTag : initializedTags) {
            DemandTemplateTagEnum templateTagEnum = DemandTemplateTagEnum.analyze(initializeTag);
            DemandTemplateSaveDaoParam saveDaoParam = demandCalendarConverter.copy(saveTemplateDaoParam);
            saveDaoParam.setDemandTemplateTag(initializeTag);
            saveDaoParam.setDemandTemplateName(templateTagEnum.getReasonPhrase());
            saveDaoParam.setIsEnabled(DEFAULT_ENABLED_TAG.contains(initializeTag));
            result.add(saveDaoParam);
        }
        return result;
    }

    @Override
    public String logTag() {
        return "需求管理-需求日历";
    }
}
