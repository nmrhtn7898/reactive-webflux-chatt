package me.nuguri.reactivewebfluxchattserver.handler;

import lombok.RequiredArgsConstructor;
import me.nuguri.reactivewebfluxchattserver.entity.Users;
import me.nuguri.reactivewebfluxchattserver.service.UsersService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.net.URI.create;

@Component
@RequiredArgsConstructor
public class UsersHandler {

    private final UsersService usersService;

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return ServerResponse
                .created(create("/api/v1/user"))
                .body(
                        serverRequest
                                .bodyToMono(Users.class)
                                .flatMap(usersService::save),
                        Users.class
                );
    }

}
