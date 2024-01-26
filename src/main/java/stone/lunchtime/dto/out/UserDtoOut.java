// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dto.out;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.DtoUtils;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.Sex;

/**
 * The DTO out class for the User.
 */
@Schema(description = "Represents a user of the application. No password is present in this DTO.")
public class UserDtoOut extends AbstractDtoOut {
	@JsonIgnore
	private static final Logger LOG = LoggerFactory.getLogger(UserDtoOut.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "Adress of the user.", example = "3 road of iron")
	private String address;
	@Schema(description = "Amount of money owned by the user.", example = "35.5")
	private Float wallet;
	@Schema(description = "Postal code of the user.", example = "78140")
	private String postalCode;
	@Schema(description = "Date of creation.")
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime registrationDate;
	@Schema(description = "Email of the user.", example = "toto@aol.com")
	private String email;
	@Schema(description = "Indicates if the user has the lunch lady role.")
	private Boolean isLunchLady;
	// NO password for DTO-OUT
	@Schema(description = "The name of the user.", example = "Albert")
	private String name;
	@Schema(description = "The first name of the user.", example = "Smith")
	private String firstname;
	@Schema(description = "The phone number of the user.", example = "0147503190")
	private String phone;
	@Schema(description = "The town of the user.", example = "Versailles")
	private String town;
	@Schema(description = "The sex of the user. 0 for man, 1 for woman, 2 for other", example = "0", type = "number", allowableValues = {
			"0", "1", "2" })
	private Sex sex;
	@Schema(description = "The status for the user. 0 for Enabled, 1 for Disabled, 2 for Deleted", example = "0", type = "number", allowableValues = {
			"0", "1", "2" })
	private EntityStatus status;

	// We do this in order to limit the size of this DTO.
	// Otherwise, image would be sent always, this will cause a heavy load of data
	@Schema(description = "The image id.", nullable = true)
	private Integer imageId;

	/**
	 * Constructor of the object.
	 */
	public UserDtoOut() {
		super();
	}

	/**
	 * Constructor of the object.<br>
	 *
	 * @param pId a value for PK
	 */
	public UserDtoOut(Integer pId) {
		super(pId);
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pMap where to take information
	 */
	public UserDtoOut(Map<String, ?> pMap) {
		super();
		if (pMap.get("id") != null) {
			this.setId((Integer) pMap.get("id"));
		}
		this.setAddress((String) pMap.get("address"));
		if (pMap.get("wallet") != null) {
			this.setWallet(Float.valueOf(((Number) pMap.get("wallet")).floatValue()));
		} else {
			this.setWallet(Float.valueOf(0F));
		}
		this.setPostalCode((String) pMap.get("postalCode"));
		this.setEmail((String) pMap.get("email"));
		this.setImageId((Integer) pMap.get("imageId"));
		this.setName((String) pMap.get("name"));
		this.setFirstname((String) pMap.get("firstname"));
		this.setPhone((String) pMap.get("phone"));
		this.setTown((String) pMap.get("town"));

		this.setIsLunchLady(Boolean.FALSE);
		if (pMap.get("isLunchLady") != null) {
			this.setIsLunchLady((Boolean) pMap.get("isLunchLady"));
		}

		this.setSex(Sex.OTHER);
		var mapS = (String) pMap.get("sex");
		if (mapS != null) {
			this.setSex(Sex.valueOf(mapS));
		}

		if (pMap.get("registrationDate") != null) {
			UserDtoOut.LOG.atTrace().log("registrationDate is stored as {}",
					pMap.get("registrationDate").getClass().getName());
			@SuppressWarnings("unchecked")
			var o = (List<Integer>) pMap.get("registrationDate");
			// [2019, 3, 2, 15, 17, 28]
			this.setRegistrationDate(
					LocalDateTime.of(o.get(0), Month.of(o.get(1)), o.get(2), o.get(3), o.get(4), o.get(5), 0));
		}

		this.setStatus(EntityStatus.DISABLED);
		var mapStatus = (String) pMap.get("status");
		if (mapStatus != null) {
			this.setStatus(EntityStatus.valueOf(mapStatus));
		}
	}

	/**
	 * Gets the attribute imageId.
	 *
	 * @return the value of imageId.
	 */
	public Integer getImageId() {
		return this.imageId;
	}

	/**
	 * Sets a new value for the attribute imageId.
	 *
	 * @param pImageId the new value for the attribute.
	 */
	public void setImageId(Integer pImageId) {
		this.imageId = pImageId;
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
	 * @param pAddress the new value for address attribute
	 */
	public void setAddress(String pAddress) {
		this.address = pAddress;
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
		this.wallet = pWallet;
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
	 * @param pPostalCode the new value for postalCode attribute
	 */
	public void setPostalCode(String pPostalCode) {
		this.postalCode = pPostalCode;
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
		this.email = pEmail;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the isLunchLady value.
	 */
	public Boolean getIsLunchLady() {
		return this.isLunchLady;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pIsLunchLady the new value for isLunchLady attribute
	 */
	public void setIsLunchLady(Boolean pIsLunchLady) {
		this.isLunchLady = pIsLunchLady;
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
	 * @param pName the new value for name attribute
	 */
	public void setName(String pName) {
		this.name = pName;
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
	 * @param pFirstname the new value for firstname attribute
	 */
	public void setFirstname(String pFirstname) {
		this.firstname = pFirstname;
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
	 * @param pPhone the new value for phone attribute
	 */
	public void setPhone(String pPhone) {
		this.phone = pPhone;
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
	 * @param pTown the new value for town attribute
	 */
	public void setTown(String pTown) {
		this.town = pTown;
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
		this.sex = pSex;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the status value.
	 */
	public EntityStatus getStatus() {
		return this.status;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pStatus the new value for status attribute
	 */
	public void setStatus(EntityStatus pStatus) {
		this.status = pStatus;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append(",address=");
		sb.append(this.address);
		sb.append(",wallet=");
		sb.append(DtoUtils.formatNumber(this.wallet));
		sb.append(",postalCode=");
		sb.append(this.postalCode);
		sb.append(",email=");
		sb.append(this.email);
		sb.append(",isLunchLady=");
		sb.append(this.isLunchLady);
		sb.append(",status=");
		sb.append(this.getStatus());
		sb.append(",name=");
		sb.append(this.name);
		sb.append(",firstname=");
		sb.append(this.firstname);
		sb.append(",phone=");
		sb.append(this.phone);
		sb.append(",town=");
		sb.append(this.town);
		if (this.getImageId() != null) {
			sb.append(",imageId=");
			sb.append(this.getImageId());
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Indicates if user has the status EntityStatus.ENABLED
	 *
	 * @return true if user has status EntityStatus.ENABLED
	 */
	@JsonIgnore
	public boolean isEnabled() {
		return EntityStatus.isEnabled(this.getStatus());
	}

	/**
	 * Indicates if user has the status EntityStatus.DELETED
	 *
	 * @return true if user has status EntityStatus.DELETED
	 */
	@JsonIgnore
	public boolean isDeleted() {
		return EntityStatus.isDeleted(this.getStatus());
	}

	/**
	 * Indicates if user has the status EntityStatus.DISABLED
	 *
	 * @return true if user has status EntityStatus.DISABLED
	 */
	@JsonIgnore
	public boolean isDisabled() {
		return EntityStatus.isDisabled(this.getStatus());
	}
}
