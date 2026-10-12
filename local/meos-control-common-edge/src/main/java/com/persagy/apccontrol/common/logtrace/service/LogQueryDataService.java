package com.persagy.apccontrol.common.logtrace.service;

import com.persagy.apccontrol.common.logtrace.dto.LogQueryParamDataDto;
import com.persagy.apccontrol.common.logtrace.dto.LogQueryTemplateAndParamDto;
import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import com.persagy.apccontrol.common.logtrace.enums.TemplateHeaderType;
import com.persagy.apccontrol.common.logtrace.persistence.LogNodeExtendRepository;
import com.persagy.apccontrol.common.logtrace.persistence.LogNodeRepository;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateHeaderExtractKeyRepository;
import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderExtractKeyPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;
import com.persagy.apccontrol.common.logtrace.vo.LogQueryTemplateRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Service
@Slf4j
public class LogQueryDataService {

    @Autowired
    private LogNodeService logNodeService;

    @Autowired
    private LogQueryTemplateService logQueryTemplateService;


    @Autowired
    private LogNodeRepository nodeRepository;

    @Autowired
    private LogQueryTemplateHeaderService templateHeaderService;

    @Autowired
    private LogQueryTemplateHeaderExtractKeyRepository extractKeyRepository;


    @Autowired
    private LogNodeExtendRepository logNodeExtendRepository;

    @Autowired
    private LogApplicationService logApplicationService;

    @Autowired
    private LogQueryTemplateHeaderService logQueryTemplateHeaderService;


