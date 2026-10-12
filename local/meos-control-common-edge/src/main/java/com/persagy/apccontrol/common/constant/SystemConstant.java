package com.persagy.apccontrol.common.constant;

/**
 * @author shaohongbo
 */
public interface SystemConstant {

	/**
	 * 5分钟时间正则
	 */
	public static final String FIVE_MINUTE_TIMEP_ATTERN = "^\\d{10}[0-5]{1}[05]{1}$";
    public interface CustomInfoCode {
        //群控计算运行状态
         String RC_RUN_STATUS = "rcRunStatus";

         //设备是否锁定，0否1是
         String IS_FORCE = "isForce";

        //设备是否认为干预,0否1是
        String IS_INTERVENE = "isIntervene";
    }
}
