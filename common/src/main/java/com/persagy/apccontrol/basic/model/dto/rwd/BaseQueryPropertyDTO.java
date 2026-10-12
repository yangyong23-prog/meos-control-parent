package com.persagy.apccontrol.basic.model.dto.rwd;


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
@ToString(callSuper = true)
public class BaseQueryPropertyDTO {

    private List<String> id;
    private List<String> localId;
    private String localName;
    private String projectId;
    private String objType;
    private String groupCode;
    private Integer valid;

    private RelationFromDTO relationFrom;

    private RelationToDTO relationTo;

    public void setRelationFrom(String graphCode, String relCode,List<String> objTo) {
        relationFrom = new RelationFromDTO();
        relationFrom.setGraphCode(graphCode);
        relationFrom.setRelCode(relCode);
        relationFrom.setObjTo(objTo);
    }

    public void setRelationTo(String graphCode, String relCode, List<String> objFrom) {
        relationTo = new RelationToDTO();
        relationTo.setGraphCode(graphCode);
        relationTo.setRelCode(relCode);
        relationTo.setObjFrom(objFrom);
    }


}
