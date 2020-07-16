package me.nuguri.reactivewebfluxchattserver.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.reactivewebfluxchattserver.entity.Users;
import me.nuguri.reactivewebfluxchattserver.repository.UsersRepository;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public Flux<Users> findAll() {
        return usersRepository.findAll();
    }

    public Mono<Users> save(Users users) {
        return usersRepository.save(users);
    }

    public Mono<Users> findById(String id) {
        return usersRepository.findById(id);
    }

}
