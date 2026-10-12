package com.persagy.apccontrol.basic.model.base;

import java.util.HashMap;
import java.util.Map;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/11 11:04
 */
public class InterfaceUrlConst {

    public static final String INFO_CODE_LIST = "/infoCode/listAll";
    public static final String MEOI_AIM_SERVER_COMMON = "/meoi-aim-server/common";
    public static final String DTP_RWD_SERVER_COMMON = "/dtp-rwd-server/common";
    public static final String VALUE_SELECT_TEMP_CARBON_RATE_BY_IOT = "/valueSelection/currentTempAndCarbonRate";
    public static final String SHELL_CONFIG = "/shellConfig/queryByInfoCode";

    public static final String BATCH_SAVE_CALCULATE_RESULT_LIST = "/calculate/batchSaveCalculateResultList";
    public static final String BATCH_SAVE_CALCULATE_RESULT_DETAIL_LIST = "/calculate/batchSaveCalculateResultDetailList";

    /************************  2D system pic ************************/
    // 交互配置
    public static final String APM_AIM_INTERACTIVE_URL = "/system/graph/interactive/config";
    // 菜单数据
    public static final String APM_AIM_MENU_URL = "/system/graph/menu/config";
    public static final String APM_AIM_RUN_URL = "/system/graph/system/run/config";
    public static final String APM_AIM_STYLE_URL = "/system/graph/area/style/config";
    public static final String APM_AIM_MULTIPLE_URL = "/system/graph/multiple/config";
    public static final String APM_AIM_ICON_URL = "/system/graph/icon/filter/config";
    public static final String APM_AIM_PIPELINE_URL = "/system/graph/pipeline/style/config";
    public static final String APM_AIM_RELATION_URL = "/system/graph/relation/graph/{}";
    public static final String APM_AIM_INSTANCE_SELECT_URL = "/system/graph/instance/select";
    public static final String APM_AIM_OBJECT_DETAIL_BASIC_URL = "/object/detail/basic/{}";
    /**
     * 查询系统图实例
     */
    public static final String APM_AIM_INSTANCE_URL = "/system/graph/instance/{}";
    /**
     * 查询系统图模型
     */
    public static final String APM_AIM_GRAPH_MODEL_URL = "/system/graph/model/{}";
    /**
     * 是否存在系统图
     */
    public static final String APM_AIM_GRAPH_HAS_URL = "/system/graph/has/{}";
    /**
     * 查询系统图图例
     */
    public static final String APM_AIM_GRAPH_LEGEND_URL = "/graph/legend/query/all";
    /**
     * 查询系统图图标
     */
    public static final String APM_AIM_GRAPH_ICON_URL = "/graph/icon/query/all";
    /**
     * 查询iot信息，post接口
     */
    public static final String APM_AIM_EQUIPMENT_IOT_URL = "/risk/event/equipment/iot";

    //设置需要生成系统图的参数 先保存参数 再进行系统图生成获取
    String APM_AIM_PARAMETER_URL = "/system/graph/save/parameter";

    /************************ 3D unity model ************************/
    /**
     * /api/WebMeshProvider/scene?path=Pj9909990006/Fl990999000221982bbdf7dd4e22b21a475ab0007a17&category=CFCSCL
     */
    public static final String BIMWEB_SCENE_URL = "/3d/object/scene?groupCode={}&projectId={}&path={}&category={}";

    /**
     * /api/WebMeshProvider/meshes?path=Pj9909990006/Fl990999000221982bbdf7dd4e22b21a475ab0007a17&category=CFCSCL&compressed=false
     */
    public static final String BIMWEB_MESHES_URL = "/3d/object/meshes?groupCode={}&projectId={}&path={}&category={}&compressed={}";

    public static final String BIMWEB_VERSION_URL = "/3d/object/version?groupCode={}&projectId={}&project={}&t={}";

    /**
     * 边调云巡检信息
     */
    public static final String TASK_INFO_MAP = "/edge/task/mapTaskInfo";

    /**
     * 边调云强电信息卡
     */
    public static final String HOME_INFO_CARD = "/edge/home/infoCard";

    /**
     * 获取系统图下拉的GET请求参数
     *
     * @param systemObjectId
     * @param classCode
     * @param graphTypeCode
     * @return
     */
    public static Map<String, Object> getSystemInstanceSelectParam(String systemObjectId, String classCode, String graphTypeCode) {
        Map<String, Object> param = new HashMap<>();
        param.put("systemObjectId", systemObjectId);
        param.put("classCode", classCode);
        param.put("graphTypeCode", graphTypeCode);
        //param.put("businessDomain", "CONTROL_COOL");
        return param;
    }

}
