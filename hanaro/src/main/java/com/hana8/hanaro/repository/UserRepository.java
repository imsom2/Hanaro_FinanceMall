package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.entity.User;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
	boolean existsByEmail(String email);
	List<User> findAllByRole(Role role);
	Optional<User> findByEmail(String email);
}
