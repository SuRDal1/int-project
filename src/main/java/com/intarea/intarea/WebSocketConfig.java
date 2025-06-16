package com.intarea.intarea;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws").withSockJS();
        // 클라이언트가 WebSocket연결을 시작할 엔드포인트 URL 정의 = JS에서 /ws로 접속.
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        // 서버 -> 클라이언드로 보내는 메시지를 처리할 내장 메시지 브로커 활성화
        registry.enableSimpleBroker("/topic");

        // 클라이언트 -> 서버로 보낼 때 사용하는 경로 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

}
