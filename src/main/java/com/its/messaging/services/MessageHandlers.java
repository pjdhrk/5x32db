package com.its.messaging.services;

import com.its.messaging.dto.Mail;
import com.its.messaging.dto.Message;
import com.its.messaging.dto.SMS;
import com.its.messaging.repository.MessageRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MessageHandlers {

    private final MessageRepository messageRepository;


    public MessageHandlers(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Mono<ServerResponse> handleSms(ServerRequest serverRequest) {
        ServerResponseWrapper serverResponseWrapper = new ServerResponseWrapper();
        serverRequest.bodyToMono(SMS.class)
                .doOnSuccess(message ->log.info("Accepted sms message: {}", message))
        .doOnSuccess(sms -> messageRepository.insert(sms).subscribe())
        .doOnError(serverResponseWrapper::nok)
                .doOnSuccess(sms -> serverResponseWrapper.ok())
                .subscribe();
        return serverResponseWrapper.getResponseMono();
    }

    public Mono<ServerResponse> handleEmail(ServerRequest serverRequest) {
        ServerResponseWrapper serverResponseWrapper = new ServerResponseWrapper();
        serverRequest.bodyToMono(Mail.class)
                .doOnSuccess(message ->log.info("Accepted email message: {}", message))
                .doOnSuccess(message -> messageRepository.insert(message).subscribe())
                .doOnError(serverResponseWrapper::nok)
                .doOnSuccess(sms -> serverResponseWrapper.ok())
                .subscribe();
        return serverResponseWrapper.getResponseMono();
    }

    public Mono<ServerResponse> listAll(ServerRequest serverRequest) {
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
        .body(messageRepository.findAll(), Message.class);
    }

    @Getter
    private class ServerResponseWrapper {

        private Mono<ServerResponse> responseMono;

        public void ok() {
            responseMono = ServerResponse.ok().build();
        }

        public void nok(Throwable throwable) {
            responseMono = ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_EVENT_STREAM).syncBody(throwable.getMessage());
        }
    }
}
