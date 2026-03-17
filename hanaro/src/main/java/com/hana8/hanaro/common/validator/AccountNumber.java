package com.hana8.hanaro.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AccountNumberValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountNumber {
	String message() default "유효하지 않은 계좌번호 형식입니다.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
