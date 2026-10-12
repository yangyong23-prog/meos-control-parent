package com.persagy.apccontrol.common.logtrace.persistence;


import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface LogNodeRepository extends IService<LogNodePo> {

    List<LogNodePo> findNodeByBusinessIds(Collection<String> businessIds, String businessType);

    List<LogNodePo> getByTime(LocalDateTime beginTime, LocalDateTime endTime, String nodeCode,String application);


}
