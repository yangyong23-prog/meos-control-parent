package com.persagy.apccontrol.common.model.vo;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.rwd.BaseNamePropertyDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.BaseShaftPropertyDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
public class SystemRelationVO extends DtpRwdObjectDTO {

    private List<BaseNamePropertyDTO> spaces;
    private List<JSONObject> equipments;
    private List<BaseShaftPropertyDTO> shafts;
    private List<BaseNamePropertyDTO> meshes;
}
