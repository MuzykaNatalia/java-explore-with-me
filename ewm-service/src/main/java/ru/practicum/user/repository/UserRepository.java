package ru.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.User;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsById(@NonNull Long id);

    List<User> findAllByIdIn(List<Long> ids, Pageable pageable);
}
