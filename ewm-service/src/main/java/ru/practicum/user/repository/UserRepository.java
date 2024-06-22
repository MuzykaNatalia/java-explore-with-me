package ru.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.user.model.User;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsById(@NonNull Long id);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIds(List<Long> ids, Pageable pageable);
}
