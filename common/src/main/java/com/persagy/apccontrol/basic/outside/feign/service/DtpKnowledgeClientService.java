package com.persagy.apccontrol.basic.outside.feign.service;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeTypeCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeClassCodeTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.*;
import org.jspecify.annotations.Nullable;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 中台字典定义
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/11/10 19:07
 */
public interface DtpKnowledgeClientService extends BaseFeignClientService {

    String FIRST_TAG_EVENT_MESSAGE = "事件消息";

    /**
     * 查询设备类编码定义
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 设备类编码定义
     * @since 5.240502.0
     */
    DtpKnowledgeObjectClassCodeResponse getClassCode(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode);

    /**
     * 查询设备类编码名称
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 设备类编码名称
     * @since 5.240502.0
     */
    String getClassCodeName(boolean queryCache, boolean isThrows, @NonNull String classCode);

    /**
     * 查询信息点编码定义
     *
     * @param classCode 类型编码
     * @param infoCode  信息点编码
     * @return 信息点编码定义
     * @since 7.250601.0
     */
    @Nullable
    DtpKnowledgeInfoCodeDTO getInfoCode(
            @NonNull String classCode,
            @NonNull String infoCode);

    /**
     * 查询信息点编码定义
     *
     * @param isThrows  是否在查询失败|数据为空时抛出异常
     * @param classCode 类型编码
     * @param infoCode  信息点编码
     * @return 信息点编码定义
     * @since 5.240402.0
     */
    @Nullable
    DtpKnowledgeInfoCodeDTO getInfoCode(
            boolean isThrows,
            @NonNull String classCode,
            @NonNull String infoCode);

    /**
     * 根据知识库编码查询类型定义
     *
     * @param typeCode 知识库编码
     * @return 类型定义
     * @since 7.250902.0
     */
    List<DtpKnowledgeTypeResponse> listTypeDefines(@NonNull DtpKnowledgeTypeCodeEnum typeCode);

    /**
     * 查询设备类编码定义
     *
     * @param classCodes 类型编码
     * @return 设备类编码定义
     * @since 7.2500501.0
     */
    List<DtpKnowledgeObjectClassCodeResponse> listClassCodes(
            @NonNull Collection<String> classCodes);

    /**
     * 查询设备类编码定义
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCodes 类型编码
     * @return 设备类编码定义
     * @since 5.240502.0
     */
    List<DtpKnowledgeObjectClassCodeResponse> listClassCodes(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes);

    /**
     * 根据末级标签查询设备类编码
     *
     * @param objectType         对象类型
     * @param typeCodeAndTagCode 标签类别 -> 标签值
     * @return 设备类编码
     * @since 7.250201.0
     */
    List<DtpKnowledgeClassCodeByTagResponse> listClassCodesByLastLevelTag(
            @NonNull ObjectTypeEnum objectType,
            @NonNull Map<String, Set<String>> typeCodeAndTagCode);

    /**
     * 查询设备类标签
     *
     * @param param 查询参数
     * @return 设备类编码
     * @since 7.250902.0
     */
    List<DtpKnowledgeClassCodeTagResponse> listClassCodeTags(
            @NonNull DtpKnowledgeClassCodeTagQueryParam param);

    /**
     * 查询信息点标签
     *
     * @param param 查询参数
     * @return 信息点编码
     * @since 7.251202.0
     */
    Map<String, List<DtpKnowledgeInfoCodeTagResponse>> listInfoByTag(
            @NonNull DtpKnowledgeInfoCodeByTagQueryParam param);

    /**
     * 查询信息点编码定义
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCodes 类型编码
     * @return 信息点编码定义
     * @since 7.250702.0
     */
    List<DtpKnowledgeInfoCodeDTO> listInfoCodes(
            boolean queryCache, boolean isThrows,
            @NonNull Set<String> classCodes);

