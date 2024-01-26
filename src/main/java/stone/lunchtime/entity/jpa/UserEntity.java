// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.entity.jpa;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.EntityUtils;
import stone.lunchtime.entity.RoleLabel;
import stone.lunchtime.entity.Sex;

/**
 * The persistent class for the user database table.
 */
@Entity
@Table(name = "ltuser")
public class UserEntity extends AbstractJpaEntity {
	private static final Logger LOG = LoggerFactory.getLogger(UserEntity.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Column(name = "address", length = 500)
	private String address;

	@Column(name = "wallet", precision = 10, scale = 2)
	private BigDecimal wallet;

	@Column(name = "postal_code", length = 10)
	private String postalCode;

	@Column(name = "registration_date")
	private LocalDateTime registrationDate;

	@Column(name = "email", nullable = false, length = 300)
	private String email;

	@Column(name = "password", nullable = false, length = 1024)
	private String password;

	@Column(name = "name", length = 256)
	private String name;

	@Column(name = "firstname", length = 256)
	private String firstname;

	@Column(name = "phone", length = 15)
	private String phone;

	@Column(name = "town", length = 150)
	private String town;

	@Column(name = "sex")
	@Enumerated(EnumType.ORDINAL)
	private Sex sex;

	@Column(name = "status", nullable = false)
	private EntityStatus status;

	@OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RoleEntity> roles;

	// Handling of image is not done by this entity
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "image_id")
	private ImageEntity image;

