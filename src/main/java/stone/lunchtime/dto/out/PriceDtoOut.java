// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The dto class used for prices.
 */
@JsonInclude(Include.NON_NULL)
@Schema(description = "Represents all computed prices")
public class PriceDtoOut implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "Price Duty Free.")
	private Float priceDF;
	@Schema(description = "Price VAT. = DT * VAT%")
	private Float priceVAT;
	@Schema(description = "VAT rate. This comes from a constraint.")
	private Float rateVAT;

	/**
	 * Constructor of the object.
	 */
	public PriceDtoOut() {
		super();
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

	/**
	 * Gets the attribute value.
	 *
	 * @return the priceVAT value.
	 */
	public Float getPriceVAT() {
		return this.priceVAT;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPriceVAT the new value for priceVAT attribute
	 */
	public void setPriceVAT(Float pPriceVAT) {
		this.priceVAT = pPriceVAT;
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
		this.rateVAT = pRateVAT;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" {priceDF=");
		sb.append(this.priceDF);
		sb.append(",priceVAT=");
		sb.append(this.priceVAT);
		sb.append(",rateVAT=");
		sb.append(this.rateVAT);
		sb.append("}");
		return sb.toString();
	}

}
