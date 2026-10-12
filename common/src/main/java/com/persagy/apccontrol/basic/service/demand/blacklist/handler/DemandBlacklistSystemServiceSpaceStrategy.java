package com.persagy.apccontrol.basic.service.demand.blacklist.handler;

import com.persagy.apccontrol.basic.model.dto.demand.blacklist.DemandBlacklistSystemServiceSpaceRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统服务的空间策略
 * 查询的是排除的空间 总空间去掉 目标空间
 */
@Validated
@Component(DemandBlacklistSystemServiceSpaceStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistSystemServiceSpaceStrategy extends AbstractDemandBlacklistStrategy<DemandBlacklistSystemServiceSpaceRuleDTO> {
    
    public static final String BEAN_NAME = "demandBlacklistSystemServiceSpaceStrategy";
    private final DtpRwdClientService dtpRwdClientService;
    
    @Override
    public Set<String> executeRule(@NonNull DemandTemplateTypeEnum demandTemplateType,
                                   @NonNull DemandBlacklistSystemServiceSpaceRuleDTO configData) {
        //查询全量空间
        Set<String> result = getAllSpaceId();
        if (CollectionUtils.isEmpty(configData.getSystemIds())) {
            return result;
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setClassCodeEnum(DtpRwdClassCodeEnum.SPACE)
                .setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE)
                        .setObjFrom(configData.getSystemIds()));
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(queryParam);
        
        if (CollectionUtils.isEmpty(spaces)) {
            return result;
        }
        Set<String> ids = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
        result.removeAll(ids);
        return result;
    }
    
    
}
