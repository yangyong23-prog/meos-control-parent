package com.persagy.apccontrol.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.apccontrol.controller.dto.FloorDTO;
import com.persagy.apccontrol.controller.vo.TerminalEquipmentVO;
import com.persagy.apccontrol.service.TerminalEquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 终端设备服务实现类
 *
 * @author MAF
 * @date 2023/12/12
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TerminalEquipmentServiceImpl implements TerminalEquipmentService {

    private final BizDtpRwdService bizDtpRwdService;

    private final IotDataCacheService iotDataCacheService;

    @Override
    public List<TerminalEquipmentVO> getTerminalStatistics(String groupCode, String projectId, String objectId) {
        List<TerminalEquipmentVO> resultList = new ArrayList<>();
        FloorDTO floorDTO = new FloorDTO();
        floorDTO.setProjectId(projectId);

        // 获取所有的设备
        List<JSONObject> equipments = null;
        if (objectId.startsWith("Pj")) {
            // 获取所有的建筑
            equipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACATAH", "ACATFU", "ACATFC"), null, null, null, null);
        } else if (objectId.startsWith("Bd")) {
            equipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACATAH", "ACATFU", "ACATFC"), "MechInArch", "Eq2Bd", null, Lists.newArrayList(objectId));
        } else if (objectId.startsWith("Fl")) {
            equipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACATAH", "ACATFU", "ACATFC"), "MechInArch", "Eq2Fl", null, Lists.newArrayList(objectId));
        } else if (objectId.startsWith("Sp")) {
            equipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACATAH", "ACATFU", "ACATFC"), "MechInArch", "Eq2Sp", null, Lists.newArrayList(objectId));
        }

        if (CollectionUtil.isEmpty(equipments)) {
            return resultList;
        }

        // 设备分类
        Map<String, Set<String>> classCode_ids = new HashMap<>();
        for (JSONObject equipment : equipments) {
            String equipId = equipment.getString("id");
            String classCode = equipment.getString("classCode");
            String installType = equipment.getString("installType");
            if ("ACATAH".equals(classCode)) {
                if ("1".equals(installType)) {
                    classCode_ids.computeIfAbsent("ACATAHG", v -> Sets.newHashSet()).add(equipId);
                } else {
                    classCode_ids.computeIfAbsent("ACATAHZ", v -> Sets.newHashSet()).add(equipId);
                }
            } else {
                classCode_ids.computeIfAbsent(classCode, v -> Sets.newHashSet()).add(equipId);
            }
        }

        // 统计该建筑下各设备类对应的实时状态
        for (Map.Entry<String, Set<String>> entry : classCode_ids.entrySet()) {
            String classCode = entry.getKey();
            Set<String> objIdList = entry.getValue();

            List<String> aiObjIdList = new ArrayList<>();
            List<String> runObjIdList = new ArrayList<>();
            List<String> manualObjIdList = new ArrayList<>();
            TerminalEquipmentVO vo = new TerminalEquipmentVO();
            for (String objId : objIdList) {
                String status = iotDataCacheService.queryIotRealData(objId, InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(), null);
                if ("1".equals(status)) {
                    aiObjIdList.add(objId);
                } else {
                    manualObjIdList.add(objId);
                }
                String runStatus = iotDataCacheService.queryIotRealData(objId, InfoCodeEnum.RUN_STATUS.getCode(), null);
                if ("1".equals(runStatus)) {
                    runObjIdList.add(runStatus);
                }
            }

            vo.setTotalNum(objIdList.size());
            vo.setRunNum(runObjIdList.size());
            vo.setClassCode(classCode);
            vo.setManualNum(manualObjIdList.size());
            vo.setAiNum(aiObjIdList.size());
            //vo.setAiIdList(aiObjIdList);
            //vo.setManualIdList(manualObjIdList);

            //获取手动干预的设备数量
            //handleInterveneObj(vo, Lists.newArrayList(objIdList), floorDTO);
            vo.setInterveneNum(0);
            vo.setInterveneIdList(Lists.newArrayList());
            resultList.add(vo);
        }

        return resultList;
    }

}
