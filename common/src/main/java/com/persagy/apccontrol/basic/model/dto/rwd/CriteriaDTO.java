package com.persagy.apccontrol.basic.model.dto.rwd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @Description:
 * @author: yancheng
 * @date: 2023/3/23
 * @version: V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
public class CriteriaDTO {

    private String objType;

    private String classCode;

    private Integer valid;

}