	/**
	 * Gets the attribute value.
	 *
	 * @return the address value.
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pAdresse the new value for address attribute
	 */
	public void setAddress(String pAdresse) {
		this.address = EntityUtils.checkAndClean(pAdresse);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the wallet value.
	 */
	public BigDecimal getWallet() {
		return this.wallet == null ? BigDecimal.valueOf(0D) : this.wallet;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pCagnote the new value for wallet attribute
	 */
	public void setWallet(BigDecimal pCagnote) {
		if (pCagnote == null || pCagnote.doubleValue() < 0D) {
			this.wallet = BigDecimal.valueOf(0D);
		} else {
			this.wallet = pCagnote;
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the sex value.
	 */
	public Sex getSex() {
		return this.sex != null ? this.sex : Sex.MAN;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pSexe the new value for sex attribute
	 */
	public void setSex(Sex pSexe) {
		if (pSexe != null) {
			this.sex = pSexe;
		} else {
			this.sex = Sex.MAN;
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the postalCode value.
	 */
	public String getPostalCode() {
		return this.postalCode;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pCodePostal the new value for postalCode attribute
	 */
	public void setPostalCode(String pCodePostal) {
		this.postalCode = EntityUtils.checkAndClean(pCodePostal);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the registrationDate value.
	 */
	public LocalDateTime getRegistrationDate() {
		return this.registrationDate;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pRegistrationDate the new value for registrationDate attribute
	 */
	public void setRegistrationDate(LocalDateTime pRegistrationDate) {
		if (pRegistrationDate != null) {
			// Some data base handle nano s
			pRegistrationDate = pRegistrationDate.withNano(0);
		}
		this.registrationDate = pRegistrationDate;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the email value.
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pEmail the new value for email attribute
	 */
	public void setEmail(String pEmail) {
		this.email = EntityUtils.checkAndClean(pEmail);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the isLunchLady value.
	 */
	public Boolean getIsLunchLady() {
		if (this.roles != null && !this.roles.isEmpty()) {
			for (RoleEntity lre : this.roles) {
				if (RoleLabel.ROLE_LUNCHLADY == lre.getLabel()) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pIsLunchLady the new value for isLunchLady attribute
	 */
	public void setIsLunchLady(Boolean pIsLunchLady) {
		if (this.roles == null || this.roles.isEmpty()) {
			this.roles = new ArrayList<>();
		}
		if (Boolean.FALSE.equals(this.getIsLunchLady()) && Boolean.TRUE.equals(pIsLunchLady)) {
			UserEntity.LOG.atDebug().log("User {}/{} will be a lunchlady !", this.getId(), this.getEmail());
			this.roles.add(new RoleEntity(RoleLabel.ROLE_LUNCHLADY, this));
		} else if (Boolean.TRUE.equals(this.getIsLunchLady()) && Boolean.TRUE.equals(pIsLunchLady)) {
			UserEntity.LOG.atWarn().log("User {}/{} is already lunchlady !", this.getId(), this.getEmail());
		} else if (Boolean.TRUE.equals(this.getIsLunchLady()) && Boolean.FALSE.equals(pIsLunchLady)) {
			UserEntity.LOG.atDebug().log("User {}/{} will no more be a lunchlady !", this.getId(), this.getEmail());
			var iter = this.roles.iterator();
			while (iter.hasNext()) {
				var re = iter.next();
				if (RoleLabel.ROLE_LUNCHLADY == re.getLabel()) {
					iter.remove();
					break;
				}
			}
		}
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the password value.
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pMotDePasse the new value for password attribute
	 */
	public void setPassword(String pMotDePasse) {
		this.password = EntityUtils.checkAndClean(pMotDePasse);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the name value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pNom the new value for name attribute
	 */
	public void setName(String pNom) {
		this.name = EntityUtils.checkAndClean(pNom);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the firstname value.
	 */
	public String getFirstname() {
		return this.firstname;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPrenom the new value for firstname attribute
	 */
	public void setFirstname(String pPrenom) {
		this.firstname = EntityUtils.checkAndClean(pPrenom);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the phone value.
	 */
	public String getPhone() {
		return this.phone;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pTelephone the new value for phone attribute
	 */
	public void setPhone(String pTelephone) {
		this.phone = EntityUtils.checkAndClean(pTelephone);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the town value.
	 */
	public String getTown() {
		return this.town;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pVille the new value for town attribute
	 */
	public void setTown(String pVille) {
		this.town = EntityUtils.checkAndClean(pVille);
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
		if (pStatus != null) {
			this.status = pStatus;
		} else {
			this.status = EntityStatus.DISABLED;
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

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",name=");
		sb.append(this.getName());
		sb.append(",firstname=");
		sb.append(this.getFirstname());
		sb.append(",email=");
		sb.append(this.getEmail());
		sb.append(",status=");
		sb.append(this.getStatus());
		// Never show password in toString
		sb.append(",isLunchLady=");
		sb.append(this.getIsLunchLady());
		sb.append(",wallet=");
		sb.append(this.getWallet());
		sb.append(",registrationDate=");
		sb.append(this.getRegistrationDate());

		if (this.getAddress() != null && !this.getAddress().isEmpty()) {
			sb.append(",address=");
			sb.append(this.getAddress());
		}
		if (this.getPostalCode() != null && !this.getPostalCode().isEmpty()) {
			sb.append(",postalCode=");
			sb.append(this.getPostalCode());
		}
		if (this.getPhone() != null && !this.getPhone().isEmpty()) {
			sb.append(",phone=");
			sb.append(this.getPhone());
		}
		if (this.getTown() != null && !this.getTown().isEmpty()) {
			sb.append(",town=");
			sb.append(this.getTown());
		}
		if (this.getImage() != null) {
			sb.append(",image=");
			sb.append(this.getImage());
		}

		sb.append("}");
		return sb.toString();
	}

	/**
	 * Indicates if user has the status EntityStatus.ENABLED
	 *
	 * @return true if user has status EntityStatus.ENABLED
	 */
	public boolean isEnabled() {
		return EntityStatus.isEnabled(this.getStatus());
	}

	/**
	 * Indicates if user has the status EntityStatus.DELETED
	 *
	 * @return true if user has status EntityStatus.DELETED
	 */
	public boolean isDeleted() {
		return EntityStatus.isDeleted(this.getStatus());
	}

	/**
	 * Indicates if user has the status EntityStatus.DISABLED
	 *
	 * @return true if user has status EntityStatus.DISABLED
	 */
	public boolean isDisabled() {
		return EntityStatus.isDisabled(this.getStatus());
	}

	/**
	 * Indicates if user is man
	 *
	 * @return true if user is a man
	 */
	public boolean isMan() {
		return Sex.isMan(this.getSex());
	}

	/**
	 * Indicates if user is woman
	 *
	 * @return true if user is a woman
	 */
	public boolean isWoman() {
		return Sex.isWoman(this.getSex());
	}

	/**
	 * Indicates if user is not a man nor a woman
	 *
	 * @return true if user is not a man nor a woman
	 */
	public boolean isOther() {
		return Sex.isOther(this.getSex());
	}

	/**
	 * Gets the attribute roles.
	 *
	 * @return the value of roles.
	 */
	public List<RoleEntity> getRoles() {
		return this.roles;
	}

	/**
	 * Sets a new value for the attribute roles.
	 *
	 * @param pRoles the new value for the attribute.
	 */
	public void setRoles(List<RoleEntity> pRoles) {
		if (pRoles == null) {
			pRoles = new ArrayList<>();
		}
		this.roles = pRoles;
	}

}
