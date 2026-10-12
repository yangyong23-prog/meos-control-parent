package com.persagy.apccontrol.basic.structure.bizcheck.service;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckConfig;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务检查领域服务接口
 *
 * @author: wxf
 * @create: 2026-02-26 14:26
 **/
public interface BizCheckDomainService extends BizComponent<BizCheckDomainService> {

    /**
     * 保存业务可用性检查配置
     *
     * @param config 配置参数
     * @return 保存结果
     */
    Boolean saveConfig(@Valid BizCheckConfig config);

    /**
     * 获取业务配置
     *
     * @param bizCode 业务编码
     * @return 业务配置
     */
    BizCheckConfig getConfig(@NotNull RunningMonitorTypeEnum bizCode);

    // ==================== BizCheckResult相关方法 ====================

    /**
     * 保存检查结果
     *
     * @param result 检查结果实体
     * @return 保存结果
     */
    Boolean saveResult(@Valid BizCheckResult result);


    /**
     * 自动检查
     */
    void autoCheck();

    /**
     * 开始检查任务
     *
     * @param bizCheckConfig   配置
     * @param checkType 检查类型
     * @return 更新结果
     */
    Boolean startCheck(@NotNull BizCheckConfig bizCheckConfig, @NotNull BizCheckTypeEnum checkType);

    /**
     *完成检查任务
     *
     * @param result    检查结果
     * @return 更新结果
     */
    Boolean completeCheck(@NotNull BizCheckResult result);

    /**
     *根业务条线编码获取最新的检查结果
     *
     * @param bizCode 业务条线编码
     * @return检查结果
     */
    List<BizCheckResult> getResults(@NotBlank RunningMonitorTypeEnum bizCode);
}