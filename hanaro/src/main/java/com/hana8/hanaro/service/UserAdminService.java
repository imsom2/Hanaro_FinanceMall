package com.hana8.hanaro.service;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.Role;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.UserAdminDTO;
import com.hana8.hanaro.dto.UserAdminSearchDTO;
import com.hana8.hanaro.dto.UserAdminSubscriptionDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.mapper.UserAdminMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.SubscriptionRepository;
import com.hana8.hanaro.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserAdminMapper userAdminMapper;

	public List<UserAdminDTO> getUsers() {
		return userAdminMapper.toUserDTOList(
			userRepository.findAllByRole(Role.ROLE_USER)
		);
	}

	public List<UserAdminSearchDTO> searchUsers(String keyword) {
		return userRepository.searchByKeyword(keyword).stream()
			.map(user -> UserAdminSearchDTO.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.createdAt(user.getCreatedAt())
				.subscriptions(
					userAdminMapper.toSubscriptionDTOList(
						subscriptionRepository.findAllByUserIdAndStatus(user.getId(), AccountStatus.ACTIVE)
					)
				)
				.build())
			.toList();
	}

	public List<UserAdminSubscriptionDTO> getUserSubscriptions(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
		}
		return userAdminMapper.toSubscriptionDTOList(
			subscriptionRepository.findAllByUserIdAndStatusIn(
				userId, List.of(AccountStatus.ACTIVE, AccountStatus.MATURED)
			)
		);
	}

	@Transactional
	public void processMaturity(Long userId, Long accountId) {
		Subscription subscription = subscriptionRepository.findByAccountId(accountId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

		if (subscription.getStatus() == AccountStatus.MATURED) {
			throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_MATURED);
		}
		if (subscription.getStatus() == AccountStatus.CANCELLED) {
			throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
		}

		// 만기 이자 계산
		BigDecimal maturityInterest = subscription.getMaturityInterest() != null
			? subscription.getMaturityInterest()
			: BigDecimal.ZERO;

		// 현재 잔액
		Long currentBalance = subscription.getAccount().getBalance();

		// 기본 입출금 계좌 조회
		Account basicAccount = accountRepository
			.findByUserIdAndAccountType(userId, AccountType.BASIC)
			.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

		// 잔액 + 만기 이자 기본 계좌로 이체
		Long transferAmount = currentBalance + maturityInterest.longValue();
		basicAccount.setBalance(basicAccount.getBalance() + transferAmount);
		accountRepository.save(basicAccount);

		// 구독 계좌 잔액 0으로
		Account subAccount = accountRepository.findById(subscription.getAccount().getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
		subAccount.setBalance(0L);
		accountRepository.save(subAccount);

		// 만기 처리
		subscription.setStatus(AccountStatus.MATURED);
		subscription.setMaturedAt(LocalDateTime.now());

		log.info("만기 처리 완료: 회원ID={}, 계좌ID={}, 이체금액={}",
			userId, accountId, transferAmount);
	}
}
