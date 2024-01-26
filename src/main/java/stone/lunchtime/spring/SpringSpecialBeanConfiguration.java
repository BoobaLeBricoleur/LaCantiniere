// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

/**
 * Other bean declaration. <br>
 */
@Configuration
public class SpringSpecialBeanConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(SpringSpecialBeanConfiguration.class);

	/**
	 * Password encoder for password.
	 *
	 * @return password encoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		SpringSpecialBeanConfiguration.LOG.atDebug().log("Loading BCryptPasswordEncoder bean");
		return new BCryptPasswordEncoder(BCryptVersion.$2Y);
	}

	/**
	 * Used by actuator metrics
	 *
	 * @return the observed aspect used by @Observed
	 */
	@Profile("actuator")
	@Bean
	public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		SpringSpecialBeanConfiguration.LOG.atDebug().log("Loading ObservedAspect bean for Actuator Profile only");
		return new ObservedAspect(observationRegistry);
	}
}
