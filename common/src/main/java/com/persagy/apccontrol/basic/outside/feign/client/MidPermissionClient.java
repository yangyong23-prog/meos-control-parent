package com.persagy.apccontrol.basic.outside.feign.client;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.GroupQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.ProjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.ProjectValidQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.midpermission.GroupResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.midpermission.ProjectResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 运维系统
 *
 * @author yuyao
 */
@FeignClient(name = "mid-permission-server", url = "${inner-url.permission.mid-permission.service:}")
public interface MidPermissionClient {

    String JSON_STRING = "jsonString";

    // ------------------------- 集团 -------------------------

    /**
     * 查询集团列表
     *
     * @param param 查询参数
     * @return 集团列表
     * @since v6.0.0
     */
    @PostMapping("/rwd/groupAndSchema/admin/queryGroupList")
    CustomerResponse<List<GroupResponse>> listGroup(@RequestBody GroupQueryParam param);

    // ------------------------- 项目 -------------------------

    /**
     * 查询项目列表
     *
     * @param groupCode 集团编码
     * @param param     查询参数
     * @return 项目列表
     * @since v5.0.0
     */
    @PostMapping("/Spring/MVC/entrance/unifier/getProjectByIdService")
    CustomerResponse<List<ProjectResponse>> getProject(
            @RequestHeader String groupCode,
            @RequestParam(JSON_STRING) ProjectQueryParam param);

    /**
     * 查询有效项目列表
     *
     * @param groupCode 集团编码
     * @param param     查询参数
     * @return 项目列表
     * @since v6.0.0
     */
    @PostMapping("/Spring/MVC/entrance/unifier/queryInUseProjectsByIds")
    CustomerResponse<List<ProjectResponse>> listProjectOfValid(
            @RequestHeader String groupCode,
            @RequestParam(JSON_STRING) ProjectValidQueryParam param);

    // ------------------------- 其他 -------------------------

    /**
     * 查询项目列表
     *
     * @param groupCode 集团编码
     * @param param     查询参数
     * @return 项目列表
     * @since v5.0.0
     */
    @PostMapping("/Spring/MVC/entrance/unifier/loginUserServiceForEncrypt")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    CustomerResponse<List<JSONObject>> loginUserServiceForEncrypt(@RequestHeader String groupCode, @RequestParam(JSON_STRING) String param);

    /**
     * Description:根据项目id查询项目信息
     *
     * @param jsonString
     * @return String
     * @author shaohongbo
     * @since 2021年02月6日: 下午2:30:19 Update By shaohongbo 2021年02月6日: 下午2:30:19
     * @deprecated see {@link #getProject(String, ProjectQueryParam)}
     */
    @Deprecated
    @PostMapping("/Spring/MVC/entrance/unifier/getProjectByIdService")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    String saasGetProjectByIdService(@RequestParam("jsonString") String jsonString);

    /**
     * Description:根据项目id查询项目信息
     *
     * @param jsonString
     * @return String
     * @author shaohongbo
     * @since 2021年02月6日: 下午2:30:19 Update By shaohongbo 2021年02月6日: 下午2:30:19
     * @deprecated see {@link #listProjectOfValid(String, ProjectValidQueryParam)}
     */
    @Deprecated
    @PostMapping("/Spring/MVC/entrance/unifier/queryInUseProjectsByIds")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    String queryInUseProjectsByIds(@RequestParam("jsonString") String jsonString);

    @Deprecated
    @PostMapping("/rwd/groupAndSchema/admin/queryGroupList")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    String queryGroupList(JSONObject param);


    @PostMapping("/user/synUserByProjectId")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    String synUserByProjectId(JSONObject param);

    // 新的运维平台的权限校验
    @PostMapping("/Spring/MVC/entrance/unifier/auth")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    CustomerResponse verifyToken(@RequestBody String token);


    // 运维平台根据userId查询账号信息
    @PostMapping("/Spring/MVC/entrance/unifier/getUserInfoListByUserIds")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    CustomerResponse<List<JSONObject>> getUserInfoListByUserIds(@RequestParam("jsonString") String jsonString);


}
