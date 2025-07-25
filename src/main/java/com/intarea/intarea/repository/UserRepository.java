package com.intarea.intarea.repository;

import com.intarea.intarea.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository <Users, Long> {

    Optional<Users> findByLoginId(String loginId);
}
