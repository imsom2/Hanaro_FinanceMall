package com.hana8.hanaro.dto.auth;

import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDTO {

	private Long id;
	private String email;
	private String name;
	private String accountNum;
}
