package com.persagy.apccontrol.basic.model.param.common;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * 运行策略设备查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class EquipmentQueryParam extends BaseObjectQueryParam<DtpRwdEquipmentDTO> {

    /**
     * 根据空间ID过滤设备所在空间关系, 为空时不过滤
     */
    private Set<String> filterEquipmentInSpaceIds;

    /**
     * 根据空间ID过滤设备服务空间关系, 为空时不过滤
     */
    private Set<String> filterEquipmentServiceSpaceIds;

    /**
     * 根据系统ID过滤设备所在系统关系, 为空时不过滤
     */
    private Set<String> filterEquipmentInSystemIds;

    /**
     * 根据系统ID过滤设备组所在系统关系, 为空时不过滤
     */
    private Set<String> filterEquipmentGroupInSystemIds;

    @Override
    protected List<DtpRwdEquipmentDTO> queryObject(
            @NonNull DtpRwdObjectQueryParam originalQueryParam,
            @NonNull DtpRwdObjectQueryParam realQueryParam,
            @Nullable Function<DtpRwdObjectQueryParam, List<DtpRwdEquipmentDTO>> customQuery) {
        // 当查询参数没有关联参数时, 可以直接赋值关联条件, 无需特殊处理
        // 当查询参数存在关联参数时, 需要进行内存过滤, 然后通过ID筛选
        Set<String> containsRelationKeys = new HashSet<>();
        if (CollectionUtils.isNotEmpty(this.filterEquipmentInSpaceIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterEquipmentInSpaceIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjTo(this.filterEquipmentInSpaceIds));
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterEquipmentServiceSpaceIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterEquipmentServiceSpaceIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(this.filterEquipmentServiceSpaceIds));
            }
        }
        if (CollectionUtils.isNotEmpty(this.filterEquipmentInSystemIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterEquipmentInSystemIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(this.filterEquipmentInSystemIds));
            }
        }
        if (CollectionUtils.isNotEmpty(filterEquipmentGroupInSystemIds)) {
            if (realQueryParam.containsRelationParam()) {
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(filterEquipmentGroupInSystemIds));
            }
        }
        List<DtpRwdEquipmentDTO> result = Objects.nonNull(customQuery)
                ? customQuery.apply(realQueryParam)
                : dtpRwdClientService.listEquipment(realQueryParam);
        // 当查询参数存在关联参数时, 需要进行二次查询过滤
        Collection<String> filterIds = null;
        if (containsRelationKeys.contains(Fields.filterEquipmentInSpaceIds)) {
            filterIds = filterByRelationFrom(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE,
                    this.filterEquipmentInSpaceIds);
        }
        if (containsRelationKeys.contains(Fields.filterEquipmentServiceSpaceIds)) {
            filterIds = filterByRelationFrom(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE,
                    this.filterEquipmentServiceSpaceIds);
        }
        if (containsRelationKeys.contains(Fields.filterEquipmentInSystemIds)) {
            filterIds = filterByRelationTo(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.EQ_IN_SYSTEM,
                    this.filterEquipmentInSystemIds);
        }
        filterByIds(result, filterIds);
        return result;
    }

    @Override
    protected String mysqlCacheKeyType() {
        return "equipment";
    }

    /**
     * 根据实例填充设备所在关系
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
                this.setFilterEquipmentInSpaceIds(Sets.newHashSet(instance.getInstanceId()));
                break;
            default:
        }
    }
}
