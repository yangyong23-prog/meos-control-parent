package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author mj
 * @date 2022/7/1 15:36
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DirectEquip extends AimObjectDto {
    private String id;
    private String classCode;
    private String localName;
    private String bimLocation;
    private JSONArray outline;
    private JSONArray skeletonLine;
    private String floorId;
    private String buildingId;

    public DirectEquip() {
        setObjType(DmpObjTypeEnum.EQUIPMENT);
    }

}
