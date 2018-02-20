package com.its.messaging;

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

	public static final SMS SMS = com.its.messaging.SMS.builder().sender("+11 222 333 444").recipient("+99 888 777 666").message("A message").build();
	public static final MailMessage MAIL_MESSAGE = MailMessage.builder().sender("sender@domain.com").recipient("recipient@other.domain.com")
            .body("Hello my friend")
            .subject("Greetings").build();
	@Autowired
	ApplicationContext applicationContext;

	WebTestClient client;

	@Before
	public void setUp() {
		client = WebTestClient
				.bindToApplicationContext(applicationContext)
				.configureClient()
				.baseUrl("http://localhost:8080/")
				.build();
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
				.exchange()
				.expectStatus()
				.isOk();
	}

	@Test
	public void shouldGetOkStatusForSendEMail() {
		client.post().uri("/send/email")
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
				.contains(
						SMS
				);
	}

	@Test
	public void listingSentMailAndSmsShouldReturnMailAndSms() {
		client.post().uri("/send/email")
				.syncBody(MAIL_MESSAGE)
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
				.contains(
						MAIL_MESSAGE,
						SMS
				);
	}

}
