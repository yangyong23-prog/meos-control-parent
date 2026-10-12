package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

/**
 * 需求管理-实例对象树查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-实例对象树查询参数")
public class DemandTreeInstanceQueryParam {

    @Schema(description = "需求模版类型")
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "是否查询空间, 默认不查询")
    private boolean querySpace = false;

    @Schema(description = "过滤服务系统ID, 为空时不过滤, querySpace为true时生效")
    private Set<String> serviceSystemIds;

    @Schema(description = "是否查询自定义标签, 默认不查询")
    private boolean queryCustomizedTag = false;

    @Schema(description = "是否过滤黑名单空间, 默认不过滤")
    private boolean filterInShieldedSpace = false;

    @Schema(description = "是否过滤黑名单空间, 默认过滤")
    private boolean filterInBlacklistedSpace = true;

    @Schema(description = "实例类型 -> 查询字段, 为空时查询默认字段")
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns;

}
