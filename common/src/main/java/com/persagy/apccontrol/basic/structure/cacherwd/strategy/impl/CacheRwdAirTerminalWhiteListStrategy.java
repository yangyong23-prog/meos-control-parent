package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigByInfoCodeParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.SaveShellConfigParam;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.ShellConfigBasic;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.cacherwd.WhitelistTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * 物理世界对象缓存-空调末端策略实现
 *
 * @author wxf
 * @since 7.260302.0
 */
@Component(CacheRwdAirTerminalWhiteListStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdAirTerminalWhiteListStrategy extends BaseCacheRwdStrategy {

    public static final String BEAN_NAME = "cacheRwdAirTerminalWhiteListStrategy";

    private final CacheRwdDomainService cacheRwdDomainService;

    /**
     * 壳配置服务，用于读取末端空间范围模式并在首版迁移时补写白名单模式。
     *
     * <p>初始化不能复用吞异常的通用读取方法，否则瞬时查询失败会被误判为全量模式；迁移补写
     * 必须作为新版 Key 替换的本地回调执行，避免模式与对象范围只成功一部分。</p>
     *
     * @since v7.260701.0
     */
    private final @NonNull ShellConfigService shellConfigService;

    /**
     * 缓存对象模型状态服务；历史白名单只允许在数据库首次 READY 前迁移一次。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    /**
     * RWD 对象服务，用于按白名单 ID 一次查询完整缓存字段。
     *
     * @since v7.260701.0
     */
    private final @NonNull DtpRwdClientService dtpRwdClientService;

    /**
     * 新版对象 Key 关联仓储；首版迁移完成后，白名单配置范围只从该关联读取。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 新版对象模型写入服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.AIR_TERMINAL;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        String key = CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
        List<String> objectIds = this.cacheObjectRepository.listObjectIdsByCacheKey(key);
        if (CollectionUtils.isEmpty(objectIds) && this.shouldMigrateLegacyWhitelist()) {
            // 历史版本以旧白名单 Key 是否有对象同时表达模式和范围，因此首版迁移必须先于
            // 缺失时默认为 FULL 的新版壳配置判断，避免旧白名单被清空后直接写入 READY。
            List<CacheRwdObject> legacyObjects =
                    this.cacheRwdDomainService.getByCustomIndexKey(key);
            if (CollectionUtils.isNotEmpty(legacyObjects)) {
                List<String> legacyObjectIds = legacyObjects.stream()
                        .map(CacheRwdObject::getObjectId)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(legacyObjectIds)) {
                    this.logProjectInstance().info("迁移历史末端白名单到新版缓存键",
                            Pair.of("objectCount", legacyObjectIds.size()));
                    return this.replaceWhitelist(
                            key, legacyObjectIds, this::saveWhitelistSpaceRangeConfig);
                }
            }
        }
        if (!this.isWhitelistSpaceRangeConfigured()) {
            // FULL 模式不保留白名单范围，完整重建同时清理直接壳配置保存等路径留下的旧 Key。
            return this.replaceWhitelist(key, Collections.emptyList());
        }
        return this.replaceWhitelist(key, objectIds);
    }

    /**
     * 判断是否需要在首次初始化中读取历史白名单配置 ID。
     *
     * <p>调用前已经确认新版白名单 Key 为空；这里只判断数据库是否已有 READY 事实。一旦
     * READY，旧表即永久失去配置权威性。这里不能只看 Redis，否则服务启动恢复窗口会把旧残留
     * 重新迁回。</p>
     *
     * @return 是否允许执行一次历史白名单迁移
     * @since v7.260701.0
     */
    private boolean shouldMigrateLegacyWhitelist() {
        return this.cacheObjectModelStatusService.getPersistedReadyTime(
                RunningMonitorTypeEnum.AIR_TERMINAL) == null;
    }

    /**
     * 持久化首版迁移后的末端白名单模式。
     *
     * <p>该方法只执行本地壳配置写入，由新版 Key 替换的短事务回调调用，使历史对象 ID 和
     * {@code WHITE_LIST} 模式只能一起提交或一起回滚。</p>
     *
     * @return 是否保存成功
     * @since v7.260701.0
     */
    private boolean saveWhitelistSpaceRangeConfig() {
        ShellConfigBasic item = new ShellConfigBasic();
        item.setCode(ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getCode());
        item.setValue(WhitelistTypeEnum.CUSTOM.getShellConfigValue());

        SaveShellConfigParam.ShellConfigGroup group =
                new SaveShellConfigParam.ShellConfigGroup();
        group.setGroup(ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getGroup());
        group.setTab(ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getTab());
        group.setItems(Lists.newArrayList(item));

        SaveShellConfigParam saveParam = new SaveShellConfigParam();
        saveParam.setProjectId(ContextUtil.getProjectId());
        saveParam.setScene(ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getScene());
        saveParam.setGroups(Lists.newArrayList(group));
        return Boolean.TRUE.equals(this.shellConfigService.save(
                ContextUtil.getGroupCode(), saveParam));
    }

    /**
     * 读取末端持久化空间范围模式。
     *
     * <p>配置缺失时使用 {@code FULL} 默认值；配置查询异常必须继续抛出，使本次完整构建失败且
     * 不写 READY，不能把“无法确认模式”伪装成全量模式。</p>
     *
     * @return {@code true} 表示当前明确选择白名单空间范围
     * @since v7.260701.0
     */
    private boolean isWhitelistSpaceRangeConfigured() {
        QueryShellConfigByInfoCodeParam queryParam = new QueryShellConfigByInfoCodeParam();
        queryParam.setProjectId(ContextUtil.getProjectId());
        queryParam.setCode(ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getCode());
        ShellConfigItem configuredItem = this.shellConfigService.queryByInfoCode(
                ContextUtil.getGroupCode(), queryParam);
        String configuredRange = configuredItem == null
                || StringUtils.isBlank(configuredItem.getValue())
                ? ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getDefaultValues()
                : configuredItem.getValue();
        if (StringUtils.equals(
                WhitelistTypeEnum.CUSTOM.getShellConfigValue(),
                configuredRange)) {
            return true;
        }
        if (!StringUtils.equals(
                ShellConfigItemEnum.SPACE_RANGE_TERMINAL.getDefaultValues(),
                configuredRange)) {
            this.logProjectInstance().warn(
                    "末端空间范围壳配置值无法识别，按全量可管控空间清理白名单 Key",
                    Pair.of("configValue", configuredRange));
        }
        return false;
    }

    /**
     * 按配置对象 ID 实时替换新版白名单缓存。
     *
     * <p>RWD 只查询一次，实际命中的空间直接作为新版对象投影输入，避免生成旧缓存对象或
     * 再按 ID 补查。未命中的 ID 不写入，空配置只清理新版 Key 关联。</p>
     *
     * @param cacheKey 白名单新版缓存 Key
     * @param objectIds 白名单配置对象 ID
     * @return 是否替换成功
     * @throws BusinessException Key 或 RWD 对象结构不合法时抛出
     * @since v7.260701.0
     */
    public boolean replaceWhitelist(
            @NonNull String cacheKey,
            @NonNull Collection<String> objectIds) {
        return this.replaceWhitelist(cacheKey, objectIds, () -> true);
    }

    /**
     * 按配置对象 ID 实时准备新版白名单，并在 Key 替换后执行本地提交动作。
     *
     * <p>RWD 查询在进入写服务前完成；本地提交动作与新版 Key 替换共用一个短事务，返回
     * {@code false} 时两部分均回滚。</p>
     *
     * @param cacheKey 白名单新版缓存 Key
     * @param objectIds 白名单配置对象 ID
     * @param afterReplaceAction Key 替换后的本地持久化动作
     * @return 是否完成原子替换和本地持久化
     * @throws BusinessException Key 或 RWD 对象结构不合法时抛出
     * @since v7.260701.0
     */
    public boolean replaceWhitelist(
            @NonNull String cacheKey,
            @NonNull Collection<String> objectIds,
            @NonNull BooleanSupplier afterReplaceAction) {
        List<DtpRwdObjectDTO> sourceObjects = CollectionUtils.isEmpty(objectIds)
                ? Collections.emptyList() : this.listActualWhitelistSpaces(objectIds);
        return this.cacheObjectModelWriteService.replaceModelCacheKey(
                cacheKey, sourceObjects, Collections.emptyList(), afterReplaceAction);
    }

    /**
     * 按白名单 ID 一次批量查询当前仍存在的空间对象。
     *
     * @param objectIds 白名单配置对象 ID
     * @return 实际命中的普通空间
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdObjectDTO> listActualWhitelistSpaces(
            @NonNull Collection<String> objectIds) {
        Set<String> queryFields = CacheObjectExtensionFieldConfig.listRwdQueryFields(
                DtpRwdClassCodeEnum.SPACE.value());
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(objectIds);
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.SPACE);
        queryParam.addWithColumn(queryFields.toArray(new String[0]));
        return this.dtpRwdClientService.listObject(queryParam);
    }

    @Override
    public String logTag() {
        return "物理世界缓存-空调末端白名单策略";
    }
}
