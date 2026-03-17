package com.hana8.hanaro.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.auth.LoginRequest;
import com.hana8.hanaro.dto.auth.SignUpDTO;
import com.hana8.hanaro.dto.auth.SignUpRequestDTO;
import com.hana8.hanaro.service.AuthService;
import com.hana8.hanaro.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증/인가", description = "인증/인가에 관련한 API입니다")
public class AuthController {

	private final UserService userService;
	private final AuthService authService;

	@Operation(summary = "회원가입", description = "회원가입과 동시에 기본 입출금 계좌가 생성됩니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "회원가입 성공"),
		@ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content),
		@ApiResponse(responseCode = "409", description = "이메일 또는 계좌번호 중복", content = @Content)
	})
	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public SuccessResponseDTO<SignUpDTO> signUp(@Validated @RequestBody SignUpRequestDTO dto) {
		SignUpDTO response = userService.signUp(dto);
		return SuccessResponseDTO.of(SuccessCode.SIGNUP_SUCCESS, response);
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 accessToken / refreshToken 정보를 반환합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인 성공"),
		@ApiResponse(responseCode = "401", description = "로그인 실패", content = @Content)
	})
	@PostMapping("/signin")
	public SuccessResponseDTO<Map<String, Object>> signIn(@RequestBody @Validated LoginRequest loginRequest) {
		Map<String, Object> response = authService.signIn(loginRequest);
		return SuccessResponseDTO.of(SuccessCode.SIGNIN_SUCCESS, response);
	} // <- 여기서 } 하나가 과하게 들어가 클래스가 닫혔었습니다.

	@Operation(summary = "토큰 재발급", description = "Authorization 헤더의 access token과 refreshToken을 이용해 access token을 재발급합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
		@ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰", content = @Content)
	})
	@PostMapping("/refresh")
	public SuccessResponseDTO<Map<String, Object>> refresh(
		@RequestHeader("Authorization") String authHeader,
		@RequestParam String refreshToken
	) {
		Map<String, Object> response = authService.refresh(authHeader, refreshToken);
		return SuccessResponseDTO.of(SuccessCode.TOKEN_REFRESH_SUCCESS, response);
	}
}
