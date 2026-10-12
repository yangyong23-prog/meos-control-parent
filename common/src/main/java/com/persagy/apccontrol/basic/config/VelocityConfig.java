package com.persagy.apccontrol.basic.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.io.StringWriter;
import java.util.Map;

public class VelocityConfig {
    private static VelocityEngine velocityEngine;
    static {
        //初始化模板引擎
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
        velocityEngine.addProperty("string.resource.loader.repository.static", "false");
        velocityEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        velocityEngine.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF8");
        velocityEngine.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF8");
        velocityEngine.init();
    }
    /**
     * 根据模板数据获取播报内容
     *
     * @param msgDto
     * @param templateMsg
     * @return
     * @throws JsonProcessingException
     */
    public static <T> String getMessageByTemplate(T msgDto, String templateMsg) throws JsonProcessingException {
        //模板
        if(StrUtil.isBlank(templateMsg)) {
            return templateMsg;
        }
        StringResourceRepository repo = (StringResourceRepository) velocityEngine
                .getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.putStringResource(templateMsg, templateMsg);
        Template template = velocityEngine.getTemplate(templateMsg);
        ObjectMapper objectMapper = CommonConst.JSON_MAPPER;
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        Map<String, Object> map = objectMapper.readValue(JSON.toJSONString(msgDto), new TypeReference<Map<String, Object>>() {});
        VelocityContext context = new VelocityContext(map);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        String content = writer.toString();
        return content;
    }
}
