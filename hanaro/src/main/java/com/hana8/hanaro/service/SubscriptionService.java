package com.hana8.hanaro.service;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;
import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.common.exception.BusinessException;
import com.hana8.hanaro.common.exception.ErrorCode;
import com.hana8.hanaro.dto.SubscriptionDTO;
import com.hana8.hanaro.dto.SubscriptionRequestDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;
import com.hana8.hanaro.mapper.SubscriptionMapper;
import com.hana8.hanaro.repository.AccountRepository;
import com.hana8.hanaro.repository.ProductRepository;
import com.hana8.hanaro.repository.SubscriptionRepository;
import com.hana8.hanaro.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final AccountRepository accountRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final SubscriptionMapper subscriptionMapper;
	private final InterestCalculator interestCalculator;
	private final AccountService accountService;


	@Transactional
	public void subscribeProduct(Long userId, Long productId, SubscriptionRequestDTO dto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		Product product = productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

		Long amount = dto.getPaymentAmount();
		if (amount < product.getMin() || amount > product.getMax()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT,
				String.format("납입 금액은 %d원 이상 %d원 이하로 설정해야 합니다.", product.getMin(), product.getMax()));
		}
		String accountNum = accountService.resolveAccountNum(dto.getWishAccountNum());

		if (product.getProductType() == ProductType.DEPOSIT) {
			Account basicAccount = accountRepository
				.findByUserIdAndAccountType(userId, AccountType.BASIC)
				.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

			if (basicAccount.getBalance() < amount) {
				throw new BusinessException(ErrorCode.ACCOUNT_INSUFFICIENT);
			}

			Long beforeBalance = basicAccount.getBalance();
			basicAccount.setBalance(beforeBalance - amount);
			accountRepository.save(basicAccount);

			log.info("예금 가입 이체: 회원ID={}, 상품명={}, 이체금액={}원, 입출금계좌잔액 {}→{}",
				userId, product.getProductName(), amount, beforeBalance, basicAccount.getBalance());
		}

		Integer finalDay = null;
		DayOfWeek finalDayOfWeek = null;

		if (product.getProductType() == ProductType.SAVINGS) {
			finalDay = dto.getPaymentDay();
			finalDayOfWeek = dto.getPaymentDayOfWeek();
		}

		BigDecimal maturityInterest = interestCalculator.calculateMaturityInterest(amount, product);

		Account subAccount = Account.builder()
			.user(user)
			.accountNum(accountNum)
			.accountType(product.getProductType() == ProductType.DEPOSIT
				? AccountType.DEPOSIT
				: AccountType.SAVINGS)
			.balance(product.getProductType() == ProductType.DEPOSIT ? amount : 0L)
			.build();
		accountRepository.save(subAccount);

		Subscription subscription = subscriptionMapper.toEntity(
			dto, user, product, subAccount, finalDay, finalDayOfWeek, maturityInterest
		);
		subscriptionRepository.save(subscription);

		log.info("상품 가입 성공: 회원ID={}, 구독ID={}, 상품명={}, 예상만기이자={}",
			userId, subscription.getId(), product.getProductName(), maturityInterest);
	}

	public List<SubscriptionDTO> getSubscriptions(Long userId) {
		List<Subscription> subscriptions = subscriptionRepository.findAllByUserIdAndStatus(userId, AccountStatus.ACTIVE);
		return subscriptions.stream()
			.map(sub -> {
				SubscriptionDTO dto = subscriptionMapper.toListDTO(sub);
				dto.setBalance(interestCalculator.calculateCurrentBalance(sub));
				return dto;
			})
			.toList();
	}
}
