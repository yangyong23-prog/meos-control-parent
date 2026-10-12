package com.persagy.apccontrol.basic.model.entity.systemgraph.base.simple;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author mj
 * @date 2022/10/14 13:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SimpleEquipmentVo extends SimpleObjectVo {
    {
        setObjType(DmpObjTypeEnum.EQUIPMENT);
    }
}
