package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * 需求管理-照明需求-类型分组树
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@Schema(title = "需求管理-照明需求-类型分组树")
public class DemandLightingTypeGroupTree {

    @Schema(description = "照明回路功能类型分组树")
    private List<DemandLightingFuncTypeSettingVO> funcTypeSettings;

    @Schema(description = "空间功能类型分组树")
    private List<DemandLightingSpaceFuncTypeVO> spaceFuncTypeSettings;

    @Schema(description = "分组ID-回路ID集合")
    private Map<Long, Set<String>> groupIdAndCircuitIds;

    /**
     * 填充回路功能类型配置
     *
     * @since 7.250601.0
     */
    public void fillFuncTypeWithGroup() {
        if (MapUtils.isEmpty(this.groupIdAndCircuitIds) || CollectionUtils.isEmpty(this.funcTypeSettings)) {
            return;
        }
        for (DemandLightingFuncTypeSettingVO funcTypeSetting : this.funcTypeSettings) {
            fillFunTypeWithGroup(funcTypeSetting);
        }
    }

    /**
     * 填充空间功能类型配置
     *
     * @since 7.250601.0
     */
    public void fillRoomFuncTypeWithGroup() {
        if (MapUtils.isEmpty(this.groupIdAndCircuitIds) || CollectionUtils.isEmpty(this.spaceFuncTypeSettings)) {
            return;
        }
        for (DemandLightingSpaceFuncTypeVO spaceFuncTypeSetting : this.spaceFuncTypeSettings) {
            List<DemandLightingFuncTypeSettingVO> circuitFuncTypes = spaceFuncTypeSetting.getCircuitFuncTypes();
            if (CollectionUtils.isEmpty(circuitFuncTypes)) {
                continue;
            }
            List<DemandLightingCircuitSettingVO> finalCircuitSettings = new ArrayList<>();
            for (DemandLightingFuncTypeSettingVO circuitFuncType : circuitFuncTypes) {
                fillFunTypeWithGroup(circuitFuncType);
                List<DemandLightingCircuitSettingVO> settings = circuitFuncType.getCircuitSettings();
                if (CollectionUtils.isNotEmpty(settings)) {
                    finalCircuitSettings.addAll(settings);
                }
            }
            spaceFuncTypeSetting.setCircuitSettings(finalCircuitSettings);
        }
    }

    /**
     * 填充回路功能类型配置
     *
     * @param funcTypeSetting 回路功能类型配置
     * @since 7.250601.0
     */
    private void fillFunTypeWithGroup(@NonNull DemandLightingFuncTypeSettingVO funcTypeSetting) {
        if (MapUtils.isEmpty(this.groupIdAndCircuitIds)) {
            return;
        }
        List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = funcTypeSetting.getCircuitGroupSettings();
        List<DemandLightingCircuitSettingVO> circuitSettings = funcTypeSetting.getCircuitSettings();
        if (CollectionUtils.isEmpty(circuitGroupSettings)) {
            return;
        }
        // 最终照明回路ID
        Map<String, DemandLightingCircuitSettingModeEnum> circuitIdAndSettingMode = new HashMap<>();
        // 预处理前置分组相关数据
        if (CollectionUtils.isNotEmpty(circuitGroupSettings)) {
            for (DemandLightingCircuitGroupSettingDTO circuitGroupSetting : circuitGroupSettings) {
                Long groupId = circuitGroupSetting.getGroupId();
                DemandLightingTypeSettingModeEnum settingMode = circuitGroupSetting.getSettingMode();
                Set<String> circuitIdsInGroup = this.groupIdAndCircuitIds.get(groupId);
                if (CollectionUtils.isEmpty(circuitIdsInGroup)) {
                    return;
                }
                for (String circuitIdInGroup : circuitIdsInGroup) {
                    circuitIdAndSettingMode.put(circuitIdInGroup, settingMode.toCircuitSettingMode());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(circuitSettings)) {
            // 重写回路配置模式
            for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
                String circuitId = circuitSetting.getCircuitId();
                DemandLightingCircuitSettingModeEnum settingMode = circuitIdAndSettingMode.get(circuitId);
                circuitSetting.setSettingMode(settingMode);
            }
        }
    }
}
