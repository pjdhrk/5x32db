package com.its.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class MessagingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessagingApplication.class, args);
	}

}

@RestController
@Slf4j
class MessagingController {

	@PostMapping("/send/{type}")
	public Mono<String> send(@PathVariable String type, @RequestBody(required = false) String contents) {
		return Mono.empty();
	}

	@GetMapping(value = "/list", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Message> list() {
		log.info("I'm here!");
		return Flux.empty();
	}
}

interface Message {

}