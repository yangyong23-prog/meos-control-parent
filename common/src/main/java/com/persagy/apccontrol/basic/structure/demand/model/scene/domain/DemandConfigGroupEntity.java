package com.persagy.apccontrol.basic.structure.demand.model.scene.domain;

import com.persagy.apccontrol.basic.model.base.domain.Entity;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.identifier.DemandConfigGroupIdentifier;
import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * 需求配置组实体
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
public class DemandConfigGroupEntity implements Entity<DemandConfigGroupIdentifier> {

    /**
     * 标识符
     */
    private DemandConfigGroupIdentifier identifier;

    /**
     * 配置组名称
     */
    private String groupName;

    /**
     * 创建配置分组实体
     *
     * @param configGroup 配置分组
     * @return 配置分组实体
     * @since 7.250202.0
     */
    public static DemandConfigGroupEntity of(@NonNull DemandConfigGroup configGroup) {
        DemandConfigGroupIdentifier identifier = new DemandConfigGroupIdentifier();
        identifier.setGroupId(configGroup.getId());
        identifier.setRelTemplateId(configGroup.getRelTemplateId());

        DemandConfigGroupEntity result = new DemandConfigGroupEntity();
        result.setIdentifier(identifier);
        result.setGroupName(configGroup.getGroupName());
        return result;
    }
}
