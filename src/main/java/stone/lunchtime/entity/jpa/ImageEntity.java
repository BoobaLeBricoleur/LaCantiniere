// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.LongVarcharJdbcType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import stone.lunchtime.entity.EntityUtils;

/**
 * The persistent class for the ltimage database table.
 */
@Entity
@Table(name = "ltimage")
public class ImageEntity extends AbstractJpaEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "image_path", length = 500)
	private String imagePath;

	@Column(name = "image_64", columnDefinition = "TEXT")
	@Lob
	@JdbcType(LongVarcharJdbcType.class) // For Postgres
	private String image64;

	@Column(name = "is_default", nullable = false)
	private Boolean isDefault = Boolean.FALSE;

	@OneToMany(mappedBy = "image", fetch = FetchType.LAZY)
	private List<IngredientEntity> ingredients;

	@OneToMany(mappedBy = "image", fetch = FetchType.LAZY)
	private List<MealEntity> meals;

	@OneToMany(mappedBy = "image", fetch = FetchType.LAZY)
	private List<MenuEntity> menus;

	@OneToMany(mappedBy = "image", fetch = FetchType.LAZY)
	private List<UserEntity> users;

	public ImageEntity() {
		super();
	}

	public ImageEntity(Integer pId) {
		super(pId);
	}

	public ImageEntity(String pImagePath, String pImage64, Boolean pIsDefault) {
		super();
		this.setImagePath(pImagePath);
		this.setImage64(pImage64);
		this.setIsDefault(pIsDefault);
	}

	/**
	 * Gets the attribute isDefault.
	 *
	 * @return the value of isDefault.
	 */
	public Boolean getIsDefault() {
		return this.isDefault;
	}

	/**
	 * Sets a new value for the attribute isDefault.
	 *
	 * @param pIsDefault the new value for the attribute.
	 */
	public void setIsDefault(Boolean pIsDefault) {
		this.isDefault = Objects.requireNonNullElse(pIsDefault, Boolean.FALSE);
	}

	/**
	 * Gets the attribute imagePath.
	 *
	 * @return the value of imagePath.
	 */
	public String getImagePath() {
		return this.imagePath;
	}

	/**
	 * Sets a new value for the attribute imagePath.
	 *
	 * @param pImagePath the new value for the attribute.
	 */
	public void setImagePath(String pImagePath) {
		this.imagePath = EntityUtils.checkAndClean(pImagePath);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the image value.
	 */
	public String getImage64() {
		return this.image64;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pImage the new value for image attribute
	 */
	public void setImage64(String pImage) {
		this.image64 = EntityUtils.checkAndClean(pImage);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		if (this.getImagePath() != null) {
			sb.append(",path=");
			sb.append(this.getImagePath(), 0, Math.min(20, this.getImagePath().length()));
			sb.append("...");
		}
		if (this.getImage64() != null) {
			sb.append(",base64=");
			sb.append(this.getImage64(), 0, Math.min(10, this.getImage64().length()));
			sb.append("...");
		}
		sb.append(",default=");
		sb.append(this.getIsDefault());
		sb.append("}");
		return sb.toString();
	}

}
