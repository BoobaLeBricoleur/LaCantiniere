// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;

import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.LongVarcharJdbcType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import stone.lunchtime.entity.EntityUtils;
import stone.lunchtime.entity.RoleLabel;

/**
 * The persistent class for the role table.
 */
@Entity
@Table(name = "ltrole")
public class RoleEntity extends AbstractJpaEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "description", columnDefinition = "TEXT")
	@Lob
	@JdbcType(LongVarcharJdbcType.class) // For Postgres
	private String description;

	@Column(name = "label", length = 200, nullable = false)
	@Enumerated(EnumType.STRING)
	private RoleLabel label;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	/**
	 * Constructor of the object. Default role is USER.
	 */
	public RoleEntity() {
		this(RoleLabel.ROLE_USER, null);
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pLabel a role
	 * @param pUser  a user
	 */
	public RoleEntity(RoleLabel pLabel, UserEntity pUser) {
		super();
		this.setLabel(pLabel);
		this.setUser(pUser);
	}

	/**
	 * Gets the attribute user.
	 *
	 * @return the value of user.
	 */
	public UserEntity getUser() {
		return this.user;
	}

	/**
	 * Sets a new value for the attribute user.
	 *
	 * @param pUser the new value for the attribute.
	 */
	public void setUser(UserEntity pUser) {
		this.user = pUser;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the description value.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pDescription the new value for description attribute
	 */
	public void setDescription(String pDescription) {
		this.description = EntityUtils.checkAndClean(pDescription);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the label value.
	 */
	public RoleLabel getLabel() {
		return this.label;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pLabel the new value for label attribute
	 */
	public void setLabel(RoleLabel pLabel) {
		this.label = pLabel;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		if (this.getDescription() != null) {
			sb.append(",description=");
			sb.append(this.getDescription());
		}
		sb.append(",label=");
		sb.append(this.getLabel().toString());

		sb.append("}");
		return sb.toString();
	}

}
