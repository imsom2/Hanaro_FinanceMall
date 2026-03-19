package com.hana8.hanaro.dto;

import com.hana8.hanaro.common.enums.AccountStatus;
import com.hana8.hanaro.common.enums.AccountType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Schema(description = "가입 내역 응답 DTO")
public class SubscriptionDTO {

	@Schema(description = "구독 ID", example = "1")
	private Long subscriptionId;

	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "계좌 ID")
	private Long accountId;

	@Schema(description = "계좌번호", example = "100-1234-5678")
	private String accountNum;

	@Schema(description = "계좌 유형", allowableValues = {"DEPOSIT", "SAVINGS"})
	private AccountType accountType;

	@Schema(description = "잔액", example = "900000")
	private Long balance;

	@Schema(description = "상품명", example = "하나 목돈 마련 적금")
	private String productName;

	@Schema(description = "가입일", example = "2025-12-18")
	private LocalDate joinDate;

	@Schema(description = "만기일", example = "2026-12-18")
	private LocalDate endDate;

	@Schema(description = "상태", allowableValues = {"ACTIVE", "MATURED", "CANCELLED"})
	private AccountStatus status;

	@Schema(description = "납입 금액", example = "300000")
	private Long paymentAmount;

	@Schema(description = "만기 예상 이자", example = "819000.00")
	private BigDecimal maturityInterest;
}
