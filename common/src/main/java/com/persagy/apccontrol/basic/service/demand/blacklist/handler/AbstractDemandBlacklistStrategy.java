package com.persagy.apccontrol.basic.service.demand.blacklist.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.BasicsDemandBlacklistRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandBlacklistTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractDemandBlacklistStrategy<T extends BasicsDemandBlacklistRuleDTO>
        implements DemandBlacklistStrategy {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Override
    public Set<String> execute(@NonNull DemandTemplateTypeEnum demandTemplateType, DemandBlacklistTypeEnum type, @NonNull JsonNode jsonNode) {
        return executeRule(demandTemplateType, getConfigData(jsonNode, type));
    }


    public T getConfigData(@NonNull JsonNode jsonNode, DemandBlacklistTypeEnum type) {
        return (T) objectMapper.convertValue(jsonNode, type.getClazz());
    }

    /**
     * 执行规则,由具体策略类实现
     */
    public abstract Set<String> executeRule(@NonNull DemandTemplateTypeEnum demandTemplateType, @NonNull T configData);
    
    
    /**
     * 获取全量空间 ID
     */
    protected Set<String> getAllSpaceId() {
        Set<String> result = new HashSet<>();
        //查询全量空间
        List<DtpRwdSpaceDTO> allSpaces =
                dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id));
        if (CollectionUtils.isEmpty(allSpaces)) {
            return result;
        }
        //添加空间 ID 到 result
        allSpaces.stream().map(DtpRwdSpaceDTO::getId).forEach(result::add);
        return result;
    }

}
