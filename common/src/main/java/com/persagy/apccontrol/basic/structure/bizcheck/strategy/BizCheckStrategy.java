package com.persagy.apccontrol.basic.structure.bizcheck.strategy;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.bizcheck.context.BizCheckContext;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-27 17:55
 **/
public interface BizCheckStrategy<T> extends LogComponent {
    /**
     * 获取检查项编码
     * @return 检查项编码
     */
    BizCheckStrategyTypeEnum getStrategyType();

    /**
     * 执行检查
     * @param context 检查上下文
     * @return 检查结果
     */
    BizCheckItemDTO<T> execute(BizCheckContext context) throws ExecutionException;
}
