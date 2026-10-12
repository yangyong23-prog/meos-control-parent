package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.vo.outside.SystemGraphVO;
import com.persagy.apccontrol.basic.structure.outside.systemgraph.ToProjectClassCodeEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface SystemGraphExternalService {

    /**
     * 需要转换classCode为project的编码集合
     */
    List<String> CONVERT_PROJECT_CLASSCODE_LIST = Arrays.asList(ToProjectClassCodeEnum.FFSR.value(), ToProjectClassCodeEnum.WSDW.value());

    List<SystemGraphVO> querySystemGraphList(Collection<String> classCodes);
}
