package com.persagy.apccontrol.basic.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.resource.param.RcBasicsConfigQueryDaoParam;
import com.persagy.framework.common.entity.PageParams;
import com.persagy.framework.database.service.SuperService;
import org.jspecify.annotations.NonNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【rc_basics_config(配置日志表)】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcBasicsConfigService extends SuperService<RcBasicsConfig> {

    /**
     * 获取所有的配置数据
     *
     * @return
     */
    List<RcBasicsConfig> listAll();

    /**
     * 查询基础配置范围
     *
     * @param param 查询参数
     * @return 基础配置
     * @since 7.240403.0
     */
    RcBasicsConfig getValid(@NonNull RcBasicsConfigQueryDaoParam param);

    /**
     * 查询基础配置范围
     *
     * @param param 查询参数
     * @return 基础配置
     * @since v5.1.0
     */
    List<RcBasicsConfig> list(@NonNull RcBasicsConfigQueryDaoParam param);

    /**
     * 查询基础配置范围
     *
     * @param param 查询参数
     * @return 基础配置
     * @since v5.1.0
     */
    IPage<RcBasicsConfig> pageValidDuration(@NonNull PageParams<RcBasicsConfigQueryDaoParam> param);

    /**
     * 通过项目编号和类型获取配置(匹配最新一条)
     *
     * @param projectId projectId
     */
    RcBasicsConfig queryConfig(String projectId, BasicsConfigTypeEnum type);

    /**
     * 获取配置(匹配最新一条)
     *
     * @param projectId 项目编号
     * @param systemId  系统编号
     * @param type      类型
     * @return
     */
    RcBasicsConfig queryConfig(String projectId, String systemId, BasicsConfigTypeEnum type);

    /**
     * 通过项目编号，系统编号，类型获取配置
     *
     * @param projectId
     * @param type
     * @param systemId
     * @return
     */
    List<RcBasicsConfig> queryListConfig(String projectId, BasicsConfigTypeEnum type, String systemId);

    /**
     * 时间范围配置查询（包含任一时间）
     *
     * @param projectId 项目编号
     * @param type      类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    @Deprecated
    List<RcBasicsConfig> queryListConfig(String projectId, BasicsConfigTypeEnum type, Date startTime, Date endTime);

    /**
     * 查询配置（包含任一时间）
     *
     * @param projectId
     * @param systemId
     * @param type
     * @param startTime
     * @param endTime
     * @return
     */
    List<RcBasicsConfig> queryListConfig(String projectId, String systemId, BasicsConfigTypeEnum type, Date startTime, Date endTime);

    /**
     * 配置查询(当前及未来时间生效)
     *
     * @param cgf
     * @return
     */
    List<RcBasicsConfig> queryListAfterNow(RcBasicsConfig cgf);

    /**
     * 保存配置 如果类型已经存在就更新
     *
     * @param cgf cgf
     */
    void saveOrUpdateByType(RcBasicsConfig cgf);

    /**
     * 保存配置
     *
     * @param cgf cgf
     */
    RcBasicsConfig saveOrUpdateBasicsConfig(RcBasicsConfig cgf);

    /**
     * 在独立事务中以固定状态主键 UPSERT 缓存对象模型状态配置。
     *
     * @param config 缓存对象模型状态配置
     * @return 影响行数
     * @since v7.260701.0
     */
    int upsertCacheObjectModelStatus(@NonNull RcBasicsConfig config);

    /**
     * 编辑温湿度目标忽略空间ID
     *
     * @param param          查询参数
     * @param ignoreSpaceIds 忽略空间ID
     * @return 操作条数
     * @since v5.1.0
     */
    int editHumitureIgnoreSpaceIds(@NonNull RcBasicsConfigQueryDaoParam param, @NonNull Set<String> ignoreSpaceIds);

    /**
     * 根据ID删除配置
     *
     * @param cgf
     */
    void deleteById(RcBasicsConfig cgf);

    /**
     * 数据同步
     *
     * @param task
     */
    void syn(RcBasicsConfig task);

    /**
     * 数据同步
     * 带类型的删除
     *
     * @param task
     */
    void batchSyn(List<RcBasicsConfig> task);


    List<RcBasicsConfig> querySyn(String projectId, List<BasicsConfigTypeEnum> type);

}
