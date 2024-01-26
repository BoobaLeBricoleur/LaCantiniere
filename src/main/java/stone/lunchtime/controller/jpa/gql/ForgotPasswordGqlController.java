// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import io.micrometer.observation.annotation.Observed;
import stone.lunchtime.service.IAuthenticationService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.SendMailException;

/**
 * Forgot password controller.
 */
@Controller
public class ForgotPasswordGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(ForgotPasswordGqlController.class);

	private final IAuthenticationService service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public ForgotPasswordGqlController(IAuthenticationService pService) {
		super();
		this.service = pService;
	}

	/**
	 * Will email the specified user with its password in it.
	 *
	 * @param pEmail an email
	 *
	 * @return the HTTP Status regarding success or failure
	 * @throws InconsistentStatusException if an error occurred
	 * @throws SendMailException           if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 *
	 */
	@MutationMapping
	@Observed(name = "graphql.forgotpassword", contextualName = "graphql#forgotpassword")
	public Integer forgotPassword(@Argument("email") String pEmail)
			throws EntityNotFoundException, SendMailException, InconsistentStatusException {
		ForgotPasswordGqlController.LOG.atInfo().log("--> forgotPassword - {}", pEmail);
		this.service.forgotPassword(pEmail);
		ForgotPasswordGqlController.LOG.atInfo().log("<-- forgotPassword - Email send at {}", pEmail);
		return HttpStatus.OK.ordinal();
	}

}
