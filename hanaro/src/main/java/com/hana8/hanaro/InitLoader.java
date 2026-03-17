package com.hana8.hanaro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hana8.hanaro.common.enums.*;
import com.hana8.hanaro.entity.*;
import com.hana8.hanaro.repository.*;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitLoader implements ApplicationRunner {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final AccountRepository accountRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final InterestRepository interestRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(@Nullable ApplicationArguments args) {
		if (userRepository.count() > 0) return;

		// 1. User 생성
		User admin = User.builder()
			.email("admin@hanaro.com").passwd(passwordEncoder.encode("admin1234!"))
			.name("관리자").role(Role.ROLE_ADMIN).build();
		userRepository.save(admin);

		User user1 = User.builder()
			.email("user1@hanaro.com").passwd(passwordEncoder.encode("user1234!"))
			.name("홍길동").role(Role.ROLE_USER).build();
		userRepository.save(user1);

		User user2 = User.builder()
			.email("user2@hanaro.com").passwd(passwordEncoder.encode("user1234!"))
			.name("김하나").role(Role.ROLE_USER).build();
		userRepository.save(user2);

		// 2. Product 생성
		Product depositProduct = Product.builder()
			.productName("하나 안심 정기예금").productType(ProductType.DEPOSIT)
			.min(100000L).max(100000000L).period(12)
			.maturityYield(new BigDecimal("3.50")).cancelYield(new BigDecimal("1.00"))
			.description("1년 만기 정기예금 상품입니다.").build();
		productRepository.save(depositProduct);

		Product savingsProduct = Product.builder()
			.productName("하나 목돈 마련 적금").productType(ProductType.SAVINGS)
			.min(10000L).max(1000000L).period(12)
			.maturityYield(new BigDecimal("4.20")).cancelYield(new BigDecimal("1.50"))
			.description("매월 자유롭게 납입하는 적금 상품입니다.").build();
		productRepository.save(savingsProduct);

		// 3. ProductImage 생성
		ProductImage img1 = ProductImage.builder()
			.product(depositProduct).orgName("deposit_main.jpg")
			.saveName("20260314_deposit_main.jpg").saveDir("2026/03/14").build();
		productImageRepository.save(img1);

		// 4. Account 생성
		Account basicAccount1 = Account.builder()
			.user(user1).accountNum("11012345678").accountType(AccountType.BASIC).balance(5000000L)
			.build();
		accountRepository.save(basicAccount1);

		Account depositAccount1 = Account.builder()
			.user(user1).accountNum("11098765432").accountType(AccountType.DEPOSIT).balance(0L)
			.build();
		accountRepository.save(depositAccount1);

		Account basicAccount2 = Account.builder()
			.user(user2).accountNum("11011122233").accountType(AccountType.BASIC).balance(3000000L)
			.build();
		accountRepository.save(basicAccount2);

		// 5. Subscription 생성
		Subscription sub1 = Subscription.builder()
			.product(depositProduct).account(depositAccount1).user(user1)
			.paymentAmount(5000000L).paymentCycle(PaymentCycle.MONTHLY)
			.joinDate(LocalDate.now().minusMonths(3)).endDate(LocalDate.now().plusMonths(9))
			.status(SubStatus.ACTIVE).build();
		subscriptionRepository.save(sub1);

		// 6. Interest 생성
		Interest interest1 = Interest.builder()
			.subscription(sub1).amount(new BigDecimal("12671.23"))
			.calcDate(LocalDateTime.now().minusMonths(1))
			.appliedRate(new BigDecimal("3.50")).elapsedDays(30).build();
		interestRepository.save(interest1);
	}
}
