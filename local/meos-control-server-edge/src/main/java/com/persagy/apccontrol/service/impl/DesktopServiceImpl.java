package com.persagy.apccontrol.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.common.model.param.desktop.DesktopEquipmentDetailsQueryParam;
import com.persagy.apccontrol.common.model.vo.desktop.DesktopEquipmentDetailsVO;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.apccontrol.common.service.impl.RunningMonitorServiceImpl;
import com.persagy.apccontrol.common.structure.convert.DesktopConverter;
import com.persagy.apccontrol.service.DesktopService;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 桌面端服务实现
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Service
public class DesktopServiceImpl implements DesktopService {

    @Autowired
    private DesktopConverter desktopConverter;
    @Autowired
    private DtpRwdConvert dtpRwdConvert;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Autowired
    private IotDataCacheService iotDataCacheService;
    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;
    @Autowired
    private RunningMonitorServiceImpl runningMonitorService;

    @Override
    public List<DesktopEquipmentDetailsVO> listEquipmentDetails(@NonNull DesktopEquipmentDetailsQueryParam param) {
        String projectId = param.getProjectId();
        Set<String> equipmentIds = param.getEquipmentIds();
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Lists.newArrayList();
        }

        // 查询对象
        List<DtpRwdObjectDTO> rwdObjects = dtpRwdClientService.listObject(false, false,
                projectId, new DtpRwdObjectQueryParam().setId(equipmentIds));
        if (CollectionUtils.isEmpty(rwdObjects)) {
            return Lists.newArrayList();
        }
        Set<String> rwdObjectIds = rwdObjects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        // 查询对象所在空间
        List<DtpRwdObjectDTO> rwdSpaces = dtpRwdClientService.listObject(false, false,
                projectId, new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.SPACE)
                        .setRelationTo(new DtpRwdObjectRelationQueryParam(
                                DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(rwdObjectIds)));
        Map<String, DtpRwdObjectDTO> objectIdAndSpace = rwdSpaces.stream()
                .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                .collect(Collectors.toMap(item -> new ArrayList<>(item.getRelObjIds()).get(0), Function.identity(), (o1, o2) -> o1));

        List<InfoCodeEnum> infoCodeEnums = Lists.newArrayList(
                InfoCodeEnum.MANUAL_AUTO_STATUS,
                InfoCodeEnum.LOCAL_REMOTZE_STATUS,
                InfoCodeEnum.RUN_STATUS,
                InfoCodeEnum.ONLINE_STATUS,
                InfoCodeEnum.EQUIP_SWITCH_SET
        );

        List<DesktopEquipmentDetailsVO> result = new ArrayList<>();
        for (DtpRwdObjectDTO rwdObject : rwdObjects) {
            String objectId = rwdObject.getId();
            String objectClassCode = rwdObject.getClassCode();

            List<RcInfoCodeCfg> infoCodeConfigs = new ArrayList<>();
            for (InfoCodeEnum infoCodeEnum : infoCodeEnums) {
                String infoCode = infoCodeEnum.getCode();
                RcInfoCodeCfg infoCodeConfig = localRedisCacheHandler.getInfoCodeConfig(objectClassCode, infoCode);
                if (Objects.isNull(infoCodeConfig)) {
                    continue;
                }
                RcInfoCodeCfg rc = new RcInfoCodeCfg();
                rc.setInfoCode(infoCode);
                rc.setDataSource(infoCodeConfig.getDataSource());
                rc.setName(infoCodeConfig.getName());
                rc.setUnit(infoCodeConfig.getUnit());
                rc.setDataValue(iotDataCacheService.queryIotRealData(objectId, infoCode, infoCodeConfig));
                infoCodeConfigs.add(rc);
            }
            DtpRwdObjectVO relSpace = dtpRwdConvert.toObjectVo(objectIdAndSpace.get(objectId));

            JSONObject jsonObject = runningMonitorService.queryManualAutoStatusByObjectId(
                    ContextUtil.getGroupCode(), projectId, objectId);
            result.add(desktopConverter.toEquipmentDetailsVo(rwdObject, infoCodeConfigs,
                    StringUtils.equals("0", jsonObject.getString(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode()))
                            ? jsonObject.getString("expireTime") : null, relSpace));
        }
        return result;
    }
}
