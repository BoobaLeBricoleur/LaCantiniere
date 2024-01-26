// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.spring.security.filter.JwtAuthenticationFilter;
import stone.lunchtime.spring.security.filter.JwtAuthorizationFilter;

/**
 * Security configuration. <br>
 * This is default configuration. <br>
 * Use 'unsecured' profile if you do not want to be in secured mode. Handle ONLY
 * Spring Security
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@ConditionalOnMissingBean(SpringSecurityConfigurationUnsecured.class)
public class SpringSecurityConfigurationSecured extends AbstractSpringSecurityConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityConfigurationSecured.class);

	/**
	 * Constructor of the object.
	 *
	 * @param pEnv                          environment
	 * @param pCustomAuthenticationProvider custom provider
	 * @param pMapper                       mapper
	 */
	@Autowired
	public SpringSecurityConfigurationSecured(Environment pEnv, AuthenticationProvider pCustomAuthenticationProvider,
			ObjectMapper pMapper) {
		super(pEnv, pCustomAuthenticationProvider, pMapper);
	}

	@Override
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return super.authenticationManager(authenticationConfiguration);
	}

	@Override
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		return super.corsConfigurationSource();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		final var h2Url = super.env.getProperty("spring.h2.console.path", "/h2");
		return web -> web.ignoring().requestMatchers(new AntPathRequestMatcher(h2Url + "/**"));
	}

	@Bean
	@DependsOn("corsConfigurationSource")
	public SecurityFilterChain web(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
		SpringSecurityConfigurationSecured.LOG.atDebug().log("SpringSecurityConfigurationSecured - Apply rules");
		final var h2Url = super.env.getProperty("spring.h2.console.path", "/h2");
		SpringSecurityConfigurationSecured.LOG.atDebug().log("SpringSecurityConfigurationSecured - H2 console is on {}",
				h2Url);
		// Keep cors enable here, otherwise configuration of it is not applied
		http.csrf(CsrfConfigurer::disable).cors(cors -> cors.configurationSource(this.corsConfigurationSource()));
		var handler = new StoneAuthenticationFailureHandler(this.mapper);
		http.authorizeHttpRequests(authorize -> authorize.requestMatchers(new AntPathRequestMatcher(h2Url + "/**") // h2
		).permitAll()).headers(header -> header.frameOptions(FrameOptionsConfig::disable))
				.authorizeHttpRequests(authorize -> authorize.requestMatchers(new AntPathRequestMatcher("/"), // Root
						new AntPathRequestMatcher("/favicon.ico*"), //
						new AntPathRequestMatcher("/csrf/**"), //
						new AntPathRequestMatcher("/actuator/**"), //
						new AntPathRequestMatcher("/v3/api-docs/**"), // Swagger
						new AntPathRequestMatcher("/v3/api-docs**"), //
						new AntPathRequestMatcher("/configuration/**"), //
						new AntPathRequestMatcher("/swagger-ui.html"), //
						new AntPathRequestMatcher("/swagger-resources/**"), //
						new AntPathRequestMatcher("/swagger*/**"), //
						new AntPathRequestMatcher("/webjars/**"), //
						new AntPathRequestMatcher("/forgotpassword"), // Lunchtime API
						new AntPathRequestMatcher("/graphql/**"), // All graphql request
						new AntPathRequestMatcher("/graphiql/**"), // All graphql request
						new AntPathRequestMatcher("/constraint/findall"), //
						new AntPathRequestMatcher("/constraint/find/**"), //
						new AntPathRequestMatcher("/ingredient/find/**"), //
						new AntPathRequestMatcher("/ingredient/findimg/**"), //
						new AntPathRequestMatcher("/meal/find/**"), //
						new AntPathRequestMatcher("/meal/findimg/**"), //
						new AntPathRequestMatcher("/meal/findallavailableforweek/**"), //
						new AntPathRequestMatcher("/meal/findallavailableforweekandday/**"), //
						new AntPathRequestMatcher("/meal/findallavailablefortoday"), //
						new AntPathRequestMatcher("/meal/findallavailableforthisweek"), //
						new AntPathRequestMatcher("/menu/findallavailablefortoday"), //
						new AntPathRequestMatcher("/menu/findallavailableforweekandday/**"), //
						new AntPathRequestMatcher("/menu/findallavailableforweek/**"), //
						new AntPathRequestMatcher("/menu/findallavailableforthisweek"), //
						new AntPathRequestMatcher("/menu/find/**"), //
						new AntPathRequestMatcher("/menu/findimg/**"), //
						new AntPathRequestMatcher("/user/register"), //
						new AntPathRequestMatcher("/img/**"), //
						new AntPathRequestMatcher("/error"), //
						new AntPathRequestMatcher("license.txt")).permitAll().anyRequest().authenticated())
				.addFilterBefore(new JwtAuthenticationFilter(authenticationManager, this.env, this.mapper),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JwtAuthorizationFilter(authenticationManager, this.env),
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
