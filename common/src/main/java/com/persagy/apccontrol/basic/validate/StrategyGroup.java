package com.persagy.apccontrol.basic.validate;

/**
 * 策略组
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public interface StrategyGroup {

    /**
     * 固定时间策略
     *
     * @author JiYinchuan
     * @since v5.6.0
     */
    interface FixedStrategy {

        /**
         * 检查固定时间策略
         *
         * @author JiYinchuan
         * @since v5.6.0
         */
        interface Check {

            /**
             * 检查固定时间策略时间范围
             *
             * @author JiYinchuan
             * @since v5.6.0
             */
            interface TimeRange {

            }
        }
    }

    /**
     * 临时时间策略
     *
     * @author JiYinchuan
     * @since v5.6.0
     */
    interface TempStrategy {

    }
}
