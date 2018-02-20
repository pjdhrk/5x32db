package com.its.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@SpringBootApplication
public class MessagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingApplication.class, args);
    }

}

@RestController
@Slf4j
@AllArgsConstructor
class MessagingController {
    private final MessageRepository messageRepository;

    @GetMapping(value = "/list", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> list() {
        return messageRepository.findAll();
    }
}

@Configuration
class WebConfiguration {
    @Bean
    public RouterFunction<?> routes(MailService mailService, SmsService smsService) {
        return RouterFunctions
                .route(POST("/send/email"), serverRequest -> {
                    serverRequest.bodyToMono(MailMessage.class).subscribe(mailService);
                    return ServerResponse.ok().build();
                })
                .andRoute(POST("/send/sms"), serverRequest -> {
                    serverRequest.bodyToFlux(SMS.class).subscribe(smsService);
                    return ServerResponse.ok().build();
                })
                .andRoute(POST("/send/*"), serverRequest ->
                        ServerResponse.badRequest().build());
    }
}

@Service
@Slf4j
@AllArgsConstructor
class MailService implements Consumer<MailMessage> {

    private final MessageRepository messageRepository;

    @Override
    public void accept(MailMessage mailMessage) {
        log.info("Accepted email message {}", mailMessage);
        messageRepository.insert(mailMessage).subscribe();
    }
}

@Service
@Slf4j
@AllArgsConstructor
class SmsService implements Consumer<SMS> {

    private final MessageRepository messageRepository;

    @Override
    public void accept(SMS sms) {
        log.info("Accepted sms message: {}", sms);
        messageRepository.insert(sms).subscribe();
    }
}

interface Message {

}

@ToString
@Data
@Document
class MailMessage implements Message {
    private String sender;
    private String recipient;
    private String subject;
    private String body;

}

@ToString
@Data
@Document
class SMS implements Message {
    private String sender;
    private String recipient;
    private String message;

}

@Repository
interface MessageRepository extends ReactiveMongoRepository<Message, String> {
}