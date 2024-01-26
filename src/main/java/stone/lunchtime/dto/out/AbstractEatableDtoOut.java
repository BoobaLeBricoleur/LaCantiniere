// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.DtoUtils;

/**
 * Dto with price and availableForWeeks.
 */
@Schema(description = "Represents a labeled element.", subTypes = { MealDtoOut.class, MenuDtoOut.class })
public abstract class AbstractEatableDtoOut extends AbstractLabeledDtoOut {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "The price duty free for this element.")
	private Float priceDF;
	@Schema(description = "An json array of object that represents the week number and day number when this element is available.", nullable = true)
	private AvailableForWeeksAndDays availableForWeeksAndDays;

	/**
	 * Constructor of the object. <br>
	 */
	protected AbstractEatableDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.<br>
	 *
	 * @param pId a value for PK
	 */
	protected AbstractEatableDtoOut(Integer pId) {
		super(pId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the availableForWeeks value.
	 */
	public AvailableForWeeksAndDays getAvailableForWeeksAndDays() {
		return this.availableForWeeksAndDays;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pAvailableForWeeks the new value for availableForWeeks attribute
	 */
	public void setAvailableForWeeksAndDays(AvailableForWeeksAndDays pAvailableForWeeks) {
		this.availableForWeeksAndDays = pAvailableForWeeks;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the priceDF value.
	 */
	public Float getPriceDF() {
		return this.priceDF;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPriceDF the new value for priceDF attribute
	 */
	public void setPriceDF(Float pPriceDF) {
		this.priceDF = pPriceDF;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",priceDF=");
		sb.append(DtoUtils.formatNumber(this.getPriceDF()));
		sb.append(",availableForWeeks=");
		if (this.getAvailableForWeeksAndDays() != null && !this.getAvailableForWeeksAndDays().isEmpty()) {
			sb.append(this.getAvailableForWeeksAndDays());
		} else {
			sb.append("all");
		}
		sb.append("}");
		return sb.toString();
	}
}
