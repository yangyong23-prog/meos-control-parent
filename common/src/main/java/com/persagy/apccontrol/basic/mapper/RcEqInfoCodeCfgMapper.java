package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.RcEqInfoCodeCfg;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_eq_info_code_cfg(设备信息点配置 已经绑定的设备信息通过rc_info_code_cfg配置生成)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcEqInfoCodeCfg
 */
@Mapper
@Repository
public interface RcEqInfoCodeCfgMapper extends SuperMapper<RcEqInfoCodeCfg> {

    /**
     * 批量添加
     * @author tangzy
     * @param synRcEqInfoCodeCfg
     **/
    void insertBatch(@Param("listCfg") List<RcEqInfoCodeCfg> synRcEqInfoCodeCfg);
}




