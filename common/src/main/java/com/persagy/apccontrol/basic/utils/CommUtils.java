package com.persagy.apccontrol.basic.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import com.persagy.framework.common.context.ContextUtil;

/**
 * @author tangzy
 * @date 2023年03月29日 16:53
 */
public class CommUtils {

    public static String getGroupCoude(String input) {
        return ObjectUtil.isEmpty(ContextUtil.getGroupCode())?input:ContextUtil.getGroupCode();
    }

    public static String getProjectId(String input) {
        return ObjectUtil.isEmpty(ContextUtil.getProjectId())?input:ContextUtil.getProjectId();
    }

    /**
     * direct:
     *  from: 要查询出的是 fromClassCode，toClassCode是现有 to 所对应的 classCode，条件是 to -> from
     *  to: 要查询出的是 toClassCode，fromClassCode是现有 from 所对应的 classCode，条件是 from -> to
     * @param rule
     * @return
     */
    public static DirectRelationDTO parseRule(String rule) {
        if (StrUtil.isBlank(rule)) {
            return null;
        }
        DirectRelationDTO directRelationDTO = new DirectRelationDTO();
        if (rule.contains("=>")) {
            String[] split = rule.split("=>");
            String[] graph_rel = split[1].split("/");
            directRelationDTO.setGraphCode(graph_rel[0].trim());
            directRelationDTO.setRelCode(graph_rel[1].trim());
            directRelationDTO.setFromClassCode(split[0].trim());
            directRelationDTO.setToClassCode(split[2].trim());
            directRelationDTO.setDirect(CommonConst.FROM_RELATION);
        } else {
            String[] split = rule.split("<=");
            String[] graph_rel = split[1].split("/");
            directRelationDTO.setGraphCode(graph_rel[0].trim());
            directRelationDTO.setRelCode(graph_rel[1].trim());
            directRelationDTO.setFromClassCode(split[2].trim());
            directRelationDTO.setToClassCode(split[0].trim());
            directRelationDTO.setDirect(CommonConst.TO_RELATION);
        }
        return directRelationDTO;
    }

    public static void main(String[] args) {
        System.out.println(parseRule("ACPPCP <= MechSubset/Sy2Eq <= ACCC"));
    }

}
