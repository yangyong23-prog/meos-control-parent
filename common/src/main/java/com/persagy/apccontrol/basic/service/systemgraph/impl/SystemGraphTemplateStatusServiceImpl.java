package com.persagy.apccontrol.basic.service.systemgraph.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.SystemGraphTemplateStatusMapper;
import com.persagy.apccontrol.basic.model.dto.systemgraph.UpdateTemplateNameDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphTemplateLegendSelectDto;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphTemplateStatus;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphTemplateStatusService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author persagy
 * @description 针对表【system_graph_template_status(系统图模版状态表)】的数据库操作Service实现
 * @createDate 2024-06-25 11:55:20
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphTemplateStatusServiceImpl extends ServiceImpl<SystemGraphTemplateStatusMapper, SystemGraphTemplateStatus>
        implements SystemGraphTemplateStatusService {

    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public boolean getTemplateFlag(String templateId, String objectId) {
        LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphTemplateStatus::getTemplateId, templateId);
        qw.eq(SystemGraphTemplateStatus::getObjectId, objectId);
        qw.eq(SystemGraphTemplateStatus::getProjectId, ContextUtil.getProjectId());
        SystemGraphTemplateStatus systemGraphTemplateStatus = getOne(qw);
        return systemGraphTemplateStatus == null || systemGraphTemplateStatus.getUpdatedStatus();
    }

    @Override
    public List<SystemGraphTemplateStatus> listTemplateStatusByTemplateId(@NotNull String templateId, String projectId) {
        LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphTemplateStatus::getTemplateId, templateId);
        qw.eq(StringUtils.isNotBlank(projectId), SystemGraphTemplateStatus::getProjectId, projectId);
        return list(qw);
    }

    @Override
    public SystemGraphTemplateStatus saveTemplateStatus(String templateId, String objectId, String name, String graphCodeType, Integer sequenceNumberCurrent) {
        SystemGraphTemplateStatus systemGraphTemplateStatus = new SystemGraphTemplateStatus();
        systemGraphTemplateStatus.setTemplateId(templateId);
        systemGraphTemplateStatus.setObjectId(objectId);
        systemGraphTemplateStatus.setProjectId(ContextUtil.getProjectId());
        systemGraphTemplateStatus.setName(name);
        systemGraphTemplateStatus.setGraphTypeCode(graphCodeType);
        systemGraphTemplateStatus.setSequenceNumberCurrent(sequenceNumberCurrent);
        systemGraphTemplateStatus.setUpdatedStatus(false);
        LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphTemplateStatus::getTemplateId, graphCodeType);
        qw.eq(SystemGraphTemplateStatus::getObjectId, objectId);
        qw.eq(SystemGraphTemplateStatus::getProjectId, ContextUtil.getProjectId());
        SystemGraphTemplateStatus one = this.getOne(qw);
        //如果模板不存在，则插入模板
        if (one == null) {
            save(systemGraphTemplateStatus);
        }
        return systemGraphTemplateStatus;
    }

    @Override
    public void updateTemplateStatus(String templateId, String objectId, boolean updatedStatus, String graphCodeType, Integer sequenceNumberCurrent) {
        SystemGraphTemplateStatus systemGraphTemplateStatus = new SystemGraphTemplateStatus();
        systemGraphTemplateStatus.setTemplateId(templateId);
        systemGraphTemplateStatus.setGraphTypeCode(graphCodeType);
        systemGraphTemplateStatus.setSequenceNumberCurrent(sequenceNumberCurrent);
        systemGraphTemplateStatus.setObjectId(objectId);
        systemGraphTemplateStatus.setProjectId(ContextUtil.getProjectId());
        systemGraphTemplateStatus.setUpdatedStatus(updatedStatus);
        LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphTemplateStatus::getGraphTypeCode, graphCodeType);
        qw.eq(SystemGraphTemplateStatus::getObjectId, objectId);
        qw.eq(SystemGraphTemplateStatus::getProjectId, ContextUtil.getProjectId());
        List<SystemGraphTemplateStatus> list = this.list(qw);
        //如果模板不存在，则插入模板
        if (CollectionUtils.isEmpty(list)) {
            save(systemGraphTemplateStatus);
            return;
        }
        systemGraphTemplateStatus.setId(list.get(0).getId());
        updateById(systemGraphTemplateStatus);
    }

    @Override
    public void updateTemplateStatusByObjectIdAndGraphCodeType(String graphCodeType, boolean updatedStatus, String objectId) {
        LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphTemplateStatus::getGraphTypeCode, graphCodeType);
        qw.eq(StringUtils.isNotBlank(objectId), SystemGraphTemplateStatus::getObjectId, objectId);
        SystemGraphTemplateStatus systemGraphTemplateStatus = new SystemGraphTemplateStatus();
        systemGraphTemplateStatus.setUpdatedStatus(updatedStatus);
        update(systemGraphTemplateStatus, qw);
    }

    @Override
    public Boolean updateTemplateName(UpdateTemplateNameDTO updateTemplateNameDTO) {
        String name = updateTemplateNameDTO.getName();
        if (StringUtils.isNotBlank(name)) {
            LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
            qw.eq(SystemGraphTemplateStatus::getGraphTypeCode, updateTemplateNameDTO.getGraphCodeType());
            qw.eq(SystemGraphTemplateStatus::getObjectId, updateTemplateNameDTO.getObjectId());
            qw.eq(SystemGraphTemplateStatus::getName, name).or().eq(SystemGraphTemplateStatus::getAliasName, name);
            SystemGraphTemplateStatus one = this.getOne(qw);
            LambdaQueryWrapper<SystemGraphTemplateStatus> listQw = new LambdaQueryWrapper<>();
            qw.ne(SystemGraphTemplateStatus::getObjectId, updateTemplateNameDTO.getObjectId());
            listQw.eq(SystemGraphTemplateStatus::getGraphTypeCode, updateTemplateNameDTO.getGraphCodeType());
            listQw.eq(SystemGraphTemplateStatus::getName, name).or().eq(SystemGraphTemplateStatus::getAliasName, name);
            List<SystemGraphTemplateStatus> list = this.list(listQw);

            //如果修改的名称和原来的名称一致，则把别名设置为空
            if (one != null && name.equals(one.getName())) {
                name = null;
            } else if (CollectionUtils.isNotEmpty(list)) {
                throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_8a051106f67541a0b47d6cd9020d0c68"), LocaleMessageUtil.getMessage("key_8a051106f67541a0b47d6cd9020d0c68"));
            }
            LambdaUpdateWrapper<SystemGraphTemplateStatus> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SystemGraphTemplateStatus::getGraphTypeCode, updateTemplateNameDTO.getGraphCodeType())
                    .eq(SystemGraphTemplateStatus::getObjectId, updateTemplateNameDTO.getObjectId())
                    .set(SystemGraphTemplateStatus::getAliasName, name);
            return update(updateWrapper);
        }
        //如果为空，则直接把别名设置为空
        LambdaUpdateWrapper<SystemGraphTemplateStatus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SystemGraphTemplateStatus::getGraphTypeCode, updateTemplateNameDTO.getGraphCodeType())
                .eq(SystemGraphTemplateStatus::getObjectId, updateTemplateNameDTO.getObjectId())
                .set(SystemGraphTemplateStatus::getAliasName, null);
        return update(updateWrapper);
    }

    /**
     * 获取模板名称，有别名返回别名，没有别名返回原名称
     *
     * @param graphCodeType 模板id
     * @param objectId   对象id
     * @return {@link String }
     */
    @Override
    public String getTemplateName(String graphCodeType, String objectId) {
        LambdaQueryWrapper<SystemGraphTemplateStatus> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphTemplateStatus::getGraphTypeCode, graphCodeType);
        qw.eq(SystemGraphTemplateStatus::getObjectId, objectId);
        List<SystemGraphTemplateStatus> list = list(qw);
        if (CollectionUtils.isEmpty(list)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_cf65a10e19f940aaa63c120ced9494a3"), LocaleMessageUtil.getMessage("key_cf65a10e19f940aaa63c120ced9494a3"));
        }
        SystemGraphTemplateStatus one = list.get(0);
        return StringUtils.isNotBlank(one.getAliasName()) ? one.getAliasName() : one.getName();
    }

    @Override
    public boolean init(List<GraphTemplateLegendSelectDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        Set<String> objectIds = list.stream().map(GraphTemplateLegendSelectDto::getReferenceObjectId)
                .collect(Collectors.toSet());
        Map<String, String> objectIdAndLocalNameMap = dtpRwdClientService.mapObjectIdAndLocalName(true, objectIds);
        List<SystemGraphTemplateStatus> saveList = list.stream().map(e -> {
            SystemGraphTemplateStatus systemGraphTemplateStatus = new SystemGraphTemplateStatus();
            systemGraphTemplateStatus.setTemplateId(e.getTemplateOriId());
            systemGraphTemplateStatus.setObjectId(e.getReferenceObjectId());
            systemGraphTemplateStatus.setProjectId(ContextUtil.getProjectId());
            systemGraphTemplateStatus.setName(objectIdAndLocalNameMap.get(e.getReferenceObjectId()));
            systemGraphTemplateStatus.setGraphTypeCode(e.getGraphTypeCode());
            systemGraphTemplateStatus.setSequenceNumberCurrent(e.getSequenceNumberCurrent());
            systemGraphTemplateStatus.setUpdatedStatus(false);
            return systemGraphTemplateStatus;
        }).collect(Collectors.toList());

        return saveBatch(saveList);
    }
}




