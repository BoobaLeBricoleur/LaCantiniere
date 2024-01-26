// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the Quantity.
 */
@Schema(description = "Represents a number of meal/menu. Used for ordering.")
public class QuantityDtoIn extends AbstractDtoIn {
	private static final Logger LOG = LoggerFactory.getLogger(QuantityDtoIn.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "A quantity.", nullable = true)
	private Integer quantity = Integer.valueOf(0);
	@Schema(description = "A meal id.", nullable = true)
	private Integer mealId;
	@Schema(description = "A menu id.", nullable = true)
	private Integer menuId;

	/**
	 * Constructor of the object.
	 */
	public QuantityDtoIn() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pQuantity quantity id
	 * @param pMealId   meal id
	 * @param pMenuId   menu id
	 */
	public QuantityDtoIn(Integer pQuantity, Integer pMealId, Integer pMenuId) {
		super();
		this.setQuantity(pQuantity);
		this.setMealId(pMealId);
		this.setMenuId(pMenuId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the menuId value.
	 */
	public Integer getMenuId() {
		return this.menuId;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMenuId the new value for menuId attribute
	 */
	public void setMenuId(Integer pMenuId) {
		this.menuId = pMenuId;
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
	 * Gets the attribute value.
	 *
	 * @return the mealId value.
	 */
	public Integer getMealId() {
		return this.mealId;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMealId the new value for mealId attribute
	 */
	public void setMealId(Integer pMealId) {
		this.mealId = pMealId;
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

	@Override
	public void validate() {
		if (this.getQuantity() != null && this.getQuantity().intValue() < 0) {
			QuantityDtoIn.LOG.atError().log("validate - Quantity must be [0, +]");
			throw new ParameterException("Quantite n'est pas une valeur valide", "Quantity");
		}

		if (this.getMenuId() == null && this.getMealId() == null) {
			QuantityDtoIn.LOG.atError().log("validate - MenuId or MealId must be set");
			throw new ParameterException("Il faut indiquer un menu ou un plat", "MenuId");
		}

		if (this.getMenuId() != null && this.getMealId() != null) {
			QuantityDtoIn.LOG.atError().log("validate - MenuId and MealId are both set");
			throw new ParameterException("Il faut indiquer un menu ou un plat, mais pas les deux !", "MenuId");
		}

		if (this.getMenuId() != null && this.getMenuId().intValue() <= 0) {
			QuantityDtoIn.LOG.atError().log("validate - MenuId must be ]0, +]");
			throw new ParameterException("L'id du menu n'est pas une valeur valide", "MenuId");
		}

		if (this.getMealId() != null && this.getMealId().intValue() <= 0) {
			QuantityDtoIn.LOG.atError().log("validate - MealId must be ]0, +]");
			throw new ParameterException("L'id du plat n'est pas une valeur valide", "MealId");
		}

	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append("quantity=");
		sb.append(this.getQuantity());
		if (this.getMealId() != null) {
			sb.append(",mealId=");
			sb.append(this.getMealId());
		}
		if (this.getMenuId() != null) {
			sb.append(",menuId=");
			sb.append(this.menuId);
		}
		sb.append("}");
		return sb.toString();
	}

}
