/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample;

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

/**
 * @author Rob Winch
 * @since 5.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HelloWebfluxApplication.class)
@TestPropertySource(properties = "server.port=0")
public class HelloWebfluxApplicationITests {
	@Value("#{@nettyContext.address().getPort()}")
	int port;

	WebTestClient rest;

	@Before
	public void setup() {
		this.rest = WebTestClient.bindToServer()
				.responseTimeout(Duration.ofDays(1))
				.baseUrl("http://localhost:" + this.port)
				.build();
	}


	@Test
	public void basicWhenNoCredentialsThenUnauthorized() throws Exception {
		this.rest
			.get()
			.uri("/")
			.exchange()
			.expectStatus().isUnauthorized();
	}

	@Test
	public void basicWhenValidCredentialsThenOk() throws Exception {
		this.rest
			.mutate()
			.filter(userCredentials())
			.build()
			.get()
			.uri("/")
			.exchange()
			.expectStatus().isOk()
			.expectBody().json("{\"message\":\"Hello user!\"}");
	}

	@Test
	public void basicWhenInvalidCredentialsThenUnauthorized() throws Exception {
		this.rest
			.mutate()
			.filter(invalidPassword())
			.build()
			.get()
			.uri("/")
			.exchange()
			.expectStatus().isUnauthorized()
			.expectBody().isEmpty();
	}

	private ExchangeFilterFunction userCredentials() {
		return basicAuthentication("user","user");
	}

	private ExchangeFilterFunction invalidPassword() {
		return basicAuthentication("user","INVALID");
	}
}
