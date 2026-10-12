package com.persagy.apccontrol.websocket;

import com.persagy.apccontrol.basic.model.enumtype.FunctionTypeEnum;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

/**
 * @author shaohongbo
 */
@Slf4j
@ServerEndpoint(path = "/meos-control-server-edge/webSocketServer/{projectId}", port = "${paas.websocketPort:8849}")
public class NettyWebsocketServer {


    @OnOpen
    public void onOpen(Session session, HttpHeaders headers,
                       @PathVariable("projectId") String projectId,
                       @RequestParam(name = "functionType", required = false) FunctionTypeEnum functionType) {
        //log.info("有新的客户端建立连接，编号: [{}],项目id：[{}],注册的消息类型", session.id(), projectId);
        ControlLogUtil.info("有新的客户端建立连接，编号: [{}],项目id：[{}],注册的消息类型", session.id(), projectId);
        
        //冷热源和冷源是同一个订阅进行适配
        if (FunctionTypeEnum.ACCI.equals(functionType)) {
            functionType = FunctionTypeEnum.AC;
        }
        
        WSClient client = new WSClient(projectId, functionType, session);
        //将新用户存入在线的组
        WSClientCache.register(projectId, client);
    }


    @OnClose
    public void onClose(Session session, @PathVariable("projectId") String projectId) {
        //将掉线的用户移除在线的组里
        WSClientCache.cancel(projectId, session);
        ControlLogUtil.info("移除项目：{}下的，客户端：{}", projectId, session.id());
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathVariable String projectId) {
        log.warn("客户端出错", throwable);
        ControlLogUtil.warn("客户端出错", throwable);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathVariable("projectId") String projectId) {
        ChannelId clientId = session.id();
        //log.info("收到客户端{}的数据请求消息:{}", clientId, message);
        //ControlLogUtil.info("收到客户端{}的数据请求消息:{}", clientId, message);
        if ("closeSession".equals(message)) {
            onClose(session, projectId);
        }
    }
}
