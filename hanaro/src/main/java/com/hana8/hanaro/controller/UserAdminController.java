package com.hana8.hanaro.controller;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.UserAdminDTO;
import com.hana8.hanaro.dto.UserAdminSearchDTO;
import com.hana8.hanaro.dto.UserAdminSubscriptionDTO;
import com.hana8.hanaro.service.UserAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "관리자 - 회원 관리", description = "회원을 관리하는 관리자용 API입니다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class UserAdminController {

	private final UserAdminService userAdminService;

	@Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping
	public SuccessResponseDTO<List<UserAdminDTO>> getUsers() {
		return SuccessResponseDTO.of(SuccessCode._OK, userAdminService.getUsers());
	}

	@Operation(summary = "회원 검색", description = "이름 또는 이메일로 회원 ( 가입 상품 목록 )을 검색합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "검색 성공"),
		@ApiResponse(responseCode = "404", description = "회원 없음", content = @Content)
	})
	@GetMapping("/search")
	public SuccessResponseDTO<List<UserAdminSearchDTO>> searchUsers(
		@RequestParam String keyword
	) {
		return SuccessResponseDTO.of(SuccessCode._OK, userAdminService.searchUsers(keyword));
	}

	@Operation(summary = "회원별 가입 상품 조회", description = "특정 회원의 가입 상품 내역을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "회원 없음", content = @Content)
	})
	@GetMapping("/{userId}/subscriptions")
	public SuccessResponseDTO<List<UserAdminSubscriptionDTO>> getUserSubscriptions(
		@PathVariable String userId
	) {
		return SuccessResponseDTO.of(SuccessCode._OK, userAdminService.getUserSubscriptions(Long.valueOf(userId)));
	}

	@Operation(summary = "계좌 만기 처리", description = "특정 회원의 구독을 만기 처리하고 잔액과 만기 이자를 기본 계좌로 이체합니다.\n\n오늘이 만기 날짜가 아닌 경우 이체가 거부됩니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "만기 처리 성공"),
		@ApiResponse(responseCode = "400", description = "이미 만기 또는 해지된 구독", content = @Content),
		@ApiResponse(responseCode = "404", description = "구독 또는 회원 없음", content = @Content)
	})
	@PostMapping("/{userId}/accounts/{accountId}/maturity")
	public SuccessResponseDTO<Void> processMaturity(
		@PathVariable String userId,
		@PathVariable String accountId
	) {
		userAdminService.processMaturity(Long.parseLong(userId), Long.parseLong(accountId));
		return SuccessResponseDTO.of(SuccessCode.SUBSCRIPTION_MATURED);
	}
}
