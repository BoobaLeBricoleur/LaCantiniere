// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the order database table.
 */
@Schema(description = "Represents an order. Order can be composed of many menus and/or many meals.")
public class OrderDtoIn extends AbstractDtoIn {
	private static final Logger LOG = LoggerFactory.getLogger(OrderDtoIn.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "The user's id passing the order ", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer userId;
	/** If value is -1 then no constraint will be used for this order. */
	@Schema(description = "A constraint id. If value is -1 then no constraint will be used for this order.", nullable = true, minimum = "-1")
	private Integer constraintId;
	@Schema(description = "An array of quantity for menus or meals.", nullable = true)
	private List<QuantityDtoIn> quantity;
	// state is not handled by DTO IN

	/**
	 * Constructor of the object.
	 */
	public OrderDtoIn() {
		super();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the userId value.
	 */
	public Integer getUserId() {
		return this.userId;
	}

	/**
	 * Indicates if there is some meals or menus
	 *
	 * @return true if there is some meals or menus
	 */
	@JsonIgnore
	public boolean hasQuantity() {
		return this.quantity != null && !this.quantity.isEmpty();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the constraintId value.
	 */
	public Integer getConstraintId() {
		return this.constraintId;
	}

	/**
	 * Sets the attribute value. <br>
	 *
	 * If value is -1 then no constraint will be used for this order.
	 *
	 * @param pConstraintId the new value for constraintId attribute
	 */
	public void setConstraintId(Integer pConstraintId) {
		this.constraintId = pConstraintId;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the quantity value.
	 */
	public List<QuantityDtoIn> getQuantity() {
		return this.quantity;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pQuantity the new value for quantity attribute
	 */
	public void setQuantity(List<QuantityDtoIn> pQuantity) {
		if (pQuantity == null || pQuantity.isEmpty()) {
			this.quantity = null;
		} else {
			this.quantity = pQuantity;
		}
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pUserId the new value for userId attribute
	 */
	public void setUserId(Integer pUserId) {
		this.userId = pUserId;
	}

	@Override
	@JsonIgnore
	public void validate() {
		// Recall : constraint id can be null ou -1

		if (this.getUserId() == null || this.getUserId().intValue() <= 0) {
			OrderDtoIn.LOG.atError().log("validate - id user is not right)");
			throw new ParameterException("Utilisateur n'a pas un id valide!", "UserId");
		}

		OrderDtoIn.LOG.atDebug().log("validate - Will clean quantity meal if needed");
		if (this.getQuantity() != null && !this.getQuantity().isEmpty()) {
			this.getQuantity().removeIf(elm -> elm.getQuantity().intValue() == 0);
			if (this.getQuantity().isEmpty()) {
				this.setQuantity(null);
			}
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append("userId=");
		sb.append(this.userId);

		sb.append(",constraintId=");
		sb.append(this.constraintId);
		if (this.hasQuantity()) {
			sb.append(",quantity=");
			sb.append(this.quantity);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Simple method for adding a menu to the order.
	 *
	 * @param pQuantity a quantity
	 * @param pMenuId   a menu id
	 */
	public void addMenu(Integer pQuantity, Integer pMenuId) {
		var qdi = new QuantityDtoIn(pQuantity, null, pMenuId);
		var qs = this.getQuantity();
		if (qs == null) {
			qs = new ArrayList<>();
		}
		qs.add(qdi);
		this.setQuantity(qs);
	}

	/**
	 * Simple method for adding a meal for the order.
	 *
	 * @param pQuantity a quantity
	 * @param pMealId   a meal id
	 */
	public void addMeal(Integer pQuantity, Integer pMealId) {
		var qdi = new QuantityDtoIn(pQuantity, pMealId, null);
		var qs = this.getQuantity();
		if (qs == null) {
			qs = new ArrayList<>();
		}
		qs.add(qdi);
		this.setQuantity(qs);
	}
}
