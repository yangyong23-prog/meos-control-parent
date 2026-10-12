package com.persagy.apccontrol.websocket;

import com.persagy.apccontrol.basic.model.enumtype.FunctionTypeEnum;
import io.netty.channel.ChannelId;
import lombok.Getter;
import org.springframework.util.Assert;
import org.yeauty.pojo.Session;

import java.util.Objects;

/**
 * 客户端封装
 *
 * @author shaohongbo
 * @date 2023/3/28 21:44
 */
@Getter
public class WSClient {

    private final String projectId;

    private final FunctionTypeEnum functionType;

    private final ChannelId channelId;

    private final Session session;


    public WSClient(String projectId, FunctionTypeEnum functionType, Session session) {
        this.projectId = projectId;
        this.functionType = functionType;
        this.channelId = session.id();
        this.session = session;
    }

    /**
     *
     */
    public void send(String message) {
        Assert.notNull(message, "message cat not be null");
        session.sendText(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WSClient wsClient = (WSClient) o;
        return Objects.equals(channelId, wsClient.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId);
    }
}
