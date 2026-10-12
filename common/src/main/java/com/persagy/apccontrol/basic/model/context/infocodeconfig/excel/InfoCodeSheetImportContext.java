package com.persagy.apccontrol.basic.model.context.infocodeconfig.excel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.excel.ExcelUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * 信息点配置导入Sheet上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeSheetImportContext implements LogComponent {

    private static final String SPLIT_CHAR = "-";
    private static final String INFO_CODE_FILTER_IN_HEADER = "headerFilter";

    // ------------------------- 参数 -------------------------

    /**
     * sheet
     */
    private Sheet sheet;

    /**
     * 唯一标识
     */
    private final @Nullable String uniqueKey;

    /**
     * 设备类编码
     */
    private final String classCode;

    /**
     * 设备类编码
     */
    private final Integer sequence;

    /**
     * 信息点过滤条件
     */
    private final @Nullable String infoCodeFilter;

    /**
     * 配置上下文
     */
    private final List<InfoCodeConfigImportContext> configContexts;

    /**
     * 开始读取行下标
     */
    private int beginRowIndex = 1;

    // ------------------------- 过程 -------------------------

    /**
     * 对象类型
     */
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 新增后的模版类型ID
     */
    private Long templateTypeIdAfterAdded;

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
     * @param sheet Sheet
     * @since 5.24060.0
     */
    public InfoCodeSheetImportContext(@NonNull Sheet sheet, Integer sequence) {
        this.sheet = sheet;
        this.sequence = sequence + 1;
        String[] sheetTag = StringUtils.split(sheet.getSheetName(), SPLIT_CHAR);
        this.classCode = StringUtils.trimToNull(sheetTag[0]);
        String uniqueKey = null;
        String infoCodeFilter = null;
        if (sheetTag.length > 1) {
            String filter = StringUtils.trimToNull(sheetTag[1]);
            // 如果是表头信息点过滤, 则取 [B2] 作为过滤条件
            if (StringUtils.startsWithIgnoreCase(filter, INFO_CODE_FILTER_IN_HEADER)) {
                Row row = sheet.getRow(0);
                int cellIndex = 1;
                infoCodeFilter = ExcelUtils.analyzeValueByString(row, cellIndex);
                if (StringUtils.isNotBlank(infoCodeFilter)) {
                    // 存在过滤条件时, 判断是否为JSON, 为JSON时进行校验
                    if (infoCodeFilter.startsWith(JsonToken.START_OBJECT.asString())) {
                        try {
                            InfoCodeFilterValueObject filterValueObject = ObjectMapperUtils.readValue(
                                    infoCodeFilter, InfoCodeFilterValueObject.class);
                            if (Objects.nonNull(filterValueObject)) {
                                uniqueKey = StringUtils.defaultIfBlank(filterValueObject.getUniqueKey(), null);
                            }
                        } catch (JsonProcessingException e) {
                            ExcelUtils.validFailedLog(row, cellIndex, LocaleMessageUtil.getMessage("key_42524c973d1f4c9b8efe8eb2be4d0175"), e, getErrorMessages());
                            this.failed = true;
                        }
                    }
                }
                beginRowIndex++;
            } else {
                infoCodeFilter = filter;
            }
        }
        this.uniqueKey = uniqueKey;
        this.infoCodeFilter = infoCodeFilter;
        this.configContexts = InfoCodeConfigImportContext.of(sheet, beginRowIndex);
        this.failed = this.failed | checkConfigContext();
    }

    // ------------------------- 行为 -------------------------

    /**
     * 检查配置上下文
     *
     * @return 错误信息
     * @since 5.240602.0
     */
    public boolean checkConfigContext() {
        String sheetName = sheet.getSheetName();
        Map<String, Map<InfoCodeTypeEnum, List<String>>> groupCodeAndInfoTypeCode = new HashMap<>();
        configContexts.forEach(item -> MapExecutor.dynamicPut(groupCodeAndInfoTypeCode, item.getRelGroupCode(),
                item.getInfoCodeType(), item.getInfoCode(), HashMap::new, ArrayList::new));

        boolean sheetContextAnyFailed = false;
        List<String> errorMessages = new ArrayList<>();
        for (InfoCodeConfigImportContext configContext : configContexts) {
            int rowNum = configContext.getRow().getRowNum();
            String relGroupCode = configContext.getRelGroupCode();
            boolean containsInfoCodeAliasName = configContext.isContainsInfoCodeAliasName();
            List<Pair<InfoCodeTypeEnum, String>> relInfoCodeTypeAndCodes = configContext.getRelInfoCodeTypeAndCodes();
            if (configContext.isFailed()) {
                sheetContextAnyFailed = true;
            }
            if (CollectionUtils.isNotEmpty(relInfoCodeTypeAndCodes)) {
                for (Pair<InfoCodeTypeEnum, String> relInfoCodeTypeAndCode : relInfoCodeTypeAndCodes) {
                    List<String> codes = MapExecutor.dynamicGet(groupCodeAndInfoTypeCode,
                            relGroupCode, relInfoCodeTypeAndCode.getLeft());
                    if (CollectionUtils.isEmpty(codes) || !codes.contains(relInfoCodeTypeAndCode.getRight())) {
                        logProjectInstance().error("关联 InfoCode 不存在", Arrays.asList(
                                Pair.of("sheetName", sheetName),
                                Pair.of("rowNum", rowNum),
                                Pair.of("relInfoCodeTypeAndCode", relInfoCodeTypeAndCode)
                        ));
                        errorMessages.add(String.format(LocaleMessageUtil.getMessage("key_68cdb592a1724985bd8f46c08957b48b"),
                                sheetName, rowNum + 1, 8 + (containsInfoCodeAliasName ? 1 : 0), relInfoCodeTypeAndCode));
                        sheetContextAnyFailed = true;
                    }
                }
            }
        }
        getErrorMessages().addAll(errorMessages);
        return sheetContextAnyFailed;
    }

    /**
     * 转换输出上下文
     *
     * @since 5.240602.0
     */
    public void toOutPutContext() {
        this.setSheet(null);
        configContexts.forEach(InfoCodeConfigImportContext::toOutPutContext);
    }

    @Override
    public String logTag() {
        return "信息点配置-导入Sheet上下文";
    }
}
