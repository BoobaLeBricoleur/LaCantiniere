// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the menu database table.
 */
@Entity
@Table(name = "ltmenu")
public class MenuEntity extends AbstractEatableEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "ltmenu_has_meal", joinColumns = {
			@JoinColumn(name = "menu_id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "meal_id", nullable = false) })
	private List<MealEntity> meals;

	/**
	 * Gets the attribute value.
	 *
	 * @return the meals value.
	 */
	public List<MealEntity> getMeals() {
		return this.meals;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMeals the new value for meals attribute
	 */
	public void setMeals(List<MealEntity> pMeals) {
		this.meals = pMeals;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		if (this.getMeals() != null && !this.getMeals().isEmpty()) {
			sb.append(",mealIds=[");
			for (MealEntity meal : this.getMeals()) {
				sb.append(meal.getId()).append(',');
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(']');
		}

		sb.append("}");
		return sb.toString();
	}

}
