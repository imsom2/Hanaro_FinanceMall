package com.hana8.hanaro.dto.auth;

import com.hana8.hanaro.common.converter.AccountNumSerializer;

import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import com.hana8.hanaro.common.validator.AccountNumber;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDTO {

	private Long id;
	private String email;
	private String name;
	@AccountNumber
	@JsonSerialize(using = AccountNumSerializer.class)
	private String maskedAccountNum;
}
