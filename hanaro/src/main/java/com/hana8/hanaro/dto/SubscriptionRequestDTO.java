package com.hana8.hanaro.dto;

import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.common.validator.AccountNumber;
import com.hana8.hanaro.common.validator.ValidPaymentCycle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidPaymentCycle
@Schema(description = "상품 가입 요청 DTO")
public class SubscriptionRequestDTO {

	@NotNull(message = "납입 금액은 필수입니다.")
	@Min(value = 1, message = "납입 금액은 1원 이상이어야 합니다.")
	@Schema(description = "납입 금액 (상품 min/max 범위 내)", example = "300000")
	private Long paymentAmount;

	@Schema(
		description = "납입 주기 (적금만 해당, 예금은 불필요)",
		allowableValues = {"MONTHLY", "WEEKLY"},
		example = "MONTHLY"
	)
	private PaymentCycle paymentCycle;

	@Schema(description = "납입일 (MONTHLY일 경우 1-31)", example = "25")
	private Integer paymentDay;

	@Schema(
		description = "납입 요일 (WEEKLY일 경우)",
		allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"},
		example = "MONDAY"
	)
	private DayOfWeek paymentDayOfWeek;

	@AccountNumber
	@Schema(description = "희망 계좌번호 11자리 숫자 (미입력 시 자동 생성)", example = "12345678901")
	private String wishAccountNum;
}
