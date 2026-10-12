package com.persagy.apccontrol.controller.debug;

import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.service.handler.RunningLogHandle;
import com.persagy.apccontrol.task.lighting.LightingFeedbackAdjustTask;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 照明调试控制器
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@RestController("edgeComputeDebugController")
@RequestMapping(value = "/debugg")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugController {

    private final ObjectProvider<LightingFeedbackAdjustTask> lightingFeedbackAdjustTask;
    @Resource
    private RunningLogHandle runningLogHandle;

    @PostMapping("/lightingFeedbackAdjustTask")
    public R<Object> lightingFeedbackAdjustTask() {
        if (Objects.nonNull(lightingFeedbackAdjustTask.getIfAvailable())) {
            lightingFeedbackAdjustTask.getIfAvailable().execute();
        }
        return R.success(true);
    }

    @PostMapping("/test")
    public R<Void> test() {
        FeedbackInfoMsgMqttVO su = new FeedbackInfoMsgMqttVO();
        su.setGroupCode("TYXK");
        su.setProjectId("Pj9522457558");
        su.setObjectId("Eq7480382824204409776d5a48d29afd2d438404757e");
        su.setInfoCode("onlineStatus");
        su.setDataValue("1");
        su.setDataTime("2025-11-25 09:09:09");
        su.setDataStatus(2);
        su.setInvalid(1);
        su.setType(1);
        su.setId("691d757ee4b0a8668f141026");
        su.setTraceData("691d757ee4b0a8668f141026");
        su.setLocalId("7505428");
        su.setObjType("equipment");
        su.setClassCode("LTLTCC");
        su.setUser("none");
        su.setResult(1);
        su.setCmdLevel("8");
        su.setModelType("KITCHEN_RANGE_HOOD");
        runningLogHandle.handleLog( su);
        return R.success(null);
    }
}
