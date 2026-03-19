package com.hana8.hanaro.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 응답 DTO")
public class SignUpDTO {

	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "유저 ID")
	private Long id;

	@Schema(description = "이메일", example = "somi@gmail.com")
	private String email;

	@Schema(description = "이름", example = "남소미")
	private String name;

	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "생성된 기본 입출금 계좌 ID")
	private Long accountId;

	@Schema(description = "계좌번호", example = "110-****-5678")
	private String accountNum;
}
