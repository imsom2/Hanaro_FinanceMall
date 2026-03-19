package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.SubscriptionDTO;
import com.hana8.hanaro.dto.SubscriptionRequestDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper{

	@Mapping(source = "id", target = "subscriptionId")
	@Mapping(source = "account.id", target = "accountId")
	@Mapping(source = "account.accountNum", target = "accountNum")
	@Mapping(source = "account.accountType", target = "accountType")
	@Mapping(source = "account.balance", target = "balance")
	@Mapping(source = "product.productName", target = "productName")
	SubscriptionDTO toListDTO(Subscription subscription);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", source = "user")
	@Mapping(target = "product", source = "product")
	@Mapping(target = "account", source = "account")
	@Mapping(target = "status", constant = "ACTIVE")
	@Mapping(target = "paymentDay", source = "finalDay")
	@Mapping(target = "paymentDayOfWeek", source = "finalDayOfWeek")
	@Mapping(target = "maturityInterest", source = "maturityInterest")
	@Mapping(target = "joinDate", expression = "java(java.time.LocalDate.now())")
	@Mapping(target = "endDate", expression = "java(java.time.LocalDate.now().plusMonths(product.getPeriod()))")
	Subscription toEntity(SubscriptionRequestDTO dto, User user, Product product, Account account,
		Integer finalDay, DayOfWeek finalDayOfWeek, BigDecimal maturityInterest);
}
