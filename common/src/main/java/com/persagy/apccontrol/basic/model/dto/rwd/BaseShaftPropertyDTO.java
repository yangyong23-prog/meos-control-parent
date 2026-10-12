package com.persagy.apccontrol.basic.model.dto.rwd;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class BaseShaftPropertyDTO {

    private String id;// 1  更新的时候指定

    private String localId;// 项目下唯一编码

    private String localName;// 1

    private String projectId;

    private String objType;

    private String classCode;

    private String groupCode;

    private List<String> buildingId;

    private String startFloor;

    private String endFloor;

}
