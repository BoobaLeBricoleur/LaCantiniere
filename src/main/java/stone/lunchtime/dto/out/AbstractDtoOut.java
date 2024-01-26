// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.AbstractDto;
import stone.lunchtime.dto.in.LoginDtoIn;

/**
 * Mother of simple dto class used for JSon when replying.
 */
@JsonInclude(Include.NON_NULL)
@Schema(description = "Default DTO out", subTypes = { AbstractLabeledDtoOut.class, ConstraintDtoOut.class,
		LoginDtoIn.class, OrderDtoOut.class, QuantityDtoOut.class, UserDtoOut.class })
public abstract class AbstractDtoOut extends AbstractDto {
	@Serial
	private static final long serialVersionUID = 1L;
	@Schema(description = "Id of the element.")
	private Integer id;

	/**
	 * Constructor of the object. <br>
	 */
	protected AbstractDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.<br>
	 *
	 * @param pId a value for PK
	 */
	protected AbstractDtoOut(Integer pId) {
		super();
		this.setId(pId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the id value.
	 */
	public final Integer getId() {
		return this.id;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pId the new value for id attribute
	 */
	public final void setId(Integer pId) {
		this.id = pId;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("{");
		sb.append(this.getClass().getSimpleName());
		sb.append(",id=");
		sb.append(this.getId());
		sb.append("}");
		return sb.toString();
	}
}
