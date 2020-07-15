package me.nuguri.reactivewebfluxchattserver.repository;

import me.nuguri.reactivewebfluxchattserver.entity.Users;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UsersRepository extends ReactiveMongoRepository<Users, String> {

}
