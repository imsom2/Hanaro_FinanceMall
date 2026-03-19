package com.hana8.hanaro.service;

import com.hana8.hanaro.common.enums.ProductType;
import com.hana8.hanaro.entity.Product;
import com.hana8.hanaro.entity.Subscription;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class InterestCalculator {

	public BigDecimal calculateMaturityInterest(Long amount, Product product) {
		if (amount == null || product == null || product.getMaturityYield() == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal yield = product.getMaturityYield()
			.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
		int months = product.getPeriod();

		if (product.getProductType() == ProductType.DEPOSIT) {
			BigDecimal periodFactor = BigDecimal.valueOf(months)
				.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

			return BigDecimal.valueOf(amount)
				.multiply(yield)
				.multiply(periodFactor)
				.setScale(2, RoundingMode.FLOOR);
		}

		if (product.getProductType() == ProductType.SAVINGS) {
			BigDecimal monthlyYield = yield.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
			long termSum = (long) months * (months + 1) / 2;

			return BigDecimal.valueOf(amount)
				.multiply(BigDecimal.valueOf(termSum))
				.multiply(monthlyYield)
				.setScale(2, RoundingMode.FLOOR);
		}

		return BigDecimal.ZERO;
	}

	public BigDecimal calculateCancelInterest(Subscription subscription) {
		if (subscription == null) return BigDecimal.ZERO;

		Product product = subscription.getProduct();
		Long amount = subscription.getPaymentAmount();

		BigDecimal yield = product.getCancelYield()
			.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

		long elapsedMonths = ChronoUnit.MONTHS.between(
			subscription.getJoinDate(),
			LocalDate.now()
		);

		if (elapsedMonths <= 0) return BigDecimal.ZERO;

		if (product.getProductType() == ProductType.DEPOSIT) {
			BigDecimal periodFactor = BigDecimal.valueOf(elapsedMonths)
				.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

			return BigDecimal.valueOf(amount)
				.multiply(yield)
				.multiply(periodFactor)
				.setScale(2, RoundingMode.FLOOR);
		}

		if (product.getProductType() == ProductType.SAVINGS) {
			BigDecimal monthlyYield = yield.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
			long termSum = elapsedMonths * (elapsedMonths + 1) / 2;

			return BigDecimal.valueOf(amount)
				.multiply(BigDecimal.valueOf(termSum))
				.multiply(monthlyYield)
				.setScale(2, RoundingMode.FLOOR);
		}

		return BigDecimal.ZERO;
	}

	public Long calculateCurrentBalance(Subscription subscription) {
		if (subscription == null) return 0L;
		return subscription.getAccount().getBalance();
	}
}
