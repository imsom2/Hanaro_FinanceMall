package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.AccountDTO;
import com.hana8.hanaro.entity.Account;
import com.hana8.hanaro.entity.Subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

	@Mapping(target = "accountId", source = "account.id")
	@Mapping(target = "accountNum", source = "account.accountNum")
	@Mapping(target = "accountType", source = "account.accountType")
	@Mapping(target = "balance", source = "account.balance")
	@Mapping(target = "productName", ignore = true)
	@Mapping(target = "joinDate", ignore = true)
	@Mapping(target = "endDate", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "paymentAmount", ignore = true)
	@Mapping(target = "maturityYield", ignore = true)
	@Mapping(target = "cancelYield", ignore = true)
	@Mapping(target = "maturityInterest", ignore = true)
	@Mapping(target = "cancelInterest", ignore = true)
	AccountDTO toBasicAccountDTO(Account account);

	@Mapping(target = "accountId", source = "subscription.account.id")
	@Mapping(target = "accountNum", source = "subscription.account.accountNum")
	@Mapping(target = "accountType", source = "subscription.account.accountType")
	@Mapping(target = "balance", source = "subscription.account.balance")
	@Mapping(target = "productName", source = "subscription.product.productName")
	@Mapping(target = "joinDate", source = "subscription.joinDate")
	@Mapping(target = "endDate", source = "subscription.endDate")
	@Mapping(target = "status", source = "subscription.status")
	@Mapping(target = "paymentAmount", source = "subscription.paymentAmount")
	@Mapping(target = "maturityYield", source = "subscription.product.maturityYield")
	@Mapping(target = "cancelYield", source = "subscription.product.cancelYield")
	@Mapping(target = "maturityInterest", source = "subscription.maturityInterest")
	@Mapping(target = "cancelInterest", ignore = true)
	AccountDTO toSubscriptionAccountDTO(Subscription subscription);

}
