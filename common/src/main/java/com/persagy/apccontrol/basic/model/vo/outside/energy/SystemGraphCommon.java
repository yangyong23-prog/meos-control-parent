
package com.persagy.apccontrol.basic.model.vo.outside.energy;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author 薛涛
 * @Description 系统图统计公共存储类
 * @Date 15:58 2024/9/3
 * @Param 
 * @return 
 **/
@Data
public class SystemGraphCommon {



    private List<DtpRwdObjectDTO> objects;

    private List<ExceptionEventBaseInfoVO> eventInfoDTOS;

    private List<InfoCodeValueVO> listInfoCodeValueInOneHour;

    private Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap;

}