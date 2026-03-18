package com.hana8.hanaro.service;

import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.common.util.AccountUtil; // Util 추가
import com.hana8.hanaro.dto.auth.SignUpDTO;
import com.hana8.hanaro.dto.auth.SignUpRequestDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.mapper.UserMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final AccountUtil accountUtil;

	@Transactional
	public SignUpDTO signUp(SignUpRequestDTO dto) {

		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
		}

		String rawAccountNum;
		if (dto.getAccountNum() != null && !dto.getAccountNum().isBlank()) {
			String encryptedInput = accountUtil.encrypt(dto.getAccountNum());
			if (accountRepository.existsByAccountNum(encryptedInput)) {
				throw new BusinessException(ErrorCode.ACCOUNT_NUM_DUPLICATE);
			}
			rawAccountNum = dto.getAccountNum();
		} else {
			rawAccountNum = generateUniqueAccountNum();
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
			.accountNum(accountUtil.encrypt(rawAccountNum))
			.accountType(AccountType.BASIC)
			.build();

		accountRepository.save(account);

		SignUpDTO response = userMapper.toSignUpDTO(user);
		response.setMaskedAccountNum(rawAccountNum);
		return response;
	}

	private String generateUniqueAccountNum() {
		int maxRetry = 10;
		for (int i = 0; i < maxRetry; i++) {
			String accountNum = generateAccountNum();
			if (!accountRepository.existsByAccountNum(accountUtil.encrypt(accountNum))) {
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
