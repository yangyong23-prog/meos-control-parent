package com.persagy.apccontrol.basic.model.dto.rwd;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class RelationFromDTO extends BaseRelationDTO {
    private List<String> objTo;
}
