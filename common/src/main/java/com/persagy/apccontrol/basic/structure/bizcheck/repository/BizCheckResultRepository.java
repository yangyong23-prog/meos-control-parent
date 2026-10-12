package com.persagy.apccontrol.basic.structure.bizcheck.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.entity.BizCheckResult;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 业务检查结果仓储接口
 *
 * @author wxf
 * @date 2026-02-27
 */
public interface BizCheckResultRepository extends IService<BizCheckResult> {

    /**
     *根据业务条线编码获取最新的检查结果
     *
     * @param bizCode 业务条线编码
     * @return检查结果
     */
    List<BizCheckResult> listByBizCode(@NonNull RunningMonitorTypeEnum bizCode);

    boolean deleteByBizCode(RunningMonitorTypeEnum bizCode);
}