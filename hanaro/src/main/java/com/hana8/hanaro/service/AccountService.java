package com.hana8.hanaro.service;

import com.hana8.hanaro.common.converter.AccountUtil;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.AccountDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.mapper.AccountMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

	private final AccountRepository accountRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final InterestCalculator interestCalculator;
	private final AccountMapper accountMapper;

	public List<AccountDTO> getAllAccounts(Long userId) {
		List<Account> accounts = accountRepository.findAllByUserId(userId);

		return accounts.stream()
			.map(account -> {
				// A. 기본 입출금 계좌인 경우
				if (account.getAccountType() == AccountType.BASIC) {
					return accountMapper.toBasicAccountDTO(account);
				}
				// B. 상품 계좌(DEPOSIT, SAVINGS)인 경우
				return subscriptionRepository.findByAccountId(account.getId())
					.map(sub -> {
						if (sub.getStatus() == AccountStatus.CANCELLED ||
							sub.getStatus() == AccountStatus.MATURED) {
							return null;
						}
						AccountDTO dto = accountMapper.toSubscriptionAccountDTO(sub);
						dto.setBalance(interestCalculator.calculateCurrentBalance(sub));
						dto.setCancelInterest(interestCalculator.calculateCancelInterest(sub));
						return dto;
					})
					.orElseGet(() -> accountMapper.toBasicAccountDTO(account));
			})
			.filter(Objects::nonNull)
			.sorted(Comparator.comparing(dto -> dto.getAccountType() != AccountType.BASIC))
			.collect(Collectors.toList());
	}

	public AccountDTO getAccountDetail(Long userId, Long accountId) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		if (account.getAccountType() == AccountType.BASIC) {
			return accountMapper.toBasicAccountDTO(account);
		}

		Subscription subscription = subscriptionRepository.findByAccountId(accountId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

		AccountDTO dto = accountMapper.toSubscriptionAccountDTO(subscription);
		dto.setBalance(interestCalculator.calculateCurrentBalance(subscription));
		dto.setCancelInterest(interestCalculator.calculateCancelInterest(subscription));
		return dto;
	}

	@Transactional
	public void transfer(Long userId, Long accountId) {
		Subscription subscription = getValidSubscription(accountId, userId);

		if (subscription.getProduct().getProductType() == ProductType.DEPOSIT) {
			throw new BusinessException(ErrorCode.INVALID_INPUT,
				"예금 상품은 납입할 수 없습니다.");
		}

		LocalDate today = LocalDate.now();
		if (subscription.getPaymentCycle() == PaymentCycle.MONTHLY) {
			if (subscription.getPaymentDay() == null ||
				today.getDayOfMonth() != subscription.getPaymentDay()) {
				throw new BusinessException(ErrorCode.INVALID_INPUT,
					"오늘(" + today.getDayOfMonth() + "일)은 납입일이 아닙니다. 납입일: " + subscription.getPaymentDay() + "일");
			}
		} else if (subscription.getPaymentCycle() == PaymentCycle.WEEKLY) {
			if (subscription.getPaymentDayOfWeek() == null ||
				today.getDayOfWeek() != subscription.getPaymentDayOfWeek()) {
				throw new BusinessException(ErrorCode.INVALID_INPUT,
					"오늘(" + today.getDayOfWeek() + ")은 납입일이 아닙니다. 납입일: " + subscription.getPaymentDayOfWeek());
			}
		}

		Account basicAccount = accountRepository
			.findByUserIdAndAccountType(userId, AccountType.BASIC)
			.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

		Long paymentAmount = subscription.getPaymentAmount();

		if (basicAccount.getBalance() < paymentAmount) {
			throw new BusinessException(ErrorCode.ACCOUNT_INSUFFICIENT);
		}

		Long beforeBasic = basicAccount.getBalance();
		Long beforeSub = subscription.getAccount().getBalance();

		basicAccount.setBalance(beforeBasic - paymentAmount);
		accountRepository.save(basicAccount);

		Account subAccount = subscription.getAccount();
		subAccount.setBalance(beforeSub + paymentAmount);
		accountRepository.save(subAccount);

		log.info("납입 완료: 회원ID={}, 계좌ID={}, 상품명={}, 납입금액={}원, 기본계좌잔액 {}→{}, 상품계좌잔액 {}→{}",
			userId, accountId,
			subscription.getProduct().getProductName(),
			paymentAmount,
			beforeBasic, basicAccount.getBalance(),
			beforeSub, subAccount.getBalance()
		);
	}

	@Transactional
	public void cancel(Long userId, Long accountId) {
		Subscription subscription = getValidSubscription(accountId, userId);

		BigDecimal cancelInterest = interestCalculator.calculateCancelInterest(subscription);
		Long currentBalance = interestCalculator.calculateCurrentBalance(subscription);

		Account basicAccount = accountRepository
			.findByUserIdAndAccountType(userId, AccountType.BASIC)
			.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

		Long transferAmount = currentBalance + cancelInterest.longValue();
		basicAccount.setBalance(basicAccount.getBalance() + transferAmount);
		accountRepository.save(basicAccount);

		Account subAccount = subscription.getAccount();
		subAccount.setBalance(0L);
		accountRepository.save(subAccount);

		subscription.setStatus(AccountStatus.CANCELLED);
		subscription.setCancelledAt(LocalDateTime.now());

		log.info("상품 해지 완료: 회원ID={}, 계좌ID={}, 이체금액={}", userId, accountId, transferAmount);
	}

	@Transactional
	public void deposit(Long userId, Long accountId, Long amount) {
		Account account = accountRepository.findById(accountId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

		if (!account.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		if (account.getAccountType() != AccountType.BASIC) {
			throw new BusinessException(ErrorCode.INVALID_INPUT, "입출금 계좌만 충전 가능합니다.");
		}

		account.setBalance(account.getBalance() + amount);
		accountRepository.save(account);

		log.info("충전 완료: 회원ID={}, 계좌ID={}, 충전금액={}, 잔액={}",
			userId, accountId, amount, account.getBalance());
	}

	private Subscription getValidSubscription(Long accountId, Long userId) {
		Subscription subscription = subscriptionRepository.findByAccountId(accountId)
			.orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

		if (!subscription.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		if (subscription.getStatus() == AccountStatus.CANCELLED) {
			throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_CANCELLED);
		}
		if (subscription.getStatus() == AccountStatus.MATURED) {
			throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_MATURED);
		}

		return subscription;
	}

	// 계좌번호 검증 및 결정
	public String resolveAccountNum(String wishAccountNum) {
		if (wishAccountNum != null && !wishAccountNum.isBlank()) {
			if (accountRepository.existsByAccountNum(AccountUtil.encrypt(wishAccountNum))) {
				throw new BusinessException(ErrorCode.ACCOUNT_NUM_DUPLICATE);
			}
			return wishAccountNum;
		}
		return generateUniqueAccountNum();
	}

	private String generateUniqueAccountNum() {
		int maxRetry = 10;
		for (int i = 0; i < maxRetry; i++) {
			String accountNum = generateAccountNum();
			if (!accountRepository.existsByAccountNum(AccountUtil.encrypt(accountNum))) {
				return accountNum;
			}
		}
		throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
			"계좌번호 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
	}

	private String generateAccountNum() {
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 11; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}
}
