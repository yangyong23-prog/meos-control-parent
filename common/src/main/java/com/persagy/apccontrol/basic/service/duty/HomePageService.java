package com.persagy.apccontrol.basic.service.duty;

import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;

import java.util.List;

/**
 * 值班首页业务接口
 */
public interface HomePageService<T extends BasicsCardVO> {

    List<T> homePage(HomePageParam param);


}
