package me.nuguri.reactivewebfluxchattserver.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.nuguri.reactivewebfluxchattserver.config.ReactiveServerTestConfig;
import me.nuguri.reactivewebfluxchattserver.dto.UsersDto;
import me.nuguri.reactivewebfluxchattserver.entity.Users;
import me.nuguri.reactivewebfluxchattserver.repository.UsersRepository;
import me.nuguri.reactivewebfluxchattserver.service.UsersService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Import(ReactiveServerTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsersHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    // References
    // https://docs.spring.io/spring/docs/current/spring-framework-reference/pdf/testing-webtestclient.pdf
    // https://gist.github.com/pgilad/bac8a59e449dc470f3fc5954dd23761a

    @Test
    void findUsers200() {
        webTestClient
                .get()
                .uri("/api/v1/users")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(UsersDto.class)
                .consumeWith(result -> {
                    List<UsersDto> responseBody = result.getResponseBody();
                    try {
                        System.out.println(objectMapper.writeValueAsString(responseBody));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void findUser200() {
        webTestClient
                .get()
                .uri("/api/v1/user/{id}", "5f0dbae041cedb73c071903b")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(UsersDto.class)
                .consumeWith(result -> {
                    UsersDto responseBody = result.getResponseBody();
                    try {
                        System.out.println(objectMapper.writeValueAsString(responseBody));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void createUser201() {
        Users users = Users.builder().id(randomUUID().toString()).name("test").age(10).build();
        webTestClient
                .post()
                .uri("/api/v1/user")
                .body(Mono.just(users), Users.class)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(UsersDto.class)
                .consumeWith(result -> {
                    UsersDto responseBody = result.getResponseBody();
                    try {
                        System.out.println(objectMapper.writeValueAsString(responseBody));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });
    }

}
