package com.its.messaging.services;

import com.its.messaging.dto.Mail;
import com.its.messaging.dto.Message;
import com.its.messaging.dto.SMS;
import com.its.messaging.repository.MessageRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
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
        return handleMessage(serverRequest.bodyToMono(SMS.class));
    }

    public Mono<ServerResponse> handleEmail(ServerRequest serverRequest) {
        return handleMessage(serverRequest.bodyToMono(Mail.class));
    }

    private <T extends Message> Mono<ServerResponse> handleMessage(Mono<T> messageMono) {
        ServerResponseWrapper serverResponseWrapper = new ServerResponseWrapper();
        messageMono
                .doOnSuccess(message ->log.info("Accepted {} message: {}", message))
                .doOnSuccess(message -> messageRepository.insert(message).subscribe())
                .doOnError(throwable -> throwable instanceof DecodingException, serverResponseWrapper::cannotDeserialize)
                .doOnError(throwable -> !(throwable instanceof DecodingException), serverResponseWrapper::nok)
                .doOnError(throwable -> log.info("An error has been thrown {}", throwable.getMessage()))
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

        void ok() {
            responseMono = ServerResponse.ok().build();
        }

        void nok(Throwable throwable) {
            responseMono = ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_EVENT_STREAM).syncBody(throwable.getMessage());
        }

        void cannotDeserialize(Throwable throwable) {
            responseMono = ServerResponse.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.TEXT_EVENT_STREAM).syncBody(throwable.getMessage());
        }
    }
}
