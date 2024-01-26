// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.DtoUtils;
import stone.lunchtime.service.exception.ParameterException;

/**
 * The dto class for the login only.
 */
@Schema(description = "Login informations.")
public class LoginDtoIn extends AbstractDtoIn {
	private static final Logger LOG = LoggerFactory.getLogger(LoginDtoIn.class);
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "Email of a user.", requiredMode = Schema.RequiredMode.REQUIRED, example = "toto@aol.com")
	private String email;
	@Schema(description = "Password of the user.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String password;

	/**
	 * Constructor of the object.
	 */
	public LoginDtoIn() {
		super();
	}

	/**
	 * Constructor of the object.
	 *
	 * @param pEmail    an email
	 * @param pPassword a password
	 */
	public LoginDtoIn(String pEmail, String pPassword) {
		super();
		this.setEmail(pEmail);
		this.setPassword(pPassword);
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
	 * @return the password value.
	 */
	public String getPassword() {
		return this.password;
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

	@Override
	@JsonIgnore
	public void validate() {
		if (this.getEmail() == null) {
			LoginDtoIn.LOG.atError().log("validate (User email is null)");
			throw new ParameterException("Email est null ou vide !", "email");
		}
		if (this.getPassword() == null) {
			LoginDtoIn.LOG.atError().log("validate (User password is null)");
			throw new ParameterException("Mot de passe est null ou vide !", "password");
		}
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append("email=");
		// DONT append password
		sb.append("}");
		return sb.toString();
	}
}
