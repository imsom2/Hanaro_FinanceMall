package com.hana8.hanaro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 목록 응답 DTO")
public class UserAdminDTO {
	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "유저 ID")
	private Long id;

	@Schema(description = "이메일", example = "somi@gmail.com")
	private String email;

	@Schema(description = "이름", example = "홍길동")
	private String name;

	@Schema(description = "가입일시", example = "2026-03-19T10:00:00")
	private LocalDateTime createdAt;
}
