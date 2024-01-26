// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import stone.lunchtime.dto.out.UserDtoOut;

/**
 * Mother class of all controller.
 */
@Controller
public abstract class AbstractController {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractController.class);

	@Value("${configuration.date.pattern}")
	private String datePattern;

	/**
	 * Gets the connected user.
	 *
	 * @return the connected user or null if none found
	 */
	protected UserDtoOut getConnectedUser() {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		UserDtoOut resu = null;
		if (auth != null) {
			var user = auth.getDetails();
			if (user instanceof UserDtoOut dto) {
				resu = dto;
			} else {
				AbstractController.LOG.atWarn().log("getDetails is NOT a UserDtoOut but a is {}, will return null then",
						user != null ? user.getClass() : null);
			}
		}
		return resu;
	}

	/**
	 * Gets the connected user id.
	 *
	 * @return the connected user id or null if none found
	 */
	protected Integer getConnectedUserId() {
		var user = this.getConnectedUser();
		return user != null ? user.getId() : null;
	}

	/**
	 * Indicates if the authenticate user has the lunch lady role or not.
	 *
	 * @return true if the authenticate user has the lunch lady role, false
	 *         otherwise
	 */
	protected boolean hasLunchLadyRole() {
		var dto = this.getConnectedUser();
		return dto != null && dto.getIsLunchLady().booleanValue();
	}

	/**
	 * Transforms a String into a date.
	 *
	 * @param pDateValue a date value
	 * @return the date
	 */
	protected LocalDate getDate(String pDateValue) {
		if (pDateValue != null && !pDateValue.trim().isEmpty()) {
			try {
				return LocalDate.parse(pDateValue, DateTimeFormatter.ofPattern(this.datePattern));
			} catch (Exception lExp) {
				AbstractController.LOG.atWarn().log("Error, date is not valid", lExp);
			}
		}
		return null;
	}
}
