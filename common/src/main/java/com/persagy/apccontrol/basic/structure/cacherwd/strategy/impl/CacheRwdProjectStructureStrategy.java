package com.persagy.apccontrol.basic.structure.cacherwd.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheObjectModelWriteService;
import com.persagy.apccontrol.basic.structure.cacherwd.strategy.BaseCacheRwdStrategy;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 公共项目结构缓存构建策略。
 *
 * <p>建筑和楼层属于所有业务模块共享的项目基础对象，不建立业务 Key，按 classCode
 * 完整替换到新版对象和模型视图表。</p>
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
@Component(CacheRwdProjectStructureStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CacheRwdProjectStructureStrategy extends BaseCacheRwdStrategy {

    /** Spring Bean 名称。 */
    public static final @NonNull String BEAN_NAME = "cacheRwdProjectStructureStrategy";

    private static final @NonNull List<String> STRUCTURE_CLASS_CODES =
            Collections.unmodifiableList(Arrays.asList(
                    DtpRwdClassCodeEnum.BUILDING.value(),
                    DtpRwdClassCodeEnum.FLOOR.value()));

    private final @NonNull DtpRwdClientService dtpRwdClientService;

    private final @NonNull CacheObjectModelWriteService cacheObjectModelWriteService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return null;
    }

    @Override
    public boolean onlyBuildInCommonPhase() {
        return true;
    }

    @Override
    public boolean buildCacheData(RunningMonitorTypeEnum runningMonitorType) {
        List<DtpRwdBuildingDTO> buildings = this.listBuildings();
        List<DtpRwdFloorDTO> floors = this.listFloors();
        List<DtpRwdObjectDTO> objects =
                new ArrayList<>(buildings.size() + floors.size());
        objects.addAll(buildings);
        objects.addAll(floors);
        return this.cacheObjectModelWriteService.replaceObjectClassCodes(
                STRUCTURE_CLASS_CODES, objects);
    }

    private @NonNull List<DtpRwdBuildingDTO> listBuildings() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.BUILDING);
        queryParam.addWithColumn(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                DtpRwdClassCodeEnum.BUILDING.value()).toArray(new String[0]));
        return this.dtpRwdClientService.listBuilding(queryParam);
    }

    private @NonNull List<DtpRwdFloorDTO> listFloors() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.FLOOR);
        queryParam.addWithColumn(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                DtpRwdClassCodeEnum.FLOOR.value()).toArray(new String[0]));
        return this.dtpRwdClientService.listFloor(queryParam);
    }

    @Override
    public @NonNull String logTag() {
        return "物理世界缓存-公共项目结构策略";
    }
}
