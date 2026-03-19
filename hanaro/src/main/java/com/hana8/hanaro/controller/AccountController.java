package com.hana8.hanaro.controller;

import java.util.List;

import com.hana8.hanaro.common.exception.SuccessCode;
import com.hana8.hanaro.common.exception.SuccessResponseDTO;
import com.hana8.hanaro.dto.AccountDTO;
import com.hana8.hanaro.dto.DepositRequestDTO;
import com.hana8.hanaro.dto.auth.UserDetailsDTO;
import com.hana8.hanaro.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "계좌", description = "계좌 관련 일반 USER용 API입니다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	@Operation(summary = "전체 계좌 조회", description = "사용자의 기본 입출금 계좌와 가입한 상품 계좌를 모두 포함한 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	@GetMapping
	public SuccessResponseDTO<List<AccountDTO>> getAllAccounts(
		@AuthenticationPrincipal UserDetailsDTO userDetails
	) {
		return SuccessResponseDTO.of(SuccessCode._OK,
			accountService.getAllAccounts(userDetails.getId()));
	}

	@Operation(summary = "계좌 상세 조회", description = "계좌 ID로 특정 계좌의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음", content = @Content)
	})
	@GetMapping("/{accountId}")
	public SuccessResponseDTO<AccountDTO> getAccountDetail(
		@AuthenticationPrincipal UserDetailsDTO userDetails,
		@PathVariable Long accountId
	) {
		return SuccessResponseDTO.of(SuccessCode._OK,
			accountService.getAccountDetail(userDetails.getId(), accountId));
	}

	@Operation(summary = "납입 (이체)",
		description = "기본 입출금 계좌에서 상품 계좌로 납입금을 이체합니다.\n\n오늘이 납입 날짜가 아니거나 예금 상품인 경우 이체가 거절됩니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "납입 성공"),
		@ApiResponse(responseCode = "400", description = "잔액 부족", content = @Content),
		@ApiResponse(responseCode = "404", description = "계좌 정보를 찾을 수 없음", content = @Content)
	})
	@PostMapping("/{accountId}/transfer")
	public SuccessResponseDTO<Void> transfer(
		@AuthenticationPrincipal UserDetailsDTO userDetails,
		@PathVariable Long accountId
	) {
		accountService.transfer(userDetails.getId(), accountId);
		return SuccessResponseDTO.of(SuccessCode.TRANSFER_SUCCESS);
	}

	@Operation(summary = "상품 중도 해지", description = "가입한 상품을 중도 해지하고 잔액과 해지 이자를 기본 입출금 계좌로 이체합니다.")
	@PostMapping("/{accountId}/cancel")
	public SuccessResponseDTO<Void> cancel(
		@AuthenticationPrincipal UserDetailsDTO userDetails,
		@PathVariable Long accountId
	) {
		accountService.cancel(userDetails.getId(), accountId);
		return SuccessResponseDTO.of(SuccessCode.SUBSCRIPTION_CANCELLED);
	}

	@Operation(summary = "입출금 계좌 충전", description = "기본 입출금 계좌에 금액을 충전합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "충전 성공"),
		@ApiResponse(responseCode = "400", description = "입력값 오류", content = @Content),
		@ApiResponse(responseCode = "404", description = "계좌 없음", content = @Content)
	})
	@PostMapping("/{accountId}/deposit")
	public SuccessResponseDTO<Void> deposit(
		@AuthenticationPrincipal UserDetailsDTO userDetails,
		@PathVariable Long accountId,
		@Valid @RequestBody DepositRequestDTO dto
	) {
		accountService.deposit(userDetails.getId(), accountId, dto.getAmount());
		return SuccessResponseDTO.of(SuccessCode.DEPOSIT_SUCCESS);
	}
}
