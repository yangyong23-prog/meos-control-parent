package com.persagy.apccontrol.websocket;

import com.google.common.collect.Sets;
import io.netty.channel.ChannelId;
import org.yeauty.pojo.Session;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 项目点位与客户端关联信息缓存
 */
public class WSClientCache {

    /**
     * 所有在线的客户端
     */
    private static final Map<String, Set<WSClient>> CLIENTS = new ConcurrentHashMap<>();

    /**
     * 关联关系
     */
    private static final Map<ChannelId, WSClient> MAPPING = new ConcurrentHashMap<>();

    /**
     * 更具项目ID获取所有WSClient
     *
     * @param projectId 项目ID
     * @return {@link WSClient}
     */
    public static Set<WSClient> getProjectClients(String projectId) {
        return CLIENTS.getOrDefault(projectId, Collections.emptySet());
    }

    /**
     * 注册客户端
     *
     * @param projectId 项目ID
     * @param client    {@link WSClient}
     */
    public static void register(String projectId, WSClient client) {
        CLIENTS.computeIfAbsent(projectId, e -> Sets.newConcurrentHashSet()).add(client);
        MAPPING.put(client.getChannelId(), client);
    }

    /**
     * 注销客户端
     *
     * @param projectId 项目ID
     * @param session   {@link Session}
     */
    public static void cancel(String projectId, Session session) {
        WSClient client = MAPPING.remove(session.id());
        CLIENTS.computeIfAbsent(projectId, e -> Sets.newConcurrentHashSet()).remove(client);
    }

    /**
     * 清理已无效的Client
     */
    public static void clear() {
        for (Set<WSClient> clients : CLIENTS.values()) {
            for (WSClient client : clients) {
                String projectId = client.getProjectId();
                Session session = client.getSession();

                if (!session.isOpen()) {
                    cancel(projectId, session);
                }
            }
        }
    }

}
