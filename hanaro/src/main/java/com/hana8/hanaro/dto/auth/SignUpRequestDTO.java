package com.hana8.hanaro.dto.auth;

import com.hana8.hanaro.common.validator.AccountNumber;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class SignUpRequestDTO {

	@NotBlank(message = "이메일은 필수입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@Schema(description = "이메일", example = "somi@gmail.com")
	private String email;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
		message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
	)
	@Schema(description = "비밀번호", example = "somihappy2002!")
	private String passwd;

	@NotBlank(message = "이름은 필수입니다.")
	@Size(max = 20, message = "이름은 20자 이하여야 합니다.")
	@Schema(description = "이름", example = "남소미")
	private String name;

	@AccountNumber
	@Schema(description = "희망 계좌번호 11자리 숫자 (미입력 시 자동 생성)", example = "11012345678")
	private String accountNum;
}
