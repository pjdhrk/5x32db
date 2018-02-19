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
	public void shouldGetOkStatusForSend() {
		client.post().uri("/send/sms")

				.exchange()
				.expectStatus()
				.isOk();
	}

}
