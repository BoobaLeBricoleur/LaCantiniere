// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the menu.
 */
@Schema(description = "Represents a menu. Menu can be ordered.")
public class MenuDtoIn extends AbstractEatableDtoIn {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(MenuDtoIn.class);

	@Schema(description = "An array of meals id.", nullable = true)
	private List<Integer> mealIds;

	/**
	 * Constructor of the object.
	 */
	public MenuDtoIn() {
		super();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the mealIds value.
	 */
	public List<Integer> getMealIds() {
		return this.mealIds;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMealIds the new value for mealIds attribute
	 */
	public void setMealIds(List<Integer> pMealIds) {
		if (pMealIds == null || pMealIds.isEmpty()) {
			this.mealIds = null;
		} else {
			this.mealIds = pMealIds;
		}
	}

	@Override
	@JsonIgnore
	public void validate() {
		super.validate();
		if (this.getMealIds() != null) {
			for (Integer elm : this.getMealIds()) {
				if (elm == null || elm.intValue() <= 0) {
					MenuDtoIn.LOG.atError().log("validate - meal id must be between ]0, +[, found {}", elm);
					throw new ParameterException("Id du plat invalide ! (doit être entre ]0, +[)", "MealIds");
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
		if (this.getMealIds() != null) {
			sb.append(",mealIds=");
			sb.append(this.getMealIds());
		}
		sb.append("}");
		return sb.toString();
	}
}
