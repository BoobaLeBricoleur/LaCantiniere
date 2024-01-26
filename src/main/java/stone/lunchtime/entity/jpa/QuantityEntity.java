// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The persistent class for the QuantityEntity database table.
 */
@Entity
@Table(name = "ltquantity")
public class QuantityEntity extends AbstractJpaEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "quantity")
	private Integer quantity = Integer.valueOf(0);

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "meal_id")
	private MealEntity meal;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "menu_id")
	private MenuEntity menu;

	/**
	 * Gets the attribute value.
	 *
	 * @return the quantity value.
	 */
	public Integer getQuantity() {
		return this.quantity;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pQuantity the new value for quantity attribute
	 */
	public void setQuantity(Integer pQuantity) {
		if (pQuantity == null || pQuantity.intValue() < 0) {
			this.quantity = Integer.valueOf(0);
		} else {
			this.quantity = pQuantity;
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the meal value.
	 */
	public MealEntity getMeal() {
		return this.meal;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMeal the new value for meal attribute
	 */
	public void setMeal(MealEntity pMeal) {
		this.meal = pMeal;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the menu value.
	 */
	public MenuEntity getMenu() {
		return this.menu;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMenu the new value for menu attribute
	 */
	public void setMenu(MenuEntity pMenu) {
		this.menu = pMenu;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",quantity=");
		sb.append(this.getQuantity());
		if (this.getMeal() != null) {
			sb.append(",mealId=");
			sb.append(this.getMeal().getId());
		} else {
			sb.append(",no meal");
		}
		if (this.getMenu() != null) {
			sb.append(",menuId=");
			sb.append(this.getMenu().getId());
		} else {
			sb.append(",no menu");
		}
		sb.append("}");
		return sb.toString();
	}
}
