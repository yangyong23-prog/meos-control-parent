package com.persagy.apccontrol.basic.service.cacherwd;

import com.persagy.apccontrol.basic.controller.shellconfig.param.SaveShellConfigParam;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdModelViewFieldsQueryParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhiteDeleteParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhiteQueryParam;
import com.persagy.apccontrol.basic.model.param.cacherwd.CacheRwdWhitelistParam;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdAirTerminalControlSpaceVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdBuildStatusVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdStatisticsVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdWhitelistVO;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

/**
 * 物理世界缓存服务接口
 *
 * @author wxf
 * @since 2026-03-17
 */
public interface CacheRwdService extends LogComponent {

    /**
     * 触发生成缓存
     *
     * @param runningMonitorType 运行监测类型，如果为 null 则构建所有类型的缓存
     * @return 是否构建成功
     */
    Boolean triggerBuildCache(RunningMonitorTypeEnum runningMonitorType);

    /**
     * 获取构建任务状态
     *
     * @param runningMonitorType 运行监测类型，如果为空则查询所有类型的状态
     * @return 构建任务状态
     */
    CacheRwdBuildStatusVO getBuildStatus(RunningMonitorTypeEnum runningMonitorType);

    /**
     * 获取缓存统计数据（按运行监测类型分组统计）
     *
     * @return 缓存统计数据
     */
    CacheRwdStatisticsVO getStatistics();

    /**
     * 查询当前项目的末端全量管控空间。
     *
     * <p>READY 时从项目全量空间 Key 读取新版对象缓存，未 READY 时实时计算末端管控空间；
     * 当前白名单配置不缩小该候选范围。</p>
     *
     * @return 仅包含空间 ID、本地名称、楼层 ID 和建筑 ID 的管控空间
     * @throws RuntimeException 缓存或物理世界查询失败时抛出
     * @since v7.260701.0
     */
    @NonNull List<CacheRwdAirTerminalControlSpaceVO> listAirTerminalControlSpaces();

    /**
     * 按对象 ID 批量查询当前项目的模型视图字段。
     *
     * <p>请求类型 READY 时查询模型视图表；未 READY 或尚未进入整体迁移范围时，一次批量查询
     * RWD。返回结果只包含实际命中的对象，每项的 {@code objectId} 均来自真实对象 ID。</p>
     *
     * @param param 运行监测类型和对象 ID 集合
     * @return 实际命中的对象 ID 及模型视图字段
     * @since v7.260701.0
     */
    @NonNull List<Map<String, Object>> listModelViewFieldsByObjectId(
            @NonNull CacheRwdModelViewFieldsQueryParam param);

    /**
     * 保存白名单配置
     *
     * <p>保存成功后同步持久化空间范围模式。切换全量时先持久化全量模式再删除白名单 Key；
     * 切换自定义白名单时先完成 Key 替换再持久化白名单模式。</p>
     *
     * @param param 白名单配置参数
     * @return 是否保存成功
     */
    Boolean saveWhitelist(CacheRwdWhitelistParam param);

    /**
     * 查询白名单配置
     *
     * <p>已配置独立空间范围壳配置的系统以壳配置为模式事实；白名单模式下 Key 为空仍返回
     * 自定义白名单空结果，不回退全量模式。</p>
     *
     * @param param 查询参数，包含运行监测类型和对象类型
     * @return 白名单配置信息
     */
    CacheRwdWhitelistVO queryWhitelist(CacheRwdWhiteQueryParam param);

    /**
     * 删除白名单配置
     *
     * <p>先把持久化空间范围恢复为全量模式，再删除对应新版白名单 Key。</p>
     *
     * @param param 删除参数，包含运行监测类型和对象类型
     * @return 是否删除成功
     */
    Boolean deleteWhitelist(CacheRwdWhiteDeleteParam param);

    /**
     * 完整保存壳配置，并在切换末端全量空间时原子删除新版白名单缓存 Key。
     *
     * <p>调用方必须传递原始完整保存参数，避免用简化参数覆盖同场景的其他配置项。</p>
     *
     * @param param 原始完整壳配置保存参数
     * @return 是否完成壳配置保存和白名单 Key 清理
     * @since v7.260701.0
     */
    @NonNull Boolean saveShellConfigAndRemoveAirTerminalWhitelist(
            @NonNull SaveShellConfigParam param);
}
