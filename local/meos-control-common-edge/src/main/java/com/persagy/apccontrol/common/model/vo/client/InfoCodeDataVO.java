package com.persagy.apccontrol.common.model.vo.client;

import lombok.Data;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/7/22 15:52
 * @Version 1.0
 */
@Data
public class InfoCodeDataVO {

    private String infoCode;

    private String name;


    private String unit;


    private Object dataValue;

    private Object dataSource;


    /***枚举类型值，对应的名称
     */
    private String dataValueName;
}
