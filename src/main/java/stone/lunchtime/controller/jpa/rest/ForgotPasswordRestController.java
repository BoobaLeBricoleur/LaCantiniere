// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import stone.lunchtime.dto.out.ExceptionDtoOut;
import stone.lunchtime.service.IAuthenticationService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.SendMailException;

/**
 * Forgot password controller.
 */
@RestController
@RequestMapping("/forgotpassword")
@Tag(name = "Forgot password API", description = "Forgot password API")
public class ForgotPasswordRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(ForgotPasswordRestController.class);

	private final IAuthenticationService service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public ForgotPasswordRestController(IAuthenticationService pService) {
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
	@PostMapping
	@Observed(name = "rest.forgotpassword", contextualName = "rest#forgotpassword")
	@Operation(tags = {
			"Forgot password API" }, summary = "Sends an email to the user with its password.", description = "Will search the user in data base and send him a email with its password. You need to activate email sending in configuration file if you want this to work.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "An email was sent to the user."),
			@ApiResponse(responseCode = "400", description = "Email was not found or user is not in the ENABLED(0) status or email server does not work.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<Object> forgotPassword(
			@Parameter(description = "A user email", required = true) @RequestParam("email") String pEmail)
			throws EntityNotFoundException, SendMailException, InconsistentStatusException {
		ForgotPasswordRestController.LOG.atInfo().log("--> forgotPassword - {}", pEmail);
		this.service.forgotPassword(pEmail);
		ForgotPasswordRestController.LOG.atInfo().log("<-- forgotPassword - Email send at {} ", pEmail);
		return ResponseEntity.ok().build();
	}

}
