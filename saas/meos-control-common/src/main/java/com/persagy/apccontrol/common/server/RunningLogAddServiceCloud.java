package com.persagy.apccontrol.common.server;

import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogMeosMsgDTO;
import com.persagy.apccontrol.basic.service.runninglog.category.RunningLogAddService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningLogAddServiceCloud implements RunningLogAddService {
    @Override
    public void saveByMeosMsg(RunningLogMeosMsgDTO log) {
        //云上暂时不做日志保存
    }
}