    /**
     * 查询信息点编码定义
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    List<DtpKnowledgeInfoCodeDTO> listInfoCodes(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode);

    /**
     * 查询信息点编码定义
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    Map<String, DtpKnowledgeInfoCodeDTO> listInfoCodesMap(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode);

    /**
     * 查询设备类编码和名称映射
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCodes 类型编码
     * @return 设备类编码 -> 设备类名称
     * @since 5.240502.0
     */
    Map<String, String> mapClassCodeAndName(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes);

    /**
     * 查询设备类编码和定义映射
     *
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCodes 类型编码
     * @return 设备类编码 -> 定义
     * @since 5.240702.0
     */
    Map<String, DtpKnowledgeObjectClassCodeResponse> mapClassCodeAndDefine(
            boolean isThrows, @NonNull Collection<String> classCodes);

    /**
     * 查询信息点编码和名称映射
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 信息点编码 -> 信息点名称
     * @since 5.240502.0
     */
    Map<String, String> mapInfoCodeAndName(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode);

    /**
     * 查询信息点编码和名称映射
     *
     * @param classCode 类型编码
     * @return 信息点编码 -> 信息点名称
     * @since v7.260602.0
     */
    @NonNull
    Map<String, String> mapInfoCodeAndName(@NonNull String classCode);

    /**
     * 查询信息点编码和信息点定义映射
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 信息点编码 -> 信息点定义映射
     * @since 5.240502.0
     */
    Map<String, DtpKnowledgeInfoCodeDTO> mapInfoCodeAndData(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode);

    /**
     * 查询信息点数据源定义映射
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCode  类型编码
     * @return 信息点编码 -> 信息点编码定义列表
     * @since 24Q1 T3S1-5.17.0
     */
    Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> mapInfoCodeAndDataSources(
            boolean queryCache, boolean isThrows,
            @NonNull String classCode);

    /**
     * 查询类型编码和信息点定义映射
     *
     * @param classCodes 类型编码
     * @return 类型编码 -> 信息点编码定义
     * @since 7.260201.0
     */
    Map<String, List<DtpKnowledgeInfoCodeDTO>> mapClassCodeAndInfoCodeDefines(
            @NonNull Collection<String> classCodes);

    /**
     * 查询类型编码和信息点定义映射
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCodes 类型编码
     * @return 类型编码 -> 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    Map<String, List<DtpKnowledgeInfoCodeDTO>> mapClassCodeAndInfoCodeDefines(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes);

    /**
     * 查询类型编码和信息点定义映射
     *
     * @param classCodes 类型编码
     * @return 类型编码 -> 信息点编码定义
     * @since 7.250501.0
     */
    Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine(
            @NonNull Collection<String> classCodes);

    /**
     * 查询类型编码和信息点定义映射
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param classCodes 类型编码
     * @return 类型编码 -> 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine(
            boolean queryCache, boolean isThrows,
            @NonNull Collection<String> classCodes);

    /**
     * 查询类型编码和标签映射
     *
     * @param param 查询参数
     * @return 类型编码 -> 标签
     * @since 7.250902.0
     */
    Map<String, Map<String, DtpKnowledgeClassCodeTagResponse>> mapClassCodeTypeAndTag(
            @NonNull DtpKnowledgeClassCodeTagQueryParam param);

    /**
     * 根据类型编码和名称定义
     *
     * @param typeCode 知识库编码
     * @return 类型编码 -> 名称
     * @since 7.250902.0
     */
    Map<String, String> mapTypeCodeAndName(@NonNull DtpKnowledgeTypeCodeEnum typeCode);

    /**
     * 根据 code 查询字典定义
     *
     * @param codes
     * @return
     */
    List<JSONObject> listTypeByCode(List<String> codes);

    /**
     * 按类型设备类代码和名称
     *
     * @param queryCache 查询缓存
     * @param isThrows   is投掷
     * @param objectType 对象类型
     * @return {@link Map}<{@link String}, {@link String}>
     */
    Map<String, String> mapClassCodeAndNameByType(boolean queryCache, boolean isThrows, @NonNull DtpRwdObjectTypeEnum objectType);
}
