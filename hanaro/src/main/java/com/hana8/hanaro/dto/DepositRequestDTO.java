package com.hana8.hanaro.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "입출금 계좌 충전 요청 DTO")
public class DepositRequestDTO {
	@NotNull(message = "충전 금액은 필수입니다.")
	@Min(value = 1, message = "충전 금액은 1원 이상이어야 합니다.")
	@Schema(description = "충전 금액", example = "1000000")
	private Long amount;
}
