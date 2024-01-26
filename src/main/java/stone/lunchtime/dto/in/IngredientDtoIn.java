// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The dto class for the ingredient.
 */
@Schema(description = "Represents an ingredient. A meal is composed of ingredients.")
public class IngredientDtoIn extends AbstractLabeledDtoIn {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public IngredientDtoIn() {
		super();
	}

}
