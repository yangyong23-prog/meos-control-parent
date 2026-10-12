package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * LabelDto
 *
 * @author houyuanzhi
 * @date 2023/6/1 15:08
 */
@Data
public class LabelDto {
    private String id;

    private String name;

    private String compType;

    private String position;

    private CoordinateDto absoluteLocation;

    private CoordinateDto location;

    private CoordinateDto size;

    private boolean hidden;

    private String props;

    private String content;

    private String style;
}
