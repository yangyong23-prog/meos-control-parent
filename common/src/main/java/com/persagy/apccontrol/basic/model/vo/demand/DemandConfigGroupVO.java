package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 自定义配置实例接口
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/2/28 10:56
 * @since 7.250202.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自定义配置实例接口VO")
public class DemandConfigGroupVO implements Serializable {

    private static final long serialVersionUID = 7535508415546821837L;

    @Schema(description = "分组id")
    private Long groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "对象类型")
    private String objectType;

    @Schema(description = "关联实例")
    private List<DemandInstanceValueObjectVO> relInstances;

    @Schema(description = "创建时间")
    protected LocalDateTime createTime;

    @Data
    // 辅助类：复合键
    public static class GroupKey {
        private final DemandInstanceTypeEnum objectType;
        private final Long groupId;
        private final String groupName;

        public GroupKey(DemandInstanceTypeEnum objectType, Long groupId, String groupName) {
            this.objectType = objectType;
            this.groupId = groupId;
            this.groupName = groupName;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    // 辅助类：复合键
    public static class DemandInstanceValueObjectVO extends DemandInstanceValueObject {
        private static final long serialVersionUID = 4958856473913968353L;
        private final String buildingName;
    }

}
