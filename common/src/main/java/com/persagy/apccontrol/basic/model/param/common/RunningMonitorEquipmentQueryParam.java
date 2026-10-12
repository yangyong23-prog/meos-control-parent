package com.persagy.apccontrol.basic.model.param.common;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 运行监测设备查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "运行监测设备查询参数")
@Accessors(chain = true)
public class RunningMonitorEquipmentQueryParam extends EquipmentQueryParam {

    /**
     * 运行监测二级类型, 存在二级类型时不能为空, 和 {@link #runningMonitorSecondTypes} 可二选一
     */
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 运行监测二级类型, 存在二级类型时不能为空, 和 {@link #runningMonitorSecondType} 可二选一
     */
    private List<RunningMonitorSecondTypeEnum> runningMonitorSecondTypes;

    /**
     * 获取运行监测二级类型
     *
     * @return 运行监测二级类型
     * @since 7.250801.0
     */
    public List<RunningMonitorSecondTypeEnum> getRunningMonitorSecondTypes() {
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
