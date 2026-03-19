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
import com.hana8.hanaro.service.InterestCalculator;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitLoader implements ApplicationRunner {

	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final AccountRepository accountRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final PasswordEncoder passwordEncoder;
	private final InterestCalculator interestCalculator;

	@Override
	@Transactional
	public void run(@Nullable ApplicationArguments args) {
		if (userRepository.count() > 0) return;

		User admin = userRepository.save(User.builder()
			.email("hanaro@hanaro.com")
			.passwd(passwordEncoder.encode("12345678"))
			.name("관리자")
			.role(Role.ROLE_ADMIN)
			.build());

		User user1 = userRepository.save(User.builder()
			.email("somi@gmail.com")
			.passwd(passwordEncoder.encode("somihappy2002!"))
			.name("홍길동")
			.role(Role.ROLE_USER)
			.build());

		User user2 = userRepository.save(User.builder()
			.email("user2@hanaro.com")
			.passwd(passwordEncoder.encode("user1234!"))
			.name("김하나")
			.role(Role.ROLE_USER)
			.build());

		User user3 = userRepository.save(User.builder()
			.email("user3@hanaro.com")
			.passwd(passwordEncoder.encode("user1234!"))
			.name("이철수")
			.role(Role.ROLE_USER)
			.build());

		Product depositProduct = productRepository.save(Product.builder()
			.productName("하나 안심 정기예금")
			.productType(ProductType.DEPOSIT)
			.min(100000L).max(100000000L).period(12)
			.maturityYield(new BigDecimal("3.50"))
			.cancelYield(new BigDecimal("1.00"))
			.description("1년 만기 정기예금 상품입니다.")
			.build());

		Product savingsProduct = productRepository.save(Product.builder()
			.productName("하나 목돈 마련 적금")
			.productType(ProductType.SAVINGS)
			.min(10000L).max(1000000L).period(12)
			.maturityYield(new BigDecimal("4.20"))
			.cancelYield(new BigDecimal("1.50"))
			.description("매월 자유롭게 납입하는 적금 상품입니다.")
			.build());

		Product weeklySavingsProduct = productRepository.save(Product.builder()
			.productName("하나 주간 적금")
			.productType(ProductType.SAVINGS)
			.min(10000L).max(500000L).period(12)
			.maturityYield(new BigDecimal("3.80"))
			.cancelYield(new BigDecimal("1.20"))
			.description("매주 납입하는 주간 적금 상품입니다.")
			.build());

		productRepository.save(Product.builder()
			.productName("삭제된 상품")
			.productType(ProductType.DEPOSIT)
			.min(100000L).max(100000000L).period(12)
			.maturityYield(new BigDecimal("2.00"))
			.cancelYield(new BigDecimal("0.50"))
			.description("삭제된 상품입니다.")
			.deleted(true)
			.build());

		productImageRepository.save(ProductImage.builder()
			.product(depositProduct)
			.orgName("deposit_main.jpg")
			.saveName("20260314_deposit_main.jpg")
			.saveDir("20260314")
			.build());

		productImageRepository.save(ProductImage.builder()
			.product(savingsProduct)
			.orgName("savings_main.jpg")
			.saveName("20260314_savings_main.jpg")
			.saveDir("20260314")
			.build());

		productImageRepository.save(ProductImage.builder()
			.product(depositProduct)
			.orgName("deposit_sub.jpg")
			.saveName("20260314_deposit_sub.jpg")
			.saveDir("20260314")
			.deleted(true)
			.build());


		accountRepository.save(Account.builder()
			.user(admin)
			.accountNum("11099999999")
			.accountType(AccountType.BASIC)
			.balance(0L)
			.build());

		Account basicAccount1 = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11012345678")
			.accountType(AccountType.BASIC)
			.balance(10000000L)
			.build());

		Account depositAccount1 = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11098765432")
			.accountType(AccountType.DEPOSIT)
			.balance(5000000L)
			.build());

		Account savingsAccount1 = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11011122233")
			.accountType(AccountType.SAVINGS)
			.balance(900000L) // 3개월 × 300,000원
			.build());

		Account weeklySavingsAccount1 = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11055566677")
			.accountType(AccountType.SAVINGS)
			.balance(400000L)
			.build());

		Account maturityAccount = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11044455566")
			.accountType(AccountType.DEPOSIT)
			.balance(1000000L)
			.build());

		Account maturedAccount = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11077788899")
			.accountType(AccountType.DEPOSIT)
			.balance(0L)
			.build());

		Account cancelledAccount = accountRepository.save(Account.builder()
			.user(user1)
			.accountNum("11088899900")
			.accountType(AccountType.SAVINGS)
			.balance(0L)
			.build());

		Account basicAccount2 = accountRepository.save(Account.builder()
			.user(user2)
			.accountNum("11022233344")
			.accountType(AccountType.BASIC)
			.balance(100L)
			.build());

		Account savingsAccount2 = accountRepository.save(Account.builder()
			.user(user2)
			.accountNum("11033344455")
			.accountType(AccountType.SAVINGS)
			.balance(0L)
			.build());

		accountRepository.save(Account.builder()
			.user(user3)
			.accountNum("11066677788")
			.accountType(AccountType.BASIC)
			.balance(3000000L)
			.build());

		BigDecimal depositInterest = interestCalculator.calculateMaturityInterest(5000000L, depositProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(depositProduct)
			.account(depositAccount1)
			.user(user1)
			.paymentAmount(5000000L)
			.joinDate(LocalDate.now().minusMonths(3))
			.endDate(LocalDate.now().plusMonths(9))
			.status(AccountStatus.ACTIVE)
			.maturityInterest(depositInterest)
			.build());

		BigDecimal savingsInterest = interestCalculator.calculateMaturityInterest(300000L, savingsProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(savingsProduct)
			.account(savingsAccount1)
			.user(user1)
			.paymentAmount(300000L)
			.paymentCycle(PaymentCycle.MONTHLY)
			.paymentDay(LocalDate.now().getDayOfMonth()) // 오늘 날짜
			.joinDate(LocalDate.now().minusMonths(3))
			.endDate(LocalDate.now().plusMonths(9))
			.status(AccountStatus.ACTIVE)
			.maturityInterest(savingsInterest)
			.build());

		BigDecimal weeklyInterest = interestCalculator.calculateMaturityInterest(100000L, weeklySavingsProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(weeklySavingsProduct)
			.account(weeklySavingsAccount1)
			.user(user1)
			.paymentAmount(100000L)
			.paymentCycle(PaymentCycle.WEEKLY)
			.paymentDayOfWeek(LocalDate.now().getDayOfWeek()) // 오늘 요일
			.joinDate(LocalDate.now().minusMonths(1))
			.endDate(LocalDate.now().plusMonths(11))
			.status(AccountStatus.ACTIVE)
			.maturityInterest(weeklyInterest)
			.build());

		BigDecimal maturityInterest = interestCalculator.calculateMaturityInterest(1000000L, depositProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(depositProduct)
			.account(maturityAccount)
			.user(user1)
			.paymentAmount(1000000L)
			.joinDate(LocalDate.now().minusMonths(12))
			.endDate(LocalDate.now()) // 오늘 만기
			.status(AccountStatus.ACTIVE)
			.maturityInterest(maturityInterest)
			.build());

		BigDecimal maturedInterest = interestCalculator.calculateMaturityInterest(2000000L, depositProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(depositProduct)
			.account(maturedAccount)
			.user(user1)
			.paymentAmount(2000000L)
			.joinDate(LocalDate.now().minusMonths(13))
			.endDate(LocalDate.now().minusMonths(1))
			.status(AccountStatus.MATURED)
			.maturedAt(LocalDateTime.now().minusMonths(1))
			.maturityInterest(maturedInterest)
			.build());

		BigDecimal cancelledInterest1 = interestCalculator.calculateMaturityInterest(500000L, savingsProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(savingsProduct)
			.account(cancelledAccount)
			.user(user1)
			.paymentAmount(500000L)
			.paymentCycle(PaymentCycle.MONTHLY)
			.paymentDay(15)
			.joinDate(LocalDate.now().minusMonths(6))
			.endDate(LocalDate.now().plusMonths(6))
			.status(AccountStatus.CANCELLED)
			.cancelledAt(LocalDateTime.now().minusDays(10))
			.maturityInterest(cancelledInterest1)
			.build());

		BigDecimal cancelledInterest2 = interestCalculator.calculateMaturityInterest(300000L, savingsProduct);
		subscriptionRepository.save(Subscription.builder()
			.product(savingsProduct)
			.account(savingsAccount2)
			.user(user2)
			.paymentAmount(300000L)
			.paymentCycle(PaymentCycle.MONTHLY)
			.paymentDay(15)
			.joinDate(LocalDate.now().minusMonths(6))
			.endDate(LocalDate.now().plusMonths(6))
			.status(AccountStatus.CANCELLED)
			.cancelledAt(LocalDateTime.now().minusDays(10))
			.maturityInterest(cancelledInterest2)
			.build());

	}
}
