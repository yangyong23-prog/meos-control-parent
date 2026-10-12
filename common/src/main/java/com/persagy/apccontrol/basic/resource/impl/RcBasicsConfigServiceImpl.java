package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.persagy.apccontrol.basic.mapper.RcBasicsConfigMapper;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigLogService;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.resource.param.RcBasicsConfigQueryDaoParam;
import com.persagy.apccontrol.basic.utils.SnowflakeIdWorker;
import com.persagy.apccontrol.basic.utils.UserUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.entity.PageParams;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【rc_basics_config(配置日志表)】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Service
public class RcBasicsConfigServiceImpl extends SuperServiceImpl<RcBasicsConfigMapper, RcBasicsConfig> implements RcBasicsConfigService {

    @Resource
    RcBasicsConfigLogService rcBasicsConfigLogService;

    @Override
    public List<RcBasicsConfig> listAll() {
        return this.lambdaQuery().list();
    }

    @Override
    public RcBasicsConfig getValid(@NonNull RcBasicsConfigQueryDaoParam param) {
        return list(param).stream().findFirst().orElse(null);
    }

    @Override
    public List<RcBasicsConfig> list(@NonNull RcBasicsConfigQueryDaoParam param) {
        return lambdaQuery(param).list();
    }

    @Override
    public IPage<RcBasicsConfig> pageValidDuration(@NonNull PageParams<RcBasicsConfigQueryDaoParam> param) {
        return lambdaQuery(param.getModel()).page(param.buildPage());
    }

