package com.persagy.apccontrol.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.apccontrol.model.RobotEquipment;
import com.persagy.apccontrol.model.RobotFloor;
import com.persagy.apccontrol.service.RobotService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/7/24 16:35
 * @since v5.240402.00
 */
@Service
public class RobotServiceImpl implements RobotService {

    @Resource
    InstanceService instanceService;
    @Resource
    IotDataCacheService iotDataCacheService;

    @Override
    public List<RobotFloor> listOfFloor(String projectId) {
        List<RobotFloor> list = new ArrayList<>();
        // 1、获取楼层
        List<DtpRwdFloorDTO> dtpRwdFloorList = instanceService.listFloorsByInstance(projectId, DemandInstanceTypeEnum.PROJECT, projectId);
        if (CollUtil.isEmpty(dtpRwdFloorList)) {
            return list;
        }
        Map<String, RobotFloor> floorMap = new HashMap<>();
        dtpRwdFloorList.forEach(dtpRwdFloorDTO -> {
            RobotFloor robotFloor = new RobotFloor();
            robotFloor.setFloorId(dtpRwdFloorDTO.getId());
            robotFloor.setFloorName(dtpRwdFloorDTO.getLocalName());
            robotFloor.setFloorSequenceId(dtpRwdFloorDTO.getFloorSequenceId());
            list.add(robotFloor);
            floorMap.put(robotFloor.getFloorName(), robotFloor);
        });
        // 排序
        list.sort((o1, o2) -> {
            if (o1.getFloorSequenceId() == null || o2.getFloorSequenceId() == null) {
                return 0;
            }
            return o2.getFloorSequenceId() - o1.getFloorSequenceId();
        });

        // 2、获取设备 设备编码，PDPDRB-机器人，PDPDBX-搬运箱
        List<JSONObject> equipmentList = instanceService.listEquipmentByClassCode(projectId, DtpRwdClassCodeEnum.PDPDRB.getValue(), DtpRwdClassCodeEnum.PDPDBX.getValue());
        if (CollUtil.isEmpty(equipmentList)) {
            return list;
        }
        Map<String, RobotEquipment> equipmentHashMap = new HashMap<>();
        equipmentList.forEach(jsonObject -> {
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String localName = jsonObject.getString("localName");
            String classCode = jsonObject.getString("classCode");
            RobotEquipment robotEquipment = new RobotEquipment();
            robotEquipment.setId(id);
            robotEquipment.setName(StrUtil.isNotBlank(localName) ? localName : name);
            robotEquipment.setClassCode(classCode);
            equipmentHashMap.put(id, robotEquipment);
        });

        // 3、获取设备信息点 taskStatus、bluetoothSignCalLocation、sysFloor
        List<FeedbackInfoMsgMqttVO> feedbackInfoMsgMqttVOS = iotDataCacheService.queryIotRealDataList(equipmentHashMap.keySet(), Lists.newArrayList("taskStatus", "bluetoothSignCalLocation", "sysFloor", "cartId", "cartLoadStatus"));
        if (CollUtil.isEmpty(feedbackInfoMsgMqttVOS)) {
            return list;
        }

        // 4、组装数据
        feedbackInfoMsgMqttVOS.forEach(feedbackInfoMsgMqttVO -> {
            RobotEquipment robotEquipment = equipmentHashMap.get(feedbackInfoMsgMqttVO.getObjectId());
            if ("taskStatus".equals(feedbackInfoMsgMqttVO.getInfoCode())) {
                robotEquipment.setTaskStatus(feedbackInfoMsgMqttVO.getDataValue());
            }
            if ("bluetoothSignCalLocation".equals(feedbackInfoMsgMqttVO.getInfoCode())) {
                robotEquipment.setBluetoothSignCalLocation(feedbackInfoMsgMqttVO.getDataValue());
            }
            if ("cartId".equals(feedbackInfoMsgMqttVO.getInfoCode())) {
                robotEquipment.setCartId(feedbackInfoMsgMqttVO.getDataValue());
            }
            if ("cartLoadStatus".equals(feedbackInfoMsgMqttVO.getInfoCode())) {
                robotEquipment.setCartLoadStatus(feedbackInfoMsgMqttVO.getDataValue());
            }
            if ("sysFloor".equals(feedbackInfoMsgMqttVO.getInfoCode())) {
                RobotFloor floor = floorMap.getOrDefault(feedbackInfoMsgMqttVO.getDataValue(), new RobotFloor());
                List<RobotEquipment> equipmentList1 = floor.getEquipmentList();
                if (null == equipmentList1) {
                    equipmentList1 = new ArrayList<>();
                }
                equipmentList1.add(robotEquipment);
                floor.setEquipmentList(equipmentList1);
            }
        });

        return list;
    }
}
