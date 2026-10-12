package com.persagy.apccontrol.basic.service.instance.config;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.instance.config.InstanceConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.instance.InstanceConfigTypeEnum;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.instance.InstanceConfigSaveParam;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceConfigVO;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import java.util.List;

public interface InstanceConfigService extends IService<InstanceConfig>, LogComponent {


    Boolean saveInstanceConfig(@Valid InstanceConfigSaveParam param, Boolean isEdge);

    List<InstanceConfigVO> listInstanceConfig(@Valid InstanceConfigQueryParam queryParam);

    InstanceConfig getInstanceConfig(@NonNull InstanceConfigTypeEnum configType,
                                     @NonNull DtpRwdObjectTypeEnum objectType,
                                     RunningMonitorSecondTypeEnum runningMonitorSecondType,
                                     @NonNull RunningMonitorTypeEnum runningMonitorType,
                                     String relMsg);
}
