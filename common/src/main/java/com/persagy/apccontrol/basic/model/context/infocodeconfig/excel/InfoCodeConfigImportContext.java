package com.persagy.apccontrol.basic.model.context.infocodeconfig.excel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAssembleModel;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectDetailModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.InfoCodeTakeValueLogicRwdObjectModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue.StaticInfoCodeTackValueLogicModel;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.apccontrol.basic.utils.excel.ExcelUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 信息点配置导入配置上下文
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeConfigImportContext implements LogComponent {

    private static final String REL_INFO_CODE_SPLIT = "-";

    // ------------------------- 参数 -------------------------

    /**
     * 文件
     */
    private Row row;

    /**
     * 信息点名称
     */
    private final String infoCode;

    /**
     * 信息点名册
     */
    private final String infoCodeName;

    /**
     * 信息点别名
     */
    @Nullable
    private String infoCodeAliasName;

    /**
     * 是否包含信息点别名
     */
    private boolean containsInfoCodeAliasName = false;

    /**
     * 信息点类型
     */
    private final InfoCodeTypeEnum infoCodeType;

    /**
     * 信息点顺序
     */
    private final Integer sequence;

    /**
     * 关联分组编码
     */
    private final String relGroupCode;

    /**
     * 关联信息点编码
     */
    @Nullable
    private List<Pair<InfoCodeTypeEnum, String>> relInfoCodeTypeAndCodes;

    /**
     * 取值逻辑
     */
    @Nullable
    private JsonNode tackValueLogic;

    /**
     * 展示样式
     */
    @Nullable
    private String showStyle;

    // ------------------------- 过程 -------------------------

    /**
     * 新增后配置ID
     */
    private Long configIdAfterAdded;

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
     * @param titleRow 标题行
     * @param row      row
     * @see #of(Sheet, int)
     * @since 5.240602.0
     */
    private InfoCodeConfigImportContext(@NonNull Row titleRow, @NonNull Row row) {
        this.row = row;
        this.infoCodeType = BaseEnum.analyze(ExcelUtils.analyzeValueByString(row, 0),
                InfoCodeTypeEnum.class, false);
        if (Objects.isNull(infoCodeType)) {
            ExcelUtils.validFailedLog(row, 0, LocaleMessageUtil.getMessage("key_5c307ff000974a3cb525849ab04e3a69"), getErrorMessages());
            this.failed = true;
        }
        AtomicInteger cellIndex = new AtomicInteger(3);
        this.infoCodeName = ExcelUtils.analyzeValueByString(row, cellIndex.get());
        if (StringUtils.isBlank(infoCodeName)) {
            ExcelUtils.validFailedLog(row, cellIndex.get(), LocaleMessageUtil.getMessage("key_9ddd0fc2fa2146b4a1d47932e92d80dd"), getErrorMessages());
            this.failed = true;
        }
        cellIndex.incrementAndGet();
        String infoCodeOrAliasName = ExcelUtils.analyzeValueByString(row, cellIndex.get());
        if (isInfoCodeAliasNameCell(titleRow, cellIndex.get())) {
            this.infoCodeAliasName = infoCodeOrAliasName;
            this.containsInfoCodeAliasName = true;
            infoCodeOrAliasName = ExcelUtils.analyzeValueByString(row, cellIndex.incrementAndGet());
        }
        this.infoCode = infoCodeOrAliasName;
        cellIndex.incrementAndGet();
        this.relGroupCode = ExcelUtils.analyzeValueByString(row, cellIndex.get());
        if (StringUtils.isBlank(relGroupCode)) {
            ExcelUtils.validFailedLog(row, 5, LocaleMessageUtil.getMessage("key_8334b51e87554d319b694ae7813ba6c4"), getErrorMessages());
            this.failed = true;
        }
        cellIndex.incrementAndGet();
        this.sequence = ExcelUtils.analyzeValueByInteger(row, cellIndex.get());
        cellIndex.incrementAndGet();
        String relInfoCode = ExcelUtils.analyzeValueByString(row, cellIndex.get());
        if (StringUtils.isNotBlank(relInfoCode)) {
            if (relInfoCode.contains(InfoCodeAssembleModel.ASSEMBLE_SIGN)) {
                this.relInfoCodeTypeAndCodes = Arrays.stream(StringUtils.split(relInfoCode,
                                InfoCodeAssembleModel.ASSEMBLE_SIGN))
                        .map(this::analyzeSingleRelInfoCodeTypeAndCode)
                        .collect(Collectors.toList());
            } else {
                this.relInfoCodeTypeAndCodes = Lists.newArrayList(analyzeSingleRelInfoCodeTypeAndCode(relInfoCode));
            }
        }
        cellIndex.incrementAndGet();
        if (Objects.nonNull(this.infoCodeType)) {
            Class<?> tackValueLogicModel;
            if (this.infoCodeType == InfoCodeTypeEnum.JSON_OBJECT) {
                tackValueLogicModel = JsonNode.class;
            } else {
                tackValueLogicModel = this.infoCodeType.getTackValueLogicModel();
                tackValueLogicModel = Objects.isNull(tackValueLogicModel) ? StaticInfoCodeTackValueLogicModel.class : tackValueLogicModel;
            }
            this.tackValueLogic = ExcelUtils.analyzeValueByJsonNode(row, cellIndex.get(), e -> {
                ExcelUtils.validFailedLog(row, cellIndex.get(), LocaleMessageUtil.getMessage("key_16f4a5def7924964b774d161ad08e98b"), getErrorMessages());
                this.failed = true;
            }, tackValueLogicModel, e -> {
                ExcelUtils.validFailedLog(row, cellIndex.get(), LocaleMessageUtil.getMessage("key_5b07d7a821d74a2aa3594e79055b4d15"), getErrorMessages());
                this.failed = true;
            });
            if (tackValueLogicModel != StaticInfoCodeTackValueLogicModel.class
                    && tackValueLogicModel != JsonNode.class
                    && Objects.isNull(this.tackValueLogic)) {
                ExcelUtils.validFailedLog(row, cellIndex.get(), LocaleMessageUtil.getMessage("key_faa0b7f1363a4e4189e3d01657752030"), getErrorMessages());
                this.failed = true;
            }
            // 校验 RWD_OBJECT_DETAIL / RWD_OBJECT_LIST / RWD_OBJECT_STAT 的 source 配置合法性
            // 并在 sourceSequence 未配置时自动填充, 保证入库后即使 MySQL JSON 列重排 source key 顺序,
            // 运行时仍可通过 sourceSequence 还原配置顺序
            if (!this.failed && Objects.nonNull(this.tackValueLogic)
                    && InfoCodeTakeValueLogicRwdObjectDetailModel.class.equals(tackValueLogicModel)) {
                try {
                    InfoCodeTakeValueLogicRwdObjectDetailModel detailModel =
                            ObjectMapperUtils.convertValue(this.tackValueLogic, InfoCodeTakeValueLogicRwdObjectDetailModel.class);
                    if (Objects.nonNull(detailModel.getSource())) {
                        for (Map.Entry<String, InfoCodeTakeValueLogicRwdObjectModel> entry : detailModel.getSource().entrySet()) {
                            try {
                                entry.getValue().validate(entry.getKey());
                            } catch (IllegalArgumentException e) {
                                ExcelUtils.validFailedLog(row, cellIndex.get(), e.getMessage(), getErrorMessages());
                                this.failed = true;
                            }
                        }
                        // 自动填充 sourceSequence: 若未显式配置, 则按 source 的 key 顺序生成
                        // 确保数据库 JSON 列重排 key 后仍可还原原始配置顺序
                        JsonNode sourceSequenceNode = this.tackValueLogic.get("sourceSequence");
                        if (Objects.isNull(sourceSequenceNode)
                                && this.tackValueLogic.isObject()
                                && this.tackValueLogic.has("source")) {
                            JsonNode sourceNode = this.tackValueLogic.get("source");
                            if (sourceNode.isObject() && !sourceNode.isEmpty()) {
                                ArrayNode sourceSequenceArray = ObjectMapperUtils.getObjectMapper().createArrayNode();
                                sourceNode.fieldNames().forEachRemaining(sourceSequenceArray::add);
                                ((com.fasterxml.jackson.databind.node.ObjectNode) this.tackValueLogic)
                                        .set("sourceSequence", sourceSequenceArray);
                            }
                        }
                    }
                    // 校验 fallbackQueryParams 合法性：每个回退查询条件必须包含至少一个有效查询维度
                    List<DtpRwdObjectQueryParam> fallbackQueryParams = detailModel.getFallbackQueryParams();
                    if (CollectionUtils.isNotEmpty(fallbackQueryParams)) {
                        for (int i = 0; i < fallbackQueryParams.size(); i++) {
                            DtpRwdObjectQueryParam fallbackQueryParam = fallbackQueryParams.get(i);
                            if (Objects.isNull(fallbackQueryParam) || fallbackQueryParam.invalid()) {
                                ExcelUtils.validFailedLog(row, cellIndex.get(),
                                        String.format(LocaleMessageUtil.getMessage(
                                                "key_69b1347d3bf24e66b331b64796b3175f"), i), getErrorMessages());
                                this.failed = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // treeToValue 失败时已在 validCallback 中记录，此处忽略
                }
            }
            cellIndex.incrementAndGet();
        }
        this.showStyle = ExcelUtils.analyzeValueByString(row, cellIndex.get());
    }

    // ------------------------- 行为 -------------------------

    /**
     * 创建配置上下文
     *
     * @param sheet         Sheet
     * @param beginRowIndex 开始读取行下标
     * @return 配置上下文
     * @since 5.240602.0
     */
    public static List<InfoCodeConfigImportContext> of(@NonNull Sheet sheet, int beginRowIndex) {
        Row titleRow = ExcelUtils.analyzeRow(sheet, beginRowIndex - 1);
        // 从第一行开始解析
        List<Row> rows = ExcelUtils.analyzeRows(sheet, beginRowIndex);
        return rows.stream().map(item -> new InfoCodeConfigImportContext(titleRow, item)).collect(Collectors.toList());
    }

    /**
     * 转换输出上下文
     *
     * @since 5.240602.0
     */
    public void toOutPutContext() {
        this.row = null;
    }

    /**
     * 是否为信息点别名列
     *
     * @param row       行
     * @param cellIndex 列下标
     * @return 是否为信息点别名列
     * @since 5.241101.0
     */
    private boolean isInfoCodeAliasNameCell(@NonNull Row row, int cellIndex) {
        return StringUtils.equalsAny(ExcelUtils.analyzeValueByString(row, cellIndex),
                LocaleMessageUtil.getMessage("key_8ff05a785f6a4d72b4a4d1390b9f3621"), "Information Alias Name");
    }

    /**
     * 解析单个关联信息点类型和编码映射
     *
     * @param relInfoCode 关联信息点
     * @return 信息点类型 -> 信息点编码
     * @since 5.241002.0
     */
    private Pair<InfoCodeTypeEnum, String> analyzeSingleRelInfoCodeTypeAndCode(@NonNull String relInfoCode) {
        if (relInfoCode.contains(REL_INFO_CODE_SPLIT)) {
            String[] relInfoCodeSplit = StringUtils.split(relInfoCode, REL_INFO_CODE_SPLIT);
            InfoCodeTypeEnum relInfoCodeType = BaseEnum.analyze(relInfoCodeSplit[0], InfoCodeTypeEnum.class, false);
            if (Objects.isNull(relInfoCodeType)) {
                ExcelUtils.validFailedLog(row, 7, LocaleMessageUtil.getMessage("key_7e4dbde4a7f84d17b8a2f9b6cf7bdacb"), getErrorMessages());
                this.failed = true;
            }
            return Pair.of(relInfoCodeType, relInfoCodeSplit[1]);
        }
        return Pair.of(this.infoCodeType, relInfoCode);
    }

    @Override
    public String logTag() {
        return "信息点配置-导入配置上下文";
    }
}
