package com.persagy.apccontrol.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeGroupImportContext;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import com.persagy.framework.common.entity.R;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 信息点配置导入服务
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeImportService {

    /**
     * 解析前端配置
     *
     * @param file 前端配置表
     * @return 响应结果
     * @since 7.250602.0
     */
    R<ObjectNode> parseWebConfig(@NonNull MultipartFile file);

    /**
     * 导入分组表
     *
     * <p>接口操作公共库，不使用集团或项目上下文。</p>
     *
     * @param file                   分组表
     * @param checkGroupCodeInConfig 是否校验配置表中的分组编码
     * @return 响应结果
     * @since 5.240602.0
     */
    R<InfoCodeGroupImportContext> importGroupSheet(
            @NonNull MultipartFile file, boolean checkGroupCodeInConfig);

    /**
     * 导入配置表
     *
     * <p>公共配置提交后，会按旧模板 ID 遍历全部集团和项目维护缓存。</p>
     *
     * @param files                 配置表
     * @param skipNotDefineInfoCode 跳过未定义信息点
     * @return 响应结果, 配置表名称 -> 错误信息
     * @since 5.240602.0
     */
    R<Map<String, Collection<String>>> importTableSheet(
            @NonNull List<MultipartFile> files, boolean skipNotDefineInfoCode);

    /**
     * 删除配置
     *
     * <p>公共模板删除完成后，会遍历全部集团和项目清理关联缓存。</p>
     *
     * @param param 删除参数
     * @return 响应结果
     * @since 7.250401.0
     */
    boolean del(@NonNull @Valid InfoCodeTemplateTypeRemoveDaoParam param);

    /**
     * 翻译信息点名称
     *
     * @param response 响应
     * @return 中文信息点名称 -> 英文信息点名称
     * @throws IOException IO异常
     * @since 7.250901.0
     */
    Map<String, String> translateInfoCodeName(@Nullable HttpServletResponse response) throws IOException;

    /**
     * 导入信息点名称
     *
     * @param file 文件
     * @return 响应结果
     * @since 7.250902.0
     */
    R<Collection<String>> importInfoCodeName(MultipartFile file);

}
