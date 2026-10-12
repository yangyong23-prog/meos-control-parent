package com.persagy.apccontrol.basic.controller.cacherwd;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdBuildParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdModelViewFieldsQueryParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhiteDeleteParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhiteQueryParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhitelistParam;
import com.persagy.apccontrol.basic.service.cacherwd.CacheRwdService;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdAirTerminalControlSpaceVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdBuildStatusVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdStatisticsVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdWhitelistVO;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 缓存物理世界控制器
 *
 * @author wxf
 * @since 2026-02-26
 */
@RestController
@RequestMapping("/cacheRwd")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "缓存物理世界", description = VersionConstant.V7_260302_0)
public class CacheRwdController {

    private final CacheRwdService cacheRwdService;

    @Operation(summary = "触发缓存构建", description = "触发物理世界缓存构建，参数为空时构建所有类型缓存")
    @PostMapping("/triggerBuildCache")
    public R<Boolean> triggerBuildCache(@RequestBody CacheRwdBuildParam param) {
        return R.success(cacheRwdService.triggerBuildCache(param.getRunningMonitorType()));
    }

    @Operation(summary = "获取构建任务状态", description = "获取物理世界缓存构建任务状态，包含构建状态和最后更新时间")
    @PostMapping("/getBuildStatus")
    public R<CacheRwdBuildStatusVO> getBuildStatus(
            @RequestBody CacheRwdBuildParam param) {
        return R.success(cacheRwdService.getBuildStatus(param.getRunningMonitorType()));
    }

    @Operation(summary = "获取缓存统计", description = "获取物理世界缓存统计数据，按运行监测类型分组统计缓存数量")
    @GetMapping("/getStatistics")
    public R<CacheRwdStatisticsVO> getStatistics() {
        return R.success(cacheRwdService.getStatistics());
    }

    /**
     * 查询当前项目的末端全量管控空间。
     *
     * @return 仅包含空间 ID、本地名称、楼层 ID 和建筑 ID 的管控空间
     * @throws RuntimeException 缓存或物理世界查询失败时抛出
     * @since v7.260701.0
     */
    @Operation(summary = "查询末端管控空间",
            description = "查询当前项目可供白名单配置选择的末端全量管控空间")
    @PostMapping("/listAirTerminalControlSpaces")
    public @NonNull R<List<CacheRwdAirTerminalControlSpaceVO>> listAirTerminalControlSpaces() {
        return R.success(this.cacheRwdService.listAirTerminalControlSpaces());
    }

    /**
     * 按真实对象 ID 批量查询模型视图字段。
     *
     * @param param 运行监测类型和对象 ID 集合
     * @return 实际命中的对象 ID 及模型视图字段
     * @since v7.260701.0
     */
    @Operation(summary = "批量查询模型视图字段",
            description = "READY 时查询模型视图缓存，未 READY 时按对象 ID 实时查询 RWD")
    @PostMapping("/listModelViewFieldsByObjectId")
    public @NonNull R<List<Map<String, Object>>> listModelViewFieldsByObjectId(
            @RequestBody @Validated @NonNull CacheRwdModelViewFieldsQueryParam param) {
        return R.success(this.cacheRwdService.listModelViewFieldsByObjectId(param));
    }

    @Operation(summary = "保存白名单配置",
            description = "保存白名单对象范围，并同步全量或白名单空间范围模式")
    @PostMapping("/whitelist/save")
    public R<Boolean> saveWhitelist(@RequestBody @Validated CacheRwdWhitelistParam param) {
        return R.success(cacheRwdService.saveWhitelist(param));
    }

    @Operation(summary = "查询白名单配置",
            description = "按持久化空间范围模式查询指定运行监测类型的白名单配置")
    @PostMapping("/whitelist/query")
    public R<CacheRwdWhitelistVO> queryWhitelist(
            @RequestBody @Validated CacheRwdWhiteQueryParam param) {
        return R.success(cacheRwdService.queryWhitelist(param));
    }

    @Operation(summary = "删除白名单配置",
            description = "删除指定运行监测类型的白名单配置，并恢复全量空间范围模式")
    @PostMapping("/whitelist/delete")
    public R<Boolean> deleteWhitelist(
            @RequestBody @Validated CacheRwdWhiteDeleteParam param) {
        return R.success(cacheRwdService.deleteWhitelist(param));
    }
}
