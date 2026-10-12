package com.persagy.apccontrol.common.logtrace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.logtrace.assembler.LogNodeExtendAssembler;
import com.persagy.apccontrol.common.logtrace.dto.LogQuerySearchDto;
import com.persagy.apccontrol.common.logtrace.entity.LogQueryTemplateParamEntity;
import com.persagy.apccontrol.common.logtrace.persistence.LogNodeExtendRepository;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateRepository;
import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;
import com.persagy.apccontrol.common.logtrace.vo.LogNodeExtendVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogNodeExtendService {

    @Autowired
    private LogNodeExtendRepository logNodeExtendRepository;

    @Autowired
    private LogQueryTemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LogNodeExtendAssembler logNodeExtendAssembler;


    public List<LogNodeExtendVo> getQuerySearchByTemplateId(LogQuerySearchDto searchDto) throws JsonProcessingException {
        LogQueryTemplatePo templatePo = templateRepository.getById(searchDto.getTemplateId());
        if (templatePo == null) {
            return new ArrayList<>();
        }
        if (!StringUtils.hasLength(templatePo.getQueryParam())) {
            log.warn("template:{} param is empty", searchDto.getTemplateId());
            return new ArrayList<>();
        }
        List<LogQueryTemplateParamEntity> params = objectMapper.readValue(templatePo.getQueryParam(), objectMapper.getTypeFactory().constructCollectionType(List.class, LogQueryTemplateParamEntity.class));
        Set<String> nodeCodes = params.stream().map(p->p.getNodecode()).collect(Collectors.toSet());
        Set<String> extendCodes = params.stream().map(p->p.getKey()).collect(Collectors.toSet());
        LambdaQueryWrapper<LogNodeExtendPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(!CollectionUtils.isEmpty(nodeCodes), LogNodeExtendPo::getNodeCode, nodeCodes)
                .in(!CollectionUtils.isEmpty(extendCodes), LogNodeExtendPo::getExtendCode, extendCodes)
        .between(LogNodeExtendPo::getBusinessTime,searchDto.getBeginTime(),searchDto.getEndTime());
        List<LogNodeExtendPo> logNodeExtendPos = logNodeExtendRepository.getBaseMapper().selectList(queryWrapper);
        return logNodeExtendAssembler.poToVoList(logNodeExtendPos);
    }


}
