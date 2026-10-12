package com.persagy.apccontrol.basic.model.dto.rwd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class BasePropertyDTO {

    private String id;// 1  更新的时候指定
    private String localId;// 项目下唯一编码
    private String localName;// 1
    private String projectId;
    private String objType;
    private String classCode;
    private String groupCode;
    private String buildingId;
    private String floorId;
//    private String roomFuncType;
    private List<String> relObjIds;

}
