package com.its.messaging;

import com.its.messaging.dto.Mail;
import com.its.messaging.dto.Message;
import com.its.messaging.dto.SMS;
import com.its.messaging.repository.MessageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessagingApplicationTests {

    private static final SMS SMS = com.its.messaging.dto.SMS.builder().sender("+11 222 333 444").recipient("+99 888 777 666").message("A message").build();
    private static final Mail MAIL = Mail.builder().sender("sender@domain.com").recipient("recipient@other.domain.com")
            .body("Hello my friend")
            .subject("Greetings").build();
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MessageRepository messageRepository;

    private WebTestClient client;

    @Before
    public void setUp() {
        client = WebTestClient
                .bindToApplicationContext(applicationContext)
                .configureClient()
                .baseUrl("http://localhost:8080/")
                .build();
        messageRepository.deleteAll().subscribe();

    }

    @Test
    public void shouldGetOkStatusForList() {
        client.get().uri("/list")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void shouldGetOkStatusForSendSMS() {
        client.post().uri("/send/sms")
                .syncBody(SMS)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void shouldGetOkStatusForSendEMail() {
        client.post().uri("/send/email")
                .syncBody(MAIL)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void shouldGetNokStatusForSendingUnsupportedMessage() {
        client.post().uri("/send/unsupported")
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    public void listingSentSmsShouldReturnSms() {
        client.post().uri("/send/sms")
                .syncBody(SMS)
                .exchange()
                .expectStatus()
                .isOk();
        client.get().uri("/list/")
                .exchange()
                .expectBodyList(SMS.class)
                .hasSize(1)
                .contains(SMS)
                ;
    }

    @Test
    public void listingSentMailShouldReturnMail() {
        client.post().uri("/send/email")
                .syncBody(MAIL)
                .exchange()
                .expectStatus()
                .isOk();
        client.get().uri("/list/")
                .exchange()
                .expectBodyList(Mail.class)
                .hasSize(1)
                .contains(
                        MAIL
                );
    }

    @Test
    public void listingSentMailAndSmsShouldReturnMailAndSms() {
        client.post().uri("/send/email")
                .syncBody(MAIL)
                .exchange()
                .expectStatus()
                .isOk();
        client.post().uri("/send/sms")
                .syncBody(SMS)
                .exchange()
                .expectStatus()
                .isOk();
        client.get().uri("/list/")
                .exchange()
                .expectBodyList(Message.class)
                .hasSize(2)
                .contains(
                        MAIL,
                        SMS
                );
    }

}
