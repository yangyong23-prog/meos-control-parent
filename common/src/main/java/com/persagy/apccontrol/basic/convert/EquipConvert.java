package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.EquipRecordLog;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 物理世界转换器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Mapper(componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface EquipConvert extends BaseConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.6.0
     */
    EquipRecordLog toObjectVo(EquipRecord source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @InheritConfiguration
    List<EquipRecordLog> toObjectVo(List<EquipRecord> source);
}
