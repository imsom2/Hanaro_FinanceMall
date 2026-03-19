package com.hana8.hanaro.service;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
		return userRepository.findByEmailWithBasicAccount(email)
			.map(user -> {
				UserDetailsDTO dto = new UserDetailsDTO(
					user.getId(),
					user.getEmail(),
					user.getPasswd(),
					user.getName(),
					user.getRole()
				);
				user.getAccounts().stream()
					.filter(a -> a.getAccountType() == AccountType.BASIC)
					.findFirst()
					.ifPresent(account -> dto.setAccountId(account.getId()));
				return dto;
			})
			.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
	}

}
