// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The persistent class for the constraint database table. All default values
 * are set.
 */
@Entity
@Table(name = "ltconstraint")
public class ConstraintEntity extends AbstractJpaEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "order_time_limit", nullable = false)
	private LocalTime orderTimeLimit = LocalTime.of(10, 30, 0);

	@Column(name = "maximum_order_per_day", nullable = false)
	private Integer maximumOrderPerDay = Integer.valueOf(500);

	@Column(name = "rate_vat", precision = 5, scale = 2, nullable = false)
	private BigDecimal rateVAT = BigDecimal.valueOf(20D);

	/**
	 * Gets the attribute value.
	 *
	 * @return the orderTimeLimit value.
	 */
	public LocalTime getOrderTimeLimit() {
		return this.orderTimeLimit;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pOrderTimeLimit the new value for orderTimeLimit attribute
	 */

	public void setOrderTimeLimit(LocalTime pOrderTimeLimit) {
		if (pOrderTimeLimit == null) {
			this.orderTimeLimit = LocalTime.of(10, 30, 0);
		} else {
			this.orderTimeLimit = pOrderTimeLimit;
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the maximumOrderPerDay value.
	 */

	public Integer getMaximumOrderPerDay() {
		return this.maximumOrderPerDay;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMaximumOrderPerDay the new value for maximumOrderPerDay attribute
	 */

	public void setMaximumOrderPerDay(Integer pMaximumOrderPerDay) {
		if (pMaximumOrderPerDay == null || pMaximumOrderPerDay.intValue() <= 0) {
			this.maximumOrderPerDay = Integer.valueOf(500);
		} else {
			this.maximumOrderPerDay = pMaximumOrderPerDay;
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the rateVAT value.
	 */

	public BigDecimal getRateVAT() {
		return this.rateVAT;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pRateVAT the new value for rateVAT attribute
	 */

	public void setRateVAT(BigDecimal pRateVAT) {
		if (pRateVAT == null || pRateVAT.doubleValue() < 0) {
			this.rateVAT = BigDecimal.valueOf(20D);
		} else {
			this.rateVAT = pRateVAT;
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",orderTimeLimit=");
		sb.append(this.getOrderTimeLimit());
		sb.append(",maximumOrderPerDay=");
		sb.append(this.getMaximumOrderPerDay());
		sb.append(",rateVAT=");
		sb.append(this.getRateVAT());
		sb.append("}");
		return sb.toString();
	}

}
