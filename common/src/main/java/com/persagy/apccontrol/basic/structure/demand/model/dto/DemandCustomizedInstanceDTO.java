package com.persagy.apccontrol.basic.structure.demand.model.dto;

import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandConfigGroupEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * 需求管理-自定义配置实例DTO
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理-自定义配置实例DTO")
public class DemandCustomizedInstanceDTO extends DemandInstanceValueObject {

    /**
     * 配置组
     */
    @Nullable
    private DemandConfigGroupEntity configGroup;

    /**
     * 创建时间
     */
    protected LocalDateTime createTime;

}
