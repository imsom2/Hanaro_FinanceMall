package com.hana8.hanaro.common.validator;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.DayOfWeek;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hana8.hanaro.common.enums.PaymentCycle;
import com.hana8.hanaro.dto.SubscriptionRequestDTO;

import jakarta.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
class PaymentCycleValidatorTest {

	private PaymentCycleValidator validator;

	@Mock
	private ConstraintValidatorContext context;

	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder builder;

	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

	@BeforeEach
	void setUp() {
		validator = new PaymentCycleValidator();
	}

	@Test
	void nullPaymentCycle_returnsTrue() {
		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(null);

		assertThat(validator.isValid(dto, context)).isTrue();
	}

	@Test
	void monthly_nullPaymentDay_returnsFalse() {
		given(context.buildConstraintViolationWithTemplate(anyString())).willReturn(builder);
		given(builder.addPropertyNode(anyString())).willReturn(nodeBuilder);

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(PaymentCycle.MONTHLY);
		dto.setPaymentDay(null);

		assertThat(validator.isValid(dto, context)).isFalse();
	}

	@Test
	void monthly_paymentDayTooLow_returnsFalse() {
		given(context.buildConstraintViolationWithTemplate(anyString())).willReturn(builder);
		given(builder.addPropertyNode(anyString())).willReturn(nodeBuilder);

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(PaymentCycle.MONTHLY);
		dto.setPaymentDay(0); // < 1

		assertThat(validator.isValid(dto, context)).isFalse();
	}

	@Test
	void monthly_paymentDayTooHigh_returnsFalse() {
		given(context.buildConstraintViolationWithTemplate(anyString())).willReturn(builder);
		given(builder.addPropertyNode(anyString())).willReturn(nodeBuilder);

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(PaymentCycle.MONTHLY);
		dto.setPaymentDay(32); // > 31

		assertThat(validator.isValid(dto, context)).isFalse();
	}

	@Test
	void monthly_validPaymentDay_returnsTrue() {
		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(PaymentCycle.MONTHLY);
		dto.setPaymentDay(15);

		assertThat(validator.isValid(dto, context)).isTrue();
	}

	@Test
	void weekly_nullPaymentDayOfWeek_returnsFalse() {
		given(context.buildConstraintViolationWithTemplate(anyString())).willReturn(builder);
		given(builder.addPropertyNode(anyString())).willReturn(nodeBuilder);

		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(PaymentCycle.WEEKLY);
		dto.setPaymentDayOfWeek(null);

		assertThat(validator.isValid(dto, context)).isFalse();
	}

	@Test
	void weekly_validPaymentDayOfWeek_returnsTrue() {
		SubscriptionRequestDTO dto = new SubscriptionRequestDTO();
		dto.setPaymentCycle(PaymentCycle.WEEKLY);
		dto.setPaymentDayOfWeek(DayOfWeek.MONDAY);

		assertThat(validator.isValid(dto, context)).isTrue();
	}
}
