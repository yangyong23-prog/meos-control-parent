package com.persagy.apccontrol.basic.structure.cacherwd.service;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.cacherwd.model.entity.CacheRwdObject;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdBuildStatusVO;
import com.persagy.apccontrol.basic.structure.cacherwd.model.vo.CacheRwdStatisticsVO;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 物理世界对象缓存领域服务接口
 *
 * @author: wxf
 * @create: 2026-02-26 14:26
 **/
public interface CacheRwdDomainService extends BizComponent<CacheRwdDomainService> {

    /**
     * 批量保存物理世界对象缓存
     *
     * @param cacheRwdObjects 物理世界对象缓存列表
     * @return 是否保存成功
     */
    boolean saveBatch(@NotEmpty List<CacheRwdObject> cacheRwdObjects);

    /**
     * 根据自定义索引key保存缓存数据（先删除旧数据再保存新数据）
     *
     * @param customIndexKey 自定义索引key
     * @param cacheRwdObjects 物理世界对象缓存列表
     * @return 是否保存成功
     */
    boolean saveByCustomIndexKey(@NotBlank String customIndexKey, @NotEmpty List<CacheRwdObject> cacheRwdObjects);

    /**
     * 根据自定义索引key获取缓存数据
     *
     * @param customIndexKey 自定义索引key
     * @return 物理世界对象缓存列表
     */
    List<CacheRwdObject> getByCustomIndexKey(@NotBlank String customIndexKey);

    /**
     * 根据自定义索引key获取对象列表
     *
     * @param customIndexKey 自定义索引key
     * @return 对象列表
     * @since 7.260302.0
     */
    List<DtpRwdObjectDTO> listObjects(@NonNull String customIndexKey);

    /**
     * 执行策略的缓存构建方法
     *
     * <p>显式构建末端或照明时保留策略、事务和 READY 提交异常；其他类型及未指定类型的
     * 全量构建维持失败返回值，并继续执行未受影响的缓存类型。</p>
     *
     * @param runningMonitorType 运行监测类型，如果为空则执行所有策略
     * @return 是否构建成功
     * @throws RuntimeException 显式构建末端或照明发生异常时抛出
     */
    Boolean buildCache(RunningMonitorTypeEnum runningMonitorType);

    /**
     * 根据自定义索引 key 删除缓存数据
     *
     * @param customIndexKey 自定义索引 key
     * @return 是否删除成功
     */
    boolean deleteByCustomIndexKey(@NotBlank String customIndexKey);

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
     * 构造一级运行监测类型和对象类型对应的白名单缓存键。
     *
     * @param runningMonitorType 一级运行监测类型
     * @param objectType         对象类型
     * @return 白名单缓存键
     */
    String buildWhitelistKey(RunningMonitorTypeEnum runningMonitorType, DtpRwdObjectTypeEnum objectType);

}
