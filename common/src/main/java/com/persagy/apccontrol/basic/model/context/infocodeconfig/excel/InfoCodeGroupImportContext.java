package com.persagy.apccontrol.basic.model.context.infocodeconfig.excel;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 信息点分组配置导入上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@Accessors(chain = true)
public class InfoCodeGroupImportContext implements LogComponent {

    // ------------------------- 参数 -------------------------

    /**
     * 文件
     */
    private MultipartFile file;

    /**
     * 是否校验配置表中的分组编码
     */
    private final boolean checkGroupCodeInConfig;

    /**
     * 分组配置上下文
     */
    private final List<InfoCodeGroupConfigImportContext> configContexts;

    // ------------------------- 过程 -------------------------

    // ------------------------- 结果 -------------------------

    /**
     * 是否失败
     */
    private boolean failed = false;

    /**
     * 错误信息
     */
    private List<String> errorMessages = new ArrayList<>();

    /**
     * 构造器
     *
     * @param file 文件
     * @since 5.240602.0
     */
    public InfoCodeGroupImportContext(@NonNull MultipartFile file, boolean checkGroupCodeInConfig) {
        this.file = file;
        this.checkGroupCodeInConfig = checkGroupCodeInConfig;
        try {
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            Sheet sheet = reader.getSheet();

            this.configContexts = InfoCodeGroupConfigImportContext.of(sheet);
            if (CollectionUtils.isEmpty(configContexts) || configContexts.stream()
                    .anyMatch(InfoCodeGroupConfigImportContext::isFailed)) {
                this.errorMessages.addAll(configContexts.stream()
                        .filter(InfoCodeGroupConfigImportContext::isFailed)
                        .flatMap(contexts -> contexts.getErrorMessages().stream())
                        .collect(Collectors.toList()));
                this.failed = true;
                return;
            }
            // 递归填充配置子集
            deepFillChildrenConfigContext(listTopConfigContexts(), mapParentCodeAndConfigContext());
        } catch (IOException e) {
            logProjectInstance().error("文件解析失败", e);
            throw new ApplicationException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), e.getLocalizedMessage(), LocaleMessageUtil.getMessage("key_a1e92efd2da847f598847f47df3f8625"));
        }
    }

    // ------------------------- 行为 -------------------------

    /**
     * 获取顶级分组配置上下文
     *
     * @return 顶级分组配置上下文
     * @since 5.240602.0
     */
    public List<InfoCodeGroupConfigImportContext> listTopConfigContexts() {
        return configContexts.stream()
                .filter(item -> StringUtils.isBlank(item.getParentGroupCode()))
                .collect(Collectors.toList());
    }

    /**
     * 获取父级分组编码和配置上下文
     *
     * @return 父级分组编码 -> 上下文
     * @since 5.240602.0
     */
    public Map<String, List<InfoCodeGroupConfigImportContext>> mapParentCodeAndConfigContext() {
        return configContexts.stream()
                .filter(item -> StringUtils.isNotBlank(item.getParentGroupCode()))
                .collect(Collectors.groupingBy(InfoCodeGroupConfigImportContext::getParentGroupCode));
    }

    /**
     * 获取分组编码和配置上下文
     *
     * @return 分组编码 -> 配置上下文
     * @since 5.240602.0
     */
    public Map<String, InfoCodeGroupConfigImportContext> mapGroupCodeAndConfigContext() {
        return configContexts.stream().collect(Collectors.toMap(
                InfoCodeGroupConfigImportContext::getGroupCode, Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 转换输入上下文
     *
     * @since 5.240602.0
     */
    public InfoCodeGroupImportContext toOutPutContext() {
        this.file = null;
        this.configContexts.clear();
        return this;
    }

    /**
     * 转换错误输出上下文
     *
     * @since 5.240702.0
     */
    public InfoCodeGroupImportContext toErrorOutPutContext() {
        return toOutPutContext();
    }

    /**
     * 递归填充配置子集
     *
     * @param topConfigContexts          顶级配置上下文
     * @param parentCodeAndConfigContext 父级分组编码 -> 上下文
     * @since 5.240602.0
     */
    private void deepFillChildrenConfigContext(
            @NonNull List<InfoCodeGroupConfigImportContext> topConfigContexts,
            @NonNull Map<String, List<InfoCodeGroupConfigImportContext>> parentCodeAndConfigContext) {
        for (InfoCodeGroupConfigImportContext topConfigContext : topConfigContexts) {
            String groupCode = topConfigContext.getGroupCode();
            List<InfoCodeGroupConfigImportContext> children = Optional.ofNullable(
                    parentCodeAndConfigContext.get(groupCode)).orElse(Collections.emptyList());
            if (CollectionUtils.isNotEmpty(children)) {
                deepFillChildrenConfigContext(children, parentCodeAndConfigContext);
            }
            topConfigContext.setChildren(children);
        }
    }

    @Override
    public String logTag() {
        return "信息点分组配置-导入上下文";
    }
}
