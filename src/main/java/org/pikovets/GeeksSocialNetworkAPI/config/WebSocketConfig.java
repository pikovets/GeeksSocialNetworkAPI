package org.pikovets.GeeksSocialNetworkAPI.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String TOPIC_PREFIX = "/topic/";
    public static final String REGISTRY = "/ws";


    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.login}")
    private String login;

    @Value("${spring.rabbitmq.passcode}")
    private String passcode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(REGISTRY).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.setPathMatcher(new AntPathMatcher("."));
        config.enableStompBrokerRelay(TOPIC_PREFIX)
                .setRelayHost(host)
                .setClientLogin(login)
                .setClientPasscode(passcode)
                .setSystemLogin(login)
                .setSystemPasscode(passcode);
    }
}
