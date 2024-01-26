// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The dto class for the quantityMeal.
 */
@Schema(description = "Represents a number of meal. Used for ordering.")
public class QuantityDtoOut extends AbstractDtoOut {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "A quantity.")
	private Integer quantity;
	@Schema(description = "Meal linked to the quantity. Can be null.")
	private MealDtoOut meal;
	@Schema(description = "Menu linked to the quantity. Can be null.")
	private MenuDtoOut menu;

	/**
	 * Constructor of the object.
	 */
	public QuantityDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.<br>
	 *
	 * @param pId a value for PK
	 */
	public QuantityDtoOut(Integer pId) {
		super(pId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the menu value.
	 */
	public MenuDtoOut getMenu() {
		return this.menu;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMenu the new value for menu attribute
	 */
	public void setMenu(MenuDtoOut pMenu) {
		this.menu = pMenu;
	}

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
		this.quantity = pQuantity;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the meal value.
	 */
	public MealDtoOut getMeal() {
		return this.meal;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMeal the new value for meal attribute
	 */
	public void setMeal(MealDtoOut pMeal) {
		this.meal = pMeal;
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
			sb.append(",meal=");
			sb.append(this.getMeal());
		}
		if (this.getMenu() != null) {
			sb.append(",menu=");
			sb.append(this.getMenu());
		}
		sb.append("}");
		return sb.toString();
	}
}
