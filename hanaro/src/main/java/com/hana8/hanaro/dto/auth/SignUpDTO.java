package com.hana8.hanaro.dto.auth;

import com.hana8.hanaro.common.serializer.AccountNumSerializer;

import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDTO {

	private Long id;
	private String email;
	private String name;
	@JsonSerialize(using = AccountNumSerializer.class)
	private String maskedAccountNum;
}
