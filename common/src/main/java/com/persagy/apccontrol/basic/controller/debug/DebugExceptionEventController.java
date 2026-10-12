package com.persagy.apccontrol.basic.controller.debug;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 异常事件调试控制器
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@RestController
@RequestMapping(value = "/debug/exceptionEvent")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class DebugExceptionEventController {

    private final AbstractCommonServer abstractCommonServer;

    @PostMapping("/mapInstanceIdAndExceptionEventsWithEquipments")
    public R<Map<String, List<ExceptionEventBaseInfoVO>>> mapInstanceIdAndExceptionEventsWithEquipments(@RequestBody Set<String> spaceIds) {
        return R.success(abstractCommonServer.mapInstanceIdAndExceptionEventsWithEquipments(spaceIds, null));
    }
}
