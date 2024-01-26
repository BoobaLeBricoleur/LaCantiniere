// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.spring.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import stone.lunchtime.dto.out.UserDtoOut;

/**
 * Used when asking for secured information.
 * <a href="https://dev.to/keysh/spring-security-with-jwt-3j76">tuto</a>
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtAuthorizationFilter extends BasicAuthenticationFilter implements SecurityConstants {
	private static final Logger LOG = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

	private final byte[] signingKey;
	private final SecretKey secretKey;

	/**
	 * Constructor of the object.
	 *
	 * @param pAuthenticationManager the authentication manager
	 * @param pEnv                   environment information
	 */
	public JwtAuthorizationFilter(AuthenticationManager pAuthenticationManager, Environment pEnv) {
		super(pAuthenticationManager);
		this.signingKey = pEnv
				.getProperty("configuration.jwt.key",
						"-KaPdSgVkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5u7x!A%")
				.getBytes(StandardCharsets.UTF_8);
		this.secretKey = Keys.hmacShaKeyFor(this.signingKey);
	}

	/**
	 * Checks for token validity.
	 *
	 * @param request     the request
	 * @param response    the response
	 * @param filterChain filters
	 * @throws IOException      if an error occurred
	 * @throws ServletException if an error occurred
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		final var url = request.getRequestURL().toString();
		var jwtToken = request.getHeader(SecurityConstants.TOKEN_HEADER);
		JwtAuthorizationFilter.LOG.atTrace().log("<-- JwtAuthorizationFilter.doFilterInternal - {} - JWT token is {}",
				url, jwtToken);
		var jwtParser = Jwts.parser().verifyWith(this.secretKey).build();
		if (ObjectUtils.isEmpty(jwtToken) || !jwtToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
			JwtAuthorizationFilter.LOG.atWarn()
					.log("<-- JwtAuthorizationFilter.doFilterInternal - {} - JWT token is Empty", url);
			// SecurityContextHolder.clearContext();
		} else if (!this.validateToken(jwtToken.replace(SecurityConstants.TOKEN_PREFIX, ""), jwtParser)) {
			JwtAuthorizationFilter.LOG.atError()
					.log("<-- JwtAuthorizationFilter.doFilterInternal - {} - JWT token is Invalid", url);
			// SecurityContextHolder.clearContext();
		} else {
			Authentication authentication = this.getAuthentication(jwtToken.replace(SecurityConstants.TOKEN_PREFIX, ""),
					jwtParser);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			JwtAuthorizationFilter.LOG.atDebug()
					.log("<-- JwtAuthorizationFilter.doFilterInternal - {} - OK - Set authentication back", url);
		}
		filterChain.doFilter(request, response);
	}

	/**
	 * Will validate token.
	 *
	 * @param token  a token without Bearer in it
	 * @param parser parser
	 * @return true if token is ok, false if not
	 */
	private boolean validateToken(String token, JwtParser parser) {
		JwtAuthorizationFilter.LOG.atTrace().log("--> JwtAuthorizationFilter.validateToken - Token - {}", token);
		try {
			parser.parseSignedClaims(token);
			JwtAuthorizationFilter.LOG.atTrace().log("--> JwtAuthorizationFilter.validateToken - Token is OK");
			return true;
		} catch (Exception e) {
			JwtAuthorizationFilter.LOG.atError().log("--> JwtAuthorizationFilter.validateToken - Token is KO", e);
		}
		return false;
	}

	/**
	 * Rebuild UsernamePasswordAuthenticationToken for SpringSecurity from JWT
	 * token.
	 *
	 * @param token  the JWT token without Bearer in it
	 * @param parser the JwtParser
	 * @return the token for Spring Security
	 */
	private UsernamePasswordAuthenticationToken getAuthentication(String token, JwtParser parser) {
		JwtAuthorizationFilter.LOG.atDebug().log("--> JwtAuthorizationFilter.getAuthentication - Token - {}", token);
		try {

			var parsedToken = parser.parseSignedClaims(token.replace(SecurityConstants.TOKEN_PREFIX, "").trim());

			var username = parsedToken.getPayload().getSubject();
			// FIXME We are building the Spring Security token FROM the JWT
			// But what if the JWT has wrong information ...
			Collection<? extends GrantedAuthority> authorities = ((List<?>) parsedToken.getPayload()
					.get(SecurityConstants.TOKEN_ROLES)).stream()
					.map(authority -> new SimpleGrantedAuthority((String) authority)).toList();

			if (!ObjectUtils.isEmpty(username)) {
				var resu = new UsernamePasswordAuthenticationToken(username, null, authorities);
				@SuppressWarnings("unchecked")
				Map<String, ?> userDto = (Map<String, ?>) parsedToken.getPayload().get(SecurityConstants.TOKEN_USER);
				JwtAuthorizationFilter.LOG.atTrace().log("val {}", userDto);
				var userDtoOut = new UserDtoOut(userDto);
				resu.setDetails(userDtoOut);
				JwtAuthorizationFilter.LOG.atDebug().log(
						"<-- JwtAuthorizationFilter.getAuthentication - Token was pushed into Spring Security, {}",
						resu);
				return resu;
			}
		} catch (Exception exception) {
			JwtAuthorizationFilter.LOG.atError().log("- JwtAuthorizationFilter.getAuthentication : {} failed", token,
					exception);
		}
		return null;
	}

}
