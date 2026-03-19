package com.hana8.hanaro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.Optional;

import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@InjectMocks
	private CustomUserDetailsService customUserDetailsService;

	@Mock
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("test@test.com");
		user.setPasswd("encoded-password");
		user.setName("테스트유저");
		user.setRole(Role.ROLE_USER);
		user.setAccounts(new ArrayList<>());
	}

	@Test
	void loadUserByUsername_success_withBasicAccount() {
		Account basicAccount = new Account();
		basicAccount.setId(100L);
		basicAccount.setAccountType(AccountType.BASIC);
		basicAccount.setUser(user);
		user.getAccounts().add(basicAccount);

		given(userRepository.findByEmailWithBasicAccount("test@test.com"))
			.willReturn(Optional.of(user));

		UserDetailsDTO result =
			(UserDetailsDTO) customUserDetailsService.loadUserByUsername("test@test.com");

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getUsername()).isEqualTo("test@test.com");
		assertThat(result.getPassword()).isEqualTo("encoded-password");
		assertThat(result.getName()).isEqualTo("테스트유저");
		assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
		assertThat(result.getAccountId()).isEqualTo(100L);
	}

	@Test
	void loadUserByUsername_success_withoutBasicAccount() {
		given(userRepository.findByEmailWithBasicAccount("test@test.com"))
			.willReturn(Optional.of(user));

		UserDetailsDTO result =
			(UserDetailsDTO) customUserDetailsService.loadUserByUsername("test@test.com");

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getUsername()).isEqualTo("test@test.com");
		assertThat(result.getAccountId()).isNull();
	}

	@Test
	void loadUserByUsername_fail_whenUserNotFound() {
		given(userRepository.findByEmailWithBasicAccount("none@test.com"))
			.willReturn(Optional.empty());

		assertThatThrownBy(() ->
			customUserDetailsService.loadUserByUsername("none@test.com"))
			.isInstanceOf(UsernameNotFoundException.class)
			.hasMessage("사용자를 찾을 수 없습니다: none@test.com");
	}
}
