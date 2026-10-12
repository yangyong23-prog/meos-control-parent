package com.persagy.apccontrol.basic.mapper.demand;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustom;
import com.persagy.apccontrol.basic.model.entity.demand.airconditioning.DemandConfigAirconditioningCustomTimeGroup;
import com.persagy.apccontrol.basic.resource.demand.param.airconditioning.DemandConfigAirconditioningCustomQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 需求配置-空调需求-价值选择Mapper
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper
@Repository
public interface DemandConfigAirconditioningCustomMapper extends BaseMapper<DemandConfigAirconditioningCustom> {

    /**
     * 查询需求配置
     *
     * @param param 查询参数
     * @return 需求配置
     * @since v6.0.0
     */
    DemandConfigAirconditioningCustom get(@NonNull @Param("param") DemandConfigAirconditioningCustomQueryDaoParam param);

    /**
     * 查询需求配置
     *
     * @param param 查询参数
     * @return 需求配置
     * @since v6.0.0
     */
    List<DemandConfigAirconditioningCustom> list(@NonNull @Param("param") DemandConfigAirconditioningCustomQueryDaoParam param);

    /**
     * 按价值选择查询快照
     *
     * @return {@link List }<{@link JSONObject }>
     */
    List<DemandConfigAirconditioningCustomTimeGroup> queryFixDataList();
    
    
    /**
     * 查询满时间范围内的快照信息
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return {@link List }<{@link JSONObject }>
     */
    List<DemandConfigAirconditioningCustomTimeGroup> queryTimeScopeFixDataList(@NonNull @Param("projectId") String projectId,
                                                                               @NonNull @Param("startDate") LocalDate startDate,
                                                                               @NonNull @Param("endDate") LocalDate endDate
    );
    
    /**
     * 按需求模板id查询快照
     *
     * @return {@link List }<{@link JSONObject }>
     */
    List<DemandConfigAirconditioningCustomTimeGroup> querySnapshotByDemandTemplateId();
    void updateDemandConfigAirconditioningCustomTimeGroup(DemandConfigAirconditioningCustomTimeGroup data);

    /**
     * 删除所有rel_template_id为价值选择的数据
     */
    void deleteSnapshotByAllValueSelect();
}