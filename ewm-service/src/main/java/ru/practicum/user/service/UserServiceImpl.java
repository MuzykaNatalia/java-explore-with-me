package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        if (!ids.isEmpty()) {
            List<User> usersByIds = userRepository.findByIds(ids, pageable);
            log.info("Received users by id={}", ids);
            return userMapper.toUserDtoList(usersByIds);
        }
        List<User> allUsers = userRepository.findAll(pageable).getContent();
        log.info("Received users");
        return userMapper.toUserDtoList(allUsers);
    }

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        User createdUser = userRepository.save(userMapper.toUser(newUserRequest));
        log.info("User created={}", createdUser);
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found",
                    Collections.singletonList("User id does not exist"));
        }

        log.info("User by id={} deleted", userId);
        userRepository.deleteById(userId);
    }

    private Pageable getPageable(Integer from, Integer size) {
        return PageRequest.of(from / size, size, Sort.by(Sort.Order.asc("id")));
    }
}
