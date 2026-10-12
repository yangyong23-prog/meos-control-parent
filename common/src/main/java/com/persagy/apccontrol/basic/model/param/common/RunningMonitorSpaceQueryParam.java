package com.persagy.apccontrol.basic.model.param.common;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 运行监测空间查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "运行监测空间查询参数")
public class RunningMonitorSpaceQueryParam extends SpaceQueryParam {

    /**
     * 运行监测二级类型, 存在二级类型时不能为空
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 运行监测二级类型, 存在二级类型时不能为空
     */
    private List<RunningMonitorSecondTypeEnum> runningMonitorSecondTypes;
    
    /**
     * 是否是空间树查询空间 默认fasle
     * 如果为true 查询模块全量空间信息
     */
    private boolean spaceTree =  false;

    /**
     * 是否使用缓存，默认 true
     * 设置为 false 时直接查询真实数据，不走缓存热刷新
     */
    private boolean meosCache = true;

    /**
     * 获取运行监测二级类型
     *
     * @return 运行监测二级类型
     * @since 7.250801.0
     */
    public @Nullable List<RunningMonitorSecondTypeEnum> getRunningMonitorSecondTypes() {
        if (CollectionUtils.isNotEmpty(this.runningMonitorSecondTypes)) {
            return this.runningMonitorSecondTypes;
        }
        if (Objects.nonNull(this.runningMonitorSecondType)) {
            return Lists.newArrayList(this.runningMonitorSecondType);
        }
        return null;
    }

    @Override
    protected String mysqlCacheKeyType() {
        return Optional.ofNullable(this.getRunningMonitorSecondTypes())
                .map(types -> types.stream()
                        .map(RunningMonitorSecondTypeEnum::getValue)
                        .collect(Collectors.joining("_")))
                .map(types -> types + "_" + super.mysqlCacheKeyType())
                .orElseGet(super::mysqlCacheKeyType);
    }
}
