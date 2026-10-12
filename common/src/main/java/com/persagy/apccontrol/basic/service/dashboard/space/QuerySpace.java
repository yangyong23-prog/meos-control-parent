package com.persagy.apccontrol.basic.service.dashboard.space;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;

import java.util.List;

/**
 * 空间查询
 */
public interface QuerySpace extends LogComponent {

    public List<? extends DtpRwdSpaceVO> querySpace(DemandInstanceValueObject instance, boolean useCache);

}
