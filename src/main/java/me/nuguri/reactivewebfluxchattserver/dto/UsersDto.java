package me.nuguri.reactivewebfluxchattserver.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.nuguri.reactivewebfluxchattserver.entity.Users;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsersDto {

    private String id;

    private String name;

    private int age;

    public UsersDto(Users users) {
        this.id = users.getId();
        this.name = users.getName();
        this.age = users.getAge();
    }

}
