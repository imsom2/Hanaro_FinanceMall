package com.hana8.hanaro.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter
public class AccountNumConverter implements AttributeConverter<String, String> {
	private static AccountUtil accountUtil;

	@Autowired
	public void setAccountUtil(AccountUtil util) {
			AccountNumConverter.accountUtil = util;
	}

	@Override
	public String convertToDatabaseColumn(String attribute) {
		return accountUtil != null ? accountUtil.encrypt(attribute) : attribute;
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		return accountUtil != null ? accountUtil.decrypt(dbData) : dbData;
	}
}
