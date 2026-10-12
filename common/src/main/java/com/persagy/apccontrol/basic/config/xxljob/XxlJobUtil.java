package com.persagy.apccontrol.basic.config.xxljob;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.framework.common.exception.BusinessException;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;


/**
 * xxl-job工具类，可以执行xxl-job的各种操作，如：添加任务，开始任务，停止任务，移除任务，登录等
 *
 * @author yuyao
 * @date 2022/9/11 15:58
 */
@Slf4j
@Component
@Data
public class XxlJobUtil {

    /**
     * 登录
     */
    private static final String XXLJOB_LOGIN = "/login";
    /**
     * 执行器列表
     */
    private static final String XXLJOB_JOBGROUP_PAGELIST = "/jobgroup/pageList";
    /**
     * 停止任务
     */
    private static final String XXLJOB_STOP = "/jobinfo/stop";
    /**
     * 添加任务
     */
    private static final String XXLJOB_ADD = "/jobinfo/add";
    /**
     * 执行任务
     */
    private static final String XXLJOB_START = "/jobinfo/start";
    /**
     * 移除任务
     */
    private static final String XXLJOB_REMOVE = "/jobinfo/remove";
    /**
     * 更新任务
     */
    private static final String XXLJOB_UPDATE = "/jobinfo/update";

    /**
     * 执行任务
     */
    private static final String XXLJOB_TRIGGER = "/jobinfo/trigger";
    /**
     * 任务列表
     */
    private static final String XXLJON_JOBINFO_PAGELIST = "/jobinfo/pageList";
    /**
     * 添加执行器
     */
    private static final String XXLJOB_JOBGROUP_SAVE = "/jobgroup/save";

    private String cookie;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private XxlJobConfig xxlJobConfig;

    /**
     * 新增job
     */
    public JSONObject addJob(LinkedMultiValueMap<String, Object> map) {
        return doPost(XXLJOB_ADD, map);
    }

    /**
     * 新增执行器
     */
    public JSONObject addGroup() {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("appname", xxlJobConfig.getAppname());
        map.add("title", xxlJobConfig.getTitle());
        map.add("addressType", 0);
        map.add("addressList", "");
        return doPost(XXLJOB_JOBGROUP_SAVE, map);
    }

    /**
     * 更新job
     */
    public JSONObject updateJob(LinkedMultiValueMap<String, Object> map) {
        return doPost(XXLJOB_UPDATE, map);
    }


