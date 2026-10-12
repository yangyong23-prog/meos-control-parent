package com.persagy.apccontrol.basic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.entity.InfoCodeSetExtend;
import com.persagy.apccontrol.basic.model.enumtype.ManualAutoSetEnum;

import java.util.List;

/**
 * @author yuecaipu
 */
public interface InfoCodeSetExtendService extends IService<InfoCodeSetExtend> {


    /**
     * 根据对象ID和infoCode更新或保存
     *
     * @param infoCodeSetExtend   对象
     * @author yuecaipu
     * @date 2024/8/6 16:30
     * @since v5.240801
     */
    void saveOrUpdateWithObjectIdAndInfoCode(InfoCodeSetExtend infoCodeSetExtend);

    /**
     * listByObjectId
     *
     * @param objectId 对象ID
     * @return {@link List<InfoCodeSetExtend>}
     * @author yuecaipu
     * @date 2024/8/6 16:29
     * @since v5.240801
     */
    List<InfoCodeSetExtend> listByObjectId(String objectId);

    /**
     * listByObjectIdAndInfoCode
     *
     * @param objectId 对象ID
     * @param infoCode 信息点编码
     * @return {@link InfoCodeSetExtend}
     * @author yuecaipu
     * @date 2024/8/12 15:13
     * @since v5.240801
     */
    InfoCodeSetExtend getByObjectIdAndInfoCode(String objectId, String infoCode);

    /**
     * listByInfoCodeAndModel
     *
     * @param infoCode 信息点编码
     * @param manualAutoSet    手自动状态
     * @return {@link List<InfoCodeSetExtend>}
     * @author yuecaipu
     * @date 2024/8/6 16:29
     * @since v5.240801
     */
    List<InfoCodeSetExtend> listByInfoCodeAndModel(String infoCode, ManualAutoSetEnum manualAutoSet);
}
