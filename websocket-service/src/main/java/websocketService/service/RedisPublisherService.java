package websocketService.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import websocketService.model.ChatMessage;

@Service
public class RedisPublisherService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPublisherService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(ChatMessage chatMessage) {
        redisTemplate.convertAndSend("chat", chatMessage);
    }
}
