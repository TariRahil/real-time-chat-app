package websocketService.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import websocketService.model.ChatMessage;
import websocketService.service.RedisPublisherService;

@Controller
public class ChatController {
    private final RedisPublisherService redisPublisherService;

    public ChatController(RedisPublisherService redisPublisherService) {
        this.redisPublisherService = redisPublisherService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        redisPublisherService.publish(chatMessage);
        return chatMessage;
    }
}
