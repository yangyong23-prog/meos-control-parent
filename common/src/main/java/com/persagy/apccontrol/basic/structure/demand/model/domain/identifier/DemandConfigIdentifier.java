package com.persagy.apccontrol.basic.structure.demand.model.domain.identifier;

import com.persagy.apccontrol.basic.model.base.domain.Identifier;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.Map;

/**
 * 需求配置标识符
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigIdentifier implements Identifier {

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 继承配置类型
     */
    private InheritConfigTypeEnum inheritConfigType;

    /**
     * 需求模版标签
     */
    private String templateTag;

    /**
     * 需求模版ID
     */
    private Long templateId;

    /**
     * 配置日期, 默认为当前日期
     */
    private LocalDate configDate;

    /**
     * 需求场景
     */
    @Nullable
    private DemandSceneEntity scene;

    /**
     * 配置组
     */
    @Nullable
    private DemandConfigGroupEntity configGroup;

    /**
     * 空间功能类型分组ID -> 配置分组
     */
    @Nullable
    private Map<Long, DemandConfigGroupEntity> roomFuncTypeRelGroupIdAndConfigGroup;

    /**
     * 需求实例
     */
    private DemandInstanceValueObject instance;

    /**
     * 创建需求配置标识符
     *
     * @param templateType                         模版类型
     * @param templateTag                          模版标签
     * @param templateId                           模板ID
     * @param configDate                           配置日期
     * @param scene                                需求场景
     * @param configGroup                          配置分组
     * @param roomFuncTypeRelGroupIdAndConfigGroup 空间功能类型分组ID -> 配置分组
     * @param instance                             需求实例
     * @return 需求配置标识符
     * @since 7.250202.0
     */
    public static DemandConfigIdentifier of(
            DemandTemplateTypeEnum templateType,
            String templateTag, Long templateId, LocalDate configDate,
            DemandSceneEntity scene, DemandConfigGroupEntity configGroup,
            Map<Long, DemandConfigGroupEntity> roomFuncTypeRelGroupIdAndConfigGroup,
            DemandInstanceValueObject instance) {
        DemandConfigIdentifier identifier = new DemandConfigIdentifier();
        identifier.setTemplateType(templateType);
        identifier.setInheritConfigType(templateType.demandConfigAnalyzeStrategy().inheritConfigType());
        identifier.setTemplateTag(templateTag);
        identifier.setTemplateId(templateId);
        identifier.setConfigDate(configDate);
        identifier.setScene(scene);
        identifier.setConfigGroup(configGroup);
        identifier.setRoomFuncTypeRelGroupIdAndConfigGroup(roomFuncTypeRelGroupIdAndConfigGroup);
        identifier.setInstance(instance);
        return identifier;
    }
}
