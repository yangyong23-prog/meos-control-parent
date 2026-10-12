package com.persagy.apccontrol.basic.controller.perception;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.base.InterfaceUrlConst;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.LoginUserParam;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.util.json.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 需要转发的接口
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/29 15:10
 */
@Slf4j
@RestController
@Tag(name = "路由转发")
public class RunningForwardController {
    
    @Value("${inner-url.bdtp.dtp-rwd.service:}")
    private String dtpRwdServer;

    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Autowired
    private MidPermissionClientService midPermissionClientService;

    @Operation(summary = "查询运维平台 获取登录tock")
    @PostMapping("/loginUserServiceForEncrypt")
    public R<String> querySystemRunningStatistic(@RequestBody LoginUserParam userParam) {
        return R.success(this.midPermissionClientService.getTokenByLoginUser(ContextUtil.getGroupCode(), userParam.getLoginName(), userParam.getPassword()));
    }

    @Operation(summary = "2D系统图-样式数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/menu/config")
    public void query2DSystemMenueConfig(@RequestHeader(name = "group-code", required = false) String groupCode,
                                         @RequestHeader(name = "project-id", required = false) String projectId,
                                         @RequestParam(value = "menuCode", required = false) String menuCode,
                                         @RequestParam(value = "productModule", required = false) String productModule, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        Map<String, Object> param = new HashMap<>();
        param.put("menuCode", menuCode);
        param.put("productModule", productModule);
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_MENU_URL, Method.GET, null, param);
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-样式数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/area/style/config")
    public void query2DSystemStyleConfig(@RequestHeader(name = "group-code", required = false) String groupCode,
                                         @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_STYLE_URL, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-配置数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/multiple/config")
    public void query2DSystemMultipleConfig(@RequestHeader(name = "group-code", required = false) String groupCode,
                                            @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_MULTIPLE_URL, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-图标数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/icon/filter/config")
    public void query2DSystemPicIconData(@RequestHeader(name = "group-code", required = false) String groupCode,
                                         @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_ICON_URL, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-管道数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/pipeline/style/config")
    public void query2DSystemPicPipelineData(@RequestHeader(name = "group-code", required = false) String groupCode,
                                             @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_PIPELINE_URL, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-实例下拉", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/relation/graph/{systemId}")
    public void query2DSystemPicRelationData(@PathVariable(name = "systemId") String systemId, @RequestHeader(name = "group-code", required = false) String groupCode,
                                             @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String uri = StrUtil.format(InterfaceUrlConst.APM_AIM_RELATION_URL, systemId);
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, uri, Method.GET, null, null);

        try {
            JSONObject jsonObject = StrUtil.isBlank(result) ? null : JSONObject.parseObject(result);
            JSONArray data = jsonObject == null ? null : jsonObject.getJSONArray("data");
            if (data != null && data.size() != 0) {
                result = JsonUtils.toJson(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-实例下拉-新版", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/instance/select")
    public void query2DSystemPicInstanceSelectData(@RequestParam(value = "systemObjectId") String systemObjectId, @RequestParam(value = "classCode") String classCode,
                                                   @RequestParam(value = "graphTypeCode", required = false) String graphTypeCode, @RequestHeader(name = "group-code", required = false) String groupCode,
                                                   @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        Map<String, Object> systemInstanceSelectParam = InterfaceUrlConst.getSystemInstanceSelectParam(systemObjectId, classCode, graphTypeCode);
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_INSTANCE_SELECT_URL, Method.GET, null, systemInstanceSelectParam);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "[]".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-水力平衡图", tags = {VersionConstant.V5_8_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/relation/graph/moduan/{projectId}")
    public void query2DSystemPicWaterBalanceMap(@PathVariable(name = "projectId") String projectId, @RequestHeader(name = "group-code", required = false) String groupCode, HttpServletResponse httpServletResponse) throws Exception {
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        outputStream.write("[]".getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-实例数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/instance/{id}")
    public void query2DSystemPicInstanceData(@PathVariable(name = "id") String id, @RequestHeader(name = "group-code", required = false) String groupCode,
                                             @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String uri = StrUtil.format(InterfaceUrlConst.APM_AIM_INSTANCE_URL, id);
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, uri, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "{}".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "2D系统图-运行数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/system/graph/system/run/config")
    public void query2DSystemPicRunData(@RequestHeader(name = "group-code", required = false) String groupCode,
                                        @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, InterfaceUrlConst.APM_AIM_RUN_URL, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "{}".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "查询设备基础信息", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/{meoi-aim-server}/object/detail/basic/{id}")
    public void queryObjectBasicInfo(@RequestHeader(name = "group-code", required = false) String groupCode,
                                     @RequestHeader(name = "project-id", required = false) String projectId,
                                     @PathVariable(name = "id") String id,
                                     HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String uri = StrUtil.format(InterfaceUrlConst.APM_AIM_OBJECT_DETAIL_BASIC_URL, id);
        String result = this.abstractCommonServer.getMeoiCommon(groupCode, projectId, uri, Method.GET, null, null);

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(result) ? "{}".getBytes() : result.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "场景数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/scene")
    public void querySceneModelInfo(@RequestParam(value = "path") String path, @RequestParam(value = "category") String category,
                                    @RequestHeader(name = "group-code", required = false) String groupCode,
                                    @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String url = dtpRwdServer + StrUtil.format(InterfaceUrlConst.BIMWEB_SCENE_URL, groupCode, projectId, path, category);
        byte[] response = HttpUtil.createGet(url).timeout(180 * 1000).execute().bodyBytes();
        response = response == null ? new byte[0] : response;
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/octet-stream");
        httpServletResponse.setIntHeader("Content-Length", response.length);
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "管网数据", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/meshes")
    public void queryMeshesModelInfo(@RequestParam(value = "path") String path, @RequestParam(value = "category") String category,
                                     @RequestParam(value = "compressed", required = false) Boolean compressed,
                                     @RequestHeader(name = "group-code", required = false) String groupCode,
                                     @RequestHeader(name = "project-id", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();

        String url = dtpRwdServer + StrUtil.format(InterfaceUrlConst.BIMWEB_MESHES_URL, groupCode, projectId, path, category, BooleanUtil.isTrue(compressed));
        byte[] response = HttpUtil.createGet(url).execute().bodyBytes();
        response = response == null ? new byte[0] : response;
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/octet-stream");
        httpServletResponse.setIntHeader("Content-Length", response.length);
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "版本信息", tags = {VersionConstant.V5_6_0})
    @GetMapping(value = "/version")
    public void queryVersionModelInfo(@RequestParam(value = "project", required = false) String project,
                                      @RequestParam(value = "t", required = false) String t,
                                      @RequestHeader(name = "groupCode", required = false) String groupCode,
                                      @RequestHeader(name = "projectId", required = false) String projectId, HttpServletResponse httpServletResponse) throws Exception {
        groupCode = StrUtil.isNotBlank(groupCode) ? groupCode : ContextUtil.getGroupCode();
        projectId = StrUtil.isNotBlank(projectId) ? projectId : ContextUtil.getProjectId();
        String url = dtpRwdServer + StrUtil.format(InterfaceUrlConst.BIMWEB_VERSION_URL, groupCode, projectId, project, t);
        String response = HttpUtil.createGet(url).timeout(180 * 1000).execute().body();
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", "application/json");
        outputStream.write(StrUtil.isBlank(response) ? "[]".getBytes() : response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
