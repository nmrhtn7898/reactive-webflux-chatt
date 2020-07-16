package me.nuguri.reactivewebfluxchattserver.handler;

import lombok.RequiredArgsConstructor;
import me.nuguri.reactivewebfluxchattserver.dto.UsersDto;
import me.nuguri.reactivewebfluxchattserver.entity.Users;
import me.nuguri.reactivewebfluxchattserver.service.UsersService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.net.URI.create;
import static org.springframework.web.reactive.function.server.ServerResponse.*;
import static reactor.core.publisher.Mono.just;

@Component
@RequiredArgsConstructor
public class UsersHandler {

    private final UsersService usersService;

    public Mono<ServerResponse> findUsers(ServerRequest serverRequest) {
        return ok().body(usersService.findAll(), Users.class);
    }

    public Mono<ServerResponse> findUser(ServerRequest serverRequest) {
        return usersService
                .findById(serverRequest.pathVariable("id"))
                .flatMap(u -> ok().body(just(new UsersDto(u)), UsersDto.class))
                .switchIfEmpty(notFound().build());
    }

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(Users.class)
                .flatMap(usersService::save)
                .flatMap(u -> created(create("/api/v1/user/" + u.getId())).body(just(new UsersDto(u)), UsersDto.class));
    }

}
