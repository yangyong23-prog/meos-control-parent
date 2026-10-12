package com.persagy.apccontrol.basic.structure.infocodeconfig.gateway;

import com.persagy.apccontrol.basic.model.base.domain.Gateway;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeIdentifier;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 信息点网关
 *
 * <p>------------------------- 使用方式 -------------------------<p>
 * <ol>
 *     <li><b>生成信息点配置聚合根</b> - {@link #of(InfoCodeInitByObjectParam)}</li>
 *     <li><b>按需填充信息点定义</b> - {@link #fillInfoCodeDefineModel(Collection)}</li>
 *     <li><b>按需填充信息点值</b> - {@link #fillInfoCodeDataModel(Collection, Map)}</li>
 * </ol>
 *
 * <p>------------------------- 静态方法 -------------------------<p>
 * <ul>
 *     <li><b>listObjectRequiredColumns</b> - 获取对象必要字段</li>
 * </ul>
 * <p>------------------------- 外部方法 -------------------------<p>
 * <ul>
 *     <li><b>of</b> - 生成聚合根</li>
 *     <li><b>查询模版类型关联对象数量</b> - 生成聚合根</li>
 *     <li><b>listTemplateTypes</b> - 查询模版类型</li>
 *     <li><b>listTemplateTypeObjects</b> - 查询模版类型关联物理世界对象</li>
 *     <li><b>mapTemplateTypeAndObjectCount</b> - 查询模版类型和关联对象数量映射</li>
 *     <li><b>mapTemplateTypeAndObjects</b> - 查询模版类型物理世界对象映射</li>
 *     <li><b>fillInfoCodeDefineModel</b> - 填充信息点定义模型</li>
 *     <li><b>fillInfoCodeDataModel</b> - 填充信息点值模型</li>
 *     <li><b>filterControlObjects</b> - 过滤管控对象</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public interface InfoCodeGateway extends Gateway<InfoCodeIdentifier, InfoCodeAggregate> {

    String CACHE_KEY_TEMPLATE = "infoCodeTemplate";
    String CACHE_KEY_TEMPLATE_OBJECT = "infoCodeTemplateObject";
    String CACHE_KEY_TEMPLATE_OBJECT_DEPRECATED = CACHE_KEY_TEMPLATE_OBJECT + "_deprecated";
    String CACHE_KEY_TEMPLATE_OBJECT_IOT_RULE = "infoCodeTemplateObjectIotRule";

    /**
     * 生成聚合根
     *
     * @param param 生成参数
     * @return 聚合根
     * @since 5.240602.0
     */
    List<InfoCodeAggregate> of(@NonNull @Valid InfoCodeInitByObjectParam param);

    /**
     * 生成聚合根（原始数据）
     *
     * @param param 生成参数
     * @return 聚合根
     * @since 7.260501.0
     */
    List<InfoCodeAggregate> ofOrigin(@NonNull @Valid InfoCodeInitByObjectParam param);

    /**
     * 查询模版类型关联对象数量
     *
     * @param param 查询参数
     * @return 模版类型关联对象数量
     * @since 5.240802.0
     */
    int countObject(@NonNull @Valid InfoCodeObjectQueryParam param);

    /**
     * 根据对象ID查询信息点对象
     *
     * @param objectIds 对象ID
     * @return 信息点对象
     * @since 7.250902.0
     */
    List<DtpRwdObjectDTO> listObjectByIds(@NonNull Collection<String> objectIds);

    /**
     * 查询模版类型
     *
     * @param param 查询参数
     * @return 模版类型
     * @since 7.250102.0
     */
    List<InfoCodeTemplateTypeModel> listTemplateTypes(@NonNull InfoCodeTemplateTypeQueryDaoParam param);

    /**
     * 查询模版类型带对象
     *
     * @param param 查询参数
     * @return 模版类型带对象
     * @since 7.250602.0
     */
    List<InfoCodeTemplateTypeModel> listTemplateTypesWithObjects(@NonNull InfoCodeObjectQueryParam param);

    /**
     * 查询模版类型关联物理世界对象
     *
     * @param param 查询参数
     * @return 模版类型关联物理世界对象
     * @since 5.240801.0
     */
    List<DtpRwdObjectDTO> listTemplateTypeObjects(@NonNull @Valid InfoCodeObjectQueryParam param);

    /**
     * 查询模版类型和关联对象数量映射
     *
     * @param param 查询参数
     * @return 模版类型 -> 关联物理世界对象数量
     * @since 5.240802.0
     */
    Map<InfoCodeTemplateTypeModel, Integer> mapTemplateTypeAndObjectCount(@NonNull @Valid InfoCodeObjectQueryParam param);

    /**
     * 查询模版类型物理世界对象映射
     *
     * @param param 查询参数
     * @return 模版类型 -> 关联物理世界对象
     * @since 5.240602.0
     */
    Map<InfoCodeTemplateTypeModel, List<DtpRwdObjectDTO>> mapTemplateTypeAndObjects(
            @NonNull @Valid InfoCodeObjectQueryParam param);

    /**
     * 填充信息点模版信息
     *
     * @param templateTypeModels 模版类型
     * @since 7.250901.0
     */
    void fillInfoCodeTemplateTypeModel(@NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels);

    /**
     * 填充信息点定义模型
     *
     * @param aggregates 聚合根
     * @since 5.240602.0
     */
    void fillInfoCodeDefineModel(@NonNull Collection<InfoCodeAggregate> aggregates);

    /**
     * 填充信息点值模型
     *
     * @param aggregates           聚合根
     * @param additionalDataValues 参数附加条件
     * @since 5.240602.0
     */
    void fillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates,
                               @Nullable Map<String, Object> additionalDataValues);

    /**
     * 过滤管控对象
     *
     * @param objects 对象
     * @since 5.240602.4
     */
    void filterControlObjects(@NonNull List<DtpRwdObjectDTO> objects);

    /**
     * 获取对象必要字段
     *
     * @return 对象必要字段
     * @since 7.250302.0
     */
    static String[] listObjectRequiredColumns() {
        return DtpRwdObjectDTO.listSimpleWithColumns();
    }
}
