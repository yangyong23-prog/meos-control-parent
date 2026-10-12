package com.persagy.apccontrol.basic.resource.demand.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.controller.shellconfig.param.SaveShellConfigParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigAirconditioningCustomMapper;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.dto.GroupDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigDto;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.ShellConfigBasic;
import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustom;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustomTimeGroup;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigSceneEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateCalendarService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.airconditioning.DemandConfigAirconditioningCustomQueryDaoParam;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkInstanceConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkInstanceValueObject;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.SortUtils;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 需求管理-空调需求-定制功能配置服务
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/11/20 14:40
 */
@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DemandConfigAirconditioningCustomServiceImpl
        extends BaseServiceImpl<DemandConfigAirconditioningCustomMapper, DemandConfigAirconditioningCustom>
        implements DemandConfigAirconditioningCustomService, LogComponent {

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Lazy
    @Autowired
    protected ShellConfigService shellConfigService;

    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Autowired
    private DemandTemplateCalendarService demandTemplateCalendarService;

    @Autowired
    private DemandConfigAirconditioningCustomMapper demandConfigAirconditioningCustomMapper;

    @Autowired
    private MidPermissionClientService midPermissionClientService;
    // 测试代码用
    @Value("${tenant.data.init.assign-tenants:}")
    private List<String> groupLimit;

    @Override
    public BasicsConfigTypeEnum getValueSelectType(String projectId, LocalDate localDate) {
        localDate = localDate == null ? DateUtils.now().toLocalDate() : localDate;

        // 1.获取当日模板配置数据
        DemandTemplateCalendarQueryDaoParam templateCalendarQueryDaoParam = new DemandTemplateCalendarQueryDaoParam();
        templateCalendarQueryDaoParam.setProjectId(projectId);
        templateCalendarQueryDaoParam.setDemandType(DemandTypeEnum.AIRCONDITIONING);
        templateCalendarQueryDaoParam.setWorkDate(localDate);
        DemandTemplateCalendar templateCalendar = this.demandTemplateCalendarService.get(projectId, templateCalendarQueryDaoParam);
        if (templateCalendar == null || templateCalendar.getRelDemandTemplateId() == null) {
            return null;
        }

        // 2.根据当日的模板数据获取对应模板所应用的价值选择方案
        DemandConfigAirconditioningCustomQueryDaoParam queryDaoParam = new DemandConfigAirconditioningCustomQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDemandInstanceId(projectId);
        queryDaoParam.setRelTemplateId(templateCalendar.getRelDemandTemplateId().toString());
        List<DemandConfigAirconditioningCustom> airconditioningCustoms = this.baseMapper.list(queryDaoParam);
        DemandConfigAirconditioningCustomDTO airconditioningCustomDTO = CollectionUtils.isEmpty(airconditioningCustoms) ? null : airconditioningCustoms.get(0).getConfigData();

        // 默认为品质优先
        BasicsConfigTypeEnum valueSelectType = airconditioningCustomDTO == null ? null : airconditioningCustomDTO.getValueSelectType();
        return valueSelectType == null ? BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST : valueSelectType;
    }

    @Override
    public DemandConfigAirconditioningCustomDTO getCustomConfig(BaseDemandTemplateParam param) {
        param.checkAndSet();
        // 1.获取当日模板配置数据
        DemandTemplate template = param.queryAndGetTemplate();
        if (template == null || template.getId() == null) {
            logProjectInstance().warn("日期无有效模版", Pair.of("param", param));
            return null;
        }
        String projectId = ContextUtil.getProjectId();
        Long templateId = template.getId();
        // 2.根据当日的模板数据获取对应模板所应用的价值选择方案
        DemandConfigAirconditioningCustomQueryDaoParam queryDaoParam = new DemandConfigAirconditioningCustomQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDemandInstanceId(projectId);
        queryDaoParam.setDate(param.getDate());
        queryDaoParam.setRelTemplateId(templateId.toString());
        DemandConfigAirconditioningCustom customConfig = getBaseMapper().get(queryDaoParam);
        if (Objects.isNull(customConfig)) {
            return DemandConfigAirconditioningCustomDTO.defaultInstance();
        }
        DemandConfigAirconditioningCustomDTO customConfigData = customConfig.getConfigData();
        if (Objects.isNull(customConfigData)) {
            return DemandConfigAirconditioningCustomDTO.defaultInstance();
        }
        return customConfigData;
    }

    @Override
    public List<ValueSelectConfigDto> getValueSelectRangeConfig(Long templateId, BasicsConfigTypeEnum valueSelectType) {
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        templateParam.setTemplateId(templateId);
        DemandConfigAirconditioningCustomDTO customConfig = getCustomConfig(templateParam);
        if (Objects.isNull(customConfig)) {
            return Lists.newArrayList();
        }
        return customConfig.parseValueSelectRange(valueSelectType);
    }

    @Override
    public List<DemandConfigAirconditioningCustomTimeGroup> queryEverydayFixDataList(@NonNull LocalDate startDate,
                                                                                     @NonNull LocalDate endDate) {
        String projectId = ContextUtil.getProjectId();
        return getBaseMapper().queryTimeScopeFixDataList(projectId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateValueSelectRange(
            Long templateId,
            BasicsConfigTypeEnum valueSelectType,
            List<ValueSelectConfigDto> valueSelectRange) {
        return addOrEditCustomConfig(templateId, config ->
                config.fillValueSelectConfig(valueSelectType, valueSelectRange));
    }

    /**
     * 新增或编辑自定义配置
     *
     * @param templateId         模版ID
     * @param configDataConsumer 配置回调
     * @return 操作是否成功
     * @since 7.250102.0
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrEditCustomConfig(
            @NonNull Long templateId,
            @NonNull Consumer<DemandConfigAirconditioningCustomDTO> configDataConsumer) {
        String projectId = ContextUtil.getProjectId();
        DemandConfigAirconditioningCustomQueryDaoParam queryDaoParam = new DemandConfigAirconditioningCustomQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDemandInstanceId(projectId);
        queryDaoParam.setRelTemplateId(templateId.toString());
        DemandConfigAirconditioningCustom customConfig = Optional.ofNullable(getBaseMapper()
                .get(queryDaoParam)).orElseGet(DemandConfigAirconditioningCustom::new);
        DemandConfigAirconditioningCustomDTO configData = Objects.nonNull(customConfig.getConfigData())
                ? customConfig.getConfigData()
                : new DemandConfigAirconditioningCustomDTO();
        configDataConsumer.accept(configData);
        customConfig.setProjectId(projectId);
        customConfig.setDemandInstanceType(DemandInstanceTypeEnum.PROJECT);
        customConfig.setDemandInstanceId(projectId);
        customConfig.setRelTemplateId(templateId.toString());
        customConfig.setConfigData(configData);
        return saveOrUpdate(customConfig);
    }

    @Override
    public DemandConfigAirconditioningCustomDTO getConfigData(String projectId, String relTemplateId, LocalDate date) {
        // 获取当前模板下所配置的价值选择方案类型及其空调工况
        DemandConfigAirconditioningCustomQueryDaoParam queryDaoParam = new DemandConfigAirconditioningCustomQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDate(date);
        queryDaoParam.setDemandInstanceId(projectId);
        queryDaoParam.setRelTemplateId(relTemplateId);
        DemandConfigAirconditioningCustom customConfig = this.baseMapper.get(queryDaoParam);
        DemandConfigAirconditioningCustomDTO configData =
                customConfig == null ? null : customConfig.getConfigData();
        return this.convertDefaultCustomConfig(projectId, configData);
    }

    @Override
    public DemandConfigAirconditioningWorkConditionValueObject getShellConfigData(String projectId, Long relTemplateId, LocalDate localDate) {
        if (relTemplateId == null && localDate == null) {
            return new DemandConfigAirconditioningWorkConditionValueObject();
        }

        // 1.获取当日模板配置数据
        if (relTemplateId == null && localDate != null) {
            DemandTemplateCalendarQueryDaoParam templateCalendarQueryDaoParam = new DemandTemplateCalendarQueryDaoParam();
            templateCalendarQueryDaoParam.setProjectId(projectId);
            templateCalendarQueryDaoParam.setDemandType(DemandTypeEnum.AIRCONDITIONING);
            templateCalendarQueryDaoParam.setWorkDate(localDate);
            DemandTemplateCalendar templateCalendar = this.demandTemplateCalendarService.get(projectId, templateCalendarQueryDaoParam);
            relTemplateId = templateCalendar == null ? null : templateCalendar.getRelDemandTemplateId();
        }
        if (relTemplateId == null) {
            return new DemandConfigAirconditioningWorkConditionValueObject();
        }

        // 2.获取当前项目、模板下的空调工况
        DemandConfigAirconditioningCustomDTO configData = this.getConfigData(projectId, relTemplateId.toString(), localDate);
        if (configData == null) {
            return new DemandConfigAirconditioningWorkConditionValueObject();
        }

        // 3.填充壳配置的配置值
        return this.fillShellConfigData(projectId, configData.getWorkCondition());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateConfigData(String projectId, Long relTemplateId, BasicsConfigTypeEnum valueSelectType, DemandConfigAirconditioningWorkConditionValueObject workCondition) {
        addOrEditCustomConfig(relTemplateId, config -> {
            config.setValueSelectType(valueSelectType);
            config.setWorkCondition(workCondition);
        });
        // 同步更新壳配置对应的参数数据值
        String groupCode = ContextUtil.getGroupCode();
        this.wirteShellConfigParam(groupCode, projectId, relTemplateId.toString());
        // CompletableFuture.supplyAsync(() -> this.wirteShellConfigParam(groupCode, projectId, relTemplateId.toString()), BusinessThreadPoolExecutor.ttlExecutor());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearConfigData(String projectId, String relTemplateId) {
        this.lambdaUpdate().eq(DemandConfigAirconditioningCustom::getProjectId, projectId).eq(DemandConfigAirconditioningCustom::getRelTemplateId, relTemplateId).remove();
        return true;
    }

    /**
     * 填充壳配置的配置值，进入空调工况的运行标准配置中去
     *
     * @param projectId
     * @param workCondition
     * @return
     */
    private DemandConfigAirconditioningWorkConditionValueObject fillShellConfigData(String projectId, DemandConfigAirconditioningWorkConditionValueObject workCondition) {
        if (workCondition == null) {
            return new DemandConfigAirconditioningWorkConditionValueObject();
        }

        // 1.获取壳配置参数
        List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(ContextUtil.getGroupCode(), projectId, ShellConfigSceneEnum.SCENE_ALG.getCode(), null);
        if (CollectionUtil.isEmpty(shellConfigVOS)) {
            return workCondition;
        }

        this.fillShellConfigData(shellConfigVOS, workCondition.getCoolingSeason(), true);
        this.fillShellConfigData(shellConfigVOS, workCondition.getHeatingSeason(), false);
        return workCondition;
    }

    /**
     * 填充壳配置的数据
     *
     * @param shellConfigVOS
     * @param seasonConfigDatas
     * @param cooling
     */
    private void fillShellConfigData(List<ShellConfigVO> shellConfigVOS, List<DemandConfigAirconditioningWorkInstanceValueObject> seasonConfigDatas, boolean cooling) {
        if (CollectionUtil.isEmpty(seasonConfigDatas) || CollectionUtil.isEmpty(shellConfigVOS)) {
            return;
        }

        // 数据填充
        Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> seasonInstanceMap = this.getAirconditioningWorkInstanceConfigValueObjectMap(seasonConfigDatas);
        // 拿到空调冷源运行工况配置Map
        Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> airconditioningWorkInstanceConfigValueObjectMap = getDemandConfigAirconditioningWorkInstanceConfigValueObjectMap(seasonConfigDatas);
        for (ShellConfigVO shellConfigVO : shellConfigVOS) {
            List<ShellConfigGroup> groupList = shellConfigVO.getGroups();
            if (CollectionUtil.isEmpty(groupList)) {
                continue;
            }
            for (ShellConfigGroup groupItem : groupList) {
                List<ShellConfigItem> items = groupItem.getItems();
                if (CollectionUtil.isEmpty(items)) {
                    continue;
                }
                for (ShellConfigItem shellConfigItem : items) {
                    if (StrUtil.isNotBlank(shellConfigItem.getObjectId())) {
                        // 判断是否是空调冷源系统相关配置
                        if (airconditioningWorkInstanceConfigValueObjectMap.containsKey(shellConfigItem.getObjectId())) {
                            DemandConfigAirconditioningWorkInstanceConfigValueObject instanceConfigValueObject = airconditioningWorkInstanceConfigValueObjectMap.get(shellConfigItem.getObjectId());
                            this.overrideValue(shellConfigItem.getObjectId(), shellConfigItem.getShellConfigValue(), shellConfigItem.getCode(), instanceConfigValueObject, cooling, false, true);
                            continue;
                        }
                        // 回写到空调运行工况标准中去
                        DemandConfigAirconditioningWorkInstanceConfigValueObject instanceConfigValueObject = seasonInstanceMap.get(shellConfigItem.getObjectId());
                        if (instanceConfigValueObject == null || !shellConfigItem.getObjectId().equals(instanceConfigValueObject.getId())) {
                            continue;
                        }
                        this.overrideValue(shellConfigItem.getObjectId(), shellConfigItem.getShellConfigValue(), shellConfigItem.getCode(), seasonInstanceMap.get(shellConfigItem.getObjectId()), cooling, false, false);
                    }
                }
            }
        }
    }

    /**
     * 拿到空调冷源运行工况配置Map
     *
     * @param seasonConfigDatas 配置数据
     * @return {@code Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> }
     * @since 7.250802.0
     */
    @NotNull
    private Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> getDemandConfigAirconditioningWorkInstanceConfigValueObjectMap(List<DemandConfigAirconditioningWorkInstanceValueObject> seasonConfigDatas) {
        if (CollectionUtil.isEmpty(seasonConfigDatas)) {
            return new HashMap<>();
        }
        // getConfigData 会先用当前 RWD 系统重建工况，这里的 classCode 是权威查询结果，无需再按 ID 查询两次。
        List<DemandConfigAirconditioningWorkInstanceValueObject> airConditioningSeasonInstanceConfigs =
                seasonConfigDatas.stream()
                        .filter(seasonInstance -> seasonInstance.getSystemConfig() != null)
                        .filter(seasonInstance -> DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(
                                        seasonInstance.getSystemConfig().getClassCode())
                                || DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(
                                        seasonInstance.getSystemConfig().getClassCode()))
                        .collect(Collectors.toList());
        // 拿到空调运行工况配置Map
        return this.getAirconditioningWorkInstanceConfigValueObjectMap(airConditioningSeasonInstanceConfigs);
    }

    /**
     * 将当前配置的空调工况运行标准数据，根据对象ID回写到壳配置
     *
     * @param groupCode
     * @param projectId
     * @param relTemplateId
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean wirteShellConfigParam(String groupCode, String projectId, String relTemplateId) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        DemandConfigAirconditioningWorkConditionValueObject workCondition = null;
        try {
            // 1.这里需要主动查询一次，因为前端的传参，不传递温压差类型
            DemandConfigAirconditioningCustomDTO configData = this.getConfigData(projectId, relTemplateId, null);
            if (configData == null || configData.getWorkCondition() == null) {
                return true;
            }
            workCondition = configData.getWorkCondition();

            // 2.获取壳配置参数，这里必须按照 query save 的接口方式进行查询与存储，因为倘若项目第一次上线，数据库中是没有壳配置的数据的，所以这里主动查询与保存一次（需要与前端逻辑保持一致）
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(groupCode, projectId, ShellConfigSceneEnum.SCENE_ALG.getCode(), null);
            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                return true;
            }
            Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> coolingSeasonInstanceMap = this.getAirconditioningWorkInstanceConfigValueObjectMap(workCondition.getCoolingSeason());
            Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> heatingSeasonInstanceMap = this.getAirconditioningWorkInstanceConfigValueObjectMap(workCondition.getHeatingSeason());
            // 空调冷源运行工况配置Map
            Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> airconditioningWorkInstanceConfigValueObjectMap = getDemandConfigAirconditioningWorkInstanceConfigValueObjectMap(workCondition.getCoolingSeason());
            Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> heatAirconditioningWorkInstanceConfigValueObjectMap = getDemandConfigAirconditioningWorkInstanceConfigValueObjectMap(workCondition.getHeatingSeason());

            // 3.根据当前空调工况运行标准配置，回写当前壳配置的参数
            List<SaveShellConfigParam.ShellConfigGroup> shellConfigGroupList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> groupList = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(groupList)) {
                    continue;
                }
                for (ShellConfigGroup groupItem : groupList) {
                    String group = groupItem.getGroup();
                    List<ShellConfigBasic> shellConfigBasics = this.convertAlgShellConfigGroupItems(groupItem.getItems(), coolingSeasonInstanceMap, heatingSeasonInstanceMap, airconditioningWorkInstanceConfigValueObjectMap, heatAirconditioningWorkInstanceConfigValueObjectMap);
                    if (CollectionUtil.isEmpty(shellConfigBasics) || StrUtil.isBlank(group)) {
                        continue;
                    }
                    SaveShellConfigParam.ShellConfigGroup shellConfigGroup = new SaveShellConfigParam.ShellConfigGroup();
                    shellConfigGroup.setGroup(group);
                    shellConfigGroup.setItems(shellConfigBasics);
                    shellConfigGroupList.add(shellConfigGroup);
                }
            }

            if (shellConfigGroupList.size() > 0) {
                SaveShellConfigParam saveShellConfigParam = new SaveShellConfigParam();
                saveShellConfigParam.setProjectId(projectId);
                saveShellConfigParam.setScene(ShellConfigSceneEnum.SCENE_ALG.getCode());
                saveShellConfigParam.setGroups(shellConfigGroupList);
                this.shellConfigService.save(groupCode, saveShellConfigParam);
            }
        } catch (Exception e) {
            this.logProjectInstance().error("空调工况运行标准回写壳配置异常", e, Arrays.asList(
                    Pair.of("projectId", projectId),
                    Pair.of("templateId", relTemplateId)
            ));
        }
        return true;
    }

    /**
     * 将本方所存储的配置与现有中台交付的配置进行互补操作后返回
     *
     * @param projectId
     * @param airconditioningCustomDTO
     * @return
     */
    private DemandConfigAirconditioningCustomDTO convertDefaultCustomConfig(String projectId, DemandConfigAirconditioningCustomDTO airconditioningCustomDTO) {
        DemandConfigAirconditioningCustomDTO customDTO = new DemandConfigAirconditioningCustomDTO();
        customDTO.setValueSelectType(airconditioningCustomDTO == null || airconditioningCustomDTO.getValueSelectType() == null ? BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST : airconditioningCustomDTO.getValueSelectType());

        // 1.获取绑点规则
        Set<String> classCodes = Sets.newHashSet(DtpRwdClassCodeEnum.COLD_SYSTEM.getValue(), DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue(), DtpRwdClassCodeEnum.HOT_SYSTEM.getValue(),
                DtpRwdClassCodeEnum.ACCPC_GROUP.getValue(), DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue(), DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue(), DtpRwdClassCodeEnum.ACHPC_GROUP.getValue(),
                DtpRwdClassCodeEnum.ACPPC_GROUP.getValue());
        Map<String, Set<String>> objectsExistRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(ContextUtil.getGroupCode(), projectId, classCodes);
        if (CollectionUtil.isEmpty(objectsExistRuleMap)) {
            return customDTO;
        }

        // 自测用
        /*for (Map.Entry<String, Set<String>> setEntry : objectsExistRuleMap.entrySet()) {
            Set<String> entryValue = setEntry.getValue();
            if (CollectionUtil.isNotEmpty(entryValue)) {
                entryValue.addAll(Sets.newHashSet("primaryChillWaterMainSupplyTemp", "chillWaterMainSupplyTemp", "2heatWaterMainSupplyTemp", "heatWaterMainSupplyTemp",
                        "heatSecondarySideWaterDetaTempSet", "heatSecondarySideWaterDetaPressureSet", "chillWaterDetaTempSet", "chillWaterDetaPressSet", "branchSupplyTemp", "branchDetaTempSet", "branchDetaPressSet"));
            }
        }*/

        // 2.查询中台交付的所有冷站、热站、冷热站
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setClassCode(Sets.newHashSet(DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue(), DtpRwdClassCodeEnum.COLD_SYSTEM.getValue(),
                DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue(), DtpRwdClassCodeEnum.HOT_SYSTEM.getValue(), DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue()));
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = this.dtpRwdClientService.listObject(queryParam);
        if (CollectionUtil.isEmpty(dtpRwdObjectDTOS)) {
            return customDTO;
        }
        Map<String, List<DtpRwdObjectDTO>> systemInfoMap = dtpRwdObjectDTOS.stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
        DemandConfigAirconditioningWorkConditionValueObject defaultWorkConditionValueObject = this.getDefaultAirconditioningWorkConditionValueObject(objectsExistRuleMap, systemInfoMap);

        // 以当前默认配置为准，为其赋用户自定义的配置值
        if (airconditioningCustomDTO != null && airconditioningCustomDTO.getWorkCondition() != null && defaultWorkConditionValueObject != null) {
            wirteCustomValue(defaultWorkConditionValueObject.getCoolingSeason(), airconditioningCustomDTO.getWorkCondition().getCoolingSeason());
            wirteCustomValue(defaultWorkConditionValueObject.getHeatingSeason(), airconditioningCustomDTO.getWorkCondition().getHeatingSeason());
        }
        customDTO.setWorkCondition(defaultWorkConditionValueObject);
        return customDTO;
    }

    /**
     * 数值填充
     *
     * @param defaultSeason
     * @param customSeason
     */
    private void wirteCustomValue(List<DemandConfigAirconditioningWorkInstanceValueObject> defaultSeason, List<DemandConfigAirconditioningWorkInstanceValueObject> customSeason) {
        if (CollectionUtil.isEmpty(defaultSeason) || CollectionUtil.isEmpty(customSeason)) {
            return;
        }

        // 已配置项的抽取
        Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> customConfigMap = new HashMap<>();
        for (DemandConfigAirconditioningWorkInstanceValueObject workInstanceValueObject : customSeason) {
            DemandConfigAirconditioningWorkInstanceConfigValueObject systemConfig = workInstanceValueObject.getSystemConfig();
            List<DemandConfigAirconditioningWorkInstanceConfigValueObject> pumpConfig = workInstanceValueObject.getPumpConfig();
            if (systemConfig != null) {
                customConfigMap.put(systemConfig.getId(), systemConfig);
            }
            if (CollectionUtil.isNotEmpty(pumpConfig)) {
                for (DemandConfigAirconditioningWorkInstanceConfigValueObject pump : pumpConfig) {
                    customConfigMap.put(pump.getId(), pump);
                }
            }
        }

        // 默认配置赋值
        for (DemandConfigAirconditioningWorkInstanceValueObject workInstanceValueObject : defaultSeason) {
            DemandConfigAirconditioningWorkInstanceConfigValueObject systemConfig = workInstanceValueObject.getSystemConfig();
            List<DemandConfigAirconditioningWorkInstanceConfigValueObject> pumpConfig = workInstanceValueObject.getPumpConfig();
            if (systemConfig != null) {
                this.wirteWorkInstanceConfigValueObjectValue(systemConfig, customConfigMap.get(systemConfig.getId()));
            }
            if (CollectionUtil.isNotEmpty(pumpConfig)) {
                for (DemandConfigAirconditioningWorkInstanceConfigValueObject pump : pumpConfig) {
                    this.wirteWorkInstanceConfigValueObjectValue(pump, customConfigMap.get(pump.getId()));
                }
            }
        }

    }

    /**
     * 默认配置值写入用户所输入的值
     *
     * @param defaultConfig
     * @param customConfig
     */
    private void wirteWorkInstanceConfigValueObjectValue(DemandConfigAirconditioningWorkInstanceConfigValueObject defaultConfig, DemandConfigAirconditioningWorkInstanceConfigValueObject customConfig) {
        if (customConfig == null || defaultConfig == null) {
            return;
        }
        if (StrUtil.isNotBlank(defaultConfig.getSupplyTempName())) {
            defaultConfig.setSupplyTemp(customConfig.getSupplyTemp());
            defaultConfig.setSupplyTempTime(customConfig.getSupplyTempTime());
        }
        if (StrUtil.isNotBlank(defaultConfig.getPrimaryChillWaterMainReturnTempName())) {
            defaultConfig.setLowerPrimaryChillWaterMainReturnTemp(customConfig.getLowerPrimaryChillWaterMainReturnTemp());
            defaultConfig.setUpperPrimaryChillWaterMainReturnTemp(customConfig.getUpperPrimaryChillWaterMainReturnTemp());
        }
        List<DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO> costonTempAndPressureSetList = customConfig.getTempAndPressureSetList();
        List<DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO> defaultTempAndPressureSetList = defaultConfig.getTempAndPressureSetList();
        if (CollectionUtils.isNotEmpty(defaultTempAndPressureSetList) && CollectionUtils.isNotEmpty(costonTempAndPressureSetList)) {
            for (DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO : defaultTempAndPressureSetList) {
                Optional<DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO> optional = costonTempAndPressureSetList.stream().filter(e -> e.getChillWaterDetaSetType().equals(tempAndPressureSetDTO.getChillWaterDetaSetType())).findFirst();
                if (optional.isPresent()) {
                    tempAndPressureSetDTO.setLowerChillWaterDetaSet(optional.get().getLowerChillWaterDetaSet());
                    tempAndPressureSetDTO.setUpperChillWaterDetaSet(optional.get().getUpperChillWaterDetaSet());
                }
            }
        }
        if (StrUtil.isNotBlank(defaultConfig.getChillWaterDetaSetName())) {
            defaultConfig.setLowerChillWaterDetaSet(customConfig.getLowerChillWaterDetaSet());
            defaultConfig.setUpperChillWaterDetaSet(customConfig.getUpperChillWaterDetaSet());
        }
    }

    /**
     * 根据当前时刻点该项目的交付数据，返回默认的空调工况数据
     *
     * @param objectsExistRuleMap
     * @param systemInfoMap
     * @return
     */
    private DemandConfigAirconditioningWorkConditionValueObject getDefaultAirconditioningWorkConditionValueObject(Map<String, Set<String>> objectsExistRuleMap, Map<String, List<DtpRwdObjectDTO>> systemInfoMap) {
        if (CollectionUtil.isEmpty(systemInfoMap)) {
            return null;
        }
        DemandConfigAirconditioningWorkConditionValueObject workConditionValueObject = new DemandConfigAirconditioningWorkConditionValueObject();

        List<DemandConfigAirconditioningWorkInstanceValueObject> coolingSeason = new ArrayList<>();
        List<DemandConfigAirconditioningWorkInstanceValueObject> heatingSeason = new ArrayList<>();
        workConditionValueObject.setCoolingSeason(coolingSeason);
        workConditionValueObject.setHeatingSeason(heatingSeason);

        // 获取每个系统下的泵组信息
        for (List<DtpRwdObjectDTO> dtpRwdObjectDTOS : systemInfoMap.values()) {
            for (DtpRwdObjectDTO systemInfo : dtpRwdObjectDTOS) {
                boolean cityHeat = false;
                boolean boilerHeat = false;
                boolean oneSystem = false;
                boolean twoSystem = false;
                if (DtpRwdClassCodeEnum.COLD_SYSTEM.getValue().equals(systemInfo.getClassCode())
                        || DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(systemInfo.getClassCode())) {
                    // 冷站，判断系统类型
                    String chillPumpType = systemInfo.getStringUndefinedProperties("chillPumpType");
                    if ("1".equals(chillPumpType)) {
                        oneSystem = true;
                    }
                    if ("2".equals(chillPumpType)) {
                        twoSystem = true;
                    }
                    if (!oneSystem && !twoSystem) {
                        continue;
                    }
                } else if (DtpRwdClassCodeEnum.HOT_SYSTEM.getValue().equals(systemInfo.getClassCode())) {
                    // 热站，判断是市政换热还是锅炉，并判断系统类型
                    String heatPumpType = systemInfo.getStringUndefinedProperties("heatPumpType");
                    if ("1".equals(heatPumpType)) {
                        oneSystem = true;
                    }
                    if ("2".equals(heatPumpType)) {
                        twoSystem = true;
                    }
                    if (!oneSystem && !twoSystem) {
                        continue;
                    }

                    Set<String> heatSoureTypes = systemInfo.getSetUndefinedProperties("heatSoureType");
                    String heatMediumType = systemInfo.getStringUndefinedProperties("heatMediumType");
                    String boilerPressureType = systemInfo.getStringUndefinedProperties("boilerPressureType");
                    // 是否属于锅炉的
                    if ("1".equals(boilerPressureType) && "2".equals(heatMediumType) && CollectionUtil.isNotEmpty(heatSoureTypes) && heatSoureTypes.contains("4")) {
                        boilerHeat = true;
                    }

                    // 是否属于市政换热
                    if (CollectionUtil.isNotEmpty(heatSoureTypes) && heatSoureTypes.contains("1")) {
                        cityHeat = true;
                    }

                    // 既不是锅炉，也不是市政
                    if (!cityHeat && !boilerHeat) {
                        continue;
                    }
                } else if (DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(systemInfo.getClassCode())) {
                    String heatPumpType = systemInfo.getStringUndefinedProperties("heatPumpType");
                    if ("1".equals(heatPumpType)) {
                        oneSystem = true;
                    }
                    if ("2".equals(heatPumpType)) {
                        twoSystem = true;
                    }
                    if (!oneSystem && !twoSystem) {
                        continue;
                    }
                }

                // 获取该系统的默认配置
                DemandConfigAirconditioningWorkInstanceValueObject coolWorkInstanceValueObject = new DemandConfigAirconditioningWorkInstanceValueObject();
                DemandConfigAirconditioningWorkInstanceValueObject heatWorkInstanceValueObject = new DemandConfigAirconditioningWorkInstanceValueObject();

                Set<String> infoCodeRules = objectsExistRuleMap.get(systemInfo.getId());
                DemandConfigAirconditioningWorkInstanceConfigValueObject coolSystemConfig = this.getSystemWorkInstanceValueObject(systemInfo, infoCodeRules, true, cityHeat, oneSystem);
                DemandConfigAirconditioningWorkInstanceConfigValueObject heatSystemConfig = this.getSystemWorkInstanceValueObject(systemInfo, infoCodeRules, false, cityHeat, oneSystem);

                coolWorkInstanceValueObject.setSystemConfig(coolSystemConfig);
                heatWorkInstanceValueObject.setSystemConfig(heatSystemConfig);

                // 获取该系统下所有的泵组
                List<DtpRwdObjectDTO> pumpObjects = null;
                if (DtpRwdClassCodeEnum.COLD_SYSTEM.getValue().equals(systemInfo.getClassCode()) || DtpRwdClassCodeEnum.HOT_SYSTEM.getValue().equals(systemInfo.getClassCode())) {
                    List<DtpRwdFreezePumpDTO> accpcGroups = this.dtpRwdClientService.listFreezePumpGroup(false, false,
                            ContextUtil.getProjectId(), Sets.newHashSet(systemInfo.getId()), null, null, true);
                    if (CollectionUtil.isNotEmpty(accpcGroups)) {
                        pumpObjects = new ArrayList<>();
                        for (DtpRwdFreezePumpDTO accpcGroup : accpcGroups) {
                            if (BooleanUtils.isTrue(accpcGroup.getIsTerminalPump())) {
                                DtpRwdObjectDTO dtpRwdObjectDTO = new DtpRwdObjectDTO();
                                dtpRwdObjectDTO.setId(accpcGroup.getId());
                                dtpRwdObjectDTO.setLocalId(accpcGroup.getLocalId());
                                dtpRwdObjectDTO.setLocalName(accpcGroup.getLocalName());
                                pumpObjects.add(dtpRwdObjectDTO);
                            }
                        }
                    }
                } else {
                    DtpRwdObjectQueryParam objectQueryParam = new DtpRwdObjectQueryParam();
                    if (DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(systemInfo.getClassCode())) {
                        objectQueryParam.setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACPPC_GROUP, DtpRwdClassCodeEnum.ACHPC_GROUP));
                    } else {
                        objectQueryParam.setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACCPC_GROUP));
                    }
                    pumpObjects = dtpRwdClientService.listObject(objectQueryParam
                            .setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(systemInfo.getId())));
                    pumpObjects = CollectionUtil.isEmpty(pumpObjects) ? Lists.newArrayList() : pumpObjects.stream().filter(item -> "1".equals(item.analyzeProperties("isTerminalPump"))).collect(Collectors.toList());
                }

                List<DemandConfigAirconditioningWorkInstanceConfigValueObject> coolPumpConfigs = new ArrayList<>();
                List<DemandConfigAirconditioningWorkInstanceConfigValueObject> heatPumpConfigs = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(pumpObjects)) {
                    for (DtpRwdObjectDTO pumpObject : pumpObjects) {
                        DemandConfigAirconditioningWorkInstanceConfigValueObject coolPumpConfig = getPumpGroupWorkInstanceValueObject(systemInfo.getClassCode(), pumpObject, objectsExistRuleMap.get(pumpObject.getId()), true, cityHeat, oneSystem);
                        DemandConfigAirconditioningWorkInstanceConfigValueObject heatPumpConfig = getPumpGroupWorkInstanceValueObject(systemInfo.getClassCode(), pumpObject, objectsExistRuleMap.get(pumpObject.getId()), false, cityHeat, oneSystem);
                        if (coolPumpConfig != null) {
                            coolPumpConfigs.add(coolPumpConfig);
                        }
                        if (heatPumpConfig != null) {
                            heatPumpConfigs.add(heatPumpConfig);
                        }
                    }
                }


                if (coolSystemConfig != null) {
                    if (!coolPumpConfigs.isEmpty()) {
                        coolPumpConfigs.sort(Comparator.comparing(e -> Optional.ofNullable(e.getLocalName()).orElse("")));
                        coolWorkInstanceValueObject.setPumpConfig(coolPumpConfigs);
                    }
                    coolingSeason.add(coolWorkInstanceValueObject);
                }
                if (heatSystemConfig != null) {
                    if (!heatPumpConfigs.isEmpty()) {
                        heatPumpConfigs.sort(Comparator.comparing(e -> Optional.ofNullable(e.getLocalName()).orElse("")));
                        heatWorkInstanceValueObject.setPumpConfig(heatPumpConfigs);
                    }
                    heatingSeason.add(heatWorkInstanceValueObject);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(coolingSeason)) {
            SortUtils.sortByMultipleCustomOrders(coolingSeason, new SortUtils.SortField<>(item -> item.getSystemConfig().getClassCode(),
                            Arrays.asList(DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue(), DtpRwdClassCodeEnum.COLD_SYSTEM.getValue(), DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue())),
                    new SortUtils.SortField<DemandConfigAirconditioningWorkInstanceValueObject, Object>(item -> item.getSystemConfig().getLocalName(), null));
        }
        if (CollectionUtils.isNotEmpty(heatingSeason)) {
            SortUtils.sortByMultipleCustomOrders(heatingSeason, new SortUtils.SortField<>(item -> item.getSystemConfig().getClassCode(),
                            Arrays.asList(DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue(), DtpRwdClassCodeEnum.HOT_SYSTEM.getValue(), DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue())),
                    new SortUtils.SortField<DemandConfigAirconditioningWorkInstanceValueObject, Object>(item -> item.getSystemConfig().getLocalName(), null));
        }
        return workConditionValueObject;
    }

    /**
     * 返回系统默认配置数据，不包含配置的值
     *
     * @param systemInfo    系统信息
     * @param infoCodeRules 信息码规则
     * @param cool          是否属于供冷工况
     * @param cityHeat      是否是市政换热
     * @param oneSystem     是否是一次泵系统
     * @return 系统默认配置数据
     */
    private DemandConfigAirconditioningWorkInstanceConfigValueObject getSystemWorkInstanceValueObject(DtpRwdObjectDTO systemInfo, Set<String> infoCodeRules, boolean cool, boolean cityHeat, boolean oneSystem) {
        String classCode = systemInfo.getClassCode();
        // 热源系统
        boolean hotSystem = DtpRwdClassCodeEnum.HOT_SYSTEM.getValue().equals(classCode);
        // 空调热源系统
        boolean airHeatSystem = DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(classCode);
        // 冷源系统
        boolean coldSystem = DtpRwdClassCodeEnum.COLD_SYSTEM.getValue().equals(classCode);
        // 空调冷源系统
        boolean airColdSystem = DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(classCode);
        if (cool && (hotSystem || airHeatSystem)) {
            return null;
        }
        if (!cool && (coldSystem || airColdSystem)) {
            return null;
        }

        DemandConfigAirconditioningWorkInstanceConfigValueObject systemConfig = new DemandConfigAirconditioningWorkInstanceConfigValueObject();
        systemConfig.setId(systemInfo.getId());
        systemConfig.setLocalId(systemInfo.getLocalId());
        systemConfig.setLocalName(systemInfo.getLocalName());
        systemConfig.setClassCode(systemInfo.getClassCode());
        if (CollectionUtil.isEmpty(infoCodeRules)) {
            return systemConfig;
        }
        // 251002产品需求更新
        // 供暖工况下
        // 一次泵系统，只能设置系统实例下的参数(温差，压差，供水温度，回水温度)，没有泵组，
        // 二次泵系统，系统实例下的参数只能设置供水温度/回水温度设定（没有温差压差），+泵组
        boolean coldHotSystem = DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue().equals(classCode);
        String supplyTempCode = airHeatSystem ? "heatWaterSupplyTempSet"
                : coldHotSystem ? "chillWaterMainSupplyTemp"
                : hotSystem ? (cityHeat ? "2heatWaterMainSupplyTemp" : "heatWaterMainSupplyTemp") : null;
        if (!oneSystem && !cool) {
            // 系统出水温度编码
            if (StrUtil.isNotBlank(supplyTempCode) && infoCodeRules.contains(supplyTempCode)) {
                String supplyTempName = LocaleMessageUtil.getMessage("key_62099a3d1ea8495ebb8d599e8b28f31e");
                systemConfig.setSupplyTempName(supplyTempName);
            }
            return systemConfig;
        }

        // 系统出水温度编码
        boolean isColdSystem = coldSystem || airColdSystem;
        supplyTempCode = isColdSystem ? "primaryChillWaterMainSupplyTemp" : supplyTempCode;
        if (StrUtil.isNotBlank(supplyTempCode) && infoCodeRules.contains(supplyTempCode)) {
            String supplyTempName = LocaleMessageUtil.getMessage("key_62099a3d1ea8495ebb8d599e8b28f31e");
            systemConfig.setSupplyTempName(supplyTempName);
        }

        // 系统回水温度
        supplyTempCode = airColdSystem ? "primaryChillWaterMainReturnTemp" : null;
        if (StrUtil.isNotBlank(supplyTempCode) && infoCodeRules.contains(supplyTempCode)) {
            String supplyTempName = LocaleMessageUtil.getMessage("key_c0dfb5a77e9e4e21adde34c879d0eb3f");
            systemConfig.setPrimaryChillWaterMainReturnTempName(supplyTempName);
        }

        // 系统温差信息点编码
        String chillWaterDetaTempCode = isColdSystem ? "chillWaterDetaTempSet"
                : airHeatSystem ? "waterDetaTempSet"
                : coldHotSystem ? "chillWaterDetaTempSet"
                : hotSystem ? cityHeat ? "heatSecondarySideWaterDetaTempSet" : "waterDetaTempSet" : null;

        // 系统压差信息点编码
        String chillWaterDetaPressCode = isColdSystem ? "chillWaterDetaPressSet"
                : airHeatSystem ? "waterDetaPressSet"
                : coldHotSystem ? "chillWaterDetaPressSet"
                : hotSystem ? cityHeat ? "heatSecondarySideWaterDetaPressureSet" : "waterDetaPressSet" : null;

        // 系统温压差的显示名称（前端使用）
        boolean tempType = StrUtil.isNotBlank(chillWaterDetaTempCode) && infoCodeRules.contains(chillWaterDetaTempCode);
        boolean pressType = StrUtil.isNotBlank(chillWaterDetaPressCode) && infoCodeRules.contains(chillWaterDetaPressCode);
        if (airColdSystem || airHeatSystem) {
            List<DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO> tempAndPressureSetList = new ArrayList<>();
            if (tempType) {
                DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO = new DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO();
                tempAndPressureSetDTO.setChillWaterDetaSetName(LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce"));
                tempAndPressureSetDTO.setChillWaterDetaSetType(DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET);
                tempAndPressureSetDTO.setChillWaterDetaUnit("℃");
                tempAndPressureSetList.add(tempAndPressureSetDTO);
            }
            if (pressType) {
                DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO = new DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO();
                tempAndPressureSetDTO.setChillWaterDetaSetName(LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a"));
                tempAndPressureSetDTO.setChillWaterDetaSetType(DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET);
                tempAndPressureSetDTO.setChillWaterDetaUnit("kPa");
                tempAndPressureSetList.add(tempAndPressureSetDTO);
            }
            systemConfig.setTempAndPressureSetList(tempAndPressureSetList);
        } else {
            if (tempType || pressType) {
                String chillWaterDetaSetName = tempType ? LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce") : LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a");
                if (StrUtil.isNotBlank(chillWaterDetaSetName)) {
                    systemConfig.setChillWaterDetaSetName(chillWaterDetaSetName);
                    systemConfig.setChillWaterDetaSetType(tempType ? DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET : DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET);
                    systemConfig.setChillWaterDetaUnit(tempType ? "℃" : "kPa");
                }
            }
        }

        return systemConfig;
    }

    /**
     * 返回泵组默认配置数据，不包含配置的值
     *
     * @param systemClassCode 系统类编码
     * @param pumpGroupInfo   泵组信息
     * @param infoCodeRules   实例化规则
     * @param cool            是否属于供冷工况
     * @param cityHeat        是否是市政换热
     * @param oneSystem       是否是一次泵系统
     * @return 泵组默认配置数据
     */
    private DemandConfigAirconditioningWorkInstanceConfigValueObject getPumpGroupWorkInstanceValueObject(String systemClassCode, DtpRwdObjectDTO pumpGroupInfo, Set<String> infoCodeRules, boolean cool, boolean cityHeat, boolean oneSystem) {
        // 热源系统
        boolean hotSystem = DtpRwdClassCodeEnum.HOT_SYSTEM.getValue().equals(systemClassCode);
        // 空调热源系统
        boolean airHeatSystem = DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(systemClassCode);
        // 冷源系统
        boolean coldSystem = DtpRwdClassCodeEnum.COLD_SYSTEM.getValue().equals(systemClassCode);
        // 空调冷源系统
        boolean airColdSystem = DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(systemClassCode);
        if (cool && (hotSystem || airHeatSystem)) {
            return null;
        }
        if (!cool && coldSystem) {
            return null;
        }
        if (oneSystem && (hotSystem || airColdSystem || airHeatSystem || coldSystem)) {
            return null;
        }
        DemandConfigAirconditioningWorkInstanceConfigValueObject pumpConfig = new DemandConfigAirconditioningWorkInstanceConfigValueObject();
        pumpConfig.setId(pumpGroupInfo.getId());
        pumpConfig.setLocalId(pumpGroupInfo.getLocalId());
        pumpConfig.setLocalName(pumpGroupInfo.getLocalName());
        if (CollectionUtil.isEmpty(infoCodeRules)) {
            return pumpConfig;
        }

        // 泵组出水温度编码
        String supplyTempCode = "branchSupplyTemp";
        if (infoCodeRules.contains(supplyTempCode)) {
            String supplyTempName = coldSystem || airColdSystem ? LocaleMessageUtil.getMessage("key_62099a3d1ea8495ebb8d599e8b28f31e")
                    : DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue().equals(systemClassCode) ? LocaleMessageUtil.getMessage("key_62099a3d1ea8495ebb8d599e8b28f31e")
                    : hotSystem || airHeatSystem ? LocaleMessageUtil.getMessage("key_62099a3d1ea8495ebb8d599e8b28f31e") : null;
            pumpConfig.setSupplyTempName(supplyTempName);
        }

        // 泵组温差信息点编码
        String chillWaterDetaTempCode = "branchDetaTempSet";

        // 泵组压差信息点编码
        String chillWaterDetaPressCode = "branchDetaPressSet";

        // 泵组温压差的显示名称（前端使用）
        boolean tempType = infoCodeRules.contains(chillWaterDetaTempCode);
        boolean pressType = infoCodeRules.contains(chillWaterDetaPressCode);
        if (airColdSystem || airHeatSystem) {
            List<DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO> tempAndPressureSetList = new ArrayList<>();
            if (tempType) {
                DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO = new DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO();
                tempAndPressureSetDTO.setChillWaterDetaSetName(LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce"));
                tempAndPressureSetDTO.setChillWaterDetaSetType(DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET);
                tempAndPressureSetDTO.setChillWaterDetaUnit("℃");
                tempAndPressureSetList.add(tempAndPressureSetDTO);
            }
            if (pressType) {
                DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO = new DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO();
                tempAndPressureSetDTO.setChillWaterDetaSetName(LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a"));
                tempAndPressureSetDTO.setChillWaterDetaSetType(DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET);
                tempAndPressureSetDTO.setChillWaterDetaUnit("kPa");
                tempAndPressureSetList.add(tempAndPressureSetDTO);
            }
            pumpConfig.setTempAndPressureSetList(tempAndPressureSetList);
        } else {
            if (tempType || pressType) {
                String chillWaterDetaSetName = coldSystem ? tempType ? LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce") : LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a")
                        : DtpRwdClassCodeEnum.COLD_HOT_SYSTEM.getValue().equals(systemClassCode) ? tempType ? LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce") : LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a")
                        : hotSystem ? tempType ? LocaleMessageUtil.getMessage("key_8144d6421321446fbd8e554ee14a20ce") : LocaleMessageUtil.getMessage("key_619ed56bd4ca4e4385d7c2d53933bb9a")
                        : null;
                if (StrUtil.isNotBlank(chillWaterDetaSetName)) {
                    pumpConfig.setChillWaterDetaSetName(chillWaterDetaSetName);
                    pumpConfig.setChillWaterDetaSetType(tempType ? DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET : DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET);
                    pumpConfig.setChillWaterDetaUnit(tempType ? "℃" : "kPa");
                }
            }
        }

        return pumpConfig;
    }

    /**
     * 获取空调工况配置的对象
     *
     * @param workInstanceValueObjects
     * @return
     */
    @NotNull
    private Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> getAirconditioningWorkInstanceConfigValueObjectMap(List<DemandConfigAirconditioningWorkInstanceValueObject> workInstanceValueObjects) {
        Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> workInstanceConfigValueObjectMap = new HashMap<>();
        if (CollectionUtil.isEmpty(workInstanceValueObjects)) {
            return workInstanceConfigValueObjectMap;
        }
        for (DemandConfigAirconditioningWorkInstanceValueObject workInstanceValueObject : workInstanceValueObjects) {
            DemandConfigAirconditioningWorkInstanceConfigValueObject systemConfig = workInstanceValueObject.getSystemConfig();
            List<DemandConfigAirconditioningWorkInstanceConfigValueObject> pumpConfigs = workInstanceValueObject.getPumpConfig();
            if (systemConfig != null) {
                workInstanceConfigValueObjectMap.put(systemConfig.getId(), systemConfig);
            }
            if (CollectionUtil.isNotEmpty(pumpConfigs)) {
                pumpConfigs.forEach(item -> workInstanceConfigValueObjectMap.put(item.getId(), item));
            }
        }

        return workInstanceConfigValueObjectMap;
    }

    /**
     * 将查询出来的壳配置参数，保存入库一次
     *
     * @param items                                           物品
     * @param coolingSeasonInstanceMap                        冷却季节实例map
     * @param heatingSeasonInstanceMap                        加热季节实例map
     * @param airconditioningWorkInstanceConfigValueObjectMap 空调工作实例配置价值对象map
     * @return {@code List<ShellConfigBasic> }
     * @since 7.250801.0
     */
    private List<ShellConfigBasic> convertAlgShellConfigGroupItems(List<ShellConfigItem> items, Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> coolingSeasonInstanceMap,
                                                                   Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> heatingSeasonInstanceMap,
                                                                   Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> airconditioningWorkInstanceConfigValueObjectMap,
                                                                   Map<String, DemandConfigAirconditioningWorkInstanceConfigValueObject> heatAirconditioningWorkInstanceConfigValueObjectMap) {
        if (CollectionUtil.isEmpty(items)) {
            return null;
        }
        List<ShellConfigBasic> shellConfigBasics = new ArrayList<>();
        for (ShellConfigItem configItem : items) {
            ShellConfigBasic shellConfigBasic = new ShellConfigBasic();
            shellConfigBasic.setCode(configItem.getCode());
            shellConfigBasic.setValue(configItem.getShellConfigValue());
            shellConfigBasic.setObjectId(configItem.getObjectId());
            shellConfigBasic.setObjectName(configItem.getObjectName());
            // 判断是否需要进行值的重写
            if (StrUtil.isNotBlank(configItem.getObjectId())) {
                if (airconditioningWorkInstanceConfigValueObjectMap.containsKey(configItem.getObjectId())) {
                    shellConfigBasic.setValue(this.overrideValue(configItem.getObjectId(), shellConfigBasic.getValue(), configItem.getCode(), airconditioningWorkInstanceConfigValueObjectMap.get(configItem.getObjectId()), true, true, true));
                } else if (heatAirconditioningWorkInstanceConfigValueObjectMap.containsKey(configItem.getObjectId())) {
                    shellConfigBasic.setValue(this.overrideValue(configItem.getObjectId(), shellConfigBasic.getValue(), configItem.getCode(), heatAirconditioningWorkInstanceConfigValueObjectMap.get(configItem.getObjectId()), false, true, true));
                } else {
                    shellConfigBasic.setValue(this.overrideValue(configItem.getObjectId(), shellConfigBasic.getValue(), configItem.getCode(), coolingSeasonInstanceMap.get(configItem.getObjectId()), true, true, false));
                    shellConfigBasic.setValue(this.overrideValue(configItem.getObjectId(), shellConfigBasic.getValue(), configItem.getCode(), heatingSeasonInstanceMap.get(configItem.getObjectId()), false, true, false));
                }
            }

            shellConfigBasics.add(shellConfigBasic);
        }

        return shellConfigBasics;
    }

    /**
     * 重写壳配置值
     *
     * @param objectId             对象ID
     * @param value                当前配置值
     * @param shellConfigCode      当前配置编码
     * @param seasonInstanceConfig 空调运行工况配置
     * @param cooling              是否供冷
     * @param syncShellConfig      同步方向，true-空调运行标准同步至壳配置，false-壳配置同步到空调运行工况
     * @param isAirConditioning    是否是空调冷源
     * @return
     */
    private String overrideValue(String objectId, String value, String shellConfigCode, DemandConfigAirconditioningWorkInstanceConfigValueObject seasonInstanceConfig, boolean cooling, boolean syncShellConfig, boolean isAirConditioning) {
        if (seasonInstanceConfig == null || !objectId.equals(seasonInstanceConfig.getId())) {
            return value;
        }

        // 后重新给什么字段赋值，0-supplyTemp, 1-supplyTempTime，2-lowerChillWaterDetaSet-upperChillWaterDetaSet
        int fieldNameFlag = -1;
        boolean upper = true;
        String overrideValue = null;
        try {
            if (isAirConditioning) {
                return handleAirConditioningShellConfigValue(shellConfigCode, value, seasonInstanceConfig, overrideValue, upper, syncShellConfig, cooling);
            }

            boolean tempType = DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.equals(seasonInstanceConfig.getChillWaterDetaSetType());

            // 冷机出水温度设定逻辑条件输入（针对单个冷站）
            if (cooling && ShellConfigItemEnum.ALG_COOLING_OUT_TEMP_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 0;
                overrideValue = this.getConfigLimitValue(value, null, seasonInstanceConfig.getSupplyTemp());
            } else if (cooling && ShellConfigItemEnum.ALG_COOLING_OUT_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            }

            // 供回水温差/压差逻辑条件输入（针对单个冷站）
            else if (cooling && tempType && ShellConfigItemEnum.ALG_COOLING_SYSTEM_TEMP_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (cooling && !tempType && ShellConfigItemEnum.ALG_COOLING_SYSTEM_PRESS_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 冷站下的泵组 泵组供回水温差/压差逻辑条件输入（针对单个泵组）
            else if (cooling && ShellConfigItemEnum.ALG_COOLING_PRESS_DIFF_TEMP_UPPER.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 0;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTemp());
            } else if (cooling && ShellConfigItemEnum.ALG_COOLING_PRESS_DIFF_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            } else if (cooling && tempType && ShellConfigItemEnum.ALG_COOLING_TEMP_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (cooling && !tempType && ShellConfigItemEnum.ALG_COOLING_PRESS_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 主机出水温度设定逻辑条件输入-供冷模式（针对单个冷热站）
            else if (cooling && ShellConfigItemEnum.ALG_COLDHOT_OUT_TEMP_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 0;
                overrideValue = this.getConfigLimitValue(value, null, seasonInstanceConfig.getSupplyTemp());
            } else if (cooling && ShellConfigItemEnum.ALG_COLDHOT_OUT_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            }

            // 供回水温差/压差逻辑条件输入-供冷模式（针对单个冷热站）
            else if (cooling && tempType && ShellConfigItemEnum.ALG_COLDHOT_SYSTEM_COOL_TEMP_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (cooling && !tempType && ShellConfigItemEnum.ALG_COLDHOT_SYSTEM_COOL_PRESS_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 泵组供回水温差/压差逻辑条件输入-供冷模式（针对单个泵组）
            else if (cooling && ShellConfigItemEnum.ALG_COLDHOT_PRESS_DIFF_TEMP_UPPER.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 0;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTemp());
            } else if (cooling && ShellConfigItemEnum.ALG_COLDHOT_PRESS_DIFF_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            } else if (cooling && tempType && ShellConfigItemEnum.ALG_COLDHOT_TEMP_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (cooling && !tempType && ShellConfigItemEnum.ALG_COLDHOT_PRESS_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 主机出水温度设定逻辑条件输入-供热模式（针对单个冷热站）
            else if (!cooling && ShellConfigItemEnum.ALG_COLDHOT_OUT_TEMP_THRESHOLD_HEAT.getCode().equals(shellConfigCode)) {
                upper = false;
                fieldNameFlag = 0;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getSupplyTemp(), null);
            } else if (!cooling && ShellConfigItemEnum.ALG_COLDHOT_OUT_TEMP_OVERRUN_TIME_HEAT.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            }

            // 供回水温差/压差逻辑条件输入-供热模式（针对单个冷热站）
            else if (!cooling && tempType && ShellConfigItemEnum.ALG_COLDHOT_SYSTEM_HOT_TEMP_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (!cooling && !tempType && ShellConfigItemEnum.ALG_COLDHOT_SYSTEM_HOT_PRESS_DIFF_THRESHOLD.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 泵组供回水温差/压差逻辑条件输入-供热模式（针对单个泵组）
            else if (!cooling && ShellConfigItemEnum.ALG_COLDHOT_PRESS_DIFF_TEMP_UPPER_HEAT.getCode().equals(shellConfigCode)) {
                upper = false;
                fieldNameFlag = 0;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTemp());
            } else if (!cooling && ShellConfigItemEnum.ALG_COLDHOT_PRESS_DIFF_TEMP_OVERRUN_TIME_HEAT.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            } else if (!cooling && tempType && ShellConfigItemEnum.ALG_COLDHOT_TEMP_DIFF_THRESHOLD_HEAT.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (!cooling && !tempType && ShellConfigItemEnum.ALG_COLDHOT_PRESS_DIFF_THRESHOLD_HEAT.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 出水温度设定逻辑条件（针对单个热站）
            else if (!cooling && ShellConfigItemEnum.ALG_HOT_TEMP_DEFAULT_MAX_MIN.getCode().equals(shellConfigCode)) {
                upper = false;
                fieldNameFlag = 0;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getSupplyTemp(), null);
            } else if (!cooling && ShellConfigItemEnum.ALG_HOT_TEMP_DEFAULT_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            }

            // 温差/压差设定逻辑条件（针对单个热站）
            else if (!cooling && tempType && ShellConfigItemEnum.ALG_HOT_DETA_TEMP_PRESS_TEMP_MAX_MIN.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (!cooling && !tempType && ShellConfigItemEnum.ALG_HOT_DETA_TEMP_PRESS_PRESS_MAX_MIN.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            // 温差/压差设定逻辑条件（针对单个泵组）
            else if (!cooling && ShellConfigItemEnum.ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_UPPER.getCode().equals(shellConfigCode)) {
                upper = false;
                fieldNameFlag = 0;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTemp());
            } else if (!cooling && ShellConfigItemEnum.ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 1;
                overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
            } else if (!cooling && tempType && ShellConfigItemEnum.ALG_HOT_DETA_TEMP_PRESS_PUMP_TEMP_MAX_MIN.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            } else if (!cooling && !tempType && ShellConfigItemEnum.ALG_HOT_DETA_TEMP_PRESS_PUMP_PRESS_MAX_MIN.getCode().equals(shellConfigCode)) {
                fieldNameFlag = 2;
                overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getLowerChillWaterDetaSet(), seasonInstanceConfig.getUpperChillWaterDetaSet());
            }

            if (!syncShellConfig && fieldNameFlag > -1 && StrUtil.isNotBlank(overrideValue) && !StrUtil.EMPTY.equals(overrideValue.trim())) {
                if (fieldNameFlag == 0 && StrUtil.isNotBlank(seasonInstanceConfig.getSupplyTempName())) {
                    String[] split = overrideValue.split(",");
                    Field declaredField = seasonInstanceConfig.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.FIELD_NAME_SUPPLY_TEMP);
                    declaredField.setAccessible(true);
                    declaredField.set(seasonInstanceConfig, split.length < 2 ? new BigDecimal(overrideValue) : new BigDecimal(upper ? split[1] : split[0]));
                } else if (fieldNameFlag == 1 && StrUtil.isNotBlank(seasonInstanceConfig.getSupplyTempName())) {
                    Field declaredField = seasonInstanceConfig.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.FIELD_NAME_SUPPLY_TEMP_TIME);
                    declaredField.setAccessible(true);
                    declaredField.set(seasonInstanceConfig, Integer.valueOf(overrideValue));
                } else if (fieldNameFlag == 2 && StrUtil.isNotBlank(seasonInstanceConfig.getChillWaterDetaSetName())) {
                    String[] split = overrideValue.split(",");
                    Field declaredField = seasonInstanceConfig.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.FIELD_NAME_LOWER_CHILL_WATER_DETA_SERT);
                    declaredField.setAccessible(true);
                    declaredField.set(seasonInstanceConfig, new BigDecimal(split[0]));

                    declaredField = seasonInstanceConfig.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.FIELD_NAME_UPPER_CHILL_WATER_DETA_SERT);
                    declaredField.setAccessible(true);
                    declaredField.set(seasonInstanceConfig, new BigDecimal(split[1]));
                }
            }

        } catch (Exception e) {
            log.error("重写壳配置值异常, 壳配置编码-" + shellConfigCode + "，重写后值-" + overrideValue, e);
        }
        return StrUtil.isBlank(overrideValue) ? value : overrideValue;
    }


    /**
     * 处理空调冷源壳配置value
     *
     * @param shellConfigCode      shell配置代码
     * @param value                值
     * @param seasonInstanceConfig 季节实例配置
     * @param overrideValue        覆盖值
     * @param upper                上部
     * @param syncShellConfig      同步shell配置
     * @param cooling              是否是供冷
     * @return {@code String }
     * @since 7.250801.0
     */
    private String handleAirConditioningShellConfigValue(String shellConfigCode, String value, DemandConfigAirconditioningWorkInstanceConfigValueObject seasonInstanceConfig, String overrideValue, boolean upper, boolean syncShellConfig, boolean cooling) {
        // 后重新给什么字段赋值，0-supplyTemp, 1-supplyTempTime，2-lowerChillWaterDetaSet-upperChillWaterDetaSet，3-回水温度上下限
        int fieldNameFlag = -1;
        boolean listFieldNameFlag = false;
        // 是否有温差
        boolean isHaveTemp = false;
        // 是否有压差
        boolean isHavePress = false;
        // 冷机出水温度设定逻辑条件输入（针对单个冷站）
        if (cooling && ShellConfigItemEnum.ALG_AIR_COOLING_TEMP_MAX_MIN.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 0;
            overrideValue = this.getConfigLimitValue(value, null, seasonInstanceConfig.getSupplyTemp());
        } else if (cooling && ShellConfigItemEnum.ALG_AIR_COOLING_TEMP_DIFF_UPPER.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 0;
            overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTemp());
        } else if (cooling && ShellConfigItemEnum.ALG_AIR_COOLING_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 1;
            overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
        } else if (cooling && ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 1;
            overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
        } else if (!cooling && ShellConfigItemEnum.ALG_AIR_HOT_TEMP_MAX_MIN.getCode().equals(shellConfigCode)) {
            upper = false;
            fieldNameFlag = 0;
            overrideValue = this.getConfigLimitValue(value, seasonInstanceConfig.getSupplyTemp(), null);
        } else if (!cooling && ShellConfigItemEnum.ALG_AIR_HOT_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 1;
            overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
        } else if (!cooling && ShellConfigItemEnum.ALG_AIR_HOT_PUMP_TEMP_OVERRUN_TIME.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 1;
            overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTempTime());
        } else if (ShellConfigItemEnum.ALG_AIR_HOT_TEMP_DIFF_LOWER.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 1;
            overrideValue = this.getConfigValue(value, seasonInstanceConfig.getSupplyTemp());
        } else if (ShellConfigItemEnum.ALG_AIR_COOLING_TEMP_RETURN_THRESHOLD.getCode().equals(shellConfigCode)) {
            fieldNameFlag = 3;
            overrideValue = this.getConfigLimitValueNotProcess(value,
                    seasonInstanceConfig.getLowerPrimaryChillWaterMainReturnTemp(),
                    seasonInstanceConfig.getUpperPrimaryChillWaterMainReturnTemp(), syncShellConfig);
        }

        List<DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO> tempAndPressureSetList = seasonInstanceConfig.getTempAndPressureSetList();
        if (CollectionUtil.isNotEmpty(tempAndPressureSetList)) {
            // 处理温差压差列表
            for (DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO : tempAndPressureSetList) {
                ShellConfigItemEnum itemEnum = ShellConfigItemEnum.getByCode(shellConfigCode);
                if (itemEnum == null) {
                    continue;
                }
                boolean tempType = DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.equals(tempAndPressureSetDTO.getChillWaterDetaSetType());
                if (tempType) {
                    // 温差
                    switch (itemEnum) {
                        case ALG_AIR_COOLING_TEMP_DIFF_THRESHOLD:
                        case ALG_AIR_COOLING_PUMP_TEMP_DIFF_THRESHOLD:
                        case ALG_AIR_HOT_TEMP_DIFF_THRESHOLD:
                        case ALG_AIR_HOT_PUMP_TEMP_DIFF_THRESHOLD:
                            isHaveTemp = true;
                            listFieldNameFlag = true;
                            overrideValue = this.getConfigLimitValue(value, tempAndPressureSetDTO.getLowerChillWaterDetaSet(), tempAndPressureSetDTO.getUpperChillWaterDetaSet());
                            break;
                        default:
                    }
                } else {
                    // 压差
                    switch (itemEnum) {
                        case ALG_AIR_COOLING_PRES_DIFF_THRESHOLD:
                        case ALG_AIR_COOLING_PUMP_PRES_DIFF_THRESHOLD:
                        case ALG_AIR_HOT_PRES_DIFF_THRESHOLD:
                        case ALG_AIR_HOT_PUMP_PRES_DIFF_THRESHOLD:
                            isHavePress = true;
                            listFieldNameFlag = true;
                            overrideValue = this.getConfigLimitValue(value, tempAndPressureSetDTO.getLowerChillWaterDetaSet(), tempAndPressureSetDTO.getUpperChillWaterDetaSet());
                            break;
                        default:
                    }
                }
            }
        }
        try {
            if (!syncShellConfig && (fieldNameFlag > -1 || listFieldNameFlag) && StrUtil.isNotBlank(overrideValue) && !StrUtil.EMPTY.equals(overrideValue.trim())) {
                if (fieldNameFlag == 0 && StrUtil.isNotBlank(seasonInstanceConfig.getSupplyTempName())) {
                    String[] split = overrideValue.split(",");
                    Field declaredField = seasonInstanceConfig.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.FIELD_NAME_SUPPLY_TEMP);
                    declaredField.setAccessible(true);
                    declaredField.set(seasonInstanceConfig, split.length < 2 ? new BigDecimal(overrideValue) : new BigDecimal(upper ? split[1] : split[0]));
                } else if (fieldNameFlag == 1 && StrUtil.isNotBlank(seasonInstanceConfig.getSupplyTempName())) {
                    Field declaredField = seasonInstanceConfig.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.FIELD_NAME_SUPPLY_TEMP_TIME);
                    declaredField.setAccessible(true);
                    declaredField.set(seasonInstanceConfig, Integer.valueOf(overrideValue));
                } else if (fieldNameFlag == 3 && StrUtil.isNotBlank(seasonInstanceConfig.getPrimaryChillWaterMainReturnTempName())) {
                    String[] split = overrideValue.split(",");
                    seasonInstanceConfig.setLowerPrimaryChillWaterMainReturnTemp(new BigDecimal(split[0]));
                    seasonInstanceConfig.setUpperPrimaryChillWaterMainReturnTemp(new BigDecimal(split[1]));
                }
                if (listFieldNameFlag && CollectionUtil.isNotEmpty(tempAndPressureSetList)) {
                    for (DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO tempAndPressureSetDTO : tempAndPressureSetList) {
                        if (isHaveTemp && DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.equals(tempAndPressureSetDTO.getChillWaterDetaSetType())) {
                            String[] split = overrideValue.split(",");
                            Field declaredField = tempAndPressureSetDTO.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO.FIELD_NAME_LOWER_CHILL_WATER_DETA_SERT);
                            declaredField.setAccessible(true);
                            declaredField.set(tempAndPressureSetDTO, new BigDecimal(split[0]));

                            declaredField = tempAndPressureSetDTO.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO.FIELD_NAME_UPPER_CHILL_WATER_DETA_SERT);
                            declaredField.setAccessible(true);
                            declaredField.set(tempAndPressureSetDTO, new BigDecimal(split[1]));
                        }
                        if (isHavePress && DtpRwdCentralCoolingInfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.equals(tempAndPressureSetDTO.getChillWaterDetaSetType())) {
                            String[] split = overrideValue.split(",");
                            Field declaredField = tempAndPressureSetDTO.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO.FIELD_NAME_LOWER_CHILL_WATER_DETA_SERT);
                            declaredField.setAccessible(true);
                            declaredField.set(tempAndPressureSetDTO, new BigDecimal(split[0]));

                            declaredField = tempAndPressureSetDTO.getClass().getDeclaredField(DemandConfigAirconditioningWorkInstanceConfigValueObject.TempAndPressureSetDTO.FIELD_NAME_UPPER_CHILL_WATER_DETA_SERT);
                            declaredField.setAccessible(true);
                            declaredField.set(tempAndPressureSetDTO, new BigDecimal(split[1]));
                        }
                    }

                }
            }
        } catch (Exception e) {
            logInstance().error("重写空调冷源壳配置值异常, 壳配置编码-" + shellConfigCode + "，重写后值-" + overrideValue, e);
        }

        return StrUtil.isBlank(overrideValue) ? value : overrideValue;
    }

    /**
     * 获取新值
     *
     * @param shellConfigValue
     * @param demandConfigValue
     * @return
     */
    private String getConfigValue(String shellConfigValue, Integer demandConfigValue) {
        if (demandConfigValue == null) {
            return shellConfigValue;
        }
        return demandConfigValue.toString();
    }

    /**
     * 获取新值
     *
     * @param shellConfigValue
     * @param demandConfigValue
     * @return
     */
    private String getConfigValue(String shellConfigValue, BigDecimal demandConfigValue) {
        if (demandConfigValue == null) {
            return shellConfigValue;
        }
        return demandConfigValue.toPlainString();
    }

    /**
     * 获取上下限值
     *
     * @param shellConfigValue       壳配置值
     * @param lowerDemandConfigValue 运行标准配置下限值
     * @param upperDemandConfigValue 运行标准配置上限值
     * @param syncShellConfig        是否为同步壳配置
     * @return 新值
     * @since 7.251201.0
     */
    private String getConfigLimitValueNotProcess(
            String shellConfigValue,
            BigDecimal lowerDemandConfigValue,
            BigDecimal upperDemandConfigValue,
            boolean syncShellConfig) {
        if (lowerDemandConfigValue == null && upperDemandConfigValue == null) {
            // 运行标准配置为空
            return shellConfigValue;
        }
        if (StrUtil.isBlank(shellConfigValue)) {
            // 防止壳配置的默认值为空，现在并没有这种
            String lowerDemandConfig = lowerDemandConfigValue == null ? null : lowerDemandConfigValue.toPlainString();
            String upperDemandConfig = upperDemandConfigValue == null ? null : upperDemandConfigValue.toPlainString();
            return lowerDemandConfig == null ? (upperDemandConfig + "," + upperDemandConfig) : upperDemandConfig == null
                    ? (lowerDemandConfig + "," + lowerDemandConfig) : (lowerDemandConfig + "," + upperDemandConfig);
        }
        String[] split = shellConfigValue.split(",");
        if (split.length < 2) {
            return shellConfigValue;
        }
        BigDecimal lowerValue = new BigDecimal(split[0]);
        BigDecimal upperValue = new BigDecimal(split[1]);
        if (syncShellConfig) {
            if (upperDemandConfigValue != null) {
                upperValue = upperDemandConfigValue;
            }
            if (lowerDemandConfigValue != null) {
                lowerValue = lowerDemandConfigValue;
            }
        }
        return lowerValue.toPlainString() + "," + upperValue.toPlainString();
    }

    /**
     * 获取上下限值
     *
     * @param shellConfigValue       壳配置值
     * @param lowerDemandConfigValue 运行标准配置下限值
     * @param upperDemandConfigValue 运行标准配置上限值
     * @return 新值
     */
    private String getConfigLimitValue(
            String shellConfigValue,
            BigDecimal lowerDemandConfigValue,
            BigDecimal upperDemandConfigValue) {
        if (lowerDemandConfigValue == null && upperDemandConfigValue == null) {
            // 运行标准配置为空
            return shellConfigValue;
        }
        if (StrUtil.isBlank(shellConfigValue)) {
            // 防止壳配置的默认值为空，现在并没有这种
            String lowerDemandConfig = lowerDemandConfigValue == null ? null : lowerDemandConfigValue.toPlainString();
            String upperDemandConfig = upperDemandConfigValue == null ? null : upperDemandConfigValue.toPlainString();
            return lowerDemandConfig == null ? (upperDemandConfig + "," + upperDemandConfig) : upperDemandConfig == null
                    ? (lowerDemandConfig + "," + lowerDemandConfig) : (lowerDemandConfig + "," + upperDemandConfig);
        }
        String[] split = shellConfigValue.split(",");
        if (split.length < 2) {
            return shellConfigValue;
        }
        BigDecimal lowerValue = new BigDecimal(split[0]);
        BigDecimal upperValue = new BigDecimal(split[1]);
        if (upperDemandConfigValue != null) {
            upperValue = upperDemandConfigValue;
            if (upperDemandConfigValue.compareTo(lowerValue) < 0) {
                lowerValue = upperDemandConfigValue;
            }
        }
        if (lowerDemandConfigValue != null) {
            lowerValue = lowerDemandConfigValue;
            if (lowerDemandConfigValue.compareTo(upperValue) > 0) {
                upperValue = lowerDemandConfigValue;
            }
        }
        return lowerValue.toPlainString() + "," + upperValue.toPlainString();
    }

    @Override
    public Boolean compatibleValueSelectConfig() {
        List<GroupDTO> groupDTOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(groupLimit)) {
            groupDTOS = midPermissionClientService.listGroupOfValid(false, false);
        } else {
            for (String groupCode : groupLimit) {
                GroupDTO groupDTO = new GroupDTO();
                groupDTO.setGroupCode(groupCode);
                groupDTOS.add(groupDTO);
            }

        }

        if (CollectionUtils.isEmpty(groupDTOS)) {
            logProjectInstance().error("未查询到集团编码信息");
            return true;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (GroupDTO groupDTO : groupDTOS) {
            String groupCode = groupDTO.getGroupCode();
            ContextUtil.setGroupCode(groupCode);
            ContextUtil.setTenant(groupCode);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                handleDemandConfigAirConditioningCustomSnapshot();
            }, BusinessThreadPoolExecutor.ttlExecutor());
            futures.add(future);
        }
        // 确定任务执行完成再返回
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return true;
    }

    private void handleDemandConfigAirConditioningCustomSnapshot() {
        // 查询价值选择配置
        List<DemandConfigAirconditioningCustomTimeGroup> list = demandConfigAirconditioningCustomMapper.queryFixDataList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        // 先按项目分组 项目-日期-配置列表
        Map<String, List<DemandConfigAirconditioningCustomTimeGroup>> projectConfigMap = list.stream().collect(Collectors.groupingBy(DemandConfigAirconditioningCustomTimeGroup::getProjectId));
        List<DemandConfigAirconditioningCustomTimeGroup> updateDataList = new ArrayList<>();
        for (String projectId : projectConfigMap.keySet()) {
            List<DemandConfigAirconditioningCustomTimeGroup> dataList = projectConfigMap.get(projectId);
            if (CollectionUtils.isEmpty(dataList)) {
                continue;
            }
            // 先按日期排序
            List<DemandConfigAirconditioningCustomTimeGroup> sorted = dataList.stream().filter(e -> Objects.nonNull(e.getConfigData())).sorted(Comparator.comparing(DemandConfigAirconditioningCustomTimeGroup::getMaxValidEndTime)).collect(Collectors.toCollection(LinkedList::new));
            Map<LocalDate, List<ValueSelectDTO>> dateConfigMap = new HashMap<>();
            List<ValueSelectDTO> previousConfig = null;
            for (DemandConfigAirconditioningCustomTimeGroup data : sorted) {
                DemandConfigAirconditioningCustomDTO configData = data.getConfigData();
                List<ValueSelectDTO> valueSelectConfig = configData.getValueSelectConfig();
                LocalDateTime maxValidEndTime = data.getMaxValidEndTime();
                LocalDate date = maxValidEndTime.toLocalDate();
                if (CollectionUtils.isNotEmpty(valueSelectConfig)) {
                    if (CollectionUtils.isNotEmpty(valueSelectConfig)) {
                        previousConfig = new ArrayList<>(valueSelectConfig);
                    }
                    dateConfigMap.put(date, valueSelectConfig);
                } else if (CollectionUtils.isNotEmpty(previousConfig)) {
                    dateConfigMap.put(date, previousConfig);
                }
            }
            for (DemandConfigAirconditioningCustomTimeGroup data : sorted) {
                LocalDateTime maxValidEndTime = data.getMaxValidEndTime();
                LocalDate date = maxValidEndTime.toLocalDate();
                List<ValueSelectDTO> valueSelectDTOS = dateConfigMap.get(date);
                if (CollectionUtils.isEmpty(valueSelectDTOS)) {
                    continue;
                }
                DemandConfigAirconditioningCustomTimeGroup updateData = new DemandConfigAirconditioningCustomTimeGroup();
                updateData.setId(data.getId());
                DemandConfigAirconditioningCustomDTO configData = new DemandConfigAirconditioningCustomDTO();
                BeanUtils.copyProperties(data.getConfigData(), configData);
                configData.setValueSelectConfig(valueSelectDTOS);
                updateData.setConfigData(configData);
                updateData.setMaxValidEndTime(data.getMaxValidEndTime());
                updateDataList.add(updateData);
            }
        }
        logProjectInstance("").info("更新空调需求配置数据", Pair.of("updateDataList", JSON.toJSONString(updateDataList)));
        // 更新数据
        updateDataList.forEach(data -> demandConfigAirconditioningCustomMapper.updateDemandConfigAirconditioningCustomTimeGroup(data));
    }

    @Override
    public String logTag() {
        return "需求管理-空调需求自定义配置";
    }
}
