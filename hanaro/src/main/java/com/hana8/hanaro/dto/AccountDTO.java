package com.hana8.hanaro.dto;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Schema(description = "계좌 정보 응답 DTO")
public class AccountDTO {
	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "계좌 ID")
	private Long accountId;

	@Schema(description = "계좌번호", example = "100-1234-5678")
	private String accountNum;

	@Schema(description = "계좌 유형", allowableValues = {"BASIC", "DEPOSIT", "SAVINGS"})
	private AccountType accountType;

	@Schema(description = "잔액", example = "5000000")
	private Long balance;

	@Schema(description = "상품명 (상품 계좌인 경우)", example = "하나 안심 정기예금")
	private String productName;

	@Schema(description = "가입일 (상품 계좌인 경우)", example = "2025-12-18")
	private LocalDate joinDate;

	@Schema(description = "만기일 (상품 계좌인 경우)", example = "2026-12-18")
	private LocalDate endDate;

	@Schema(description = "상태 (상품 계좌인 경우)", allowableValues = {"ACTIVE", "MATURED", "CANCELLED"})
	private AccountStatus status;

	@Schema(description = "납입 금액 (상품 계좌인 경우)", example = "300000")
	private Long paymentAmount;

	@Schema(description = "만기 수익률 (상품 계좌인 경우)", example = "3.50")
	private BigDecimal maturityYield;

	@Schema(description = "해지 수익률 (상품 계좌인 경우)", example = "1.00")
	private BigDecimal cancelYield;

	@Schema(description = "만기 예상 이자 (상품 계좌인 경우)", example = "175000.00")
	private BigDecimal maturityInterest;

	@Schema(description = "현재 해지 시 이자 (상품 계좌인 경우)", example = "12500.00")
	private BigDecimal cancelInterest;
}
