/**
  * Copyright 2024 bejson.com 
  */
package com.persagy.apccontrol.basic.outside.dto.lighting;

import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author 薛涛
 * @Description
 * @Date 11:16 2024/5/15
 * @Param
 * @return
 **/
@Data
public class LightingRecords {

    private String id;
    private String name;
    private String localId;
    private String localName;
    private String objType;
    private String classCode;
    private Boolean valid;
    @Schema(description = "楼层顺序")
    private Integer floorSequenceId;
    @Schema(description = "建筑顺序")
    private Integer buildingSequenceId;

    private BasicInfos basicInfos;
    private List<InfoCodeParameters> runningParameters;
    private Integer abnormalEventCount;
    /*** 异常事件*/
    private  List<ExceptionEventBaseInfoVO> abnormalEvents;

    /** 操作列说明*/
    private OperationColumn operationColumn;

    /*** 限制信息点 key信息点,val 信息点值 未交付 返回 NULL, 无数据 返回-1*/
    private Map<String, Object> restrictInfos;

}