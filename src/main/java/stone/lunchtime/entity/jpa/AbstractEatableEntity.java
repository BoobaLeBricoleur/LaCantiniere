// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.math.BigDecimal;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import stone.lunchtime.entity.EntityUtils;

/**
 * The persistent class for the eatable table.
 */
@MappedSuperclass
public abstract class AbstractEatableEntity extends AbstractLabeledEntity {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractEatableEntity.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "price_df", precision = 5, scale = 2)
	private BigDecimal priceDF;

	@Column(name = "available_for_weeks_and_days", length = 1000)
	private String availableForWeeksAndDays;

	/**
	 * Gets the attribute value.
	 *
	 * @return the availableForWeeksAndDays value.
	 */
	public String getAvailableForWeeksAndDays() {
		return this.availableForWeeksAndDays;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pAvailableForWeeks the new value for availableForWeeksAndDays
	 *                           attribute
	 */
	public void setAvailableForWeeksAndDays(String pAvailableForWeeks) {
		this.availableForWeeksAndDays = EntityUtils.checkAndClean(pAvailableForWeeks);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the priceDF value.
	 */
	public BigDecimal getPriceDF() {
		return this.priceDF;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPriceDF the new value for priceDF attribute
	 */
	public void setPriceDF(BigDecimal pPriceDF) {
		if (pPriceDF == null || pPriceDF.doubleValue() < 0 || pPriceDF.doubleValue() > 999) {
			AbstractEatableEntity.LOG.atWarn().log("Will use default price for entity {}", this.getClass().getSimpleName());
			this.priceDF = BigDecimal.valueOf(0.01D);
		} else {
			this.priceDF = pPriceDF;
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",priceDF=");
		sb.append(this.getPriceDF());
		sb.append(",availableForWeeksAndDays=");
		if (this.getAvailableForWeeksAndDays() != null) {
			sb.append(this.getAvailableForWeeksAndDays());
		} else {
			sb.append("all");
		}
		sb.append("}");
		return sb.toString();
	}
}
