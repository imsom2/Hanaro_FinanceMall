package com.hana8.hanaro.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.auth.SignUpDTO;
import com.hana8.hanaro.dto.auth.SignUpRequestDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.mapper.UserMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock private UserRepository userRepository;
	@Mock private AccountRepository accountRepository;
	@Mock private PasswordEncoder passwordEncoder;
	@Mock private UserMapper userMapper;
	@Mock private AccountService accountService;

	@BeforeEach
	void setUp() {
		new AccountUtil("1234567890123456");
	}

	private SignUpRequestDTO createRequest(String email, String accountNum) {
		SignUpRequestDTO dto = new SignUpRequestDTO();
		dto.setEmail(email);
		dto.setPasswd("Test1234!");
		dto.setName("테스트");
		dto.setAccountNum(accountNum);
		return dto;
	}

	@Test
	void signUp_success_withManualAccountNum() {
		SignUpRequestDTO request = createRequest("test@test.com", "12345678901");

		given(userRepository.existsByEmail("test@test.com")).willReturn(false);
		given(accountService.resolveAccountNum("12345678901")).willReturn("12345678901");
		given(passwordEncoder.encode("Test1234!")).willReturn("encodedPw");

		given(userRepository.save(any(User.class))).willAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(1L);
			return user;
		});

		given(accountRepository.save(any(Account.class))).willAnswer(invocation -> {
			Account account = invocation.getArgument(0);
			account.setId(10L);
			return account;
		});

		SignUpDTO responseDto = new SignUpDTO();
		given(userMapper.toSignUpDTO(any(User.class))).willReturn(responseDto);

		SignUpDTO result = userService.signUp(request);

		assertThat(result).isNotNull();
		assertThat(result.getAccountNum()).isEqualTo("123-4567-8901");
		assertThat(result.getAccountId()).isEqualTo(10L);

		verify(userRepository).save(any(User.class));
		verify(accountRepository).save(any(Account.class));
		verify(passwordEncoder).encode("Test1234!");
	}

	@Test
	void signUp_success_withGeneratedAccountNum() {
		SignUpRequestDTO request = createRequest("auto@test.com", null);
		String generatedNum = "98765432100";

		given(userRepository.existsByEmail("auto@test.com")).willReturn(false);
		given(accountService.resolveAccountNum(null)).willReturn(generatedNum);
		given(passwordEncoder.encode("Test1234!")).willReturn("encodedPw");

		given(userRepository.save(any(User.class))).willAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(2L);
			return user;
		});

		given(accountRepository.save(any(Account.class))).willAnswer(invocation -> {
			Account account = invocation.getArgument(0);
			account.setId(20L);
			return account;
		});

		SignUpDTO responseDto = new SignUpDTO();
		given(userMapper.toSignUpDTO(any(User.class))).willReturn(responseDto);

		SignUpDTO result = userService.signUp(request);

		assertThat(result).isNotNull();
		assertThat(result.getAccountNum()).isNotBlank();
		assertThat(result.getAccountNum()).hasSize(13); // "XXX-XXXX-XXXX" 포맷
		assertThat(result.getAccountId()).isEqualTo(20L);

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		verify(accountRepository).save(captor.capture());
		assertThat(captor.getValue().getAccountNum()).hasSize(11);
	}

	@Test
	void signUp_fail_whenEmailDuplicated() {
		SignUpRequestDTO request = createRequest("dup@test.com", "12345678901");

		given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

		assertThatThrownBy(() -> userService.signUp(request))
			.isInstanceOf(BusinessException.class);

		verify(userRepository, never()).save(any());
		verify(accountRepository, never()).save(any());
	}

	@Test
	void signUp_fail_whenManualAccountNumDuplicated() {
		SignUpRequestDTO request = createRequest("test@test.com", "12345678901");

		given(userRepository.existsByEmail("test@test.com")).willReturn(false);
		given(accountService.resolveAccountNum("12345678901"))
			.willThrow(new BusinessException(ErrorCode.ACCOUNT_NUM_DUPLICATE));

		assertThatThrownBy(() -> userService.signUp(request))
			.isInstanceOf(BusinessException.class);

		verify(userRepository, never()).save(any());
		verify(accountRepository, never()).save(any());
	}

	@Test
	void signUp_fail_whenGeneratedAccountNumRetryExceeded() {
		SignUpRequestDTO request = createRequest("retry@test.com", null);

		given(userRepository.existsByEmail("retry@test.com")).willReturn(false);
		given(accountService.resolveAccountNum(null))
			.willThrow(new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));

		assertThatThrownBy(() -> userService.signUp(request))
			.isInstanceOf(BusinessException.class);

		verify(userRepository, never()).save(any());
		verify(accountRepository, never()).save(any());
	}
}
