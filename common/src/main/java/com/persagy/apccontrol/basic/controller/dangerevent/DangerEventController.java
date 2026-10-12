package com.persagy.apccontrol.basic.controller.dangerevent;


import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.dangerevent.QueryByObjectParam;
import com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventVO;
import com.persagy.apccontrol.basic.service.dangerevent.DangerEventService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/dangerEvent")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DangerEventController {

    private final DangerEventService dangerEventService;

    @Operation(summary = "查询-设备的风险事件列表", tags = VersionConstant.V7_250201_0)
    @PostMapping("/listByObject")
    public R<List<DangerEventVO>> listBySpace(@RequestBody @Valid QueryByObjectParam param) {
        return R.success(dangerEventService.listByObject(param.getObjectIds()));
    }

    @Operation(summary = "查询-设备的风险事件列表", tags = {VersionConstant.V7_250201_0, VersionConstant.V7_260802_0})
    @PostMapping("/listByDangerObject")
    public R<List<DangerEventVO>> listByDangerObject(@RequestBody @Valid QueryByObjectParam param) {
        return R.success(dangerEventService.listByDangerObject(param));
    }



}
