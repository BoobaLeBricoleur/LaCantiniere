// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import stone.lunchtime.entity.MealCategory;

/**
 * The persistent class for the meal database table.
 */
@Entity
@Table(name = "ltmeal")
public class MealEntity extends AbstractEatableEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "category")
	@Enumerated(EnumType.ORDINAL)
	private MealCategory category = MealCategory.UNKNOWN;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "ltmeal_has_ingredient", joinColumns = {
			@JoinColumn(name = "meal_id", nullable = false, referencedColumnName = "id") }, inverseJoinColumns = {
					@JoinColumn(name = "ingredient_id", nullable = false, referencedColumnName = "id") })
	private List<IngredientEntity> ingredients;

	/**
	 * Gets the attribute value.
	 *
	 * @return the ingredients value.
	 */
	public List<IngredientEntity> getIngredients() {
		return this.ingredients;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pIngredients the new value for ingredients attribute
	 */
	public void setIngredients(List<IngredientEntity> pIngredients) {
		this.ingredients = pIngredients;
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public MealCategory getCategory() {
		return this.category;
	}

	/**
	 * Sets the category
	 *
	 * @param pCategory the new category
	 */
	public void setCategory(MealCategory pCategory) {
		this.category = pCategory;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",category=");
		sb.append(this.getCategory());
		if (this.getIngredients() != null && !this.getIngredients().isEmpty()) {
			sb.append(",ingredientIds=[");
			for (IngredientEntity lIngredient : this.getIngredients()) {
				sb.append(lIngredient.getId()).append(',');
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(']');
		}

		sb.append("}");
		return sb.toString();
	}

}
