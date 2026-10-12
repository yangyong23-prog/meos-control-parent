package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.framework.common.context.ContextUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Description:
 * @author: yancheng
 * @date: 2023/3/23
 * @version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
public class QueryDTO extends BaseQueryPropertyDTO{

    @Schema(description = "IOT入参",required = true)
    private CriteriaDTO criteria;

    public static QueryDTO setClassCode(){
        QueryDTO queryDTO = new QueryDTO();
        queryDTO.setCriteria(new CriteriaDTO(null, CommonConst.ACCC, 1));
        queryDTO.setGroupCode(ContextUtil.getGroupCode());
        return queryDTO;
    }

    public static QueryDTO setClassCode(String classCode){
        QueryDTO queryDTO = new QueryDTO();
        queryDTO.setCriteria(new CriteriaDTO(null, classCode, 1));
        queryDTO.setGroupCode(ContextUtil.getGroupCode());
        return queryDTO;
    }

    public static QueryDTO newQueryDTO(String objType, String classCode){
        QueryDTO queryDTO = new QueryDTO();
        queryDTO.setCriteria(new CriteriaDTO(   objType, classCode, 1));
        queryDTO.setGroupCode(ContextUtil.getGroupCode());
        return queryDTO;
    }

}
