package me.nuguri.reactivewebfluxchattserver.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.reactivewebfluxchattserver.entity.Users;
import me.nuguri.reactivewebfluxchattserver.repository.UsersRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;

    public Mono<Users> save(Users users) {
        return usersRepository.save(users);
    }

}
