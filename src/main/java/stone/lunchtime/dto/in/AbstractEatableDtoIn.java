// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.DtoUtils;
import stone.lunchtime.dto.WeekAndDay;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Dto with price and disponibilities. Package visibility
 */
@Schema(description = "Represents a labeled element.", subTypes = { MealDtoIn.class, MenuDtoIn.class })
public abstract class AbstractEatableDtoIn extends AbstractLabeledDtoIn {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractEatableDtoIn.class);

	@Schema(description = "The price duty free for this element.", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0", maximum = "999")
	private Float priceDF;
	@Schema(description = "An json array of object that represents the week number and day number when this element is available.", nullable = true)
	private AvailableForWeeksAndDays availableForWeeksAndDays;

	/**
	 * Constructor of the object. <br>
	 */
	protected AbstractEatableDtoIn() {
		super();
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
	@JsonIgnore
	public void validate() {
		if (this.getPriceDF() == null) {
			AbstractEatableDtoIn.LOG.atError().log("validate - priceDF should not be null");
			throw new ParameterException("Il faut indiquer un prix HT", "priceDF");
		}
		if (this.getPriceDF() != null
				&& (this.getPriceDF().doubleValue() <= 0.001D || this.getPriceDF().doubleValue() > 999.99D)) {
			AbstractEatableDtoIn.LOG.atError().log("validate - priceDF must be between ]0.001, 999.99]");
			throw new ParameterException("Prix HT invalid ! (doit être entre 0.001 et 999.99)", "priceDF");
		}
		if (this.getAvailableForWeeksAndDays() != null && !this.getAvailableForWeeksAndDays().isEmpty()) {
			for (WeekAndDay wd : this.getAvailableForWeeksAndDays().getValues()) {
				if (wd.getWeek() != null) {
					int w = wd.getWeek();
					if (w < 1 || w > 53) {
						AbstractEatableDtoIn.LOG.atError().log("validate - week id must be between [1, 53], found {}",
								w);
						throw new ParameterException("Week Id invalide ! (doit être entre [1, 53])", "WeekId");

					}
					if (wd.getDay() != null) {
						int d = wd.getDay();
						if (d < 1 || d > 7) {
							AbstractEatableDtoIn.LOG.atError().log("validate - day id must be between [1, 7], found {}",
									d);
							throw new ParameterException("Day Id invalide ! (doit être entre [1, 7])", "DayId");
						}
					}
				}
				if (wd.getWeek() == null && wd.getDay() != null) {
					AbstractEatableDtoIn.LOG.atError().log("validate - week id cannot be null if day id exist");
					throw new ParameterException("Week Id invalide ! (un day id doit être rataché à un week id)",
							"DayId");
				}
			}
		}

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
		if (this.getAvailableForWeeksAndDays() != null) {
			sb.append(this.getAvailableForWeeksAndDays());
		} else {
			sb.append("all");
		}
		sb.append("}");
		return sb.toString();
	}

}
