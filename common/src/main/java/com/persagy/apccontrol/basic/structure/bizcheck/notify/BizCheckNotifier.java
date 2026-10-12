package com.persagy.apccontrol.basic.structure.bizcheck.notify;

/**
 * 业务可用性检查异常通知接口
 * <p>
 * 云端（saas）提供实现，边端无实现 → {@code getBeansOfType} 返回空 Map，自动跳过。
 *
 * @since 7.260601.0
 */
public interface BizCheckNotifier {

    /**
     * 当检查结果存在异常时发送通知
     *
     * @param bizCode 业务条线编码
     */
    void notifyIfAbnormal(String bizCode);
}
