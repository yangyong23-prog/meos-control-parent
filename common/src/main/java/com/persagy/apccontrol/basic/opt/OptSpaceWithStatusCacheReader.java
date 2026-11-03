package com.persagy.apccontrol.basic.opt;

import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalEquipmentTagDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.AirTerminalSpaceWithStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalEquipmentGraphVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceGraphVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceWithStatusVO;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * opt 分支 B9（switch.opt.sseSpaceStatusCache，默认关）：SSE 空间带状态（SPACE_WITH_STATUS）读对象模型新缓存。
 * <p>
 * 原实现每拍都向 RWD 查设备、空间和两类关系。新缓存 READY 后，结构部分改为 {@link AirTerminalService#listSpaceEquipment}
 * （它在 READY 时只读本地新缓存），再按原逻辑补空间、设备标签。新缓存未 READY 时仍走原实现。
 * <p>
 * 与原实现的差异（同 listSpaceEquipment 的 READY 结果，已用于空间设备接口）：空间范围遵循白名单配置；
 * unableMonitor 只看 tdb/co2 规则；设备只带图元所需字段；空调机组群多带 controlledAirHandlingUnitIds。
 */
@Component
public class OptSpaceWithStatusCacheReader {

    @Lazy
    @Resource
    private AirTerminalService airTerminalService;
    @Lazy
    @Resource
    private CacheObjectModelStatusService cacheObjectModelStatusService;

    public boolean applicable() {
        return OptSwitch.on(OptSwitch.SSE_SPACE_STATUS_CACHE)
                && cacheObjectModelStatusService.isReady(RunningMonitorTypeEnum.AIR_TERMINAL);
    }

    public AirTerminalSpaceWithStatusVO read(AirTerminalSpaceWithStatusQueryParam param) {
        AirTerminalSpaceWithStatusVO result = airTerminalService.listSpaceEquipment(param);
        OptMetrics.inc("sse.spaceStatus.cache");
        if (result == null) {
            return null;
        }
        String projectId = param.getProjectId();
        List<AirTerminalSpaceGraphVO> spaces = result.getSpaces() == null ? Collections.emptyList() : result.getSpaces();
        List<AirTerminalEquipmentGraphVO> equipments = result.getEquipments() == null ? Collections.emptyList() : result.getEquipments();
        if (!spaces.isEmpty()) {
            Set<String> spaceIds = new LinkedHashSet<>();
            spaces.forEach(s -> spaceIds.add(s.getId()));
            Map<String, AirTerminalSpaceTagDTO> tags = airTerminalService.mapSpaceIdAndTag(projectId, spaceIds);
            spaces.forEach(s -> s.setTag(tags.get(s.getId())));
        }
        if (!equipments.isEmpty()) {
            Set<String> equipmentIds = new LinkedHashSet<>();
            equipments.forEach(e -> equipmentIds.add(e.getId()));
            Map<String, AirTerminalEquipmentTagDTO> tags = airTerminalService.mapEquipmentIdAndTag(projectId, equipmentIds);
            equipments.forEach(e -> e.setTag(tags.get(e.getId())));
        }
        return result;
    }
}
