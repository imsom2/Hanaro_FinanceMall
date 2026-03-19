package com.hana8.hanaro.dto;

import com.hana8.hanaro.common.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "회원 검색 응답 DTO)")
public class UserAdminSearchDTO {
	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "유저 ID")
	private Long id;

	@Schema(description = "이메일", example = "somi@gmail.com")
	private String email;

	@Schema(description = "이름", example = "홍길동")
	private String name;

	@Schema(description = "가입일시", example = "2026-03-19T10:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "가입 내역 목록")
	private List<UserAdminSubscriptionDTO> subscriptions;
}
