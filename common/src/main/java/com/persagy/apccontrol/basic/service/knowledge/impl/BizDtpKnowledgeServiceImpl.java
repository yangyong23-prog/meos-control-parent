package com.persagy.apccontrol.basic.service.knowledge.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeInfoCodeBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeInfoCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.ObjectsExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.service.knowledge.BizDtpKnowledgeService;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据字典业务服务实现
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>listInfoCodes</b> - 查询信息点编码定义</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Service
@Validated
public class BizDtpKnowledgeServiceImpl implements BizDtpKnowledgeService {

    @Autowired
    private DtpKnowledgeClientService dtpKnowledgeClientService;
    @Autowired
    private DtpKnowledgeConverter dtpKnowledgeConverter;
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Override
    public InfoCodeDefineModel getInfoCode(@NonNull DtpKnowledgeInfoCodeQueryParam param) {
        return dtpKnowledgeConverter.toInfoCodeDefineModel(
                dtpKnowledgeClientService.getInfoCode(param.getClassCode(), param.getInfoCode()));
    }

    @Override
    public List<ClassCodeDefineModel> listClassCodes(@NonNull DtpKnowledgeClassCodeQueryParam param) {
        return dtpKnowledgeClientService.listClassCodes(param.getQueryCache(), param.getIsThrows(), param.getClassCodes())
                .stream().map(item -> dtpKnowledgeConverter.toClassCodeDefineModel(item)).collect(Collectors.toList());
    }

    @Override
    public List<InfoCodeDefineModel> listInfoCodes(@NonNull DtpKnowledgeInfoCodeBatchQueryParam param) {
        Set<String> classCodes = param.getClassCodes();
        if (CollectionUtils.isEmpty(classCodes)) {
            return Collections.emptyList();
        }
        return dtpKnowledgeClientService.listInfoCodes(param.getQueryCache(), param.getIsThrows(), classCodes)
                .stream().map(item -> dtpKnowledgeConverter.toInfoCodeDefineModel(item)).collect(Collectors.toList());
    }

    @Override
    public List<ObjectsExistRule> queryObjectsExistRule(ObjectsExistRuleQueryParam param) {
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                param.getCriteria(), param.getTerminal());
        //通过对象 ID 进行过滤
        if (CollectionUtil.isNotEmpty(param.getObjIds()) && CollectionUtil.isNotEmpty(objectsExistRules)) {
            List<ObjectsExistRule> r = new ArrayList<>();
            objectsExistRules.forEach(i -> {
                if (param.getObjIds().contains(i.getObjectId())) {
                    r.add(i);
                }
            });
            return r;
        }
        return objectsExistRules;
    }
}
