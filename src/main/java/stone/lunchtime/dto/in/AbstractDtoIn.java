// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.AbstractDto;

/**
 * The mother of all dto in class. A DTO in is a JSon coming FROM the client.
 */
@Schema(description = "Default DTO In", subTypes = { AbstractLabeledDtoIn.class, ConstraintDtoIn.class,
		LoginDtoIn.class, OrderDtoIn.class, QuantityDtoIn.class, UserDtoIn.class })
public abstract class AbstractDtoIn extends AbstractDto {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	protected AbstractDtoIn() {
		super();
	}

	/**
	 * Will validate the DTO.
	 */
	@JsonIgnore
	public abstract void validate();

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" {}");
		return sb.toString();
	}
}
