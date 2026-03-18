package com.hana8.hanaro.service;

import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.dto.auth.SignUpDTO;
import com.hana8.hanaro.dto.auth.SignUpRequestDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.mapper.UserMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	@Transactional
	public SignUpDTO signUp(SignUpRequestDTO dto) {
		log.info("회원가입 요청 수신");
		if (userRepository.existsByEmail(dto.getEmail())) {
			log.warn("회원가입 중단: 이메일 중복");
			throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
		}

		String accountNum;
		if (dto.getAccountNum() != null && !dto.getAccountNum().isBlank()) {
			if (accountRepository.existsByAccountNum(AccountUtil.encrypt(dto.getAccountNum()))) {
				throw new BusinessException(ErrorCode.ACCOUNT_NUM_DUPLICATE);
			}
			accountNum = dto.getAccountNum();
		} else {
			accountNum = generateUniqueAccountNum();
		}

		User user = User.builder()
			.email(dto.getEmail())
			.passwd(passwordEncoder.encode(dto.getPasswd()))
			.name(dto.getName())
			.role(Role.ROLE_USER)
			.build();

		userRepository.save(user);

		Account account = Account.builder()
			.user(user)
			.accountNum(accountNum)
			.accountType(AccountType.BASIC)
			.build();

		accountRepository.save(account);

		log.info("회원가입 완료: userId={}, accountId={}", user.getId(), account.getId());

		SignUpDTO response = userMapper.toSignUpDTO(user);
		response.setMaskedAccountNum(accountNum);
		return response;
	}

	private String generateUniqueAccountNum() {
		int maxRetry = 10;
		for (int i = 0; i < maxRetry; i++) {
			String accountNum = generateAccountNum();
			if (!accountRepository.existsByAccountNum(AccountUtil.encrypt(accountNum))) {
				return accountNum;
			}
		}
		throw new BusinessException(
			ErrorCode.INTERNAL_SERVER_ERROR,
			"계좌번호 생성에 실패했습니다. 잠시 후 다시 시도해주세요."
		);
	}

	private String generateAccountNum() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 11; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}
}
