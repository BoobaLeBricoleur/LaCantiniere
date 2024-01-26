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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import stone.lunchtime.dto.out.ExceptionDtoOut;

/**
 * Handle login REST return value if error. <br>
 * Take back the code in
 * org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
 * but remove the header WWW-Authenticate as specified in normalisation.
 */
public final class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private static final Logger LOG = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

	private final ObjectMapper jsonObjectMapper;

	public RestAuthenticationEntryPoint(ObjectMapper pMapper) {
		super();
		this.jsonObjectMapper = pMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		RestAuthenticationEntryPoint.LOG.atError().log("--> 401 <--- From AuthenticationEntryPoint for '{}'",
				request.getRequestURL(), authException);

		// We want our Json Exception model instead of the one in Spring
		var out = new ExceptionDtoOut(authException);
		var expToJson = this.jsonObjectMapper.writeValueAsString(out);
		var pw = response.getWriter();
		pw.write(expToJson);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		// org.springframework.http.HttpStatus.UNAUTHORIZED in Spring
		// This will use the Spring Json Exception model :
		// response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
	}
}
