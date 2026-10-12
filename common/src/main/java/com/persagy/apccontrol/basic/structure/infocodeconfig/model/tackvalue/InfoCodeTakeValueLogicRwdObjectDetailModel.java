package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 信息点RWD对象详情取值逻辑模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTakeValueLogicRwdObjectDetailModel extends BaseInfoCodeTackValueLogicModel {

    /**
     * 查下信息点配置, 非 {@code null} 时生效
     */
    private @Nullable @Valid InfoCodeConfigQueryValueObject queryInfoCodeConfig;

    /**
     * 是否查所在空间
     */
    private boolean queryInSpaces = false;

    /**
     * 查询所在空间的拓展字段
     */
    private Set<String> queryInSpaceAdditionalWithColumns;

    /**
     * 是否查询所在楼层
     */
    private boolean queryInFloor = false;

    /**
     * 是否查所在建筑
     */
    private boolean queryInBuilding = false;

    /**
     * 查询条件
     */
    @NotNull(message = "{key_b0198d86cf1b416dae8e5d577d550fe0}")
    private @NonNull DtpRwdObjectQueryParam queryParam;

    /**
     * 回退查询条件列表
     * <p>
     * 当主查询条件 {@link #queryParam} 未查询到对象时，依次使用回退查询条件进行整批查询。
     * </p>
     */
    private @Nullable List<@Valid DtpRwdObjectQueryParam> fallbackQueryParams;

    /**
     * 自定义排序规则
     */
    private boolean customOrder = false;

    /**
     * 关联对象取值逻辑
     */
    private @Nullable InfoCodeTakeValueLogicRwdObjectDetailModel relation;

    /**
     * 数据源排序
     */
    private List<@NonNull String> sourceSequence;

    /**
     * 占位符数据源
     * <p>
     * 使用 {@link LinkedHashMap} 保证遍历顺序与配置顺序一致，
     * 后序数据源可通过占位符引用前序数据源的计算结果。
     * </p>
     */
    private @Nullable LinkedHashMap<@NonNull String, @NonNull InfoCodeTakeValueLogicRwdObjectModel> source;

    @Override
    public String logTag() {
        return "信息点配置-取值逻辑-RWD对象详情";
    }
}
