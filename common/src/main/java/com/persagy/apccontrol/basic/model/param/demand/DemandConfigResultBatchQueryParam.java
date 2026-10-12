package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求配置预计算结果批量查询参数.
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求配置预计算结果批量查询参数")
@NoArgsConstructor
@AllArgsConstructor
public class DemandConfigResultBatchQueryParam extends BaseDemandTemplateParam {

    /**
     * 场景ID，未传时不按场景过滤.
     */
    @Schema(description = "场景ID，未传时不按场景过滤")
    private @Nullable Long sceneId;

    /**
     * 需求实例类型.
     */
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    @Schema(description = "需求实例类型；必填，实例ID为空时查询该类型全部结果", required = true)
    private @NonNull DemandInstanceTypeEnum instanceType;

    /**
     * 需求实例ID集合.
     *
     * <p>校验时会过滤集合中的 {@literal null} 元素并回写新的集合；清理后为空时查询指定实例类型的全部结果.</p>
     */
    @Schema(description = "需求实例ID集合；传入非空集合时按实例类型和实例ID过滤，"
            + "未传或清理后为空时查询指定实例类型的全部结果，集合中的null元素会被忽略")
    private @Nullable Set<@Nullable String> instanceIds;

    /**
     * 是否自动补齐空实例ID.
     *
     * <p>仅保留参数兼容性；结果表查询不会访问RWD，空实例ID按指定类型全部结果查询.</p>
     */
    @Schema(description = "兼容实时批量查询参数；结果表查询不会访问RWD补齐实例ID，"
            + "空实例ID集合按对应类型全部结果查询")
    private boolean autoFillEmptyInstanceIds = false;

    /**
     * 是否过滤屏蔽空间.
     *
     * <p>默认过滤；显式传入 {@literal false} 时返回屏蔽空间，并通过响应中的shielded标识区分.</p>
     */
    @Schema(description = "是否过滤屏蔽空间；默认过滤", defaultValue = "true")
    private boolean filterShieldedSpaces = true;

    /**
     * 是否查询实例标签.
     *
     * <p>默认不查询；传入 {@literal true} 时批量查询自定义标签并在响应中返回实例标签.</p>
     */
    @Schema(description = "是否查询实例标签；默认不查询", defaultValue = "false")
    private boolean queryInstanceTag = false;

    /**
     * 是否填充实例名称.
     *
     * <p>仅保留参数兼容性；轻量结果VO不包含实例名称，该参数不会触发RWD实例名称查询.</p>
     */
    @Schema(description = "兼容参数；轻量结果VO不包含实例名称，该参数不会触发RWD实例名称查询")
    private boolean fillInstanceName = false;

    @Override
    public void checkAndSet() {
        super.checkAndSet();
        if (Objects.nonNull(this.instanceIds)) {
            this.instanceIds = this.instanceIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }
}
