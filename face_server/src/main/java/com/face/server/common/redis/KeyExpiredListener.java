package com.face.server.common.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * project : face-web-server
 * Code Create : 2019/4/27
 * Class : com.face.server.common.redis.KeyExpiredListener
 *
 * @author wangxiaoming
 * @author a345566462@163.com
 * @version 1.0.1
 * @since 1.0.1 April 2019
 */
@Slf4j
@Component
public class KeyExpiredListener implements MessageListener {
    @Autowired
    private RedisTemplate<?, ?> redisTemplate;

    public KeyExpiredListener() {
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.info("onPMessage pattern " + new String(pattern) + " " + " " + message);
        String channel = new String(message.getChannel());
        String str = (String) redisTemplate.getValueSerializer().deserialize(message.getBody());
        log.info("received message channel {}, body is {}.", channel, str);
    }
}
