package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceImplTest {
    private final UserService userService;
    private NewUserRequest newUserRequest;
    private NewUserRequest newUserRequestTwo;

    @BeforeEach
    public void setUp() {
        newUserRequest = new NewUserRequest("Ivan", "ivan@mail.ru");
        newUserRequestTwo = new NewUserRequest("Lisa", "lisa@mail.ru");
    }

    @DisplayName("Должен создать пользователя")
    @Test
    public void shouldCreateUser() {
        UserDto createdUser = userService.createUser(newUserRequest);
        UserDto test = new UserDto(null, "Ivan", "ivan@mail.ru");
        test.setId(createdUser.getId());

        assertThat(createdUser, is(equalTo(test)));
    }

    @DisplayName("Должен удалить пользователя по id")
    @Test
    public void shouldDeleteUserById() {
        UserDto createdUserOne = userService.createUser(newUserRequest);
        UserDto createdUserTwo = userService.createUser(newUserRequestTwo);

        UserDto testOne = new UserDto(null, "Ivan", "ivan@mail.ru");
        UserDto testTwo = new UserDto(null, "Lisa", "lisa@mail.ru");

        testOne.setId(createdUserOne.getId());
        testTwo.setId(createdUserTwo.getId());

        List<UserDto> usersListOne = userService.getUsers(List.of(
                createdUserOne.getId(), createdUserTwo.getId()), 0, 10);

        assertEquals(usersListOne.size(), 2);
        assertThat(usersListOne, is(equalTo(List.of(testOne, testTwo))));

        userService.deleteUser(createdUserOne.getId());

        List<UserDto> usersListTwo = userService.getUsers(List.of(
                createdUserOne.getId(), createdUserTwo.getId()), 0, 10);

        assertEquals(usersListTwo.size(), 1);
        assertThat(usersListTwo, is(equalTo(List.of(testTwo))));
    }

    @DisplayName("Должен вернуть пользователей")
    @Test
    public void shouldGetUsers() {
        UserDto createdUserOne = userService.createUser(newUserRequest);
        UserDto createdUserTwo = userService.createUser(newUserRequestTwo);

        UserDto testOne = new UserDto(null, "Ivan", "ivan@mail.ru");
        UserDto testTwo = new UserDto(null, "Lisa", "lisa@mail.ru");

        testOne.setId(createdUserOne.getId());
        testTwo.setId(createdUserTwo.getId());

        List<UserDto> usersListOne = userService.getUsers(null, 0, 10);

        assertEquals(usersListOne.size(), 2);
        assertThat(usersListOne, is(equalTo(List.of(testOne, testTwo))));

        List<UserDto> usersListTwo = userService.getUsers(List.of(
                createdUserOne.getId()), 0, 10);

        assertEquals(usersListTwo.size(), 1);
        assertThat(usersListTwo, is(equalTo(List.of(testOne))));
    }

    @DisplayName("Должен выдать исключение при попытке удалить пользователя по id, которого не существует")
    @Test
    public void shouldNotDeleteUser() {
        long userId = -200000;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(userId)
        );

        assertEquals("User with id=" + userId + " was not found", exception.getMessage());
    }

    @DisplayName("Должен выдать исключение при попытке создать пользователя с email, который уже существует")
    @Test
    public void shouldNotCreateUser() {
        userService.createUser(newUserRequest);

        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(newUserRequest));
    }
}