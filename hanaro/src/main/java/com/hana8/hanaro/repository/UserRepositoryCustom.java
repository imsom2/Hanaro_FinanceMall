package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import com.hana8.hanaro.entity.User;

public interface UserRepositoryCustom {
	Optional<User> findByEmailWithBasicAccount(String email);
	List<User> searchByKeyword(String keyword);
}
