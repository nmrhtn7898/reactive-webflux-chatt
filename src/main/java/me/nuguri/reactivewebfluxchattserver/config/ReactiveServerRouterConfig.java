package me.nuguri.reactivewebfluxchattserver.config;

import me.nuguri.reactivewebfluxchattserver.handler.UsersHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@EnableWebFlux
public class ReactiveServerRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> createUser(UsersHandler handler) {
        return RouterFunctions.route(
                POST("/api/v1/user").and(accept(APPLICATION_JSON)),
                handler::createUser
        );
    }

}
