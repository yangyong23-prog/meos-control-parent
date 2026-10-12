package com.persagy.apccontrol.common.logtrace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.logtrace.assembler.LogNodeAssembler;
import com.persagy.apccontrol.common.logtrace.entity.LogNodeEntity;
import com.persagy.apccontrol.common.logtrace.mapper.LogNodeMapper;
import com.persagy.apccontrol.common.logtrace.persistence.LogNodeRepository;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogNodeService {

    @Autowired
    private LogNodeRepository nodeRepository;

    @Autowired
    private LogNodeMapper logNodeMapper;

    @Autowired
    private LogNodeAssembler logNodeAssembler;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 返回存储后的节点id列表,返回业务对象id和node映射关系
     *
     * @param nodeEntityList
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Long> batchSave(List<LogNodeEntity> nodeEntityList) {
        if (CollectionUtils.isEmpty(nodeEntityList)) {
            return new HashMap<String, Long>();
        }
        Map<String, Long> nodeIdToBusinessIdMap = new HashMap<>();
        List<LogNodePo> logNodePos = nodeEntityList.stream().map(p -> {
            LogNodePo nodePo = logNodeAssembler.nodeEntityToNodePo(p);
            try {
              //  nodePo.setExtend(objectMapper.readTree(p.getExtend()));
            } catch (Exception e) {
                throw new RuntimeException();
            }
            Long nodeId = IdWorker.getId();
            nodeIdToBusinessIdMap.put(p.getBusinessId(), nodeId);
            nodePo.setId(nodeId);
            return nodePo;
        }).collect(Collectors.toList());
        nodeRepository.saveBatch(logNodePos);
        return nodeIdToBusinessIdMap;
    }

    public List<LogNodePo> getLogNodeByNodeIds(Collection<Long> ids) {
        return nodeRepository.listByIds(ids);
    }

    public List<LogNodePo> getLogNodeByNodeIdsAndCodes(Collection<Long> ids,Collection<String>nodeCodes) {
        LambdaQueryWrapper<LogNodePo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(!CollectionUtils.isEmpty(ids),LogNodePo::getId,ids)
                .in(!CollectionUtils.isEmpty(nodeCodes),LogNodePo::getCode,nodeCodes);
        return nodeRepository.getBaseMapper().selectList(queryWrapper);
    }

    /**
     * 根据业务对象id查询对应节点信息，并以map返回映射关系
     *
     * @param businessId
     * @return
     */
    public Map<String, Long> queryByBusinessId(Collection<String> businessId, String businessType) {
        List<LogNodePo> nodeList = nodeRepository.findNodeByBusinessIds(businessId, businessType);
        if (CollectionUtils.isEmpty(nodeList)) {
            return new HashMap<>();
        }
        return nodeList.stream().collect(Collectors.toMap(LogNodePo::getBusinessId, p -> p.getId()));
    }

    public List<LogNodePo> queryAllTopNode() {
        return logNodeMapper.queryAllTopNode();
    }


}
