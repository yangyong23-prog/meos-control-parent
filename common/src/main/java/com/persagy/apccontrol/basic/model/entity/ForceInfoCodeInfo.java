package com.persagy.apccontrol.basic.model.entity;

import lombok.Data;

/**
 * 当前信息点对应的强控信息点集合
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/29 15:39
 */
@Data
public class ForceInfoCodeInfo {

    private String classCode;

    private String graphCode;

    private String relCode;

    private String infoCode;

    /**
     * from，to
     */
    private String relDirection;

}
