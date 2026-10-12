package com.persagy.apccontrol.basic.service.systemgraph.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDuplicate;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphGenerateParameter;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphDuplicateService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphGenerateParameterService;
import com.persagy.apccontrol.basic.mapper.SystemGraphGenerateParameterMapper;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author persagy
 * @description 针对表【system_graph_generate_parameter】的数据库操作Service实现
 * @createDate 2024-06-25 11:57:27
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphGenerateParameterServiceImpl extends ServiceImpl<SystemGraphGenerateParameterMapper, SystemGraphGenerateParameter>
        implements SystemGraphGenerateParameterService {

    private final SystemGraphDuplicateService systemGraphDuplicateService;

    @Override
    public SystemGraphGenerateParameter getSystemGraphGenerateParameterByDuplicateId(Long duplicateId) {
        LambdaQueryWrapper<SystemGraphGenerateParameter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemGraphGenerateParameter::getSystemGraphDuplicateId, duplicateId);
        SystemGraphGenerateParameter one = this.getOne(queryWrapper);
        return one;
    }

    @Override
    public RequestSystemGraphDTO getParameterDataByDuplicateId(Long duplicateId) {
        SystemGraphGenerateParameter parameter = getSystemGraphGenerateParameterByDuplicateId(duplicateId);
        if (parameter == null) {
            return null;
        }
        return JSON.parseObject(parameter.getData(), RequestSystemGraphDTO.class);
    }

    @Override
    public Boolean saveAndUpdateSystemGraphGenerateParameter(Long duplicateId, RequestSystemGraphDTO dto) {
        if (duplicateId == null) {
            return false;
        }
        SystemGraphDuplicate duplicate = systemGraphDuplicateService.getById(duplicateId);
        if (duplicate == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_6216d1c28ac946ceb750984a5fd25184"), LocaleMessageUtil.getMessage("key_6216d1c28ac946ceb750984a5fd25184"));
        }
        SystemGraphGenerateParameter parameter = getSystemGraphGenerateParameterByDuplicateId(duplicateId);
        if (parameter == null) {
            parameter = new SystemGraphGenerateParameter();
            parameter.setSystemGraphDuplicateId(duplicateId);
//            parameter.setTemplateId(duplicate.getTemplateId());
            parameter.setData(JSON.toJSONString(dto));
            parameter.setClassCode(duplicate.getClassCode());
            parameter.setProjectId(ContextUtil.getProjectId());
            return save(parameter);
        }
        String data = parameter.getData();
        if (StringUtils.isBlank(data)) {
            parameter.setData(JSON.toJSONString(dto));
            return updateById(parameter);
        }
        RequestSystemGraphDTO oldDto = JSON.parseObject(data, RequestSystemGraphDTO.class);
        if (StringUtils.isNotEmpty(dto.getGenerateId())) {
            oldDto.setGenerateId(dto.getGenerateId());
        }
        oldDto.setLegendScaleRatio(1);
        oldDto.setFontSize(dto.getFontSize());
        //对象排序
        JSONObject parent2ObjectId2SortValue = dto.getParent2ObjectId2SortValue();
        JSONObject oldParent2ObjectId2SortValue = oldDto.getParent2ObjectId2SortValue();
        if (MapUtils.isNotEmpty(parent2ObjectId2SortValue)) {
            if (MapUtils.isEmpty(oldParent2ObjectId2SortValue)) {
                oldDto.setParent2ObjectId2SortValue(parent2ObjectId2SortValue);
            } else {
                oldParent2ObjectId2SortValue.putAll(parent2ObjectId2SortValue);
                oldDto.setParent2ObjectId2SortValue(oldParent2ObjectId2SortValue);
            }

        }
        //设备/设备组（机房）的文本框尺寸和留白
        JSONObject equipmentGroupDetails = dto.getEquipmentGroupDetails();
        JSONObject oldEquipmentGroupDetails = oldDto.getEquipmentGroupDetails();
        if (MapUtils.isNotEmpty(equipmentGroupDetails)) {
            if (MapUtils.isEmpty(oldEquipmentGroupDetails)) {
                oldDto.setEquipmentGroupDetails(equipmentGroupDetails);
            } else {
                oldEquipmentGroupDetails.putAll(equipmentGroupDetails);
                oldDto.setEquipmentGroupDetails(oldEquipmentGroupDetails);
            }

        }
        parameter.setData(JSON.toJSONString(oldDto));
        return updateById(parameter);
    }
}




