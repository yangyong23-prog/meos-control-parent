package com.persagy.apccontrol.basic.model.param.demand.lighting;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 照明需求管理-场景配置保存参数
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-场景配置保存参数")
public class DemandLightingTemplateSceneConfigSaveParam extends DemandLightingInstanceQueryParam {

    @Schema(description = "全局配置")
    @Valid
    private List<DemandLightingFuncTypeSettingDTO> globalSettings;

    @Schema(description = "空间类型配置")
    @Valid
    private List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings;

    /**
     * 校验
     */
    public void check() {
        // 校验全局设置
        funcTypeSettingCheck(globalSettings);
        // 校验空间设置
        if (CollectionUtils.isNotEmpty(spaceSettings)) {
            List<DemandLightingFuncTypeSettingDTO> settings = spaceSettings.stream()
                    .map(DemandLightingSpaceFuncTypeSettingDTO::getFuncTypeSettings)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            funcTypeSettingCheck(settings);
        }
    }

    /**
     * 功能类型校验
     */
    private void funcTypeSettingCheck(List<DemandLightingFuncTypeSettingDTO> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return;
        }
        List<DemandLightingCircuitSettingDTO> allCircuitSettings = new ArrayList<>();
        List<DemandLightingCircuitGroupSettingDTO> allCircuitGroupSettings = new ArrayList<>();
        for (DemandLightingFuncTypeSettingDTO setting : settings) {
            List<DemandLightingCircuitSettingDTO> circuitSettings = setting.getCircuitSettings();
            List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = setting.getCircuitGroupSettings();
            if (CollectionUtils.isNotEmpty(circuitSettings)) {
                allCircuitSettings.addAll(circuitSettings);
            }
            if (CollectionUtils.isNotEmpty(circuitGroupSettings)) {
                allCircuitGroupSettings.addAll(circuitGroupSettings);
            }
        }
        checkCircuitSetting(allCircuitSettings);
        checkCircuitGroupSetting(allCircuitGroupSettings);
    }

    /**
     * 检查照明回路编组配置
     *
     * @param settings 照明回路编组配置
     * @since 7.250601.0
     */
    private void checkCircuitGroupSetting(@Nullable List<@NonNull DemandLightingCircuitGroupSettingDTO> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return;
        }
        Set<Long> groupIds = settings.stream()
                .map(DemandLightingCircuitGroupSettingDTO::getGroupId)
                .collect(Collectors.toSet());
        DemandInstanceGroupQueryDaoParam queryDaoParam = new DemandInstanceGroupQueryDaoParam();
        queryDaoParam.setIds(groupIds);
        queryDaoParam.setQueryDetail(true);
        List<DemandInstanceGroupPO> groups = SpringUtil.getBean(DemandInstanceGroupResource.class).list(queryDaoParam);
        if (CollectionUtils.isEmpty(groups)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_4c95862972704281b7aa83d3f4843b99"));
        }
        if (groups.size() != groupIds.size()) {
            throw new ValidationException(String.format(LocaleMessageUtil.getMessage("key_7698cd5aa95845b5b6073bc5c455f8c1"), CollectionUtils.subtract(groupIds,
                    groups.stream().map(DemandInstanceGroupPO::getId).collect(Collectors.toSet()))));
        }
        Set<String> existCircuitIds = new HashSet<>();
        for (DemandInstanceGroupPO group : groups) {
            String groupName = group.getName();
            List<DemandInstanceGroupDetail> details = group.getDetails();
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
            for (DemandInstanceGroupDetail detail : details) {
                String instanceId = detail.getInstanceId();
                if (existCircuitIds.contains(instanceId)) {
                    throw new ValidationException(String.format(LocaleMessageUtil.getMessage("key_32075b03f69f4e329d73287b2034221e"), groupName, instanceId));
                }
                existCircuitIds.add(instanceId);
            }
        }
    }

    /**
     * 回路校验 校验不能重复
     */
    private void checkCircuitSetting(List<DemandLightingCircuitSettingDTO> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return;
        }
        Set<String> existingIds = new HashSet<>();

        for (DemandLightingCircuitSettingDTO dto : settings) {
            String currentId = dto.getCircuitId();
            if (existingIds.contains(currentId)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_8d6b4361c08a42ac8d3489b1f9f8a5db"));
            }
            existingIds.add(currentId);
        }
    }
}
