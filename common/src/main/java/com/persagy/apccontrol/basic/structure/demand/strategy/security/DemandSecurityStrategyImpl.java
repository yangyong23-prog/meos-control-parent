package com.persagy.apccontrol.basic.structure.demand.strategy.security;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdTwinControlObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.AbstractDemandStrategyProcessor;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 安防需求策略实现
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Component(DemandSecurityStrategyImpl.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandSecurityStrategyImpl extends AbstractDemandStrategyProcessor implements DemandSecurityStrategy, PriorityOrdered {

    public static final String BEAN_NAME = "demandSecurityStrategyImpl";

    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer commonServerCloud;

    @Override
    public List<DtpRwdSpaceDTO> listDemandSpace(@NonNull DemandSpacesQueryParam param) {
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        List<DtpRwdSpaceDTO> listSp = new ArrayList<>();
        if (DemandTemplateTypeEnum.ARMING.equals(templateType)) {
            // 入侵报警防区控制器设备ID列表获取
            List<String> spiacuIds = getDeviceIds(DtpRwdClassCodeEnum.SPIACU);
            // 监控摄像头设备ID列表获取
            List<String> spvscmIds = getDeviceIds(DtpRwdClassCodeEnum.SPVSCM);
            listSp.addAll(getSpacesByEquipment(DtpRwdClassCodeEnum.SPVSCM, spvscmIds, param));
            listSp.addAll(getSpacesByEquipment(DtpRwdClassCodeEnum.SPIACU, spiacuIds, param));
        } else if (DemandTemplateTypeEnum.PASSING.equals(templateType)) {
            List<String> objIds = getPassingEquipment();
            //都是设备所在空间只查询异常设备关联空间即可
            listSp.addAll(getSpacesByEquipment(DtpRwdClassCodeEnum.CFCSDR, objIds, param));
        }
        return listSp;
    }

    private List<String> getDeviceIds(DtpRwdClassCodeEnum classCode) {
        DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
        if (DtpRwdClassCodeEnum.SPVSCM.equals(classCode)) {
            List<String> infoCodes = new ArrayList<>();
            infoCodes.add("1");
            dtpRwdObjectQueryParam.addInfoCode(BaseConvert.INFO_CODE_HAS_ARMING_FUNC, infoCodes);
        }
        dtpRwdObjectQueryParam.setClassCode(classCode.getValue());
        dtpRwdObjectQueryParam.setWithColumns(Sets.newHashSet(DtpRwdObjectDTO.Fields.id));
        List<DtpRwdObjectDTO> objList = dtpRwdClientService.listObject(dtpRwdObjectQueryParam);
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(objList)) {
            ids = objList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        }
        return ids;
    }


    /**
     * 根据设备类型获取对应的空间列表
     *
     * @param equipmentCode 物理世界设备类编码枚举
     * @param filterParam   过滤参数
     * @return 空间信息
     */
    private List<DtpRwdSpaceDTO> getSpacesByEquipment(DtpRwdClassCodeEnum equipmentCode, List<String> objIds,
                                                      DemandSpacesQueryParam filterParam) {
        if (CollectionUtils.isNotEmpty(objIds)) {
            // 按照800个一组进行分组
            List<List<String>> subLists = Lists.partition(objIds, 800);
            List<CompletableFuture<List<DtpRwdSpaceDTO>>> futures = new ArrayList<>();

            for (List<String> subList : subLists) {
                DtpRwdObjectQueryParam param = getSpaceQueryParam(equipmentCode, subList);
                // 创建查询任务
                CompletableFuture<List<DtpRwdSpaceDTO>> future = CompletableFuture.supplyAsync(() ->
                                filterParam.queryObject(param)
                        , BusinessThreadPoolExecutor.ttlExecutor());
                futures.add(future);
            }
            // 等待所有任务完成并合并结果
            List<List<DtpRwdSpaceDTO>> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            List<DtpRwdSpaceDTO> allResults = new ArrayList<>();
            for (List<DtpRwdSpaceDTO> subResult : results) {
                allResults.addAll(subResult);
            }
            //取出所有 ID
            List<String> ids = allResults.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toList());
            // 去重
            ids = new ArrayList<>(new LinkedHashSet<>(ids));

            if (CollectionUtils.isEmpty(ids)) {
                return new ArrayList<>();
            }

            //重新获取全量数据
            Collections.sort(ids);
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
            param.setId(ids);
            return filterParam.queryObject(param);
        }
        // 如果设备ID列表为空，则返回空的空间列表
        return new ArrayList<>();
    }

    /**
     * 获取空间查询参数
     *
     * @param equipmentCode 设备类编码枚举
     * @param subList       设备 ID
     * @return 查询空间参数
     */
    private DtpRwdObjectQueryParam getSpaceQueryParam(DtpRwdClassCodeEnum equipmentCode, List<String> subList) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCode(DtpRwdClassCodeEnum.SPACE.value());
        DtpRwdRelationTypeEnum relationType = DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE;
        if (DtpRwdClassCodeEnum.SPVSCM.equals(equipmentCode) ||
                DtpRwdClassCodeEnum.CFCSDR.equals(equipmentCode) ||
                DtpRwdClassCodeEnum.CFCSFR.equals(equipmentCode) ||
                DtpRwdClassCodeEnum.SPEGFG.equals(equipmentCode)
        ) {
            relationType = DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE;
        }
        // 设置查询参数中的关系对象，包括关系类型和设备ID列表
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(relationType).setObjFrom(subList));
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        return param;
    }

    /**
     * 获取安防的通行设备ID列表
     *
     * @return 设备信息
     */
    private List<String> getPassingEquipment() {

        List<ClassCode> code = new ArrayList<>();
        code.add(new ClassCode(DtpRwdClassCodeEnum.CFCSDR.getValue(), BaseConvert.INFO_CODE_ACCESS_DEMAND_SET));
        code.add(new ClassCode(DtpRwdClassCodeEnum.CFCSFR.getValue(), BaseConvert.INFO_CODE_ACCESS_DEMAND_SET));
        code.add(new ClassCode(DtpRwdClassCodeEnum.SPEGFG.getValue(), BaseConvert.INFO_CODE_ACCESS_DEMAND_SET));
        List<ObjectsExistRule> objectsExistRules = commonServerCloud.queryObjectsExistRule(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), code);
        if (CollectionUtil.isEmpty(objectsExistRules)) {
            return new ArrayList<>();
        }

        //查询可控设备
        List<String> classCode = new ArrayList<>();
        classCode.add(DtpRwdClassCodeEnum.CFCSDR.getValue());
        classCode.add(DtpRwdClassCodeEnum.CFCSFR.getValue());
        classCode.add(DtpRwdClassCodeEnum.SPEGFG.getValue());
        List<DtpRwdTwinControlObjectDTO> controlEquipments = dtpRwdClientService.listTwinControlObjects(
                false, ContextUtil.getProjectId(), classCode);

        // 取两个集合的并集数据并转换为ArrayList返回
        return Stream.concat(objectsExistRules.stream().map(ObjectsExistRule::getObjectId),
                        controlEquipments.stream().map(DtpRwdTwinControlObjectDTO::getObjectId))
                .collect(Collectors.toList());
    }

    @Override
    public DemandTypeEnum demandType() {
        return DemandTypeEnum.SECURITY;
    }

    @Override
    public String logTag() {
        return "需求管理-安防需求策略";
    }
}
