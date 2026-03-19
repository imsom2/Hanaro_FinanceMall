package com.hana8.hanaro.mapper;

import com.hana8.hanaro.dto.UserAdminDTO;
import com.hana8.hanaro.dto.UserAdminSubscriptionDTO;
import com.hana8.hanaro.entity.Subscription;
import com.hana8.hanaro.entity.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAdminMapper{

	@Mapping(source = "createdAt", target = "createdAt")
	UserAdminDTO toUserDTO(User user);

	List<UserAdminDTO> toUserDTOList(List<User> users);

	@Mapping(source = "id", target = "subscriptionId")
	@Mapping(source = "account.id", target = "accountId")
	@Mapping(source = "account.accountNum", target = "accountNum")
	@Mapping(source = "account.accountType", target = "accountType")
	@Mapping(source = "account.balance", target = "balance")
	@Mapping(source = "product.productName", target = "productName")
	UserAdminSubscriptionDTO toSubscriptionDTO(Subscription subscription);

	List<UserAdminSubscriptionDTO> toSubscriptionDTOList(List<Subscription> subscriptions);
}
