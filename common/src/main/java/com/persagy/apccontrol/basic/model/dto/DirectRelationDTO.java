package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.model.dto.rwd.BaseRelationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class DirectRelationDTO extends BaseRelationDTO {

    /**
     * from 对象所对应的classCode
     */
    private String fromClassCode;

    /**
     * to 对象所对应的classCode
     */
    private String toClassCode;

    /**
     * direct:
     *  from: 要查询出的是 fromClassCode，toClassCode是现有 to 所对应的 classCode，条件是 to -> from
     *  to: 要查询出的是 toClassCode，fromClassCode是现有 from 所对应的 classCode，条件是 from -> to
     */
    private String direct;

}
