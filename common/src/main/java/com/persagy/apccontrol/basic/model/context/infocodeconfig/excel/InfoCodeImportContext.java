package com.persagy.apccontrol.basic.model.context.infocodeconfig.excel;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 信息点配置导入上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@Accessors(chain = true)
public class InfoCodeImportContext implements LogComponent {

    private static final String SPLIT_CHAR = "-";

    // ------------------------- 参数 -------------------------

    /**
     * 文件
     */
    private MultipartFile file;

    /**
     * 跳过未定义信息点
     */
    private final boolean skipNotDefineInfoCode;

    /**
     * 运行监测类型
     */
    private final RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    @Nullable
    private final RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版类型
     */
    private final InfoCodeTemplateTypeEnum templateType;

    /**
     * Sheet上下文
     */
    private final List<InfoCodeSheetImportContext> sheetContexts;

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
     * @param file                  文件
     * @param skipNotDefineInfoCode 跳过未定义信息点
     * @since 5.240602.0
     */
    public InfoCodeImportContext(@NonNull MultipartFile file, boolean skipNotDefineInfoCode) {
        this.file = file;
        this.skipNotDefineInfoCode = skipNotDefineInfoCode;
        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            logProjectInstance().error("无法解析文件名", Pair.of("fileName", file.getOriginalFilename()));
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_d62b3d053fed4eb793dd9a342bf611ad"));
        }
        try {
            String[] fileTag = analyzeFileTag(fileName);
            // 第一段可以为二级类型也可以为一级类型, 二级类型可以为空, 不为空时从二级类型中解析出一级类型
            this.runningMonitorSecondType = BaseEnum.analyze(fileTag[0], RunningMonitorSecondTypeEnum.class, false);
            this.runningMonitorType = Objects.nonNull(runningMonitorSecondType)
                    ? runningMonitorSecondType.toRunningMonitorTypeEnum()
                    : BaseEnum.analyze(fileTag[0], RunningMonitorTypeEnum.class);
            this.templateType = BaseEnum.analyze(fileTag[1], InfoCodeTemplateTypeEnum.class);

            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<Sheet> sheets = reader.getSheets();
            this.sheetContexts = new ArrayList<>();
            for (int i = 0; i < sheets.size(); i++) {
                this.sheetContexts.add(new InfoCodeSheetImportContext(sheets.get(i), i));
            }

            this.failed = this.failed | checkSheetContext();
        } catch (IOException e) {
            logProjectInstance().error("解析文件失败", e, Pair.of("fileName", fileName));
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_088cb31757414c32b157a973ad8ca3b4"));
        }
    }

    // ------------------------- 行为 -------------------------

    /**
     * 检查Sheet上下文
     *
     * @return Sheet上下文是否检查失败
     * @since 5.240602.0
     */
    public boolean checkSheetContext() {
        Set<String> sheetIdentifiers = new HashSet<>();
        // Sheet上下文是否任意一个失败
        boolean sheetContextAnyFailed = false;
        for (InfoCodeSheetImportContext sheetContext : sheetContexts) {
            Sheet sheet = sheetContext.getSheet();
            String classCode = sheetContext.getClassCode();
            String infoCodeFilter = sheetContext.getInfoCodeFilter();
            boolean sheetContextFailed = sheetContext.isFailed();
            if (sheetContextFailed) {
                sheetContextAnyFailed = true;
            }

            String sheetIdentifier = classCode + "-" + infoCodeFilter;
            if (sheetIdentifiers.contains(sheetIdentifier)) {
                logProjectInstance().error("Sheet定义重复", Pair.of("sheetName", sheet.getSheetName()));
                throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT,
                        String.format(LocaleMessageUtil.getMessage("key_75380607cf504368b140d9a10a49823c"), sheet.getSheetName()));
            }
            sheetIdentifiers.add(sheetIdentifier);
        }
        return sheetContextAnyFailed;
    }

    /**
     * 转换输出上下文
     *
     * @since 5.240602.0
     */
    public InfoCodeImportContext toOutPutContext() {
        this.file = null;
        sheetContexts.forEach(InfoCodeSheetImportContext::toOutPutContext);
        return this;
    }

    /**
     * 获取错误信息
     *
     * @return 错误信息
     * @since 5.241001.0
     */
    public Collection<String> getErrorMessages() {
        List<String> errorMessages = new ArrayList<>(this.errorMessages);
        for (InfoCodeSheetImportContext sheetContext : sheetContexts) {
            errorMessages.addAll(sheetContext.getErrorMessages());
            for (InfoCodeConfigImportContext configContext : sheetContext.getConfigContexts()) {
                errorMessages.addAll(configContext.getErrorMessages());
            }
        }
        return errorMessages;
    }

    /**
     * 解析文件标签
     *
     * @param fileName 文件名称
     * @return 文件标签
     * @since 5.240602.0
     */
    private String[] analyzeFileTag(@NonNull String fileName) {
        try {
            return StringUtils.split(StringUtils.substringsBetween(fileName, "【", "】")[0], SPLIT_CHAR);
        } catch (Exception e) {
            logProjectInstance().error("解析文件标签失败", e, Pair.of("fileName", fileName));
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_64f887c69fd24bf9bae5b9ca22a7aabd"));
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-导入上下文";
    }
}
