// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import stone.lunchtime.dto.out.ExceptionDtoOut;
import stone.lunchtime.service.exception.AbstractFunctionalException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Will handle default HTTP Status and response body for exceptions.
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	/**
	 * Handles functional exceptions.
	 *
	 * @param pException the targeted exception
	 * @return the HttpStatus and body regarding the exception
	 */
	@ExceptionHandler(AbstractFunctionalException.class)
	public ResponseEntity<ExceptionDtoOut> exceptionHandler(AbstractFunctionalException pException) {

		GlobalControllerExceptionHandler.LOG.atError().log("--> exceptionHandler", pException);
		var dtoOut = new ExceptionDtoOut(pException);
		GlobalControllerExceptionHandler.LOG.atError().log("<-- exceptionHandler");
		return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(dtoOut); // 412
	}

	/**
	 * Handles parameter exceptions.
	 *
	 * @param pException the targeted exception
	 * @return the HttpStatus and body regarding the exception
	 */
	@ExceptionHandler(ParameterException.class)
	public ResponseEntity<ExceptionDtoOut> exceptionHandler(ParameterException pException) {

		GlobalControllerExceptionHandler.LOG.atError().log("--> exceptionHandler", pException);
		var dtoOut = new ExceptionDtoOut(pException);
		GlobalControllerExceptionHandler.LOG.atError().log("<-- exceptionHandler");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dtoOut); // 400
	}

	/**
	 * Handles authentication and clearance exceptions.
	 *
	 * @param pException the targeted exception
	 * @return the HttpStatus and body regarding the exception
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ExceptionDtoOut> exceptionHandler(AuthenticationException pException) {

		GlobalControllerExceptionHandler.LOG.atError().log("--> exceptionHandler", pException);
		var dtoOut = new ExceptionDtoOut(pException);
		GlobalControllerExceptionHandler.LOG.atError().log("<-- exceptionHandler");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dtoOut); // 401
	}

}
