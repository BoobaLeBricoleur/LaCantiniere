// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.entity.MealCategory;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the meal.
 */
@Schema(description = "Represents a meal. Meal can be ordered or used with menu.")
public class MealDtoIn extends AbstractEatableDtoIn {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(MealDtoIn.class);

	@Schema(description = "An array of ingredients id.", nullable = true)
	private List<Integer> ingredientsId;

	@Schema(description = "The category for this element. unknown(0), appetizers(1), starters(2), main_dishes(3), others(4), desserts(5), brunchs_and_lunches(6), soups(7), sauces(8), drinks(9), sandwiches(10), snacks(11)", example = "0", type = "number", allowableValues = {
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" })
	private MealCategory category;

	/**
	 * Constructor of the object.
	 */
	public MealDtoIn() {
		super();
	}

	/**
	 * Gets the attribute category.
	 *
	 * @return the value of category.
	 */
	public MealCategory getCategory() {
		return this.category;
	}

	/**
	 * Sets a new value for the attribute category.
	 *
	 * @param pCategory the new value for the attribute.
	 */
	public void setCategory(MealCategory pCategory) {
		this.category = Objects.requireNonNullElse(pCategory, MealCategory.UNKNOWN);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the ingredientsId value.
	 */
	public List<Integer> getIngredientsId() {
		return this.ingredientsId;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pIngredientsId the new value for ingredientsId attribute
	 */
	public void setIngredientsId(List<Integer> pIngredientsId) {
		if (pIngredientsId == null || pIngredientsId.isEmpty()) {
			this.ingredientsId = null;
		} else {
			this.ingredientsId = pIngredientsId;
		}
	}

	@Override
	@JsonIgnore
	public void validate() {
		super.validate();
		if (this.getIngredientsId() != null) {
			for (Integer elm : this.ingredientsId) {
				if (elm == null || elm.intValue() <= 0) {
					MealDtoIn.LOG.atError().log("validate - ingredient id must be between ]0, +[, found {}", elm);
					throw new ParameterException("Id d'ingredient invalide ! (doit être entre ]0, +[)",
							"IngredientsId");
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
		sb.append(",category=");
		sb.append(this.getCategory());
		if (this.getIngredientsId() != null) {
			sb.append(",ingredientsId=");
			sb.append(this.getIngredientsId());
		}
		sb.append("}");
		return sb.toString();
	}

}
