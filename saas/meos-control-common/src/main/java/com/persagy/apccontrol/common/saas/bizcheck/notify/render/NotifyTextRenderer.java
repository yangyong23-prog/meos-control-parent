package com.persagy.apccontrol.common.saas.bizcheck.notify.render;

import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;

import java.util.List;

/**
 * 检查结果文本渲染器接口
 * <p>
 * 每个实现对应一种 {@link BizCheckStrategyTypeEnum}，输出一行或多行文本。
 *
 * @since 7.260601.0
 */
public interface NotifyTextRenderer {

    /**
     * 本渲染器支持的检查项类型
     */
    BizCheckStrategyTypeEnum supports();

    /**
     * 将检查结果渲染为文本行列表
     *
     * @param vo 检查结果 VO
     * @return 渲染后的文本行（每行一个异常细项）
     */
    List<String> render(BizCheckResultVO vo);
}
