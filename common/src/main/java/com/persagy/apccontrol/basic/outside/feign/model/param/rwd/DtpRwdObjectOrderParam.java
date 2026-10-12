package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 物理世界对象排序参数
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Data
public class DtpRwdObjectOrderParam {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtpRwdObjectOrderParam.class);
    private static final String LOG_TAG = "物理世界-对象排序";
    private static final DtpRwdClientService dtpRwdClientService = SpringUtil.getBean(DtpRwdClientService.class);

    /**
     * 排序字段
     */
    @NotBlank(message = "{key_46a63f74999f40b1b930c36e6e0b9e00}")
    private String column;

    /**
     * 是否升序, 默认为 {@code true}
     */
    private Boolean asc;

    /**
     * 是否按中文排序, 默认为 {@code false}
     */
    private Boolean chineseOrder;

    public static final String COLUMN_BUILDING = "building";
    public static final String COLUMN_FLOOR = "floor";

    /**
     * 转换对象排序器
     *
     * @param orders              排序对象
     * @param objects             对象列表
     * @param excludeQueryColumns 排除字段, 用于排除自定义排序字段
     * @return 排序器
     * @since 7.250601.0
     */
    @SuppressWarnings("unchecked")
    public static Comparator<DtpRwdObjectDTO> toObjectComparator(
            @Nullable Collection<DtpRwdObjectOrderParam> orders,
            @Nullable List<DtpRwdObjectDTO> objects,
            @Nullable Set<String> excludeQueryColumns) {
        if (CollectionUtils.isEmpty(objects) || CollectionUtils.isEmpty(orders)) {
            return null;
        }
        boolean orderByBuilding = false;
        boolean orderByFloor = false;
        Set<String> columns = new HashSet<>();
        for (DtpRwdObjectOrderParam order : orders) {
            String column = order.getColumn();
            switch (column) {
                case DtpRwdObjectOrderParam.COLUMN_BUILDING:
                    orderByBuilding = true;
                    break;
                case DtpRwdObjectOrderParam.COLUMN_FLOOR:
                    orderByFloor = true;
                    break;
                default:
                    if (CollectionUtils.isEmpty(excludeQueryColumns) || !excludeQueryColumns.contains(column)) {
                        columns.add(column);
                    }
            }
        }
        Map<String, String> objectIdAndBuildingId = new HashMap<>();
        Map<String, String> objectIdAndFloorId = new HashMap<>();
        Map<String, DtpRwdObjectDTO> objectIdAndData = new HashMap<>();
        Map<String, Integer> buildingIdAndSequenceId = new LinkedHashMap<>();
        Map<String, Integer> floorIdAndSequenceId = new LinkedHashMap<>();
        if (orderByBuilding || orderByFloor || CollectionUtils.isNotEmpty(columns)) {
            Set<String> objectIds = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
            queryParam.setId(objectIds);
            if (orderByBuilding) {
                queryParam.addWithColumn(BaseConvert.INFO_CODE_BUILDING_ID);
            }
            if (orderByFloor) {
                queryParam.addWithColumn(BaseConvert.INFO_CODE_FLOOR_ID);
            }
            if (CollectionUtils.isNotEmpty(columns)) {
                queryParam.addWithColumn(columns.toArray(new String[0]));
            }
            List<DtpRwdObjectDTO> orderObjects = dtpRwdClientService.listObject(queryParam);
            if (CollectionUtils.isNotEmpty(orderObjects)) {
                Set<String> buildingIds = new HashSet<>();
                Set<String> floorIds = new HashSet<>();
                for (DtpRwdObjectDTO object : orderObjects) {
                    String objectId = object.getId();
                    String buildingId = object.analyzeProperties(BaseConvert.INFO_CODE_BUILDING_ID);
                    String floorId = object.analyzeProperties(BaseConvert.INFO_CODE_FLOOR_ID);
                    if (StringUtils.isNotBlank(buildingId)) {
                        objectIdAndBuildingId.put(objectId, buildingId);
                        buildingIds.add(buildingId);
                    }
                    if (StringUtils.isNotBlank(floorId)) {
                        objectIdAndFloorId.put(objectId, floorId);
                        floorIds.add(floorId);
                    }
                    objectIdAndData.put(objectId, object);
                }
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(buildingIds)) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuilding(
                                new DtpRwdObjectQueryParam().setId(buildingIds)
                                        .addWithColumn(DtpRwdObjectDTO.Fields.id));
                        if (CollectionUtils.isNotEmpty(buildings)) {
                            for (int i = 0; i < buildings.size(); i++) {
                                buildingIdAndSequenceId.put(buildings.get(i).getId(), i);
                            }
                        }
                    }, BusinessThreadPoolExecutor.ttlExecutor()));
                }
                if (CollectionUtils.isNotEmpty(floorIds)) {
                    futures.add(CompletableFuture.runAsync(() -> {
                        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                                .setId(floorIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                                        DtpRwdFloorDTO.Fields.floorSequenceId));
                        if (CollectionUtils.isNotEmpty(floors)) {
                            for (DtpRwdFloorDTO floor : floors) {
                                floorIdAndSequenceId.put(floor.getId(), floor.getFloorSequenceId());
                            }
                        }
                    }, BusinessThreadPoolExecutor.ttlExecutor()));
                }
                if (CollectionUtils.isNotEmpty(futures)) {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                }
            }
        }
        Comparator<DtpRwdObjectDTO> rootComparator = (o1, o2) -> 0;
        for (DtpRwdObjectOrderParam order : orders) {
            String column = order.getColumn();
            rootComparator = rootComparator.thenComparing(item -> {
                String objectId = item.getId();
                switch (column) {
                    case DtpRwdObjectOrderParam.COLUMN_BUILDING:
                        return buildingIdAndSequenceId.get(objectIdAndBuildingId.get(objectId));
                    case DtpRwdObjectOrderParam.COLUMN_FLOOR:
                        return floorIdAndSequenceId.get(objectIdAndFloorId.get(objectId));
                    default:
                        // 先从自身对象中解析字段值, 如果为 null 则从排序对象中解析字段值
                        Object value = parseColumnValue(item, column);
                        if (Objects.isNull(value)) {
                            value = parseColumnValue(objectIdAndData.get(objectId), column);
                        }
                        return value;
                }
            }, toFieldComparator(order));
        }
        return rootComparator;
    }

    /**
     * 解析字段值
     *
     * @param object 对象
     * @param column 字段
     * @return 字段值
     * @since 7.250802.0
     */
    private static Object parseColumnValue(@Nullable DtpRwdObjectDTO object, @NonNull String column) {
        if (Objects.isNull(object)) {
            return null;
        }
        try {
            PropertyDescriptor descriptor = object.cachePropertyDescriptor(column);
            Object value = descriptor.getReadMethod().invoke(object);
            if (ObjectUtils.isNotEmpty(value)) {
                return value;
            }
            return object.analyzeProperties(column, Object.class);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "解析排序字段异常",
                    e, Pair.of("column", column));
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR,
                    String.format("解析排序字段 [%s] 异常", column));
        }
    }

    /**
     * 转换字段比较器
     *
     * @param param 排序参数
     * @return 比较器
     * @since 7.2550601.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Comparator toFieldComparator(DtpRwdObjectOrderParam param) {
        Comparator comparator = BooleanUtils.isTrue(param.getChineseOrder())
                ? Collator.getInstance(Locale.CHINA) : Comparator.naturalOrder();
        if (BooleanUtils.isFalse(param.getAsc())) {
            comparator = comparator.reversed();
        }
        return Comparator.nullsLast(comparator);
    }
}
