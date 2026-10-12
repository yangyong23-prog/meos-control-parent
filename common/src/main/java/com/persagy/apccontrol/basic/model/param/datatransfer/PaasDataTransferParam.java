package com.persagy.apccontrol.basic.model.param.datatransfer;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据同步到云参数
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaasDataTransferParam<T extends BaseAssignIdDO> {

    /**
     * 数据类型
     */
    @NotNull(message = "{key_79713288a2f24753b5eb599f6934a460}")
    private DataTypeEnum dataType;

    /**
     * 数据
     */
    @NotNull(message = "{key_0e2befae3156470191cd73a20ec810e9}")
    private Collection<T> data;

    /**
     * 创建实例
     *
     * @param dataType 数据类型
     * @param data     数据
     * @param <T>      数据类型
     * @return 实例
     * @since 5.241102.0
     */
    public static <T extends BaseAssignIdDO> List<PaasDataTransferParam<T>> of(
            @NonNull DataTypeEnum dataType, @NonNull T data) {
        return of(dataType, Lists.newArrayList(data));
    }

    /**
     * 创建实例
     *
     * @param dataType 数据类型
     * @param data     数据
     * @param <T>      数据类型
     * @return 实例
     * @since 5.241102.0
     */
    public static <T extends BaseAssignIdDO> List<PaasDataTransferParam<T>> of(
            @NonNull DataTypeEnum dataType, @NonNull Collection<T> data) {
        return Lists.newArrayList(new PaasDataTransferParam<>(dataType, data));
    }

    /**
     * 创建实例
     *
     * @param dataTypeAndData 数据类型和数据映射
     * @param <T>             数据类型
     * @return 实例
     * @since 5.241102.0
     */
    public static <T extends BaseAssignIdDO> List<PaasDataTransferParam<T>> of(
            @NonNull Map<DataTypeEnum, Collection<T>> dataTypeAndData) {
        return dataTypeAndData.entrySet().stream()
                .map(item -> new PaasDataTransferParam<>(item.getKey(), item.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 创建实例
     *
     * @param targetAndId 删除实例和ID
     * @param <T>         数据类型
     * @return 实例
     * @since 5.241102.0
     */
    public static <T extends BaseAssignIdDO> List<PaasDataTransferParam<T>> ofDel(
            @NonNull Pair<T, Long> targetAndId) {
        targetAndId.getLeft().setId(targetAndId.getRight());
        return of(DataTypeEnum.DELETE, targetAndId.getLeft());
    }

    /**
     * 创建实例
     *
     * @param targetAndId 删除实例和ID
     * @param <T>         数据类型
     * @return 实例
     * @since 5.241102.0
     */
    public static <T extends BaseAssignIdDO> List<PaasDataTransferParam<T>> ofDel(
            @NonNull List<Pair<T, Long>> targetAndId) {
        List<T> data = new ArrayList<>();
        targetAndId.forEach(item -> {
            T source = item.getLeft();
            source.setId(item.getRight());
            data.add(source);
        });
        return of(DataTypeEnum.DELETE, data);
    }
}
