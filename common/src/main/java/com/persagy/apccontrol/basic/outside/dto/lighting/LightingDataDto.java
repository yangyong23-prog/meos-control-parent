/**
  * Copyright 2024 bejson.com 
  */
package com.persagy.apccontrol.basic.outside.dto.lighting;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * @Author 薛涛
 * @Description
 * @Date 11:16 2024/5/15
 * @Param
 * @return
 **/
@Data
public class LightingDataDto {

    private Integer totalCount;
    private Integer totalPage;
    private List<LightingRecords> records;

    public static LightingDataDto empty() {
        LightingDataDto result = new LightingDataDto();
        result.setTotalCount(0);
        result.setTotalPage(0);
        result.setRecords(Lists.newArrayList());
        return result;
    }
}