package com.persagy.apccontrol.basic.model.vo.systemgraph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三维坐标点
 *
 * @author mj
 * @date 2023/4/12 10:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point3D {
    private double x;
    private double y;
    private double z;
}