    /**
     * 删除任务
     */
    public JSONObject removeJob(Integer id) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("id", id);
        return doPost(XXLJOB_REMOVE, map);
    }

    /**
     * 开始任务
     */
    public JSONObject startJob(Integer id) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("id", id);
        return doPost(XXLJOB_START, map);
    }

    /**
     * 停止任务
     */
    public JSONObject stopJob(Integer id) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("id", id);
        return doPost(XXLJOB_STOP, map);
    }

    /**
     * 执行任务
     *
     * @param id 身份证件
     * @return {@link JSONObject }
     */
    public JSONObject triggerJob(Integer id, String executorParam) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("id", id);
        map.add("executorParam", executorParam);
        return doPost(XXLJOB_TRIGGER, map);
    }

    /**
     * 请求服务
     */
    public JSONObject doPost(String path, MultiValueMap<String, Object> map) {
        if (!StringUtils.hasText(cookie)) {
            login();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("cookie", cookie);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        log.info("path:{};param:{}", path, JSONObject.toJSONString(map));
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);
        ResponseEntity<String> entity = restTemplate.postForEntity(xxlJobConfig.getAdminAddresses() + path, param, String.class);
        if (entity.getStatusCode() != HttpStatus.OK) {
            throw new BusinessException("访问xxlJob失败", "访问xxlJob失败");
        }
        return JSONObject.parseObject(entity.getBody());
    }


    /**
     * 登录xxl-job获取cookie
     */
    public String login() {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("userName", xxlJobConfig.getUsername());
        map.add("password", xxlJobConfig.getPassword());
        map.add("ifRemember", "on");
        //构造实体对象
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, null);
        ResponseEntity<String> entity = restTemplate.postForEntity(xxlJobConfig.getAdminAddresses() + XXLJOB_LOGIN, param, String.class);
        List<String> cookies = entity.getHeaders().get("Set-Cookie");
        StringBuilder tmpcookies = new StringBuilder();
        assert cookies != null;
        for (String c : cookies) {
            tmpcookies.append(c).append(";");
        }
        cookie = tmpcookies.toString();
        return cookie;
    }

    /**
     * 根据appname获取id
     */
    public String getJobGroupIdByAppName() {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("appname", xxlJobConfig.getAppname());
        map.add("start", "0");
        map.add("length", Integer.MAX_VALUE);
        JSONObject body = doPost(XXLJOB_JOBGROUP_PAGELIST, map);
        JSONArray data = body.getJSONArray("data");
        for (Object datum : data) {
            JSONObject jo = (JSONObject) datum;
            if (xxlJobConfig.getAppname().equals(jo.getString("appname"))) {
                return jo.getString("id");
            }
        }
        return null;
    }

    /**
     * 根据appname获取id列表
     */
    public List<Integer> getTaskIdsByAppName() {
        String jobGroup = getJobGroupIdByAppName();
        if (!StringUtils.hasText(jobGroup)) {
            throw new BusinessException("获取执行器失败", "获取执行器失败");
        }
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", jobGroup);
        params.add("start", 0);
        params.add("length", 100);
        params.add("triggerStatus", -1);
        JSONObject resultJson = doPost(XXLJON_JOBINFO_PAGELIST, params);
        JSONArray data = resultJson.getJSONArray("data");
        List<Integer> taskIds = new ArrayList<>();
        for (Object datum : data) {
            JSONObject jo = (JSONObject) datum;
            int taskId = jo.getInteger("id");
            taskIds.add(taskId);
        }
        return taskIds;
    }

    /**
     * 根据appname获取id列表
     */
    public Map<String, Integer> getTaskByAppName() {
        String jobGroup = getJobGroupIdByAppName();
        if (!StringUtils.hasText(jobGroup)) {
            throw new BusinessException("获取执行器失败", "获取执行器失败");
        }
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", jobGroup);
        params.add("start", 0);
        params.add("length", 100);
        params.add("triggerStatus", -1);
        JSONObject resultJson = doPost(XXLJON_JOBINFO_PAGELIST, params);
        JSONArray data = resultJson.getJSONArray("data");
        Map<String, Integer> taskIds = new HashMap<>();
        for (Object datum : data) {
            JSONObject jo = (JSONObject) datum;
            int taskId = jo.getInteger("id");
            String name = jo.getString("executorHandler");
            taskIds.put(name, taskId);
        }
        return taskIds;
    }

    /**
     * 根据任务描述获取任务，只返回第一个
     *
     * @param executorHandler
     * @return
     * @throws Exception
     */
    @SneakyThrows
    public JSONObject getJobInfoByJobExecutorHandler(String executorHandler) {
        String jobGroup = getJobGroupIdByAppName();
        if (!StringUtils.hasText(jobGroup)) {
            throw new BusinessException("获取执行器失败", "获取执行器失败");
        }
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", jobGroup);
        params.add("executorHandler", executorHandler);
        params.add("start", 0);
        params.add("length", 10);
        params.add("triggerStatus", -1);
        JSONObject resultJson = doPost(XXLJON_JOBINFO_PAGELIST, params);
        JSONArray data = resultJson.getJSONArray("data");
        if (!CollectionUtils.isEmpty(data)) {
            return data.getJSONObject(0);
        }
        return null;
    }

    @SneakyThrows
    public JSONObject getJobInfoByJobDesc(String desc) {
        String jobGroup = getJobGroupIdByAppName();
        if (!StringUtils.hasText(jobGroup)) {
            throw new BusinessException("获取执行器失败", "获取执行器失败");
        }
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("jobGroup", jobGroup);
        params.add("jobDesc", desc);
        params.add("start", 0);
        params.add("length", 10);
        params.add("triggerStatus", -1);
        JSONObject resultJson = doPost(XXLJON_JOBINFO_PAGELIST, params);
        JSONArray data = resultJson.getJSONArray("data");
        if (!CollectionUtils.isEmpty(data)) {
            List<JSONObject> javaList = data.toJavaList(JSONObject.class);
            Optional<JSONObject> jobInfos = javaList.stream().filter(e -> desc.equals(e.getString("jobDesc"))).findFirst();
            if (jobInfos.isPresent()) {
                return jobInfos.get();
            }
        }
        return null;
    }

    /**
     * 通过jobdesc获取任务id
     *
     * @param desc desc
     * @return {@link Integer }
     */
    public Integer getJobInfoIdByJobDesc(String desc) {
        JSONObject jobInfoByJobDesc = getJobInfoByJobDesc(desc);
        if (jobInfoByJobDesc == null || jobInfoByJobDesc.get("id") == null) {
            return null;
        }
        return jobInfoByJobDesc.getInteger("id");
    }
}