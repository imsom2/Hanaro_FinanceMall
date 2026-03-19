package com.hana8.hanaro.repository;

import java.util.List;
import java.util.Optional;

import com.hana8.hanaro.entity.User;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

	private final EntityManager em;

	@Override
	public Optional<User> findByEmailWithBasicAccount(String email) {
		List<User> result = em.createQuery(
				"SELECT u FROM User u LEFT JOIN FETCH u.accounts a"
				+ " WHERE u.email = :email AND a.accountType = 'BASIC'",
				User.class)
			.setParameter("email", email)
			.getResultList();
		return result.stream().findFirst();
	}

	@Override
	public List<User> searchByKeyword(String keyword) {
		return em.createQuery(
				"SELECT u FROM User u WHERE u.name LIKE :keyword OR u.email LIKE :keyword",
				User.class)
			.setParameter("keyword", "%" + keyword + "%")
			.getResultList();
	}
}
