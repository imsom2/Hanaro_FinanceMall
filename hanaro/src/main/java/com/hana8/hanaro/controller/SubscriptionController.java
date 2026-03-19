package com.hana8.hanaro.controller;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.SubscriptionDTO;
import com.hana8.hanaro.dto.SubscriptionRequestDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.service.SubscriptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "가입 내역", description = "상품 가입 내역 관련 일반 USER용 API입니다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SubscriptionController {

	private final SubscriptionService subscriptionService;

	@Operation(summary = "내 가입 내역 조회", description = "로그인한 사용자가 가입한 모든 상품 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
	})
	@GetMapping("/subscriptions")
	public SuccessResponseDTO<List<SubscriptionDTO>> getMySubscriptions(
		@AuthenticationPrincipal UserDetailsDTO userDetails
	) {
		return SuccessResponseDTO.of(SuccessCode._OK,
			subscriptionService.getSubscriptions(userDetails.getId()));
	}
}
