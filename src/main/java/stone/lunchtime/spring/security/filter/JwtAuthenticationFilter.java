// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.spring.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import stone.lunchtime.dto.in.LoginDtoIn;
import stone.lunchtime.dto.out.ExceptionDtoOut;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.spring.security.StoneAuthenticationFailureHandler;

/**
 * Used for authentication.
 * <a href="https://dev.to/keysh/spring-security-with-jwt-3j76">tuto</a>
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter
		implements SecurityConstants, AuthenticationFailureHandler {
	private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private final Environment env;

	private final byte[] signingKey;
	private final SecretKey secretKey;

	private final AuthenticationManager authenticationManager;

	private final ObjectMapper jsonObjectMapper;

	/**
	 * Constructor of the object.
	 *
	 * @param pAuthenticationManager the authentication manager
	 * @param pEnv                   environment information
	 * @param pJsonObjectMapper      json mapper
	 */
	public JwtAuthenticationFilter(AuthenticationManager pAuthenticationManager, Environment pEnv,
			ObjectMapper pJsonObjectMapper) {
		this.authenticationManager = pAuthenticationManager;
		this.env = pEnv;
		this.jsonObjectMapper = pJsonObjectMapper;
		this.setFilterProcessesUrl(SecurityConstants.AUTH_LOGIN_URL);
		this.setAuthenticationFailureHandler(new StoneAuthenticationFailureHandler(pJsonObjectMapper));
		this.signingKey = pEnv
				.getProperty("configuration.jwt.key",
						"-KaPdSgVkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5u7x!A%")
				.getBytes(StandardCharsets.UTF_8);
		this.secretKey = Keys.hmacShaKeyFor(this.signingKey);

	}

	/**
	 * Can handle email,password as parameter or as JSOn in body
	 * ({"email":"xxx","password":"xxx"}).
	 *
	 * @param request  the request
	 * @param response the response
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		var username = request.getParameter("email");
		var password = request.getParameter("password");
		if (ObjectUtils.isEmpty(username) || ObjectUtils.isEmpty(password)) {
			JwtAuthenticationFilter.LOG.atDebug()
					.log("--> JwtAuthenticationFilter.attemptAuthentication(email, password) as Json in Body");
			// Look as JSon in the body
			String body = null;
			try {
				body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
				// Level is in trace, but we should hide password if present in body or if in
				// production
				JwtAuthenticationFilter.LOG.atTrace()
						.log("--> JwtAuthenticationFilter.attemptAuthentication(email, password) Body={}", body);
				var loginDtoIn = this.jsonObjectMapper.readValue(body, LoginDtoIn.class);
				username = loginDtoIn.getEmail();
				password = loginDtoIn.getPassword();
			} catch (Exception lExp) {
				JwtAuthenticationFilter.LOG.atError().log(
						"--> JwtAuthenticationFilter.attemptAuthentication - Error, your JSon is not right!, found {}, should be something like {\"email\":\"toto@gmail.com\",\"password\":\"bonjour\"}. DO NOT use simple quote!",
						body, lExp);
				throw new BadCredentialsException("Error, your JSon is not right!, read server logs");
			}
		} else {
			JwtAuthenticationFilter.LOG.atDebug()
					.log("--> JwtAuthenticationFilter.attemptAuthentication(email, password) as parameter");
		}

		JwtAuthenticationFilter.LOG.atDebug().log("--> JwtAuthenticationFilter.attemptAuthentication({}, [PROTECTED])",
				username);

		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		var result = this.authenticationManager.authenticate(authenticationToken);
		JwtAuthenticationFilter.LOG.atDebug().log(
				"--> JwtAuthenticationFilter.attemptAuthentication is ok - User id is {}",
				((UserDtoOut) result.getDetails()).getId());

		return result;
	}

	/**
	 * Will build the JWT Token if authentication is ok.
	 *
	 * @param request        the request
	 * @param response       the response
	 * @param filterChain    filters
	 * @param authentication login/pwd information taken from AuthenticationManager
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain, Authentication authentication) {
		var userName = (String) authentication.getPrincipal();

		var roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

		var ue = (UserDtoOut) authentication.getDetails();
		Map<String, Object> myClaim = new HashMap<>();
		// Mes infos
		myClaim.put(SecurityConstants.TOKEN_USER, ue);
		myClaim.put(SecurityConstants.TOKEN_ROLES, roles);
		// Infos standards
		myClaim.put(Claims.ISSUER, SecurityConstants.TOKEN_ISSUER);
		myClaim.put(Claims.AUDIENCE, SecurityConstants.TOKEN_AUDIENCE);
		myClaim.put(Claims.SUBJECT, userName);

		var now = LocalDateTime.now();
		myClaim.put(Claims.ISSUED_AT, Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
		var expireValueInMs = Integer.parseInt(this.env.getProperty("configuration.jwt.expire.in.ms", "86400000"));
		var expire = now.plus(expireValueInMs, ChronoUnit.MILLIS);
		myClaim.put(Claims.EXPIRATION, Date.from(expire.atZone(ZoneId.systemDefault()).toInstant()));

		Claims claims = new DefaultClaims(myClaim);

		var signatureAlgorithm = this.env.getProperty("configuration.jwt.signature.algorithm", "none");

		JwtBuilder builder;
		if ("none".equals(signatureAlgorithm)) {
			JwtAuthenticationFilter.LOG.atWarn()
					.log("[{}] - No encryption for JWT token, this is good for testing ...");
			builder = Jwts.builder().header().type(SecurityConstants.TOKEN_TYPE).and().claims(claims);
		} else {
			JwtAuthenticationFilter.LOG.atDebug().log(
					"Encryption for JWT token is {}, do not forget to set your key in the configuration file",
					signatureAlgorithm);
			builder = Jwts.builder().signWith(this.secretKey).header().type(SecurityConstants.TOKEN_TYPE).and()
					.claims(claims);
		}

		// Sonar does not like this line because it thinks there is no signwith
		var token = builder.compact(); // NOSONAR
		response.addHeader(SecurityConstants.TOKEN_HEADER, SecurityConstants.TOKEN_PREFIX + token);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException pException) throws IOException, ServletException {
		var out = new ExceptionDtoOut(pException);
		var expToJson = this.jsonObjectMapper.writeValueAsString(out);
		var pw = response.getWriter();
		pw.write(expToJson);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

	}

}
