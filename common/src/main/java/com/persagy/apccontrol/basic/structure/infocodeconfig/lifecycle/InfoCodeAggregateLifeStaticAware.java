package com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle;

/**
 * 信息点聚合根生命周期静态回调
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
public class InfoCodeAggregateLifeStaticAware implements InfoCodeAggregateLifeAware {

    public static final InfoCodeAggregateLifeAware INSTANCE = new InfoCodeAggregateLifeStaticAware();

    @Override
    public int getOrder() {
        return 9999;
    }
}
