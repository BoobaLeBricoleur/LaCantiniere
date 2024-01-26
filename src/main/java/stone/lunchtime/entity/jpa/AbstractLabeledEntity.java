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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.EntityUtils;

/**
 * The persistent class for the labeled table.
 */
@MappedSuperclass
public abstract class AbstractLabeledEntity extends AbstractJpaEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "description", columnDefinition = "TEXT")
	@Lob
	@JdbcType(LongVarcharJdbcType.class) // For Postgres
	private String description;

	@Column(name = "label", length = 200, nullable = false)
	private String label;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.ORDINAL)
	private EntityStatus status;

	// Handling of image is not done by this entity
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "image_id")
	private ImageEntity image;

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
	public String getLabel() {
		return this.label;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pLabel the new value for label attribute
	 */
	public void setLabel(String pLabel) {
		this.label = EntityUtils.checkAndClean(pLabel);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the status value.
	 */
	public EntityStatus getStatus() {
		return this.status != null ? this.status : EntityStatus.DISABLED;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pStatus the new value for status attribute
	 */
	public void setStatus(EntityStatus pStatus) {
		if (pStatus == null) {
			this.status = EntityStatus.DISABLED;
		} else {
			this.status = pStatus;
		}
	}

	/**
	 * Gets the attribute image.
	 *
	 * @return the value of image.
	 */
	public ImageEntity getImage() {
		return this.image;
	}

	/**
	 * Sets a new value for the attribute image.
	 *
	 * @param pImage the new value for the attribute.
	 */
	public void setImage(ImageEntity pImage) {
		this.image = pImage;
	}

	/**
	 * Sets a new value for the attribute image. If both values are null then image
	 * is null.
	 *
	 * @param pImage64   the image in base 64
	 * @param pImagePath the image path
	 */
	public void setImage(String pImage64, String pImagePath) {
		if (pImage64 == null && pImagePath == null) {
			this.setImage(null);
		} else {
			var ie = new ImageEntity();
			ie.setImage64(pImage64);
			ie.setImagePath(pImagePath);
			this.setImage(ie);
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",status=");
		sb.append(this.getStatus());
		if (this.getDescription() != null) {
			sb.append(",description=");
			sb.append(this.getDescription());
		}
		sb.append(",label=");
		sb.append(this.getLabel());
		if (this.getImage() != null) {
			sb.append(",image=");
			sb.append(this.getImage());
		}

		sb.append("}");
		return sb.toString();
	}

	/**
	 * Indicates if entity has the status EntityStatus.ENABLED
	 *
	 * @return true if entity has status EntityStatus.ENABLED
	 */
	public boolean isEnabled() {
		return EntityStatus.ENABLED.equals(this.getStatus());
	}

	/**
	 * Indicates if entity has the status EntityStatus.DELETED
	 *
	 * @return true if entity has status EntityStatus.DELETED
	 */
	public boolean isDeleted() {
		return EntityStatus.DELETED.equals(this.getStatus());
	}
}
