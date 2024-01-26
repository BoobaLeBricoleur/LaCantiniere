// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * The mother of all dto in class. A DTO in is a JSon coming FROM the client.
 */
@JsonInclude(Include.NON_NULL)
public abstract class AbstractDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	protected AbstractDto() {
		super();
	}

}
