package com.persagy.apccontrol.basic.service.kitchen;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;

import java.util.Map;
import java.util.Set;

public interface KitchenRangeHoodService {


    static final String ACKLSF=LocaleMessageUtil.getMessage("key_cbeed7ac1b924fcebc56d6677f774a25");
    static final String ACATFU_ACVTSF = LocaleMessageUtil.getMessage("key_001bc3883d7f4ad2a8166d845a7232ae");
    static final String ACKLAP=LocaleMessageUtil.getMessage("key_1e5b9789d1124cf888bd6c56090634db");

    //聚合接口返回的排油烟机服务的空间
    final static String serviceSpaceKey = "MechForArch-Eq2Sp";

    /**
     * 查询厨房油烟机设备的运行状态统计
     *
     * @param param
     */

    Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(Map<String, Set<String>> param);

    /**
     * 对油烟机静态聚合数据处理为map集合
     *
     * @param groupCode
     * @param projectId
     */
    Map<String, Set<String>> handlerStaticAggregateToMap(String groupCode, String projectId);

}
