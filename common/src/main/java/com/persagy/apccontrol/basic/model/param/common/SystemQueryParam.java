package com.persagy.apccontrol.basic.model.param.common;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
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
 * 运行策略空间查询参数
 *
 * @author JiYinchuan
 * @since 7.250201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class SystemQueryParam extends BaseObjectQueryParam<DtpRwdObjectDTO> {

    /**
     * 根据设备ID过滤设备所在系统关系, 为空时不过滤, 仅支持设备所在关系时生效
     */
    private Set<String> filterSystemInEquipmentIds;

    @Override
    protected List<DtpRwdObjectDTO> queryObject(
            @NonNull DtpRwdObjectQueryParam originalQueryParam,
            @NonNull DtpRwdObjectQueryParam realQueryParam,
            @Nullable Function<DtpRwdObjectQueryParam, List<DtpRwdObjectDTO>> customQuery) {
        // 当查询参数没有关联参数时, 可以直接赋值关联条件, 无需特殊处理
        // 当查询参数存在关联参数时, 需要进行内存过滤, 然后通过ID筛选
        Set<String> containsRelationKeys = new HashSet<>();
        if (CollectionUtils.isNotEmpty(filterSystemInEquipmentIds)) {
            if (realQueryParam.containsRelationParam()) {
                containsRelationKeys.add(Fields.filterSystemInEquipmentIds);
                realQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            } else {
                realQueryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(filterSystemInEquipmentIds));
            }
        }
        List<DtpRwdObjectDTO> result = Objects.nonNull(customQuery)
                ? customQuery.apply(realQueryParam)
                : dtpRwdClientService.listObject(realQueryParam);
        if (CollectionUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        // 当查询参数存在关联参数时, 需要进行二次查询过滤
        Collection<String> filterIds = null;
        if (containsRelationKeys.contains(Fields.filterSystemInEquipmentIds)) {
            filterIds = filterByRelationTo(filterIds, originalQueryParam,
                    DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE,
                    filterSystemInEquipmentIds);
        }
        // 内存过滤不需要的对象ID
        filterByIds(result, filterIds);
        return result;
    }

    @Override
    protected String mysqlCacheKeyType() {
        return "system";
    }
}
