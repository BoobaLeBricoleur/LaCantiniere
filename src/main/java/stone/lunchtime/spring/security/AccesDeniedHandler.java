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
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import stone.lunchtime.dto.out.ExceptionDtoOut;

/**
 * Handle Access Denied.
 */
public final class AccesDeniedHandler implements AccessDeniedHandler {
	private static final Logger LOG = LoggerFactory.getLogger(AccesDeniedHandler.class);

	private final ObjectMapper jsonObjectMapper;

	public AccesDeniedHandler(ObjectMapper pMapper) {
		super();
		this.jsonObjectMapper = pMapper;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
			throws IOException, ServletException {
		AccesDeniedHandler.LOG.atError().log("--> 403 <--- From AccesDeniedHandler for '{}'", request.getRequestURL(),
				exception);

		// We want our Json Exception model instead of the one in Spring
		var out = new ExceptionDtoOut(exception);
		var expToJson = this.jsonObjectMapper.writeValueAsString(out);
		var pw = response.getWriter();
		pw.write(expToJson);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		// This will use the Spring Json Exception model :
		// response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
	}

}
