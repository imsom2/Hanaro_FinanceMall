package com.hana8.hanaro.common.validator;

import com.hana8.hanaro.dto.SubscriptionRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentCycleValidator implements ConstraintValidator<ValidPaymentCycle, SubscriptionRequestDTO> {

	@Override
	public boolean isValid(SubscriptionRequestDTO dto, ConstraintValidatorContext context) {
		if (dto.getPaymentCycle() == null) return true;

		if (dto.getPaymentCycle() == com.hana8.hanaro.common.enums.PaymentCycle.MONTHLY) {
			if (dto.getPaymentDay() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("매월 납입 시 납입일(1-31)이 필요합니다.")
					.addPropertyNode("paymentDay")
					.addConstraintViolation();
				return false;
			}
			if (dto.getPaymentDay() < 1 || dto.getPaymentDay() > 31) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("납입일은 1-31 사이여야 합니다.")
					.addPropertyNode("paymentDay")
					.addConstraintViolation();
				return false;
			}
		}

		if (dto.getPaymentCycle() == com.hana8.hanaro.common.enums.PaymentCycle.WEEKLY) {
			if (dto.getPaymentDayOfWeek() == null) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("매주 납입 시 납입 요일이 필요합니다.")
					.addPropertyNode("paymentDayOfWeek")
					.addConstraintViolation();
				return false;
			}
		}

		return true;
	}
}
