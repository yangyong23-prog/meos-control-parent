package com.persagy.apccontrol.basic.structure.bizcheck.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import org.springframework.lang.NonNull;

import java.util.List;

public interface BizCheckConfigRepository extends IService<BizCheckConfig> {
    BizCheckConfig getByBizCode(@NonNull RunningMonitorTypeEnum bizCode);

    List<BizCheckConfig> getAll();
}
