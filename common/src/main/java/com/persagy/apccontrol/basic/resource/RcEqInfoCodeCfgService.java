package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.dto.system.RcEqInfoCodeCfgDTO;
import com.persagy.apccontrol.basic.model.entity.RcEqInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.system.RcEqInfoCodeCfgVO;
import com.persagy.framework.database.service.SuperService;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_eq_info_code_cfg(设备信息点配置 已经绑定的设备信息通过rc_info_code_cfg配置生成)】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcEqInfoCodeCfgService extends SuperService<RcEqInfoCodeCfg> {

    /**
     * @descrption 数据同比
     *        
     * @param t
     * @return void        
     * @author tangzy
     * @since 2023/3/23 20:01
     */
    void synPic(RcEqInfoCodeCfg t);
    
    /**
     * 查询设备信息关系表
     * @author tangzy
     * @param dto
     * @return java.util.List<com.persagy.apccontrol.model.vo.system.RcEqInfoCodeCfgVO>
     **/
    List<RcEqInfoCodeCfgVO> queryRcEqInfoCodeCfgPage(RcEqInfoCodeCfgDTO dto);

    /**
     * 保存设备详情
     * @param synRcEqInfoCodeCfg
     */
    void saveRcEqInfoCodeCfg(List<RcEqInfoCodeCfg> synRcEqInfoCodeCfg);
}
