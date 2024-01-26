// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.entity.MealCategory;

/**
 * The dto class for the meal.
 */
@Schema(description = "Represents a meal. Meal can be ordered or used with menu.")
public class MealDtoOut extends AbstractEatableDtoOut {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "The category for this element. unknown(0), appetizers(1), starters(2), main_dishes(3), others(4), desserts(5), brunchs_and_lunches(6), soups(7), sauces(8), drinks(9), sandwiches(10), snacks(11)", example = "0", type = "number", allowableValues = {
			"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" })
	private MealCategory category;

	@Schema(description = "An array of ingredients.")
	private List<IngredientDtoOut> ingredients;

	/**
	 * Constructor of the object.
	 */
	public MealDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pId id of the entity
	 */
	public MealDtoOut(Integer pId) {
		super(pId);
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
	 * @return the ingredients value.
	 */
	public List<IngredientDtoOut> getIngredients() {
		return this.ingredients;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pIngredients the new value for ingredients attribute
	 */
	public void setIngredients(List<IngredientDtoOut> pIngredients) {
		this.ingredients = pIngredients;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",category=");
		sb.append(this.getCategory());
		sb.append(",ingredientsId=");
		if (this.getIngredients() != null && !this.getIngredients().isEmpty()) {
			sb.append('[');
			for (IngredientDtoOut elm : this.ingredients) {
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
