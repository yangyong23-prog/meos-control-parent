package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.param.strategy.QueryFreezePumpGroupParam;
import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdObjectVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.QueryHotColdStationSystemParameterVO;
import com.persagy.apccontrol.basic.model.vo.system.QuerySystemParameterVO;
import com.persagy.apccontrol.common.model.vo.strategy.chiller.QueryHotStationSystemParameterVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yuyao
 */
public interface DataBoardService {


    /**
     * 冷站运行信息查询
     *
     * @param systemId
     * @return
     */
    public QuerySystemParameterVO querySystemParameter(String systemId);

    /**
     * 冷热站运行信息查询
     *
     * @param systemId
     * @param classCode
     * @return
     */
    public QueryHotColdStationSystemParameterVO queryHotColdStationSystemParameter(String systemId, String classCode);


    /**
     * 热站运行信息查询
     *
     * @param systemId
     * @param classCode
     * @return
     */
    QueryHotStationSystemParameterVO queryHotStationSystemParameter(String systemId, String classCode);

    /**
     * 泵组运行信息查询
     *
     * @param systemId
     * @return
     */
    public List<ColdSiteSystemContrastVO> queryManualLastpumbParameter(String systemId);


    /**
     * 查询泵组下的 泵设备
     *
     * @param param
     * @return
     */
    public List<DtpRwdObjectVO> queryObjByFreezePumpGroupId(@RequestBody QueryFreezePumpGroupParam param);

}
