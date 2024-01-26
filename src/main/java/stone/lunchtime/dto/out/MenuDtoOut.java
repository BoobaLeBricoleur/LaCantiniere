// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The dto class for the menu.
 */
@Schema(description = "Represents a menu. Menu can be ordered.")
public class MenuDtoOut extends AbstractEatableDtoOut {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "An array of meals.")
	private List<MealDtoOut> meals;

	/**
	 * Constructor of the object.
	 */
	public MenuDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pId id of the entity
	 */
	public MenuDtoOut(Integer pId) {
		super(pId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the meals value.
	 */
	public List<MealDtoOut> getMeals() {
		return this.meals;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMeals the new value for meals attribute
	 */
	public void setMeals(List<MealDtoOut> pMeals) {
		this.meals = pMeals;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",mealsId=");
		if (this.getMeals() != null && !this.getMeals().isEmpty()) {
			sb.append('[');
			for (MealDtoOut elm : this.getMeals()) {
				sb.append(elm.getId()).append(',');
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(']');
		} else {
			sb.append("[]");
		}
		sb.append("}");
		return sb.toString();
	}
}
