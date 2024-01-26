// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * The persistent class for the ingredient database table.
 */
@Entity
@Table(name = "ltingredient")
public class IngredientEntity extends AbstractLabeledEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	public IngredientEntity() {
		super();
	}

}
