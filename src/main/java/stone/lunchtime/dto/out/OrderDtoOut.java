// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.entity.OrderStatus;

/**
 * The dto class for the order database table.
 */
@Schema(description = "Represents an order. Order can be composed of many menus and/or many meals.")
public class OrderDtoOut extends AbstractDtoOut {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "Creation date of the menu.")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate creationDate;
	@Schema(description = "Creation time of the menu.")
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	@JsonSerialize(using = LocalTimeSerializer.class)
	private LocalTime creationTime;

	@Schema(description = "Status of the order. 0 for Created, 1 for Delivered, 2 for Canceled", example = "0", type = "number", allowableValues = {
			"0", "1", "2" })
	private OrderStatus status;
	@Schema(description = "User responsible for this order.")
	private UserDtoOut user;
	@Schema(description = "List of quantity linked to this order. Can be null.")
	private List<QuantityDtoOut> quantity = new ArrayList<>();

	/**
	 * Constructor of the object.
	 */
	public OrderDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pId entity id
	 */
	public OrderDtoOut(Integer pId) {
		super(pId);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the creationDate value.
	 */
	public LocalDate getCreationDate() {
		return this.creationDate;
	}

	/**
	 * Gets the attribute creationTime.
	 *
	 * @return the value of creationTime.
	 */
	public LocalTime getCreationTime() {
		return this.creationTime;
	}

	/**
	 * Sets a new value for the attribute creationTime.
	 *
	 * @param pCreationTime the new value for the attribute.
	 */
	public void setCreationTime(LocalTime pCreationTime) {
		this.creationTime = pCreationTime;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pCreationDate the new value for creationDate attribute
	 */
	public void setCreationDate(LocalDate pCreationDate) {
		this.creationDate = pCreationDate;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the status value.
	 */
	public OrderStatus getStatus() {
		return this.status;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pStatus the new value for status attribute
	 */
	public void setStatus(OrderStatus pStatus) {
		this.status = pStatus;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the user value.
	 */
	public UserDtoOut getUser() {
		return this.user;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pUser the new value for user attribute
	 */
	public void setUser(UserDtoOut pUser) {
		this.user = pUser;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the quantity value.
	 */
	public List<QuantityDtoOut> getQuantity() {
		return this.quantity;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pQuantity the new value for quantity attribute
	 */
	public void setQuantity(List<QuantityDtoOut> pQuantity) {
		this.quantity = pQuantity;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",status=");
		sb.append(this.getStatus());
		sb.append(",creationDate=");
		sb.append(this.getCreationDate());
		sb.append(",creationTime=");
		sb.append(this.getCreationTime());

		sb.append(",quantityId=");
		if (this.getQuantity() != null && !this.getQuantity().isEmpty()) {
			sb.append('[');
			for (QuantityDtoOut elm : this.getQuantity()) {
				sb.append(elm.getId()).append(',');
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(']');
		} else {
			sb.append("[]");
		}

		sb.append(",user=");
		sb.append(this.getUser());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Indicates if order has the status OrderStatus.DELIVERED
	 *
	 * @return true if order has status OrderStatus.DELIVERED
	 */
	@JsonIgnore
	public boolean isDelivered() {
		return OrderStatus.DELIVERED.equals(this.getStatus());
	}

	/**
	 * Indicates if order has the status OrderStatus.CREATED
	 *
	 * @return true if order has status OrderStatus.CREATED
	 */
	@JsonIgnore
	public boolean isCreated() {
		return OrderStatus.CREATED.equals(this.getStatus());
	}

	/**
	 * Indicates if order has the status OrderStatus.CANCELED
	 *
	 * @return true if order has status OrderStatus.CANCELED
	 */
	@JsonIgnore
	public boolean isCanceled() {
		return OrderStatus.CANCELED.equals(this.getStatus());
	}
}
