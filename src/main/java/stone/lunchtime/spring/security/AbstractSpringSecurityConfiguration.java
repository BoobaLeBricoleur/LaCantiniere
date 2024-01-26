// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.spring.security;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Security configuration. <br>
 * This is default configuration. <br>
 * Use 'unsecured' profile if you do not want to be in secured mode. Handle ONLY
 * Spring Security
 */
abstract class AbstractSpringSecurityConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSpringSecurityConfiguration.class);

	private static final String[] ALL_METHODS = new String[] { CorsConfiguration.ALL, HttpMethod.GET.name(),
			HttpMethod.HEAD.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(),
			HttpMethod.PATCH.name(), HttpMethod.OPTIONS.name(), HttpMethod.TRACE.name() };

	private static final String[] ALL_HEADERS = new String[] { CorsConfiguration.ALL, "Access-Control-Allow-Headers",
			"WWW-Authenticate", "Access-Control-Allow-Origin", "Origin,Accept", "X-Requested-With", "Content-Type",
			"Access-Control-Request-Method", "Access-Control-Request-Headers", SecurityConstants.TOKEN_HEADER };

	protected final Environment env;
	protected final AuthenticationProvider customAuthenticationProvider;
	protected final ObjectMapper mapper;

	/**
	 * Constructor of the object.
	 *
	 * @param pEnv                          environment
	 * @param pCustomAuthenticationProvider custom provider
	 * @param pMapper                       mapper
	 */
	@Autowired
	AbstractSpringSecurityConfiguration(Environment pEnv, AuthenticationProvider pCustomAuthenticationProvider,
			ObjectMapper pMapper) {
		this.env = pEnv;
		this.customAuthenticationProvider = pCustomAuthenticationProvider;
		this.mapper = pMapper;
	}

	/**
	 * Global CORS configuration.
	 *
	 * @return global cors configuration for Spring Security.
	 */
	protected CorsConfigurationSource corsConfigurationSource() {
		// BEAN for children to load
		AbstractSpringSecurityConfiguration.LOG.atDebug()
				.log("AbstractSpringSecurityConfiguration - Loading CORS definition ...");

		var config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern(CorsConfiguration.ALL); // "*"

		config.setAllowedHeaders(Arrays.asList(AbstractSpringSecurityConfiguration.ALL_HEADERS));
		config.setAllowedMethods(Arrays.asList(AbstractSpringSecurityConfiguration.ALL_METHODS));
		config.setExposedHeaders(Arrays.asList(AbstractSpringSecurityConfiguration.ALL_HEADERS));
		config.setMaxAge(Duration.of(2, ChronoUnit.HOURS));

		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Autowired
	public void configureGlobalSecurity(AuthenticationManagerBuilder auth) {
		AbstractSpringSecurityConfiguration.LOG.atDebug()
				.log("AbstractSpringSecurityConfiguration - Link with our Authentication provider");
		// Our Authentication Manager
		auth.authenticationProvider(this.customAuthenticationProvider);
	}

	/**
	 * Bean that will be used as AuthenticationManager
	 *
	 * @param authenticationConfiguration the configuration
	 * @return the bean AuthenticationManager
	 * @throws Exception if a problem occurred
	 */
	protected AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		// BEAN for children to load
		return authenticationConfiguration.getAuthenticationManager();
	}
}
