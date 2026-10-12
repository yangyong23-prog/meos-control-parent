package com.persagy.apccontrol.basic.model.vo.infocode;

import lombok.Data;

import java.util.List;

/**
 * 原始信息点配置VO
 *
 * @author JiYinchuan
 * @since 7.260401.0
 */
@Data
public class InfoCodeConfigOriginVO {

    /**
     * 模版类型信息
     */
    private InfoCodeTemplateVO template;

    /**
     * 信息点配置列表
     */
    private List<InfoCodeGroupVO> relInfoCodeGroupConfigs;

}
