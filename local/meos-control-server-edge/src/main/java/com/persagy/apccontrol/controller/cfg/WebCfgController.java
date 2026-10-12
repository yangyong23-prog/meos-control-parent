package com.persagy.apccontrol.controller.cfg;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.common.outside.feign.RcSaasClient;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.List;


/**
 * 静态业务类型 接口
 *
 * @author yuyao
 */
@RestController
@RequestMapping("/webCfg")
@Slf4j
@Validated
@Tag(name = "静态业务类型 接口")
public class WebCfgController {

    @Value("${paas.websocketPort:8849}")
    private String websocketPort;

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private RcSaasClient rcSaasClient;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RcBasicsConfigService rcBasicsConfigService;


    /**
     * @Description 获取sock端口
     * @Author yuyao
     */
    @Operation(summary = "获取sock端口")
    @GetMapping("getWebsocketPort")
    public R<String> getWebsocketPort() {
        return R.success(websocketPort);
    }


    /**
     * @Description 缓存图片接口结果
     * @Author yuyao
     */
    @Operation(summary = "缓存图片文件接口")
    @GetMapping("imgUrlCache")
    public void urlCache(String url, HttpServletResponse response) throws IOException {
        byte[] content = null;
        String s = redisCacheUtil.get(url);
        if (s != null) {
            content = Base64.getDecoder().decode(s);
        }
        if (content == null) {
            return;
        }
        ServletOutputStream outputStream = response.getOutputStream();
        response.setContentType("image/jpeg;charset=utf-8");
        outputStream.write(content);
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "同步云端的基础配置表", tags = {VersionConstant.V5_240602_0})
    @GetMapping("/flushBasicConfig")
    public R<List<RcBasicsConfig>> flushBasicConfig() throws Exception {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());

        // 1.获取云端的配置数据
        R<List<RcBasicsConfig>> basicsConfigResponse = this.rcSaasClient.syncBasicsConfig(projectCfg.getGroupCode());
        if (basicsConfigResponse == null || !basicsConfigResponse.getIsSuccess() || CollectionUtil.isEmpty(basicsConfigResponse.getData())) {
            return R.success(Lists.newArrayList());
        }

        // 2.云端数据写入到边缘端，先清空，在保存
        List<RcBasicsConfig> basicsConfigs = basicsConfigResponse.getData();
        this.rcBasicsConfigService.remove(new LambdaQueryWrapper<>());
        this.rcBasicsConfigService.saveBatch(basicsConfigs);

        return R.success(basicsConfigs);
    }

}
