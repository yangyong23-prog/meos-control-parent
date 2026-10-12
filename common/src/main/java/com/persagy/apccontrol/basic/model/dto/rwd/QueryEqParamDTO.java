package com.persagy.apccontrol.basic.model.dto.rwd;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(callSuper = true)
public class QueryEqParamDTO extends BaseQueryPropertyDTO {
    private List<String> classCode;

}
