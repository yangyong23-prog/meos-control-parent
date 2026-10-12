package com.persagy.apccontrol.basic.utils.systemgraph;

import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import org.apache.commons.lang3.StringUtils;

public class ObjTypeUtil {

    /**
     * direct:
     * from: 要查询出的是 fromClassCode，toClassCode是现有 to 所对应的 classCode，条件是 to -> from
     * to: 要查询出的是 toClassCode，fromClassCode是现有 from 所对应的 classCode，条件是 from -> to
     *
     * @param rule
     * @return
     */
    public static DirectRelationDTO parseRule(String rule) {
        if (StringUtils.isBlank(rule)) {
            return null;
        }
        DirectRelationDTO directRelationDTO = new DirectRelationDTO();
        if (rule.contains("=>")) {
            String[] split = rule.split("=>");
            String[] graphRel = split[1].split("/");
            directRelationDTO.setGraphCode(graphRel[0].trim());
            directRelationDTO.setRelCode(graphRel[1].trim());
            directRelationDTO.setFromClassCode(split[0].trim());
            directRelationDTO.setToClassCode(split[2].trim());
            directRelationDTO.setDirect("from");
            return directRelationDTO;
        }
        if (rule.contains("<=")) {
            String[] split = rule.split("<=");
            String[] graphRel = split[1].split("/");
            directRelationDTO.setGraphCode(graphRel[0].trim());
            directRelationDTO.setRelCode(graphRel[1].trim());
            directRelationDTO.setFromClassCode(split[2].trim());
            directRelationDTO.setToClassCode(split[0].trim());
            directRelationDTO.setDirect("to");
            return directRelationDTO;
        }
        return null;
    }

}
