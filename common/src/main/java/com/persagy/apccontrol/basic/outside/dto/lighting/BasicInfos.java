/**
  * Copyright 2024 bejson.com 
  */
package com.persagy.apccontrol.basic.outside.dto.lighting;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
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
public class BasicInfos {

    /**关联配电箱*/
    private DtpRwdObjectDTO relDistributionCabinet;
    /**关联空间*/
    private List<DtpRwdSpaceDTO> relSpace;

    /**关联控制模块*/
    private DtpRwdObjectDTO relController;

    /**功能类型*/
    private InfoCodeParameters funcType;

    /**灯具功能类型*/
    private InfoCodeParameters lampFuncType;
}