    public LogQueryTemplateRecordVo getQueryTemplateDataById(LogQueryTemplateAndParamDto dto) {
        LogQueryTemplateRecordVo result = new LogQueryTemplateRecordVo();
        LogQueryTemplatePo templatePo = logQueryTemplateService.getQueryTemplateById(dto.getTemplateId());
        if (Objects.isNull(templatePo)) {
            log.warn("template:{} is not exist when getQueryTemplateDataById", dto.getTemplateId());
            return result;
        }
        String beginNodeCode = templatePo.getQueryIndexNodeCode();
        if (!StringUtils.hasLength(beginNodeCode)) {
            log.warn("template:{} beginNodeCode is not exist when getQueryTemplateDataById", dto.getTemplateId());
            throw new RuntimeException("template has not beginNodeCode");
        }
        Long templateId = dto.getTemplateId();

        List<LogQueryTemplateHeaderPo> templateHeaderPos = templateHeaderService.getAllQueryTemplateHeaderByTemplateId(templateId);
        if (CollectionUtils.isEmpty(templateHeaderPos)) {
            log.warn("template:{} templateHeader is not exist when getQueryTemplateDataById", templateId);
            return result;
        }
        List<LogQueryTemplateHeaderExtractKeyPo> headerExtractKeyPos = extractKeyRepository.getLogHeaderExtractByTemplateId(templateId);
        if (CollectionUtils.isEmpty(headerExtractKeyPos)) {
            log.warn("template:{} headerExtractKeyPos is not exist when getQueryTemplateDataById", dto.getTemplateId());
            return result;
        }
        List<LogNodePo> logNodePoPage = nodeRepository.getByTime(dto.getBeginTime(), dto.getEndTime(), beginNodeCode, null);
        Set<String> filterNodeCodes = headerExtractKeyPos.stream().map(p -> p.getExtractObject()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(logNodePoPage)) {
            return result;
        }
        Set<Long> beginNodeIds = logNodePoPage.stream().map(p -> p.getId()).collect(Collectors.toSet());
        //起始位置对应下的子树集合
        Map<Long, Set<Long>> beginToChildrenMap = new HashMap<>();
        //key为nodeId,value  是以nodeCode为key value为nodeId列表的map
        Map<Long, Map<String, List<Long>>> beginMap = new HashMap<>();
        beginNodeIds.forEach(id -> {
            Map<String, List<Long>> nodeCodeToId = logApplicationService.getAllNodeByTopNodeId(id, filterNodeCodes);
            beginMap.put(id, nodeCodeToId);
            Set<Long> treeNode = new HashSet<>();
            nodeCodeToId.forEach((key, value) -> {
                treeNode.addAll(value);
            });
            beginToChildrenMap.put(id, treeNode);
        });
        Set<Long> allNodeExtend = new HashSet<>();
        beginToChildrenMap.forEach((key, value) -> {
            allNodeExtend.addAll(value);
        });
        //查出模板中配置类型中的所有对象的数据
        List<LogNodeExtendPo> nodeExtendPos = logNodeExtendRepository.listByNodeIds(allNodeExtend);
        if (CollectionUtils.isEmpty(nodeExtendPos)) {
            return result;
        }
        //按照nodeId分组
        Map<Long, List<LogNodeExtendPo>> groupedByNodeId = nodeExtendPos.stream()
                .collect(Collectors.groupingBy(LogNodeExtendPo::getNodeId));
        Map<Long, List<LogNodeExtendPo>> allNodeExtendMap = new HashMap();
        List<LogQueryParamDataDto> searchParams = dto.getParams();
        //每一个树查询对象上的业务信息

        beginToChildrenMap.forEach((key, value) -> {
            List<LogNodeExtendPo> singleTree = new ArrayList<>();
            value.forEach(nodeId -> {
                if (!CollectionUtils.isEmpty(groupedByNodeId.get(nodeId))) {
                    singleTree.addAll(groupedByNodeId.get(nodeId));
                }
            });
            if(CollectionUtils.isEmpty(searchParams)){
                allNodeExtendMap.put(key, singleTree);
            }else {
                //根据搜索条件过滤数据
                if (search(singleTree, searchParams)) {
                    allNodeExtendMap.put(key, singleTree);
                }
            }
        });
        //查询是否模板中有动态表头
        List<LogQueryTemplateHeaderPo> dynamicHeaders = templateHeaderPos.stream().filter(p -> p.getHeaderType().equals(TemplateHeaderType.DYNAMIC.getCode())).collect(Collectors.toList());

        //key 配置的动态表头的id,value对应的动态表头的数据，表头id为真实数据所对应的节点id
        Map<Long, List<LogGroupTemplateEntity>> dynamicHeaderEntitiesMap = new HashMap();
        if (!CollectionUtils.isEmpty(dynamicHeaders)) {
            dynamicHeaderEntitiesMap = getDynamicHeaderEntityMap(dynamicHeaders, allNodeExtendMap);
        }
        List<LogGroupTemplateEntity> topHeaderEntity = logQueryTemplateHeaderService.getQueryTemplateHeaderTree(templateId);
        replaceDynamicHeader(topHeaderEntity, dynamicHeaderEntitiesMap);
        result.setHeaders(topHeaderEntity);
        List<Map<Long, String>> sheetData = new ArrayList<>();
        Set<Long> extractHeaderIds = headerExtractKeyPos.stream().map(p -> p.getHeaderId()).collect(Collectors.toSet());
        List<LogQueryTemplateHeaderPo> parentDynamicHeaders = logQueryTemplateHeaderService.getParentDynamicHeaderByIds(extractHeaderIds);
        Map<Long, LogQueryTemplateHeaderPo> extractToParentMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(parentDynamicHeaders)) {
            extractToParentMap = parentDynamicHeaders.stream().collect(Collectors.toMap(LogQueryTemplateHeaderPo::getChildrenId, p -> p));
        }
        //组装 allNodeExtendMap是一个 以起始nodeId 为key value 为查对象的数据集合
        Map<Long, LogQueryTemplateHeaderPo> finalExtractToParentMap = extractToParentMap;
        Map<Long, LogQueryTemplateHeaderPo> templateHeaderMap = templateHeaderPos.stream().collect(Collectors.toMap(LogQueryTemplateHeaderPo::getId, p -> p));
        Map<Long, List<LogGroupTemplateEntity>> finalDynamicHeaderEntitiesMap = dynamicHeaderEntitiesMap;
        //动态表头值map
        Map<Long, Map<String, LogGroupTemplateEntity>> dynamicHeaderEntityValues = new HashMap<>();
        dynamicHeaderEntitiesMap.forEach((key, value) -> {
            Map<String, LogGroupTemplateEntity> dynamicHeaderValue = value.stream().collect(Collectors.toMap(LogGroupTemplateEntity::getName, p -> p));
            dynamicHeaderEntityValues.put(key, dynamicHeaderValue);
        });
        allNodeExtendMap.forEach((key, value) -> {
            List<String> multiNodeCode = new ArrayList<>();
            if (!CollectionUtils.isEmpty(value)) {
                //判断查找对象中是否存在多个 对象存在多个实例，只允许一个对象多实例
                beginMap.get(key).forEach((key1, value1) -> {
                    if (!CollectionUtils.isEmpty(value1)) {
                        if (value1.size() > 1) {
                            multiNodeCode.add(key1);
                        }
                    }
                });
                if (multiNodeCode.size() > 1) {
                    log.warn("template:{} multi object has more 1 instance is not exist when single row,data:{} ", templateId, multiNodeCode);
                    throw new RuntimeException("multi object has more 1 instance is not exist when single row data:" + multiNodeCode);
                }
                //存在对象有多个
                if (multiNodeCode.size() == 1) {
                    List<LogNodeExtendPo> multiObjectExtendPos = value.stream().filter(p -> p.getNodeCode().equals(multiNodeCode.get(0))).collect(Collectors.toList());
                    List<LogNodeExtendPo> singleObjectExtendPos = value.stream().filter(p -> !p.getNodeCode().equals(multiNodeCode.get(0))).collect(Collectors.toList());
                    Map<Long,List<LogNodeExtendPo>> multiObjectGroup = multiObjectExtendPos.stream().collect(Collectors.groupingBy(LogNodeExtendPo::getNodeId));
                    multiObjectGroup.forEach((k,v)->{
                        v.addAll(singleObjectExtendPos);
                        Map<String, List<LogNodeExtendPo>> groupedByCompositeKey1 = v.stream()
                                .collect(Collectors.groupingBy(
                                        po -> po.getNodeCode() + ":" + po.getExtendCode() // 复合键
                                ));
                        Map<Long, String> singleSheet =  singleSheetAssembler(headerExtractKeyPos,templateHeaderMap,groupedByCompositeKey1,dynamicHeaderEntityValues,finalDynamicHeaderEntitiesMap,finalExtractToParentMap);
                        sheetData.add(singleSheet);
                    });
                }else{
                    Map<String, List<LogNodeExtendPo>> groupedByCompositeKey1 = value.stream()
                            .collect(Collectors.groupingBy(
                                    po -> po.getNodeCode() + ":" + po.getExtendCode() // 复合键
                            ));
                    Map<Long, String> singleSheet =  singleSheetAssembler(headerExtractKeyPos,templateHeaderMap,groupedByCompositeKey1,dynamicHeaderEntityValues,finalDynamicHeaderEntitiesMap,finalExtractToParentMap);
                    sheetData.add(singleSheet);
                }
            }
        });
        int pageSize = dto.getSize(); // 每页的大小
        int  pageNumber = dto.getCurrent(); // 当前的页码

