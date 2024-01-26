// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import stone.lunchtime.service.exception.AbstractFunctionalException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Will handle default HTTP Status and response body for exceptions.
 */
// FIXME @GraphQlExceptionHandler does NOT work for now ?
// Well not working with Spring Security annotation
@ControllerAdvice
public class GlobalGqlControllerExceptionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalGqlControllerExceptionHandler.class);

	/**
	 * Handles functional exceptions.
	 *
	 * @param pException the targeted exception
	 * @return the HttpStatus and body regarding the exception
	 */
	@GraphQlExceptionHandler(AbstractFunctionalException.class)
	public GraphQLError gqlExceptionHandler(AbstractFunctionalException pException) {
		GlobalGqlControllerExceptionHandler.LOG.atError().log("--> gqlExceptionHandler", pException);
		return GraphqlErrorException.newErrorException().message(pException.getLocalizedMessage())
				.errorClassification(ErrorType.OperationNotSupported).build();
	}

	/**
	 * Handles parameter exceptions.
	 *
	 * @param pException the targeted exception
	 * @return the HttpStatus and body regarding the exception
	 */
	@GraphQlExceptionHandler(ParameterException.class)
	public GraphQLError gqlExceptionHandler(ParameterException pException) {

		GlobalGqlControllerExceptionHandler.LOG.atError().log("--> gqlExceptionHandler", pException);
		return GraphqlErrorException.newErrorException().message(pException.getLocalizedMessage())
				.errorClassification(ErrorType.NullValueInNonNullableField).build();
	}

	/**
	 * Handles authentication and clearance exceptions.
	 *
	 * @param pException the targeted exception
	 * @return the HttpStatus and body regarding the exception
	 */
	@GraphQlExceptionHandler(AuthenticationException.class)
	public GraphQLError gqlExceptionHandler(AuthenticationException pException) {

		GlobalGqlControllerExceptionHandler.LOG.atError().log("--> gqlExceptionHandler", pException);
		return GraphqlErrorException.newErrorException().message(pException.getLocalizedMessage())
				.errorClassification(ErrorType.ExecutionAborted).build();
	}

}
