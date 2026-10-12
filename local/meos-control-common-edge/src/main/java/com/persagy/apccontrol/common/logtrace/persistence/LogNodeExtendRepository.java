package com.persagy.apccontrol.common.logtrace.persistence;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;

import java.util.Collection;
import java.util.List;

public interface LogNodeExtendRepository extends IService<LogNodeExtendPo> {


    List<LogNodeExtendPo> listByNodeIds(Collection<Long> nodeIds);



}
