package com.persagy.apccontrol.basic.model.param.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * 运行策略空间查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class SpaceQueryParam extends BaseObjectQueryParam<DtpRwdSpaceDTO> {

    /**
     * 过滤空间功能类型
     */
    private Set<String> filterRoomFuncTypes;

    /**
     * 根据设备ID过滤设备所在空间关系, 为空时不过滤, 仅支持设备所在关系时生效
     */
    private Set<String> filterSpaceInEquipmentIds;

    /**
     * 根据设备ID过滤设备服务空间关系, 为空时不过滤, 仅支持设备服务关系时生效
     */
    private Set<String> filterSpaceServiceEquipmentIds;

    /**
     * 根据系统ID过滤空间服务系统关系, 为空时不过滤
     */
    private Set<String> filterSpaceServiceSystemIds;

    @Override
    protected List<DtpRwdSpaceDTO> queryObject(
            @NonNull DtpRwdObjectQueryParam originalQueryParam,
            @NonNull DtpRwdObjectQueryParam realQueryParam,
            @Nullable Function<DtpRwdObjectQueryParam, List<DtpRwdSpaceDTO>> customQuery) {
        // 当查询参数没有关联参数时, 可以直接赋值关联条件, 无需特殊处理
        // 当查询参数存在关联参数时, 需要进行内存过滤, 然后通过ID筛选
        Set<String> containsRelationKeys = new HashSet<>();
        if (CollectionUtils.isNotEmpty(this.filterRoomFuncTypes)) {
            if (ObjectUtils.isNotEmpty(realQueryParam.getInfoCodes().get(DtpRwdSpaceDTO.Fields.roomFuncType))) {
                containsRelationKeys.add(Fields.filterRoomFuncTypes);
                realQueryParam.addWithColumn(DtpRwdSpaceDTO.Fields.roomFuncType);
            } else {
                realQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType, this.filterRoomFuncTypes);
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterSpaceInEquipmentIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterSpaceInEquipmentIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(this.filterSpaceInEquipmentIds));
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterSpaceServiceEquipmentIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterSpaceServiceEquipmentIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(this.filterSpaceServiceEquipmentIds));
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterSpaceServiceSystemIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterSpaceServiceSystemIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjFrom(this.filterSpaceServiceSystemIds));
            }
        }
        List<DtpRwdSpaceDTO> result = Objects.nonNull(customQuery)
                ? customQuery.apply(realQueryParam)
                : dtpRwdClientService.listSpace(realQueryParam);
        if (CollectionUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        // 当查询参数包含了空间功能类型, 且查询参数中已经包含了空间功能类型, 则进行内存过滤
        if (containsRelationKeys.contains(Fields.filterRoomFuncTypes)) {
            result.removeIf(item -> !this.filterRoomFuncTypes.contains(item.getRoomFuncType()));
        }
        // 当查询参数存在关联参数时, 需要进行二次查询过滤
        Collection<String> filterIds = null;
        if (containsRelationKeys.contains(Fields.filterSpaceInEquipmentIds)) {
            filterIds = filterByRelationTo(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE,
                    filterSpaceInEquipmentIds);
        }
        if (containsRelationKeys.contains(Fields.filterSpaceServiceEquipmentIds)) {
            filterIds = filterByRelationTo(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                    filterSpaceServiceEquipmentIds);
        }
        if (containsRelationKeys.contains(Fields.filterSpaceServiceSystemIds)) {
            filterIds = filterByRelationTo(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE,
                    filterSpaceServiceSystemIds);
        }
        // 内存过滤不需要的对象ID
        filterByIds(result, filterIds);
        return result;
    }

    @Override
    protected String mysqlCacheKeyType() {
        return "space";
    }

    /**
     * 根据实例填充空间所在关系
     *
     * @param instance 实例
     * @since 7.250201.0
     */
    public void fillByInstance(@NonNull DemandInstanceValueObject instance) {
        switch (instance.getInstanceType()) {
            case PROJECT:
                break;
            case BUILDING:
                this.setFilterBuildingIds(Sets.newHashSet(instance.getInstanceId()));
                break;
            case FLOOR:
                this.setFilterFloorIds(Sets.newHashSet(instance.getInstanceId()));
                break;
            case SPACE:
                this.setFilterObjectIds(Sets.newHashSet(instance.getInstanceId()));
                break;
            default:
        }
    }
}
