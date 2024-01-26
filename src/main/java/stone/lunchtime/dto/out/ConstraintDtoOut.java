// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;
import java.time.LocalTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The dto class for the constraint database table.
 */
@Schema(description = "Constraint element used for global operations.")
public class ConstraintDtoOut extends AbstractDtoOut {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "The global time limit for ordering.", example = "10:30:00")
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	@JsonSerialize(using = LocalTimeSerializer.class)
	private LocalTime orderTimeLimit = LocalTime.of(10, 30, 0);
	@Schema(description = "The global maximum number of order for a day. Not used in current version.")
	private Integer maximumOrderPerDay = Integer.valueOf(500);
	@Schema(description = "The global VAT % value.", example = "20")
	private Float rateVAT = Float.valueOf(20F);

	/**
	 * Constructor of the object.
	 */
	public ConstraintDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pId an id
	 */
	public ConstraintDtoOut(Integer pId) {
		super(pId);
	}

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
