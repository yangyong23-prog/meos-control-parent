package com.persagy.apccontrol.basic.service.demand.blacklist;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.BasicsDemandBlacklistRuleDTO;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistIntoSpacesRuleDTO;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistRemoveSpacesRuleDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandBlacklistRule;
import com.persagy.apccontrol.basic.model.param.demand.DemandBlacklistConfigAddParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandInstanceConfigAddOrEditParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistRuleVO;
import com.persagy.apccontrol.basic.resource.demand.DemandBlacklistRuleService;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.apccontrol.basic.service.demand.blacklist.handler.DemandBlacklistStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandBlacklistTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.persagy.apccontrol.basic.config.RedisKey.DEMAND_BLACKLIST_ADD_LOCK;

/**
 * 黑名单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistServiceImpl implements DemandBlacklistService {

    private final ObjectMapper objectMapper;
    private final DemandInstanceGateway demandInstanceGateway;
    private final DemandBlacklistRuleService demandBlacklistRuleService;
    private final RedissonDistributedLockerService redissonDistributedLockerService;
    ;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRules(DemandBlacklistConfigAddParam param) {
        String lockKey = DEMAND_BLACKLIST_ADD_LOCK + param.getDemandTemplateType();
        boolean lock = redissonDistributedLockerService.tryLock(lockKey, TimeUnit.SECONDS, 1, 60);
        if (!lock) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0506.getCode(), LocaleMessageUtil.getMessage("key_e323ab485e5d41beb58b55d39d643c9d"));
        }
        try {
            //1.计算黑名单空间
            Set<String> spaceId = listSpaceIdsByRule(param);

            //2.保存实例化的黑名单空间
            DemandInstanceConfigAddOrEditParam addParam = new DemandInstanceConfigAddOrEditParam();
            addParam.setTemplateType(param.getDemandTemplateType());
            addParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
            DemandInstanceConfigValueObject cfg = new DemandInstanceConfigValueObject();
            addParam.setConfig(cfg);
            //2.1 查询当前黑名单实例集合，计算差集以精确识别移出/新增的实例
            DemandInstanceConfigQueryParam currentBlacklistParam = new DemandInstanceConfigQueryParam();
            currentBlacklistParam.setTemplateType(param.getDemandTemplateType());
            currentBlacklistParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
            currentBlacklistParam.setFilterConfig(new DemandInstanceConfigValueObject().setInBlacklisted(true));
            Set<String> currentBlacklistSpaceIds = demandInstanceGateway.listInstanceIds(currentBlacklistParam);
            // out = 旧黑名单 - 新黑名单（真正移出的，需置false并触发out事件）
            Set<String> outSpaceIds = new HashSet<>(currentBlacklistSpaceIds);
            outSpaceIds.removeAll(spaceId);
            // in = 新黑名单 - 旧黑名单（真正新增的，需置true并触发in事件）
            Set<String> inSpaceIds = new HashSet<>(spaceId);
            inSpaceIds.removeAll(currentBlacklistSpaceIds);
            //2.2 移出黑名单：置false触发out事件，刷新写回result
            if (CollectionUtils.isNotEmpty(outSpaceIds)) {
                cfg.setInBlacklisted(false);
                addParam.setInstanceIds(outSpaceIds);
                demandInstanceGateway.addOrEditInstanceConfig(addParam);
            }
            //2.3 加入黑名单：置true触发in事件，删除result
            if (CollectionUtils.isNotEmpty(inSpaceIds)) {
                cfg.setInBlacklisted(true);
                addParam.setInstanceIds(inSpaceIds);
                demandInstanceGateway.addOrEditInstanceConfig(addParam);
            }
            //3.保存规则
            demandBlacklistRuleService.addRules(param);
        } finally {
            redissonDistributedLockerService.unlock(lockKey);
        }
    }

    @Override
    public Set<String> listSpaceIdsByRule(DemandBlacklistConfigAddParam param) {
        Set<String> blacklistSpaceId = Collections.synchronizedSet(new HashSet<>());

        if (MapUtils.isEmpty(param.getConfigData())) {
            return blacklistSpaceId;
        }
        // 并发处理每个规则类型
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Map.Entry<DemandBlacklistTypeEnum, JsonNode> entry : param.getConfigData().entrySet()) {
            DemandBlacklistTypeEnum type = entry.getKey();
            JsonNode configData = entry.getValue();
            // 执行处理器逻辑
            DemandBlacklistStrategy handler = type.demandBlacklistHandler();
            if (handler == null || configData == null) {
                continue;
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Set<String> spacesId = handler.execute(param.getDemandTemplateType(), type, configData);
                if (CollectionUtils.isNotEmpty(spacesId)) {
                    blacklistSpaceId.addAll(spacesId);
                }
            }, BusinessThreadPoolExecutor.ttlExecutor());
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 处理 "纳入" 规则
        addToBlacklistSpaceId(param.getConfigData().get(DemandBlacklistTypeEnum.INTO_SPACES), blacklistSpaceId);

        // 处理 "移除" 规则
        removeFromBlacklistSpaceId(param.getConfigData().get(DemandBlacklistTypeEnum.REMOVE_SPACES), blacklistSpaceId);
        return blacklistSpaceId;
    }

    @Override
    public DemandBlacklistRuleVO query(DemandBlacklistConfigAddParam param) {
        DemandBlacklistRule demandBlacklistRule = demandBlacklistRuleService.queryByDemandTemplateType(param.getDemandTemplateType());
        if (Objects.isNull(demandBlacklistRule)) {
            return null;
        }
        Map<String, JsonNode> configData = demandBlacklistRule.getConfigData();
        DemandBlacklistRuleVO result = demandBlacklistRule.toVO();
        if (MapUtils.isEmpty(configData)) {
            return result;
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Map<String, BasicsDemandBlacklistRuleDTO> configDataMap = new HashMap<>();
        for (String type : configData.keySet()) {
            DemandBlacklistTypeEnum demandBlacklistType = DemandBlacklistTypeEnum.analyze(type);
            JsonNode jsonNode = objectMapper.valueToTree(configData.get(type));
            if (jsonNode == null) {
                continue;
            }
            BasicsDemandBlacklistRuleDTO value = objectMapper.convertValue(jsonNode, demandBlacklistType.getClazz());
            if (Objects.nonNull(value)) {
                configDataMap.put(type, value);
            }
        }
        if (MapUtils.isNotEmpty(configDataMap)) {
            configDataMap.values().forEach(rule -> futures.add(CompletableFuture.runAsync(rule::fill, BusinessThreadPoolExecutor.ttlExecutor())));
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            result.putConfigData(configDataMap);
        }
        return result;
    }

    /**
     * 处理 "纳入" 规则
     */
    private void addToBlacklistSpaceId(JsonNode jsonNode, Set<String> blacklistSpaceId) {
        if (jsonNode == null) {
            return;
        }
        DemandBlacklistIntoSpacesRuleDTO into = objectMapper.convertValue(jsonNode, DemandBlacklistIntoSpacesRuleDTO.class);
        ;
        if (CollectionUtils.isNotEmpty(into.rule())) {
            blacklistSpaceId.addAll(into.rule());
        }
    }

    /**
     * 处理 "移除" 规则
     */
    private void removeFromBlacklistSpaceId(JsonNode jsonNode, Set<String> blacklistSpaceId) {
        if (jsonNode == null) {
            return;
        }
        DemandBlacklistRemoveSpacesRuleDTO remove = objectMapper.convertValue(jsonNode, DemandBlacklistRemoveSpacesRuleDTO.class);
        if (CollectionUtils.isNotEmpty(remove.rule())) {
            remove.rule().forEach(blacklistSpaceId::remove);
        }
    }


}
