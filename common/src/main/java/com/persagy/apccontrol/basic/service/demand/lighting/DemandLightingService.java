package com.persagy.apccontrol.basic.service.demand.lighting;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandAddParam;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.FeedbackConsistencyConflictVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 照明需求服务
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
public interface DemandLightingService {

    /**
     * 迁移场景数据
     *
     * @param templateType 模版类型, 为空时迁移所有
     * @return 是否成功
     * @since 7.250801.0
     */
    boolean migrateSceneData(@Nullable DemandTemplateTypeEnum templateType);

    /**
     * 迁移配置数据
     *
     * @param templateType 模版类型, 为空时迁移所有
     * @param forceMigrate 是否强制迁移, 强制迁移时将先删除照明所有配置
     * @return 是否成功
     * @since 7.250801.0
     */
    boolean migrateConfigData(@Nullable DemandTemplateTypeEnum templateType, @Nullable Boolean forceMigrate);

    /**
     * 通过运行监控类型计算照明配置结果
     *
     * @param type    运行监控二级类型
     * @param groupId 编组ID
     * @since 7.260801.0
     */
    void resultComputeDemandLightingResult(RunningMonitorSecondTypeEnum type, Long groupId);

    /**
     * 反馈调节条件多空间一致性校验（返回冲突详情）
     *
     * @param param 需求配置新增参数（与保存接口入参一致）
     * @return 冲突列表，空列表表示无冲突
     * @since 7.260801.0
     */
    List<FeedbackConsistencyConflictVO> feedbackConsistencyCheck(
            @NonNull @Valid DemandAddParam param);

}