// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.spring.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import stone.lunchtime.dto.out.ExceptionDtoOut;

/**
 * Will handle failure login and REST issues.
 */
public class StoneAuthenticationFailureHandler
		implements AuthenticationFailureHandler, AuthenticationEntryPoint, AccessDeniedHandler {

	private static final Logger LOG = LoggerFactory.getLogger(StoneAuthenticationFailureHandler.class);

	private final ObjectMapper jsonObjectMapper;

	public StoneAuthenticationFailureHandler(ObjectMapper pJsonObjectMapper) {
		super();
		this.jsonObjectMapper = pJsonObjectMapper;
	}

	/**
	 * Will handle the response.
	 *
	 * @param response   the response
	 * @param pException the exception
	 * @param httpStatus the http status
	 * @throws IOException if an error occurred
	 */
	private void handle(HttpServletResponse response, Exception pException, int httpStatus) throws IOException {
		var out = new ExceptionDtoOut(pException);
		out.setStatus(Integer.valueOf(httpStatus));
		var expToJson = this.jsonObjectMapper.writeValueAsString(out);
		var pw = response.getWriter();
		pw.write(expToJson);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(httpStatus);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		StoneAuthenticationFailureHandler.LOG.atError().log(
				"--> 401 <--- From AuthenticationFailureHandler.onAuthenticationFailure for '{}'",
				request.getRequestURL(), authException);
		this.handle(response, authException, HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		StoneAuthenticationFailureHandler.LOG.atError().log(
				"--> 401 <--- From AuthenticationEntryPoint.commence for '{}'", request.getRequestURL(), authException);

		this.handle(response, authException, HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
			throws IOException, ServletException {
		StoneAuthenticationFailureHandler.LOG.atError().log("--> 403 <--- From AccessDeniedHandler.handle for '{}'",
				request.getRequestURL(), exception);

		this.handle(response, exception, HttpServletResponse.SC_FORBIDDEN);
	}

}
