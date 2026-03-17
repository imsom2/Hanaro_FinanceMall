package com.hana8.hanaro.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
	@NotBlank(message = "이메일은 필수입니다!")
	@Size(min = 1, max = 30)
	@Schema(description = "이메일", example = "somi@gmail.com")
	@NotBlank String email,

	@Schema(description = "비밀번호", example = "somihappy2002!")
	@NotBlank String passwd
) {
}
