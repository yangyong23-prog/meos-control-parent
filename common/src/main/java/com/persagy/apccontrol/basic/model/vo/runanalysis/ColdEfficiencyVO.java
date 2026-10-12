package com.persagy.apccontrol.basic.model.vo.runanalysis;

import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import lombok.Data;

import java.util.List;

@Data
public class ColdEfficiencyVO {

    /**
     * 系统名称
     */
    private String systemName;
    /**
     * 系统id
     */
    private String systemId;

    /**
     * 设备类编码
     */
    private String classCode;

    /**
     * 可监可控状态
     */
    private String monitoringSurveillanceStatus;

    /**
     * 冷站信息点数据
     */
    List<InfoCodeValueVO> dataList;
}
