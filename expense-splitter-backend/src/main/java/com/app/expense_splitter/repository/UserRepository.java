package com.app.expense_splitter.repository;

import com.app.expense_splitter.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String number);
    Optional<User> findByEmail(String email);

}