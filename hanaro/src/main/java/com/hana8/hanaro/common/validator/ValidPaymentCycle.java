package com.hana8.hanaro.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PaymentCycleValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPaymentCycle {
	String message() default "납입 주기에 맞는 납입일 정보가 필요합니다.";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
