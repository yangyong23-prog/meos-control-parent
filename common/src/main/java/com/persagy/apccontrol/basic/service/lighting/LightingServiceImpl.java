//package com.persagy.apccontrol.basic.service.lighting;
//
//import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
//import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
//import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
//import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
//import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitTagVO;
//import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
//import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
//import com.persagy.apccontrol.basic.model.param.lighting.LightingCircuitInfoCodeQueryParam;
//import com.persagy.apccontrol.basic.model.vo.KeyValueChildrenVO;
//import com.persagy.apccontrol.basic.model.vo.KeyValueVO;
//import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
//import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitInfoCodeVO;
//import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
//import com.persagy.apccontrol.basic.service.BizComponent;
//import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
//import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
//import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
//import com.persagy.validate.utility.executor.MapExecutor;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.lang.NonNull;
//import org.springframework.lang.Nullable;
//import org.springframework.stereotype.Service;
//import org.springframework.validation.annotation.Validated;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * 照明服务实现
// *
// * @author JiYinchuan
// * @since 5.240402.0
// */
//@Service
//@Validated
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
//public class LightingServiceImpl extends AbstractLightingService {
//
//
//    @NotNull
//    @Override
//    public Map<String, String> getOnlineMap(String groupCode, String projectId, List<String> objectIdList) {
//        return Collections.emptyMap();
//    }
//
//    @Override
//    public Map<String, LightingCircuitTagVO> mapCircuitIdAndTag(String projectId, Collection<String> circuitIds, Map<String, DemandLightingCircuitSettingModeEnum> circuitIdAndSettingMode) {
//        return Collections.emptyMap();
//    }
//
//    @Override
//    public String logTag() {
//        return "照明服务";
//    }
//
//}
