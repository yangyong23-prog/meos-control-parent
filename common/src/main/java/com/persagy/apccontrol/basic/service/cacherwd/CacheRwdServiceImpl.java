package com.persagy.apccontrol.basic.service.cacherwd;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigByInfoCodeParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.SaveShellConfigParam;
import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.ShellConfigBasic;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdModelViewFieldsQueryParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhiteDeleteParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhiteQueryParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhitelistParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.dashboard.space.QuerySpace;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdAirTerminalControlSpaceVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdBuildStatusVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdStatisticsVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdWhitelistVO;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectModelViewRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl.CacheRwdAirTerminalWhiteListStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物理世界缓存服务实现
 *
 * @author wxf
 * @since 2026-03-17
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdServiceImpl implements CacheRwdService, BizComponent<CacheRwdService> {

    /**
     * 各运行监测类型对应的白名单空间范围壳配置。
     *
     * <p>当前只有末端具有独立的空间范围壳配置；后续系统接入时还需要同时补齐对应的
     * 白名单实时查询策略，不能仅增加此映射。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Map<RunningMonitorTypeEnum, ShellConfigItemEnum>
            WHITELIST_SPACE_RANGE_CONFIGS = ImmutableMap.of(
                    RunningMonitorTypeEnum.AIR_TERMINAL,
                    ShellConfigItemEnum.SPACE_RANGE_TERMINAL);

    /**
     * 末端管控空间接口需要查询和返回的字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> AIR_TERMINAL_CONTROL_SPACE_FIELDS =
            ImmutableSet.of(
                    DtpRwdObjectDTO.Fields.id,
                    DtpRwdObjectDTO.Fields.localName,
                    DtpRwdSpaceDTO.Fields.floorId,
                    DtpRwdSpaceDTO.Fields.buildingId);

    /**
     * 模型视图字段接口返回的真实对象 ID 字段名。
     *
     * @since v7.260701.0
     */
    private static final @NonNull String MODEL_VIEW_OBJECT_ID_FIELD = "objectId";

    /**
     * 白名单查询接口需要返回的空间缓存字段。
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> WHITELIST_SPACE_FIELDS = ImmutableSet.of(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.projectId,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdSpaceDTO.Fields.buildingId,
            DtpRwdSpaceDTO.Fields.floorId,
            DtpRwdSpaceDTO.Fields.roomFuncType,
            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());

    private final CacheRwdDomainService cacheRwdDomainService;
    private final QuerySpace airTerminalQuerySpace;
    private final CacheRwdAirTerminalWhiteListStrategy cacheRwdAirTerminalWhiteListStrategy;

    /**
     * 末端运行监测策略，缓存未 READY 时用于实时计算项目全量管控空间。
     *
     * @since v7.260701.0
     */
    private final @NonNull RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * 新缓存键与对象 ID 关联仓储，用于区分白名单配置存在性和对象实际命中结果。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 新缓存模型视图批量查询仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelViewRepository cacheObjectModelViewRepository;

    /**
     * 物理世界实时查询服务，供新版缓存未 READY 时读取当前模型视图字段。
     *
     * @since v7.260701.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    /**
     * 新版缓存对象模型写服务，白名单只通过该服务替换或删除新版 Key。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    /**
     * 壳配置服务，用于持久化和读取白名单空间范围模式。
     *
     * @since v7.260701.0
     */
    private final @NonNull ShellConfigService shellConfigService;


    @Override
    public Boolean triggerBuildCache(RunningMonitorTypeEnum runningMonitorType) {
        Boolean result = this.cacheRwdDomainService.buildCache(runningMonitorType);
        if (!Boolean.TRUE.equals(result)) {
            String buildBusyMessage = LocaleMessageUtil.getMessage(
                    "key_ca2137bc18a8470faffe8580821934ca");
            throw new ApplicationException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                    buildBusyMessage, buildBusyMessage);
        }
        return true;
    }

    @Override
    public CacheRwdBuildStatusVO getBuildStatus(RunningMonitorTypeEnum runningMonitorType) {
        return cacheRwdDomainService.getBuildStatus(runningMonitorType);
    }

    @Override
    public CacheRwdStatisticsVO getStatistics() {
        return cacheRwdDomainService.getStatistics();
    }

    @Override
    public @NonNull List<CacheRwdAirTerminalControlSpaceVO>
            listAirTerminalControlSpaces() {
        List<DtpRwdSpaceDTO> spaces;
        if (this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.AIR_TERMINAL)) {
            // 项目全量 Key 是白名单候选范围，不能根据当前白名单模式改查白名单 Key。
            spaces = this.cacheObjectRepository.listByCacheKey(
                    CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE,
                    DtpRwdSpaceDTO.class,
                    CacheRwdServiceImpl.AIR_TERMINAL_CONTROL_SPACE_FIELDS);
        } else {
            RunningMonitorSpaceQueryParam queryParam =
                    new RunningMonitorSpaceQueryParam();
            queryParam.addWithColumn(
                    CacheRwdServiceImpl.AIR_TERMINAL_CONTROL_SPACE_FIELDS
                            .toArray(new String[0]));
            spaces = this.runningMonitorAirTerminalStrategy.listSpaces(queryParam);
        }
        List<CacheRwdAirTerminalControlSpaceVO> result =
                new ArrayList<>(spaces.size());
        for (DtpRwdSpaceDTO space : spaces) {
            CacheRwdAirTerminalControlSpaceVO item =
                    new CacheRwdAirTerminalControlSpaceVO();
            item.setId(space.getId());
            item.setLocalName(space.getLocalName());
            item.setFloorId(space.getFloorId());
            item.setBuildingId(space.getBuildingId());
            result.add(item);
        }
        return result;
    }

    @Override
    public @NonNull List<Map<String, Object>> listModelViewFieldsByObjectId(
            @NonNull CacheRwdModelViewFieldsQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        if (!this.cacheObjectModelStatusService.isReady(runningMonitorType)) {
            this.logProjectInstance().info("模型视图缓存未就绪，实时查询 RWD",
                    Pair.of("runningMonitorType", runningMonitorType));
            return this.listRealtimeModelViewFields(param.getObjectIds());
        }

        CacheObjectModelViewQueryDaoParam queryParam =
                new CacheObjectModelViewQueryDaoParam();
        queryParam.setIds(param.getObjectIds());
        // 批量查询只读取响应所需的主键和固定投影 JSON，禁止为每个对象单独访问数据库。
        queryParam.addColumns(
                CacheObjectModelView::getId,
                CacheObjectModelView::getViewData);
        List<CacheObjectModelView> modelViews =
                this.cacheObjectModelViewRepository.listModelViews(queryParam);
        List<Map<String, Object>> result = new ArrayList<>(modelViews.size());
        for (CacheObjectModelView modelView : modelViews) {
            String viewData = modelView.getViewData();
            // 迁移允许历史视图 JSON 为 SQL NULL；该情况只保留独立列中的真实对象 ID。
            Map<String, Object> storedFields = StringUtils.isBlank(viewData)
                    ? Collections.emptyMap() : ObjectMapperUtils.readMapFailSafe(viewData);
            Map<String, Object> item = storedFields == null
                    ? new LinkedHashMap<>() : new LinkedHashMap<>(storedFields);
            // 模型视图实体主键是唯一可信对象 ID，并覆盖任何历史脏 JSON 中的同名值。
            item.put(CacheRwdServiceImpl.MODEL_VIEW_OBJECT_ID_FIELD, modelView.getId());
            result.add(item);
        }
        return result;
    }

    /**
     * 按对象 ID 一次查询 RWD 并投影模型视图字段。
     *
     * <p>请求字段取所有 classCode 模型视图配置的并集，以支持同一批请求包含多种对象类；
     * 最终响应仍按每个对象自身 classCode 取交集。转换失败只跳过该条实时结果，不影响同批
     * 其他对象。</p>
     *
     * @param objectIds 待查询对象 ID
     * @return RWD 实际命中的对象 ID 与模型视图字段
     * @since v7.260701.0
     */
    private @NonNull List<Map<String, Object>> listRealtimeModelViewFields(
            @NonNull List<String> objectIds) {
        Set<String> queryFields = new LinkedHashSet<>();
        queryFields.add(DtpRwdObjectDTO.Fields.id);
        queryFields.add(DtpRwdObjectDTO.Fields.classCode);
        CacheObjectExtensionFieldConfig.getModelViewFields().values()
                .forEach(queryFields::addAll);
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(objectIds);
        queryParam.addWithColumn(queryFields.toArray(new String[0]));
        List<DtpRwdObjectDTO> objects = this.dtpRwdClientService.listObject(queryParam);
        List<Map<String, Object>> result = new ArrayList<>(objects.size());
        for (DtpRwdObjectDTO object : objects) {
            String objectJson = ObjectMapperUtils.writeValueAsStringFailSafe(object);
            Map<String, Object> actualFields = StringUtils.isBlank(objectJson)
                    ? null : ObjectMapperUtils.readMapFailSafe(objectJson);
            if (actualFields == null) {
                this.logProjectInstance().warn("RWD 模型视图字段转换失败，跳过当前对象",
                        Pair.of("objectId", object.getId()));
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            for (String field : CacheObjectExtensionFieldConfig.getModelViewFields(
                    object.getClassCode())) {
                // containsKey 用于保留 RWD 显式返回的 null，不为未返回字段补默认值。
                if (actualFields.containsKey(field)) {
                    item.put(field, actualFields.get(field));
                }
            }
            item.put(CacheRwdServiceImpl.MODEL_VIEW_OBJECT_ID_FIELD, object.getId());
            result.add(item);
        }
        return result;
    }

    @Override
    public Boolean saveWhitelist(CacheRwdWhitelistParam param) {
        // 该历史接口用 false 表示请求参数缺失；进入实际写入后，业务校验和基础设施异常继续上抛。
        if (param == null || param.getRunningMonitorType() == null) {
            this.logProjectInstance().error("保存白名单配置失败，参数无效");
            return false;
        }
        if (param.getWhitelistType() == WhitelistTypeEnum.FULL) {
            // 方式 1：全量可管控空间模式 - 只删除用户配置，不保存数据
            return this.switchToFullSpace(param.getRunningMonitorType());
        }
        // 白名单是新版能力，所有类型都只维护新版 Key。
        return this.saveCustomWhitelist(param);
    }

    /**
     * 切换到全量可管控空间模式。
     *
     * <p>全量模式不保存独立白名单快照，只删除新版白名单 Key，不访问旧对象缓存。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @return 是否完成模式切换
     */
    private Boolean switchToFullSpace(RunningMonitorTypeEnum runningMonitorType) {
        this.logProjectInstance().info("切换到全量可管控空间模式",
                Pair.of("runningMonitorType", runningMonitorType));

        String customIndexKey = CacheObjectModelKeyConfig.buildWhitelistKey(
                runningMonitorType, DtpRwdObjectTypeEnum.SPACE);
        // FULL 必须先成为持久化事实，再在同一短事务内删除已经失效的白名单 Key。
        return this.cacheObjectModelWriteService.removeCacheKeys(
                Collections.singletonList(customIndexKey),
                () -> this.saveWhitelistSpaceRangeConfig(
                        runningMonitorType, WhitelistTypeEnum.FULL));
    }

    /**
     * 保存用户自定义白名单。
     *
     * <p>先校验对象类型和 ID，再由白名单策略在事务外按本次配置 ID 批量取得 RWD 实际字段，
     * 最后由新版写服务在事务前完成一次本地记录投影并只替换新版 Key。</p>
     *
     * @param param 白名单完整目标集合
     * @return 是否完成白名单替换；对象集合为空时返回 {@code false}
     * @throws BusinessException 对象业务键或类型非法时抛出
     */
    private Boolean saveCustomWhitelist(CacheRwdWhitelistParam param) {
        // 空集合没有可替换的白名单范围，保持历史接口的 false 语义。
        if (CollectionUtils.isEmpty(param.getObjects())) {
            this.logProjectInstance().warn("保存白名单配置失败，objects 为空",
                    Pair.of("runningMonitorType", param.getRunningMonitorType()));
            return false;
        }

        DtpRwdSpaceDTO firstSpace = param.getObjects().get(0);
        if (firstSpace == null) {
            throw CacheObjectModelExceptionFactory.requiredValue("whitelist.space");
        }
        String objType = firstSpace.getObjType();
        DtpRwdObjectTypeEnum objectType = DtpRwdObjectTypeEnum.analyze(objType);
        if (objectType == null) {
            throw CacheObjectModelExceptionFactory.invalidStructure("whitelist.objType");
        }
        String customIndexKey = CacheObjectModelKeyConfig.buildWhitelistKey(
                param.getRunningMonitorType(), objectType);

        List<String> configuredSpaceIds = new ArrayList<>(param.getObjects().size());
        for (DtpRwdSpaceDTO space : param.getObjects()) {
            if (space == null || StringUtils.isBlank(space.getId())) {
                throw CacheObjectModelExceptionFactory.requiredValue("whitelist.space.id");
            }
            configuredSpaceIds.add(space.getId());
        }
        // RWD 在策略中先完成查询；Key 替换和 WHITE_LIST 持久化随后进入同一短事务。
        return this.cacheRwdAirTerminalWhiteListStrategy.replaceWhitelist(
                customIndexKey, configuredSpaceIds,
                () -> this.saveWhitelistSpaceRangeConfig(
                        param.getRunningMonitorType(), WhitelistTypeEnum.CUSTOM));
    }

    @Override
    public CacheRwdWhitelistVO queryWhitelist(CacheRwdWhiteQueryParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();
        WhitelistTypeEnum configuredType = this.getConfiguredWhitelistType(runningMonitorType);
        if (WhitelistTypeEnum.FULL.equals(configuredType)) {
            return this.queryFullWhitelist(runningMonitorType);
        }
        String objType = param.getObjType() != null
                ? param.getObjType().getValue() : DtpRwdObjectTypeEnum.SPACE.getValue();
        String customIndexKey = CacheObjectModelKeyConfig.buildWhitelistKey(
                runningMonitorType, DtpRwdObjectTypeEnum.analyze(objType));
        List<String> configuredSpaceIds = this.cacheObjectRepository
                .listObjectIdsByCacheKey(customIndexKey);
        // 尚未配置独立空间范围壳配置的系统继续兼容原有“有 Key 即白名单”语义。
        if (configuredType == null && CollectionUtils.isEmpty(configuredSpaceIds)) {
            return this.queryFullWhitelist(runningMonitorType);
        }

        List<DtpRwdSpaceDTO> configuredSpaces = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(configuredSpaceIds)) {
            if (this.cacheObjectModelStatusService.isReady(runningMonitorType)) {
                configuredSpaces = this.cacheObjectRepository.listByObjectIds(
                        configuredSpaceIds,
                        DtpRwdSpaceDTO.class,
                        CacheRwdServiceImpl.WHITELIST_SPACE_FIELDS);
            } else {
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                queryParam.setId(configuredSpaceIds);
                queryParam.addWithColumn(
                        CacheRwdServiceImpl.WHITELIST_SPACE_FIELDS.toArray(new String[0]));
                configuredSpaces = this.dtpRwdClientService.listSpace(queryParam);
            }
        }
        this.logProjectInstance().info("使用用户配置白名单模式",
                Pair.of("runningMonitorType", runningMonitorType));
        CacheRwdWhitelistVO vo = new CacheRwdWhitelistVO();
        vo.setRunningMonitorType(runningMonitorType);
        vo.setWhitelistType(WhitelistTypeEnum.CUSTOM);
        vo.setObjects(configuredSpaces);
        return vo;
    }

    @Override
    public Boolean deleteWhitelist(CacheRwdWhiteDeleteParam param) {
        RunningMonitorTypeEnum runningMonitorType = param.getRunningMonitorType();

        String objType = param.getObjType() != null ? param.getObjType().getValue() : "";
        String customIndexKey = CacheObjectModelKeyConfig.buildWhitelistKey(
                runningMonitorType, DtpRwdObjectTypeEnum.analyze(objType));
        boolean removed = this.cacheObjectModelWriteService.removeCacheKeys(
                Collections.singletonList(customIndexKey),
                () -> this.saveWhitelistSpaceRangeConfig(
                        runningMonitorType, WhitelistTypeEnum.FULL));
        if (!removed) {
            return false;
        }

        // 全量模式本身不保存数据，所以不需要额外删除操作。
        this.logProjectInstance().info("删除白名单配置成功",
                Pair.of("runningMonitorType", runningMonitorType));
        return true;
    }

    @Override
    public @NonNull Boolean saveShellConfigAndRemoveAirTerminalWhitelist(
            @NonNull SaveShellConfigParam param) {
        String originalProjectId = ContextUtil.getProjectId();
        try {
            // 事务开始前切换到原请求的目标项目，使壳配置与缓存 SQL 共享同一项目数据源。
            ContextUtil.setProjectId(param.getProjectId());
            return this.cacheObjectModelWriteService.removeCacheKeys(
                    Collections.singletonList(
                            CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE),
                    () -> Boolean.TRUE.equals(this.shellConfigService.save(
                            ContextUtil.getGroupCode(), param)));
        } finally {
            ContextUtil.setProjectId(originalProjectId);
        }
    }

    /**
     * 读取当前运行监测类型持久化的白名单空间范围模式。
     *
     * <p>配置缺失时使用配置项默认值；未知值按全量处理并记录日志。尚未配置独立壳配置的
     * 运行监测类型返回 {@code null}，由调用方兼容原有 Key 判断语义。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @return 已配置的白名单类型；当前类型没有空间范围配置时返回 {@code null}
     * @since v7.260701.0
     */
    private @Nullable WhitelistTypeEnum getConfiguredWhitelistType(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        ShellConfigItemEnum configItem =
                CacheRwdServiceImpl.WHITELIST_SPACE_RANGE_CONFIGS.get(runningMonitorType);
        if (configItem == null) {
            return null;
        }
        QueryShellConfigByInfoCodeParam queryParam = new QueryShellConfigByInfoCodeParam();
        queryParam.setProjectId(ContextUtil.getProjectId());
        queryParam.setCode(configItem.getCode());
        ShellConfigItem configuredItem = this.shellConfigService.queryByInfoCode(
                ContextUtil.getGroupCode(), queryParam);
        String configuredValue = configuredItem == null
                || StringUtils.isBlank(configuredItem.getValue())
                ? configItem.getDefaultValues() : configuredItem.getValue();
        if (StringUtils.equals(
                WhitelistTypeEnum.CUSTOM.getShellConfigValue(), configuredValue)) {
            return WhitelistTypeEnum.CUSTOM;
        }
        if (!StringUtils.equals(configItem.getDefaultValues(), configuredValue)) {
            this.logProjectInstance().warn("白名单空间范围壳配置值无法识别，按全量模式处理",
                    Pair.of("configValue", configuredValue));
        }
        return WhitelistTypeEnum.FULL;
    }

    /**
     * 保存当前运行监测类型的白名单空间范围模式。
     *
     * <p>白名单场景当前只包含空间范围配置项，因此可以通过壳配置服务完整保存该场景。
     * 尚未配置独立壳配置的运行监测类型不阻断新版白名单 Key 操作。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @param whitelistType 白名单类型
     * @return 是否保存成功
     * @since v7.260701.0
     */
    private boolean saveWhitelistSpaceRangeConfig(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @NonNull WhitelistTypeEnum whitelistType) {
        ShellConfigItemEnum configItem =
                CacheRwdServiceImpl.WHITELIST_SPACE_RANGE_CONFIGS.get(runningMonitorType);
        if (configItem == null) {
            return true;
        }

        ShellConfigBasic item = new ShellConfigBasic();
        item.setCode(configItem.getCode());
        item.setValue(whitelistType.getShellConfigValue());
        SaveShellConfigParam.ShellConfigGroup group =
                new SaveShellConfigParam.ShellConfigGroup();
        group.setGroup(configItem.getGroup());
        group.setTab(configItem.getTab());
        group.setItems(Collections.singletonList(item));
        SaveShellConfigParam saveParam = new SaveShellConfigParam();
        saveParam.setProjectId(ContextUtil.getProjectId());
        saveParam.setScene(configItem.getScene());
        saveParam.setGroups(Collections.singletonList(group));
        return Boolean.TRUE.equals(this.shellConfigService.save(
                ContextUtil.getGroupCode(), saveParam));
    }

    /**
     * 实时查询当前项目全量可管控空间。
     *
     * <p>全量模式不复制项目空间到白名单 Key，查询时始终绕过持久化运行结果缓存。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @return 全量白名单响应
     * @since v7.260701.0
     */
    private @NonNull CacheRwdWhitelistVO queryFullWhitelist(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        this.logProjectInstance().info("使用全量可管控空间模式，实时查询",
                Pair.of("runningMonitorType", runningMonitorType));
        DemandInstanceValueObject instanceVO = DemandInstanceValueObject.ofProject();
        instanceVO.setInstanceId(ContextUtil.getProjectId());
        instanceVO.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        List<? extends DtpRwdSpaceVO> allSpaces =
                this.airTerminalQuerySpace.querySpace(instanceVO, false);
        CacheRwdWhitelistVO vo = new CacheRwdWhitelistVO();
        vo.setRunningMonitorType(runningMonitorType);
        vo.setWhitelistType(WhitelistTypeEnum.FULL);
        vo.setObjects(BeanUtil.copyToList(allSpaces, DtpRwdSpaceDTO.class));
        return vo;
    }

    @Override
    public String logTag() {
        return "物理世界缓存服务";
    }
}