    @Override
    public RcBasicsConfig queryConfig(String projectId, BasicsConfigTypeEnum type) {
        return lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, projectId)
                .eq(RcBasicsConfig::getType, type)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
    }

    @Override
    public RcBasicsConfig queryConfig(String projectId, String systemId, BasicsConfigTypeEnum type) {
        return lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, projectId)
                .eq(RcBasicsConfig::getType, type)
                .eq(StrUtil.isNotBlank(systemId), RcBasicsConfig::getSystemId, systemId)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
    }

    @Override
    public List<RcBasicsConfig> queryListConfig(String projectId, BasicsConfigTypeEnum type, String systemId) {
        return lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, projectId)
                .eq(RcBasicsConfig::getSystemId, systemId)
                .eq(RcBasicsConfig::getType, type)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .list();
    }

    @Override
    public List<RcBasicsConfig> queryListConfig(String projectId, BasicsConfigTypeEnum type, Date startTime, Date endTime) {
        return baseMapper.queryListConfig(projectId, type, startTime, endTime);
    }

    @Override
    public List<RcBasicsConfig> queryListConfig(String projectId, String systemId, BasicsConfigTypeEnum type, Date startTime, Date endTime) {
        return baseMapper.queryListConfig(projectId, systemId, type, startTime, endTime);
    }

    @Override
    public List<RcBasicsConfig> queryListAfterNow(RcBasicsConfig cgf) {
        return lambdaQuery()
                .eq(RcBasicsConfig::getType, cgf.getType())
                .eq(StringUtils.isNotBlank(cgf.getProjectId()), RcBasicsConfig::getProjectId, cgf.getProjectId())
                .eq(StringUtils.isNotBlank(cgf.getChildType()), RcBasicsConfig::getChildType, cgf.getChildType())
                .eq(StringUtils.isNotBlank(cgf.getSystemId()), RcBasicsConfig::getSystemId, cgf.getSystemId())
                .eq(StringUtils.isNotBlank(cgf.getFloorCode()), RcBasicsConfig::getFloorCode, cgf.getFloorCode())
                .eq(StringUtils.isNotBlank(cgf.getArchitectureCode()), RcBasicsConfig::getArchitectureCode, cgf.getArchitectureCode())
                .eq(StringUtils.isNotBlank(cgf.getFacilityCode()), RcBasicsConfig::getFacilityCode, cgf.getFacilityCode())
                .eq(StringUtils.isNotBlank(cgf.getRoomCode()), RcBasicsConfig::getRoomCode, cgf.getRoomCode())
                .eq(StringUtils.isNotBlank(cgf.getResult()), RcBasicsConfig::getResult, cgf.getResult())
                .ge(RcBasicsConfig::getEndTime, new Date())
                .orderByAsc(RcBasicsConfig::getUpdateTime)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByType(RcBasicsConfig cgf) {
        if (BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS.equals(cgf.getType())) {
            throw new BusinessException(
                    ErrorCodeConstants.USER_ERROR_A0400.getCode(),
                    LocaleMessageUtil.getMessage("key_ebf7c774868c4fdbaa0d003002a42fc7"));
        }
        RcBasicsConfig c = this.queryConfig(cgf);

        // 更新
        if (c != null) {
            cgf.setId(c.getId());
            cgf.setUpdateTime(LocalDateTime.now());
            cgf.setUpdateUser(UserUtils.getUserId());
            updateById(cgf);
        } else {
            // 新增
            cgf.setId(SnowflakeIdWorker.getWorkerId());
            cgf.setCreateTime(LocalDateTime.now());
            cgf.setUpdateTime(LocalDateTime.now());
            cgf.setCreateUser(UserUtils.getUserId());
            cgf.setUpdateUser(UserUtils.getUserId());
            save(cgf);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RcBasicsConfig saveOrUpdateBasicsConfig(RcBasicsConfig cgf) {
        if (BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS.equals(cgf.getType())) {
            throw new BusinessException(
                    ErrorCodeConstants.USER_ERROR_A0400.getCode(),
                    LocaleMessageUtil.getMessage("key_ebf7c774868c4fdbaa0d003002a42fc7"));
        }
        cgf.setMd5ByResult();
        cgf.setUpdateTime(LocalDateTime.now());
        cgf.setUpdateUser(UserUtils.getUserId());
        RcBasicsConfig before = cgf;
        if (StringUtils.isBlank(cgf.getId())) {
            // 新增
            cgf.setId(SnowflakeIdWorker.getWorkerId());
            cgf.setCreateTime(LocalDateTime.now());
            cgf.setCreateUser(UserUtils.getUserId());
        } else {
            // 编辑
            before = getBaseMapper().selectById(cgf.getId());
        }
        saveOrUpdate(cgf);
        // 记录配置快照
        rcBasicsConfigLogService.saveConfigLog(cgf, before);
        return cgf;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int upsertCacheObjectModelStatus(
            @NonNull RcBasicsConfig config) {
        return super.getBaseMapper().upsertCacheObjectModelStatus(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int editHumitureIgnoreSpaceIds(
            @NonNull RcBasicsConfigQueryDaoParam param,
            @NonNull Set<String> ignoreSpaceIds) {
        AbstractWrapper<?, ?, ?> wrapper = lambdaQuery(param).getWrapper();
        String ignoreSpaceIdsString = JSONObject.toJSONString(ignoreSpaceIds);
        return getBaseMapper().editHumitureIgnoreSpaceIds(wrapper, ignoreSpaceIdsString);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(RcBasicsConfig cgf) {
        if (Objects.nonNull(cgf)) {
            // 记录配置快照
            rcBasicsConfigLogService.saveConfigLog(cgf, cgf);
        }
        baseMapper.deleteById(cgf);
    }

    /**
     * 通过配置 查询存在的配置
     *
     * @param cfg
     * @return
     */
    public RcBasicsConfig queryConfig(RcBasicsConfig cfg) {
        return lambdaQuery()
                .eq(StrUtil.isNotBlank(cfg.getProjectId()), RcBasicsConfig::getProjectId, cfg.getProjectId())
                .eq(cfg.getType() != null, RcBasicsConfig::getType, cfg.getType())
                .eq(StrUtil.isNotBlank(cfg.getChildType()), RcBasicsConfig::getChildType, cfg.getChildType())
                .eq(StrUtil.isNotBlank(cfg.getSystemId()), RcBasicsConfig::getSystemId, cfg.getSystemId())
                .eq(StrUtil.isNotBlank(cfg.getFloorCode()), RcBasicsConfig::getFloorCode, cfg.getFloorCode())
                .eq(StrUtil.isNotBlank(cfg.getArchitectureCode()), RcBasicsConfig::getArchitectureCode, cfg.getArchitectureCode())
                .eq(StrUtil.isNotBlank(cfg.getFacilityCode()), RcBasicsConfig::getFacilityCode, cfg.getFacilityCode())
                .eq(StrUtil.isNotBlank(cfg.getRoomCode()), RcBasicsConfig::getRoomCode, cfg.getRoomCode())
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
    }

    @Override
    public void syn(RcBasicsConfig task) {
        // 先删除后添加
        removeById(task.getId());
        task.setSynTime(LocalDateTime.now());
        // 如果配置类型为空就只做删除操作
        if (task.getType() == null) {
            return;
        }
        // 添加数据
        save(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSyn(List<RcBasicsConfig> task) {
        // 全量覆盖 数据 先删除后覆盖
        this.saveOrUpdateBatch(task);
    }

    @Override
    public List<RcBasicsConfig> querySyn(String projectId, List<BasicsConfigTypeEnum> type) {
        return this.getBaseMapper().querySyn(projectId, type);
    }

    /**
     * 包装查询条件
     *
     * @param param 查询参数
     * @return 查询条件
     * @since v5.1.0
     */
    private LambdaQueryChainWrapper<RcBasicsConfig> lambdaQuery(@NonNull RcBasicsConfigQueryDaoParam param) {
        return lambdaQuery()
                .eq(StringUtils.isNotBlank(param.getProjectId()), RcBasicsConfig::getProjectId, param.getProjectId())
                .eq(Objects.nonNull(param.getType()), RcBasicsConfig::getType, param.getType())
                .eq(StringUtils.isNotBlank(param.getChildType()), RcBasicsConfig::getChildType, param.getChildType())
                .in(CollectionUtils.isNotEmpty(param.getChildTypes()), RcBasicsConfig::getChildType, param.getChildTypes())
                .eq(Objects.nonNull(param.getSt()), RcBasicsConfig::getSt, param.getSt())
                .eq(StringUtils.isNotBlank(param.getArchitectureCode()), RcBasicsConfig::getArchitectureCode, param.getArchitectureCode())
                .in(CollectionUtils.isNotEmpty(param.getArchitectureCodes()), RcBasicsConfig::getArchitectureCode, param.getArchitectureCodes())
                .eq(StringUtils.isNotBlank(param.getFloorCode()), RcBasicsConfig::getFloorCode, param.getFloorCode())
                .in(CollectionUtils.isNotEmpty(param.getFloorCodes()), RcBasicsConfig::getFloorCode, param.getFloorCodes())
                .eq(StringUtils.isNotBlank(param.getRoomCode()), RcBasicsConfig::getRoomCode, param.getRoomCode())
                .in(CollectionUtils.isNotEmpty(param.getRoomCodes()), RcBasicsConfig::getRoomCode, param.getRoomCodes())
                .eq(StringUtils.isNotBlank(param.getFacilityCode()), RcBasicsConfig::getFacilityCode, param.getFacilityCode())
                .in(CollectionUtils.isNotEmpty(param.getFacilityCodes()), RcBasicsConfig::getFacilityCode, param.getFacilityCodes())
                .eq(StringUtils.isNotBlank(param.getSystemId()), RcBasicsConfig::getSystemId, param.getSystemId())
                .in(CollectionUtils.isNotEmpty(param.getSystemIds()), RcBasicsConfig::getSystemId, param.getSystemIds())
                .eq(StringUtils.isNotBlank(param.getSpEl()), RcBasicsConfig::getSpEl, param.getSpEl())
                .in(CollectionUtils.isNotEmpty(param.getSpEls()), RcBasicsConfig::getSpEl, param.getSpEls())
                .gt(Objects.nonNull(param.getStartTime()), RcBasicsConfig::getEndTime, param.getStartTime())
                .lt(Objects.nonNull(param.getEndTime()), RcBasicsConfig::getStartTime, param.getEndTime())
                .orderByAsc(RcBasicsConfig::getStartTime);
    }
}




