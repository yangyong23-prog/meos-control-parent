package com.persagy.apccontrol.basic.opt;

/**
 * opt 分支：边缘盒子标准架构已移除的外部服务，对应调用按开关跳过（2026-11-17 Blake 决定）。
 * <p>
 * 开关都走 {@link OptSwitch}，默认关闭；关闭时行为与改造前完全一致，只在边缘盒子的 Nacos 打开。
 * 说明见 docs/opt/edge-scope.md。
 *
 * @author opt
 */
public final class OptEdgeScope {

    /**
     * yiheng 风险事件链路：风险事件/实例树查询返回空、MQTT event-change 与 danger-event 不处理；
     * Kafka yiheng-cloud-business-topic 监听不启动（需重启）。
     */
    public static final String NO_YIHENG = "edgeNoYiheng";

    /** energy-ddp：系统运行看板效率不再查后评估配置（等级区间为空，效率值照常计算） */
    public static final String NO_ENERGY_DDP = "edgeNoEnergyDdp";

    /** ai-au：冷机策略“是否支持 AI”直接返回 false，不再调 ai-au */
    public static final String NO_AI_AU = "edgeNoAiAu";

    private OptEdgeScope() {
    }

    public static boolean noYiheng() {
        return OptSwitch.on(NO_YIHENG);
    }

    public static boolean noEnergyDdp() {
        return OptSwitch.on(NO_ENERGY_DDP);
    }

    public static boolean noAiAu() {
        return OptSwitch.on(NO_AI_AU);
    }
}
