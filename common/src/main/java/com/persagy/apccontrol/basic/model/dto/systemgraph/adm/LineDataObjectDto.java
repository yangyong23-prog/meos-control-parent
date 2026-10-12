package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * LineDataObjectDto
 *
 * @author houyuanzhi
 * @date 2023/6/20 17:05
 */
@Data
public class LineDataObjectDto {
    private String graphCode;
    private String relCode;
    private String objFrom;
    private String objTo;
    private String flag;
}
