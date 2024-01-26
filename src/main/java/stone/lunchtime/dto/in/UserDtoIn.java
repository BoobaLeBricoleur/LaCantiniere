// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;
import java.text.DecimalFormat;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.DtoUtils;
import stone.lunchtime.entity.Sex;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the user.
 */
@Schema(description = "Represents a user of the application.")
public class UserDtoIn extends AbstractDtoIn {
	private static final Logger LOG = LoggerFactory.getLogger(UserDtoIn.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "Adress of the user.", nullable = true, example = "3 road of iron")
	private String address;
	@Schema(description = "Amount of money owned by the user.", requiredMode = Schema.RequiredMode.REQUIRED, example = "35.5", minimum = "0", maximum = "999")

	private Float wallet;
	@Schema(description = "Postal code of the user.", nullable = true, example = "78140")
	private String postalCode;
	@Schema(description = "Email of the user.", requiredMode = Schema.RequiredMode.REQUIRED, example = "toto@aol.com")

	private String email;
	@Schema(description = "Indicates if the user has the lunch lady role.", nullable = true)
	private Boolean isLunchLady;
	// Password IS handled by IN DTO
	@Schema(description = "The user password.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String password;
	@Schema(description = "The name of the user.", nullable = true, example = "Albert")
	private String name;
	@Schema(description = "The first name of the user.", nullable = true, example = "Smith")
	private String firstname;
	@Schema(description = "The phone number of the user.", nullable = true, example = "0147503190")
	private String phone;
	@Schema(description = "The town of the user.", nullable = true, example = "Versailles")
	private String town;
	@Schema(description = "The sex of the user. 0 for man, 1 for woman, 2 for other", nullable = true, example = "0", defaultValue = "0", minimum = "0", maximum = "2", type = "number", allowableValues = {
			"0", "1", "2" })

	private Sex sex;

	@Schema(description = "The image.", nullable = true)
	private ImageDtoIn image;

	// Status is not handled by IN DTO
	// registrationDate is not handled by IN DTO

	/**
	 * Constructor of the object.
	 */
	public UserDtoIn() {
		super();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the sex value.
	 */
	public Sex getSex() {
		return this.sex;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pSex the new value for sex attribute
	 */
	public void setSex(Sex pSex) {
		if (pSex == null) {
			this.sex = Sex.MAN;
		} else {
			this.sex = pSex;
		}
	}

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
	 * @param pValue the new value for address attribute
	 */
	public void setAddress(String pValue) {
		this.address = DtoUtils.checkAndClean(pValue);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the wallet value.
	 */
	public Float getWallet() {
		return this.wallet;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pWallet the new value for wallet attribute
	 */
	public void setWallet(Float pWallet) {
		if (pWallet == null || pWallet.floatValue() < 0F) {
			this.wallet = Float.valueOf(0F);
		} else {
			this.wallet = pWallet;
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
	 * Gets the attribute value.
	 *
	 * @return the email value.
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the isLunchLady value.
	 */
	public Boolean getIsLunchLady() {
		return this.isLunchLady != null ? this.isLunchLady : Boolean.FALSE;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pIsLunchLady the new value for isLunchLady attribute
	 */
	public void setIsLunchLady(Boolean pIsLunchLady) {
		this.isLunchLady = Objects.requireNonNullElse(pIsLunchLady, Boolean.FALSE);
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
	 * Gets the attribute value.
	 *
	 * @return the name value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the first name value.
	 */
	public String getFirstname() {
		return this.firstname;
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
	 * Gets the attribute value.
	 *
	 * @return the town value.
	 */
	public String getTown() {
		return this.town;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the image value.
	 */
	public ImageDtoIn getImage() {
		return this.image;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPostalCode the new value for postalCode attribute
	 */
	public void setPostalCode(String pPostalCode) {
		this.postalCode = DtoUtils.checkAndClean(pPostalCode);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pEmail the new value for email attribute
	 */
	public void setEmail(String pEmail) {
		this.email = DtoUtils.checkAndClean(pEmail);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPassword the new value for password attribute
	 */
	public void setPassword(String pPassword) {
		this.password = DtoUtils.checkAndClean(pPassword);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pName the new value for name attribute
	 */
	public void setName(String pName) {
		this.name = DtoUtils.checkAndClean(pName);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pFirstName the new value for firstname attribute
	 */
	public void setFirstname(String pFirstName) {
		this.firstname = DtoUtils.checkAndClean(pFirstName);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pPhone the new value for phone attribute
	 */
	public void setPhone(String pPhone) {
		this.phone = DtoUtils.checkAndClean(pPhone);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pTown the new value for town attribute
	 */
	public void setTown(String pTown) {
		this.town = DtoUtils.checkAndClean(pTown);
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pImage the new value for image attribute
	 */
	public void setImage(ImageDtoIn pImage) {
		this.image = pImage;
	}

	@Override
	@JsonIgnore
	public void validate() {
		if (this.getEmail() == null) {
			UserDtoIn.LOG.atError().log("validate (User email is null)");
			throw new ParameterException("Email est null ou vide !", "email");
		}
		if (this.getPassword() == null) {
			UserDtoIn.LOG.atError().log("validate (User password is null)");
			throw new ParameterException("Mot de passe est null ou vide !", "password");
		}

		if (this.getWallet() == null || this.getWallet().doubleValue() < 0) {
			UserDtoIn.LOG.atWarn().log("validate (User wallet will be 0)");
			this.setWallet(Float.valueOf(0F));
		}
		if (this.getSex() == null) {
			UserDtoIn.LOG.atWarn().log("validate (Sex is null)");
			throw new ParameterException("Sex est null ou vide !", "sex");
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append("name=");
		sb.append(this.name);
		sb.append(",firstname=");
		sb.append(this.firstname);
		sb.append(",address=");
		sb.append(this.address);
		if (this.getWallet() != null && this.getWallet().doubleValue() != 0) {
			var df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(0);
			df.setGroupingUsed(false);
			sb.append(",wallet=");
			sb.append(df.format(this.wallet));
		} else {
			sb.append(",wallet=0");
		}

		sb.append(",postalCode=");
		sb.append(this.postalCode);
		sb.append(",email=");
		sb.append(this.email);
		sb.append(",isLunchLady=");
		sb.append(this.isLunchLady);
		// DON'T : append password in toString
		sb.append(",phone=");
		sb.append(this.phone);
		sb.append(",town=");
		sb.append(this.town);
		if (this.getImage() != null) {
			sb.append(",image=");
			sb.append(this.getImage());
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Indicates if user is man
	 *
	 * @return true if user is a man
	 */
	@JsonIgnore
	public boolean isMan() {
		return Sex.MAN == this.getSex();
	}

	/**
	 * Indicates if user is woman
	 *
	 * @return true if user is a woman
	 */
	@JsonIgnore
	public boolean isWoman() {
		return Sex.WOMAN == this.getSex();
	}

	/**
	 * Indicates if user is not a man nor a woman
	 *
	 * @return true if user is not a man nor a woman
	 */
	@JsonIgnore
	public boolean isOther() {
		return Sex.OTHER == this.getSex();
	}

}
