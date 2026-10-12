package com.persagy.apccontrol.basic.model.vo.objectgroup;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;

import java.util.List;

@Data
public class ObjectGroupObjectVO extends DtpRwdObjectDTO {


    /**
     * 楼层id
     */
    private String floorId;
    /**
     * 楼层名称
     */
    private String floorName;

    /**
     * 空间功能类型
     */
    private List<String> roomFuncTypes;
    /**
     * 序列
     */
    private Integer sequence;

}
