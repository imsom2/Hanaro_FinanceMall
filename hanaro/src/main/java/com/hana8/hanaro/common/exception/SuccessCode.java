package com.hana8.hanaro.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
	// 공통
	_OK(HttpStatus.OK, "COMMON200", "성공입니다."),

	// Auth 관련
	SIGNUP_SUCCESS(HttpStatus.CREATED, "AUTH200", "회원가입이 완료되었습니다."),
	SIGNIN_SUCCESS(HttpStatus.OK, "AUTH201", "로그인에 성공하였습니다."),
	TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "AUTH202", "토큰이 재발급되었습니다."),

	// 상품 관련
	PRODUCT_CREATED(HttpStatus.CREATED, "PRODUCT200", "상품 등록이 완료되었습니다."),
	PRODUCT_UPDATED(HttpStatus.OK, "PRODUCT201", "상품 수정이 완료되었습니다."),
	PRODUCT_DELETED(HttpStatus.OK, "PRODUCT202", "상품이 삭제되었습니다."),

	// 상품 가입 관련
	SUBSCRIPTION_SUCCESS(HttpStatus.CREATED, "SUB200", "상품에 가입되었습니다."),
	SUBSCRIPTION_CANCELLED(HttpStatus.OK, "SUB201", "상품이 해지되었습니다."),
	TRANSFER_SUCCESS(HttpStatus.OK, "SUB202", "납입이 완료되었습니다."),
	SUBSCRIPTION_MATURED(HttpStatus.OK, "SUB203", "만기 처리가 완료되었습니다."),

	// 계좌 관련
	DEPOSIT_SUCCESS(HttpStatus.OK, "ACCOUNT200", "충전이 완료되었습니다."),

	// 이미지 관련
	IMAGE_UPLOADED(HttpStatus.OK, "IMAGE200", "이미지 업로드가 완료되었습니다."),
	IMAGE_DELETED(HttpStatus.OK, "IMAGE201", "이미지가 삭제되었습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
