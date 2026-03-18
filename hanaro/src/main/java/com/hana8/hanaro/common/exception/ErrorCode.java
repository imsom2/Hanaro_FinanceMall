package com.hana8.hanaro.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// ── Common ────────────────────────────────────────────────────
	INVALID_INPUT(HttpStatus.BAD_REQUEST,           "COMMON_001", "잘못된 입력입니다."),
	INVALID_FILE(HttpStatus.BAD_REQUEST,            "COMMON_002", "유효하지 않은 파일입니다."),
	FILE_NOT_FOUND(HttpStatus.NOT_FOUND,            "COMMON_003", "파일을 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_004", "서버 오류가 발생했습니다."),
	DATABASE_ERROR(HttpStatus.CONFLICT,             "COMMON_005", "데이터 처리 중 충돌이 발생했습니다."),

	// ── Auth ─────────────────────────────────────────────────────
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED,           "AUTH_001", "로그인이 필요합니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN,             "AUTH_002", "접근 권한이 없습니다."),
	JWT_EXPIRED(HttpStatus.UNAUTHORIZED,            "AUTH_003", "토큰이 만료되었습니다."),
	JWT_INVALID(HttpStatus.UNAUTHORIZED,            "AUTH_004", "유효하지 않은 토큰입니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED,           "AUTH_005", "아이디 또는 비밀번호가 올바르지 않습니다."),

	// ── User ─────────────────────────────────────────────────────
	USER_NOT_FOUND(HttpStatus.NOT_FOUND,            "USER_001", "사용자를 찾을 수 없습니다."),
	USER_EMAIL_DUPLICATE(HttpStatus.CONFLICT,       "USER_002", "이미 사용 중인 이메일입니다."),

	// ── Product ──────────────────────────────────────────────────
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,         "PRODUCT_001", "상품을 찾을 수 없습니다."),
	PRODUCT_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "PRODUCT_002", "이미 삭제된 상품입니다."),
	PRODUCT_ID_FORBIDDEN(HttpStatus.BAD_REQUEST,    "PRODUCT_003", "생성 요청에 id를 포함할 수 없습니다."),

	// ── Product Image ────────────────────────────────────────────
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND,           "IMAGE_001", "이미지를 찾을 수 없습니다."),
	IMAGE_NOT_OWNED(HttpStatus.BAD_REQUEST,         "IMAGE_002", "해당 상품의 이미지가 아닙니다."),
	IMAGE_INVALID_TYPE(HttpStatus.BAD_REQUEST,      "IMAGE_003", "지원하지 않는 파일 형식입니다. (jpeg, png, gif, webp만 허용)"),
	IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST,         "IMAGE_004", "파일 1개당 최대 크기는 2MB입니다."),
	IMAGE_TOTAL_TOO_LARGE(HttpStatus.BAD_REQUEST,   "IMAGE_005", "전체 파일 크기 합은 10MB를 초과할 수 없습니다."),
	IMAGE_EMPTY(HttpStatus.BAD_REQUEST,             "IMAGE_006", "업로드할 파일이 없습니다."),
	IMAGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST,   "IMAGE_007", "이미 삭제된 이미지입니다."),

	// ── Account ──────────────────────────────────────────────────
	ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND,         "ACCOUNT_001", "계좌를 찾을 수 없습니다."),
	ACCOUNT_NUM_DUPLICATE(HttpStatus.CONFLICT,      "ACCOUNT_002", "이미 사용 중인 계좌번호입니다."),
	ACCOUNT_INSUFFICIENT(HttpStatus.BAD_REQUEST,    "ACCOUNT_003", "잔액이 부족합니다."),
	ACCOUNT_INVALID_NUM(HttpStatus.BAD_REQUEST,    "ACCOUNT_004", "계좌번호 형식이 올바르지 않습니다."),


	// ── Subscription ─────────────────────────────────────────────
	SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND,    "SUB_001", "가입 내역을 찾을 수 없습니다."),
	SUBSCRIPTION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "SUB_002", "이미 해지된 가입 내역입니다."),
	SUBSCRIPTION_ALREADY_MATURED(HttpStatus.BAD_REQUEST,   "SUB_003", "이미 만기 처리된 가입 내역입니다."),

	// ── Interest ─────────────────────────────────────────────────
	INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST_001", "이자 내역을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
