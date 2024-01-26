// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.DtoUtils;

/**
 * The dto class for the constraint database table.
 */
@Schema(description = "Constraint element used for global operations.")
public class ConstraintDtoIn extends AbstractDtoIn {
	public static final String PATTERN = "HH:mm:ss";
	private static final Logger LOG = LoggerFactory.getLogger(ConstraintDtoIn.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "The global time limit for ordering.", nullable = true, example = "10:30:00")
	private String orderTimeLimit = "10:30:00";
	@Schema(description = "The global maximum number of order for a day. Not used in current version.", nullable = true, minimum = "1")
	private Integer maximumOrderPerDay = Integer.valueOf(500);
	@Schema(description = "The global VAT % value.", nullable = true, example = "20", minimum = "20", maximum = "100")
	private Float rateVAT = Float.valueOf(20F);

	/**
	 * Constructor of the object.
	 */
	public ConstraintDtoIn() {
		super();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the orderTimeLimit value.
	 */
	public String getOrderTimeLimit() {
		return this.orderTimeLimit;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the orderTimeLimit value.
	 */
	@JsonIgnore
	public LocalTime getOrderTimeLimitAsTime() {
		try {
			return LocalTime.parse(this.getOrderTimeLimit(), DateTimeFormatter.ofPattern(ConstraintDtoIn.PATTERN));
		} catch (Exception lExp) {
			ConstraintDtoIn.LOG.atWarn().log("Error with date", lExp);
		}
		return null;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pOrderTimeLimit the new value for orderTimeLimit attribute
	 */
	public void setOrderTimeLimit(String pOrderTimeLimit) {
		this.orderTimeLimit = DtoUtils.checkAndClean(pOrderTimeLimit);
		if (this.orderTimeLimit != null) {
			var elms = pOrderTimeLimit.split(":");
			if (elms.length < 3) {
				// an element of time is missing
				String[] timeStr = { "00", "00", "00" };
				System.arraycopy(elms, 0, timeStr, 0, elms.length);
				this.orderTimeLimit = timeStr[0] + ":" + timeStr[1] + ":" + timeStr[2];
			}
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
	public Float getRateVAT() {
		return this.rateVAT;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pRateVAT the new value for rateVAT attribute
	 */
	public void setRateVAT(Float pRateVAT) {
		if (pRateVAT == null || pRateVAT.floatValue() < 0) {
			this.rateVAT = Float.valueOf(20F);
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
		sb.append("orderTimeLimit=");
		sb.append(this.getOrderTimeLimit());
		sb.append(",maximumOrderPerDay=");
		sb.append(this.getMaximumOrderPerDay());
		sb.append(",rateVAT=");
		sb.append(this.getRateVAT());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Validate DTO
	 */
	@Override
	@JsonIgnore
	public void validate() {
		// Reset default values if value are null
		if (this.getMaximumOrderPerDay() == null || this.getMaximumOrderPerDay().intValue() <= 0) {
			this.setMaximumOrderPerDay(null);
		}
		if (this.getRateVAT() == null || this.getRateVAT().doubleValue() < 0) {
			this.setRateVAT(null);
		}
		if (this.getOrderTimeLimit() == null) {
			this.setOrderTimeLimit(null);
		}
	}

}
