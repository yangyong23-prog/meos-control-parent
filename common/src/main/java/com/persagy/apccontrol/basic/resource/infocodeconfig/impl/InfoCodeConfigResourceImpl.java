package com.persagy.apccontrol.basic.resource.infocodeconfig.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.infocodeconfig.InfoCodeConfigMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.IotMonitorConfig;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeConfigResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeConfigQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeConfigRemoveEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 信息点配置资源实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeConfigResourceImpl
        extends BaseServiceImpl<InfoCodeConfigMapper, InfoCodeConfig>
        implements InfoCodeConfigResource, BizComponent<InfoCodeConfigResource> {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<InfoCodeConfig> list(@NonNull InfoCodeConfigQueryDaoParam param) {
        param.checkAndSet();
        return lambdaQuery()
                .select(param.toSelectFunctions())
                .in(CollectionUtil.isNotEmpty(param.getInfoCodeTypes()), InfoCodeConfig::getInfoCodeType, param.getInfoCodeTypes())
                .in(CollectionUtil.isNotEmpty(param.getRelGroupCodes()), InfoCodeConfig::getRelGroupCode, param.getRelGroupCodes())
                .in(CollectionUtil.isNotEmpty(param.getRelTemplateTypeIds()), InfoCodeConfig::getRelTemplateTypeId, param.getRelTemplateTypeIds())
                .list();
    }

    @Override
    public List<InfoCodeConfig> listInvalidByGroupCode(@NonNull Collection<String> groupCodes) {
        if (CollectionUtil.isEmpty(groupCodes)) {
            return Lists.newArrayList();
        }
        return lambdaQuery()
                .notIn(InfoCodeConfig::getRelGroupCode, groupCodes)
                .list();
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull Collection<Long> relTemplateTypeIds) {
        if (CollectionUtil.isEmpty(relTemplateTypeIds)) {
            return false;
        }
        boolean result = false;
        List<Long> toBeDelConfigIds = lambdaQuery()
                .select(BaseAssignIdDO::getId)
                .in(InfoCodeConfig::getRelTemplateTypeId, relTemplateTypeIds)
                .list().stream().map(BaseAssignIdDO::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(toBeDelConfigIds)) {
            result = removeByIdIgnore(toBeDelConfigIds);
            applicationEventPublisher.publishEvent(new InfoCodeConfigRemoveEvent(toBeDelConfigIds));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<InfoCodeConfig> addForce(@NonNull List<InfoCodeConfig> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return Lists.newArrayList();
        }
        Set<Long> relTemplateTypeIds = new HashSet<>();
        for (InfoCodeConfig config : configs) {
            config.checkAddEntity();
            config.clearSaveParameter();
            relTemplateTypeIds.add(config.getRelTemplateTypeId());
        }
        del(relTemplateTypeIds);
        saveIgnore(configs);
        return configs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editRelConfigId(@NonNull Long configId, @NonNull Set<Long> relConfigIds) {
        lambdaUpdate()
                .set(InfoCodeConfig::getRelInfoCodeConfigIds, relConfigIds, JACKSON_MAPPING)
                .eq(BaseAssignIdDO::getId, configId)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfoCodeNameByIds(@NonNull Set<Long> ids, @NonNull String infoCodeName) {
        this.updateInfoCodeNameByIds(ids, infoCodeName, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfoCodeNameByIds(
            @NonNull Set<Long> ids,
            @Nullable String infoCodeName,
            @Nullable String infoCodeAliasName) {
        if (CollectionUtils.isEmpty(ids) || StringUtils.isAllBlank(infoCodeName, infoCodeAliasName)) {
            return;
        }
        this.lambdaUpdate()
                .in(BaseAssignIdDO::getId, ids)
                .set(StringUtils.isNotBlank(infoCodeName), InfoCodeConfig::getInfoCodeName, infoCodeName)
                .set(StringUtils.isNotBlank(infoCodeAliasName), InfoCodeConfig::getInfoCodeAliasName, infoCodeAliasName)
                .update();
    }

    @Override
    public List<IotMonitorConfig> listIotMonitorConfig() {
        return baseMapper.listIotMonitorConfig();
    }

    @Override
    public String logTag() {
        return "信息点配置-Dao";
    }
}