        int start = (pageNumber - 1) * pageSize;
        int end = Math.min(start + pageSize, sheetData.size());
        List<Map<Long, String>> currentPage  = sheetData.subList(start, end);
        result.setSheetData(currentPage);
        return result;

    }


    Map<Long, String>  singleSheetAssembler(List<LogQueryTemplateHeaderExtractKeyPo>headerExtractKeyPos,Map<Long, LogQueryTemplateHeaderPo> templateHeaderMap,
                                            Map<String, List<LogNodeExtendPo>> groupedByCompositeKey,Map<Long, Map<String, LogGroupTemplateEntity>> dynamicHeaderEntityValues ,
                                            Map<Long, List<LogGroupTemplateEntity>> finalDynamicHeaderEntitiesMap ,Map<Long, LogQueryTemplateHeaderPo> finalExtractToParentMap){

            Map<Long, String> singleRow = new HashMap<>();
            headerExtractKeyPos.forEach(extract -> {
                LogQueryTemplateHeaderPo extractHeader = templateHeaderMap.get(extract.getHeaderId());
                //最下级的表头是动态表头
                if (extractHeader.getHeaderType().equals(TemplateHeaderType.DYNAMIC.getCode())) {
                    //对应动态表头列数据值
                    List<LogNodeExtendPo> dynamicHeaderValue = groupedByCompositeKey.get(extractHeader.getHeaderCode());
                    if (!CollectionUtils.isEmpty(dynamicHeaderValue)) {
                        LogGroupTemplateEntity dynamicHeader = dynamicHeaderEntityValues.get(extract.getHeaderId()).get(dynamicHeaderValue.get(0).getExtendValue());
                        //取动态表头
                        if (dynamicHeader != null) {
                            singleRow.put(dynamicHeader.getId(), groupedByCompositeKey.get(extract.getExtractObject() + ":" + extract.getExtractKey()).get(0).getExtendValue());
                        }
                    }
                } else {
                    finalDynamicHeaderEntitiesMap.get(extract.getHeaderId());
                    LogQueryTemplateHeaderPo parentHeader = finalExtractToParentMap.get(extract.getHeaderId());
                    //判断父级是否为动态表头
                    if (!Objects.isNull(parentHeader)) {
                        List<LogNodeExtendPo> dynamicHeaderValue = groupedByCompositeKey.get(parentHeader.getHeaderCode());
                        LogGroupTemplateEntity dynamicHeader = dynamicHeaderEntityValues.get(parentHeader.getId()).get(dynamicHeaderValue.get(0).getExtendValue());
                        if (dynamicHeader != null) {
                            singleRow.put(extract.getHeaderId(), groupedByCompositeKey.get(extract.getExtractObject() + ":" + extract.getExtractKey()).get(0).getExtendValue());
                        }
                    } else {
                        List<LogNodeExtendPo> dd = groupedByCompositeKey.get(extract.getExtractObject() + ":" + extract.getExtractKey());
                        singleRow.put(extract.getHeaderId(), groupedByCompositeKey.get(extract.getExtractObject() + ":" + extract.getExtractKey()).get(0).getExtendValue());
                    }
                }
            });

        return singleRow;
    }


    void replaceDynamicHeader(List<LogGroupTemplateEntity> topHeaderEntity, Map<Long, List<LogGroupTemplateEntity>> dynamicHeaderEntitiesMap) {
        Map<Long, List<LogGroupTemplateEntity>> sortedMap = dynamicHeaderEntitiesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // 保持原来的键
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(LogGroupTemplateEntity::getName)) // 按name排序
                                .collect(Collectors.toList()), // 收集为新的List
                        (oldValue, newValue) -> oldValue, // 解决键冲突，保留旧值
                        LinkedHashMap::new // 保持插入顺序
                ));
        //动态表头替换
        topHeaderEntity.forEach(p -> {
            logQueryTemplateHeaderService.traverseTree(p, sortedMap);
        });
    }


    Map<Long, List<LogGroupTemplateEntity>> getDynamicHeaderEntityMap(List<LogQueryTemplateHeaderPo> dynamicHeaders, Map<Long, List<LogNodeExtendPo>> allNodeExtendMap) {
        Map<Long, List<LogGroupTemplateEntity>> dynamicHeaderEntitiesMap = new HashMap();
        if (!CollectionUtils.isEmpty(dynamicHeaders)) {
            allNodeExtendMap.forEach((key, value) -> {
                dynamicHeaders.forEach(dynamicHeader -> {
                    //去重
                    Set<String> noRepeatValue = new HashSet<>();
                    List<LogGroupTemplateEntity> dynamicHeaderEntities = new ArrayList<>();
                    value.forEach(p -> {
                        String headerCode = p.getNodeCode() + ":" + p.getExtendCode();
                        if (headerCode.equals(dynamicHeader.getHeaderCode()) && !noRepeatValue.contains(p.getExtendValue())) {
                            LogGroupTemplateEntity entity = new LogGroupTemplateEntity();
                            entity.setName(p.getExtendValue());
                            entity.setType(TemplateHeaderType.DYNAMIC.getCode());
                            entity.setId(p.getNodeId());
                            dynamicHeaderEntities.add(entity);
                            noRepeatValue.add(p.getExtendValue());
                        }
                    });
                    dynamicHeaderEntitiesMap.put(dynamicHeader.getId(), dynamicHeaderEntities);
                });
            });
        }
        return dynamicHeaderEntitiesMap;
    }


    Boolean search(List<LogNodeExtendPo> pos, List<LogQueryParamDataDto> searchParams) {
        if (CollectionUtils.isEmpty(pos)) {
            return FALSE;
        }
        //按照nodeCode分组
        Map<String, List<LogNodeExtendPo>> groupedByNodeId = pos.stream()
                .collect(Collectors.groupingBy(LogNodeExtendPo::getNodeCode));
        if (CollectionUtils.isEmpty(searchParams)) {
            return TRUE;
        } else {
            int[] search = new int[searchParams.size()];
            for (int i = 0; i < search.length; i++) {
                LogQueryParamDataDto param = searchParams.get(i);
                if (!CollectionUtils.isEmpty(groupedByNodeId.get(param.getNodecode()))) {
                    int p = i;
                    groupedByNodeId.get(param.getNodecode()).forEach(extend -> {
                        if (extend.getExtendCode().equals(param.getKey()) && extend.getExtendValue().equals(param.getValue())) {
                            search[p] = 1;
                        }
                    });
                }
            }
            int sum = Arrays.stream(search).sum();
            if (sum != search.length) {
                return FALSE;
            } else {
                return TRUE;
            }
        }
    }
}
