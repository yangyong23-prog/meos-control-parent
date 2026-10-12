package com.persagy.apccontrol.basic.resource.infocodeconfig.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.infocodeconfig.InfoCodeGroupMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import com.persagy.apccontrol.basic.resource.infocodeconfig.InfoCodeGroupResource;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeGroupQueryDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.envet.InfoCodeGroupRemoveEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备信息的分组配置资源实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeGroupResourceImpl
        extends BaseServiceImpl<InfoCodeGroupMapper, InfoCodeGroup>
        implements InfoCodeGroupResource {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public List<InfoCodeGroup> list(@NonNull InfoCodeGroupQueryDaoParam param) {
        return lambdaQuery()
                .in(CollectionUtil.isNotEmpty(param.getIds()), InfoCodeGroup::getId, param.getIds())
                .in(CollectionUtil.isNotEmpty(param.getGroupCodes()), InfoCodeGroup::getGroupCode, param.getGroupCodes())
                .in(CollectionUtil.isNotEmpty(param.getRelParentIds()), InfoCodeGroup::getRelParentId, param.getRelParentIds())
                .list();
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull Collection<String> groupCodes) {
        if (CollectionUtil.isEmpty(groupCodes)) {
            return false;
        }
        boolean result = false;
        List<Long> toBeDelGroupConfigIds = lambdaQuery()
                .select(BaseAssignIdDO::getId)
                .in(InfoCodeGroup::getGroupCode, groupCodes)
                .list().stream()
                .map(BaseAssignIdDO::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(toBeDelGroupConfigIds)) {
            result = removeByIdIgnore(toBeDelGroupConfigIds);
            applicationEventPublisher.publishEvent(new InfoCodeGroupRemoveEvent(toBeDelGroupConfigIds));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<InfoCodeGroup> addForce(@NonNull List<InfoCodeGroup> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            return Lists.newArrayList();
        }
        Set<String> groupCodes = new HashSet<>();
        for (InfoCodeGroup config : configs) {
            config.checkAddEntity();
            config.clearSaveParameter();
            groupCodes.add(config.getGroupCode());
        }
        del(groupCodes);
        saveIgnore(configs);
        return configs;
    }
}
