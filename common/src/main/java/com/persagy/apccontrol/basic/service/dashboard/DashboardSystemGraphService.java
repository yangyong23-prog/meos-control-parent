package com.persagy.apccontrol.basic.service.dashboard;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.dashboard.QuerySystemCardParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.SystemCardVO;

import java.util.List;

public interface DashboardSystemGraphService extends LogComponent {

    /**
     * 列表系统通过第一位菜单代码
     *
     * @param menuCode 菜单代码
     * @param param
     * @return {@code List<SystemVO> }
     * @since 7.250501.0
     */
    List<SystemCardVO> listSystemByFirstMenuCode(String menuCode, QuerySystemCardParam param);
}
