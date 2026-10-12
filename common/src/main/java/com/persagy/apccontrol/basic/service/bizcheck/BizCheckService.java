package com.persagy.apccontrol.basic.service.bizcheck;

import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckConfigParam;
import com.persagy.apccontrol.basic.model.param.bizcheck.BizCheckQueryParam;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckConfigVO;
import com.persagy.apccontrol.basic.model.vo.bizcheck.BizCheckResultVO;

import java.util.List;

public interface BizCheckService {
    /**
     * 保存配置
     */
    Boolean saveConfig(BizCheckConfigParam param);

    /**
     * 获取配置
     */
    BizCheckConfigVO getConfig(BizCheckQueryParam param);

    /**
     * 获取检查结果
     */
    List<BizCheckResultVO> getCheckResults(BizCheckQueryParam param);

    /**
     * 手动触发自动检查
     * 
     * @return 检查任务ID
     */
    void triggerAutoCheck();

    /**
     * 手动触发检查
     * 
     * @param param 检查查询参数
     * @return 检查任务ID
     */
    boolean triggerCheck(BizCheckQueryParam param);

}
