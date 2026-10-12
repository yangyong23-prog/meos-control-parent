package com.persagy.apccontrol.basic.model.param.demand;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 需求对象查询参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-对象树查询参数")
public class DemandObjectQueryParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "过滤实例条件")
    private List<FilterInstance> filterInstanceList;

    @Schema(description = "查询字段, 为空时查询所有字段")
    private List<String> withColumns;

    public void addFilterInstance(FilterInstance filterInstance) {
        if (CollectionUtils.isEmpty(this.filterInstanceList)) {
            this.filterInstanceList = new ArrayList<>();
        }
        this.filterInstanceList.add(filterInstance);
    }

    /**
     * 过滤对象
     */
    @Data
    @Accessors(chain = true)
    public static class FilterInstance {
        @Schema(description = "过滤实例类型")
        private DemandInstanceTypeEnum filterInstanceType;

        @Schema(description = "传递实力 ID 必须传递实例类型 过滤实例ID, 为空时不进行过滤")
        private Set<String> filterInstanceIds;

        public static FilterInstance build(DemandInstanceTypeEnum filterInstanceType) {
            FilterInstance fi = new FilterInstance();
            fi.setFilterInstanceType(filterInstanceType);
            return fi;
        }

        public static FilterInstance build(DemandInstanceTypeEnum filterInstanceType, Set<String> filterInstanceIds) {
            FilterInstance fi = build(filterInstanceType);
            fi.setFilterInstanceIds(filterInstanceIds);
            return fi;
        }

        public void check() {
            ParameterCheck.isTrue(filterInstanceType == null, LocaleMessageUtil.getMessage("key_72927ce1b3824668bb1c3d4d9f6aecbb"));
        }

    }

}
