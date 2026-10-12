package com.persagy.apccontrol.basic.model.dto.rwd;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class BaseNamePropertyDTO extends BasePropertyDTO {

    private Double height;
    private Double roomHeight;
    private Double buildArea;
    private String buildingName;
    private String buildingLocalName;

    private String spaceId;
    private String spaceLocalId;
    private String spaceLocalName;

    private String floorLocalId;
    private String floorLocalName;
    private Integer floorSequenceID;

}
