// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.spring.security.filter;

/**
 * Constraint for security.
 */
public interface SecurityConstants {
	String AUTH_LOGIN_URL = "/login";
	String AUTH_LOGOUT_URL = "/logout";

	// JWT token defaults
	String TOKEN_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String TOKEN_ROLES = "roles";
	public static final String TOKEN_USER = "user";
	public static final String TOKEN_TYPE = "JWT";
	public static final String TOKEN_ISSUER = "secure-api";
	public static final String TOKEN_AUDIENCE = "secure-app";
}
