// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.spring.security.filter.JwtAuthenticationFilter;
import stone.lunchtime.spring.security.filter.unsecured.JwtAuthorizationFilterUnsecured;

/**
 * Security configuration when you do not want it. <br>
 * This configuration can be used when the profile 'unsecured' is activated.
 * When used, you will never need to authenticate, and will always be a lunch
 * lady.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Profile("unsecured")
public class SpringSecurityConfigurationUnsecured extends AbstractSpringSecurityConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityConfigurationUnsecured.class);

	/**
	 * Constructor of the object.
	 *
	 * @param pEnv                          environment
	 * @param pCustomAuthenticationProvider custom provider
	 * @param pMapper                       mapper
	 */
	@Autowired
	public SpringSecurityConfigurationUnsecured(Environment pEnv, AuthenticationProvider pCustomAuthenticationProvider,
			ObjectMapper pMapper) {
		super(pEnv, pCustomAuthenticationProvider, pMapper);
	}

	@Override
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		return super.corsConfigurationSource();
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return super.authenticationManager(authenticationConfiguration);
	}

	@Bean
	@DependsOn("corsConfigurationSource")
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {
		SpringSecurityConfigurationUnsecured.LOG.atDebug().log("SpringSecurityConfigurationUnsecured - Apply rules");

		// Keep cors enable here, otherwise configuration of it is not applied
		http.csrf(CsrfConfigurer::disable).cors(cors -> cors.configurationSource(this.corsConfigurationSource()));
		var handler = new StoneAuthenticationFailureHandler(this.mapper);

		http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.headers(header -> header.frameOptions(FrameOptionsConfig::disable))
				.addFilterBefore(new JwtAuthenticationFilter(authenticationManager, this.env, this.mapper),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JwtAuthorizationFilterUnsecured(authenticationManager, this.env),
						UsernamePasswordAuthenticationFilter.class)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(
						exception -> exception.authenticationEntryPoint(handler).accessDeniedHandler(handler))
				.logout(logout -> {
					logout.clearAuthentication(true);
					logout.logoutSuccessHandler((pRequest, pResponse, pAuthentication) -> pResponse.setStatus(200));
				}).formLogin(FormLoginConfigurer::disable).httpBasic(HttpBasicConfigurer::disable);

		return http.build();
	}

}
