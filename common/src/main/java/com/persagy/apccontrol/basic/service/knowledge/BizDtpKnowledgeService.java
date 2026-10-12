package com.persagy.apccontrol.basic.service.knowledge;

import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeInfoCodeBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeInfoCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.ObjectsExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 数据字典业务服务
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>listClassCodes</b> - 查询设备类编码定义</li>
 *     <li><b>listInfoCodes</b> - 查询信息点编码定义</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
public interface BizDtpKnowledgeService {

    /**
     * 查询信息点编码定义
     *
     * @param param 查询参数
     * @return 信息点编码定义
     * @since 7.250601.0
     */
    InfoCodeDefineModel getInfoCode(@NonNull @Valid DtpKnowledgeInfoCodeQueryParam param);

    /**
     * 查询设备类编码定义
     *
     * @param param 查询参数
     * @return 设备类编码定义
     * @since 5.241101.0
     */
    List<ClassCodeDefineModel> listClassCodes(@NonNull @Valid DtpKnowledgeClassCodeQueryParam param);

    /**
     * 查询信息点编码定义
     *
     * @param param 查询参数
     * @return 信息点编码定义
     * @since 24Q1 T3S1-5.17.0
     */
    List<InfoCodeDefineModel> listInfoCodes(@NonNull @Valid DtpKnowledgeInfoCodeBatchQueryParam param);


    /**
     * 查询是否有绑点信息
     *
     * @param param 查询参数
     * @return 对象绑点信息
     */
    public List<ObjectsExistRule> queryObjectsExistRule(ObjectsExistRuleQueryParam param);

}
