package com.persagy.apccontrol.basic.controller.shellconfig.param;

import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import lombok.Data;

import java.util.List;

@Data
public class EfficiencyVO {

    /**
     * 系统名称
     */
    private String systemName;
    /**
     * 系统id
     */
    private String systemId;

    /**
     * 冷站信息点数据
     */
    List<InfoCodeValueVO> dataList;
}
