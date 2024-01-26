// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import stone.lunchtime.entity.OrderStatus;

/**
 * The persistent class for the order database table.
 */
@Entity
@Table(name = "ltorder")
public class OrderEntity extends AbstractJpaEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "creation_date")
	private LocalDate creationDate;
	@Column(name = "creation_time")
	private LocalTime creationTime;

	@Column(name = "status", nullable = false)
	private OrderStatus status;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "ltorder_has_quantity", joinColumns = {
			@JoinColumn(name = "order_id", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "quantity_id", nullable = false) })
	private List<QuantityEntity> quantityEntities;

	/**
	 * Gets the attribute value.
	 *
	 * @return the creationDate value.
	 */
	public LocalDate getCreationDate() {
		return this.creationDate;
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
	 * Gets the attribute value.
	 *
	 * @return the user value.
	 */
	public UserEntity getUser() {
		return this.user;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pUser the new value for user attribute
	 */
	public void setUser(UserEntity pUser) {
		this.user = pUser;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the quantityEntities value.
	 */
	public List<QuantityEntity> getQuantityEntities() {
		return this.quantityEntities;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pQuantityEntities the new value for quantityEntities attribute
	 */
	public void setQuantityEntities(List<QuantityEntity> pQuantityEntities) {
		this.quantityEntities = pQuantityEntities;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the status value.
	 */
	public OrderStatus getStatus() {
		return this.status != null ? this.status : OrderStatus.CREATED;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pStatus the new value for status attribute
	 */
	public void setStatus(OrderStatus pStatus) {
		if (pStatus != null) {
			this.status = pStatus;
		} else {
			this.status = OrderStatus.CREATED;
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",creationDate=");
		sb.append(this.getCreationDate());
		sb.append(",creationTime=");
		sb.append(this.getCreationTime());
		sb.append(",status=");
		sb.append(this.getStatus());

		if (this.getQuantityEntities() != null && !this.getQuantityEntities().isEmpty()) {
			sb.append(",quantityEntities=[");
			for (QuantityEntity elm : this.quantityEntities) {
				sb.append(elm.getId()).append(',');
			}
			sb.delete(sb.length() - 1, sb.length());
			sb.append(']');
		} else {
			sb.append(",no quantity");
		}
		sb.append(",userId=");
		sb.append(this.getUser().getId());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Indicates if order has the status OrderStatus.DELIVERED
	 *
	 * @return true if order has status OrderStatus.DELIVERED
	 */
	public boolean isDelivered() {
		return OrderStatus.DELIVERED.equals(this.getStatus());
	}

	/**
	 * Indicates if order has the status OrderStatus.CREATED
	 *
	 * @return true if order has status OrderStatus.CREATED
	 */
	public boolean isCreated() {
		return OrderStatus.CREATED.equals(this.getStatus());
	}

	/**
	 * Indicates if order has the status OrderStatus.CANCELED
	 *
	 * @return true if order has status OrderStatus.CANCELED
	 */
	public boolean isCanceled() {
		return OrderStatus.CANCELED.equals(this.getStatus());
	}

}
