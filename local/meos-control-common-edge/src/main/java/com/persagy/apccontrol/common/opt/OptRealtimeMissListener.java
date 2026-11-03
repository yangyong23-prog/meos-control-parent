package com.persagy.apccontrol.common.opt;

/**
 * opt 分支 B3：实时值读路径发现“缓存没有、Twin 有有效值”的点时回调，用于自动补订阅。
 * 实现方必须快速返回、不抛异常。
 */
public interface OptRealtimeMissListener {

    void onTwinFill(String classCode, String infoCode);
}
