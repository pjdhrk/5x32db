package com.its.messaging;

import com.its.messaging.services.MessageHandlers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class MessagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingApplication.class, args);
    }

}

@Configuration
class WebConfiguration {
    @Bean
    public RouterFunction<?> routes(MessageHandlers messageHandlers) {
        return RouterFunctions
                .nest(POST("/send"),
                        route(POST("/email"), messageHandlers::handleEmail)
                                .andRoute(POST("/sms"), messageHandlers::handleSms)
                                .andRoute(POST("/*"), serverRequest ->
                                        ServerResponse.badRequest().build()))
                .andRoute(GET("/list"), messageHandlers::listAll);
    }


}


