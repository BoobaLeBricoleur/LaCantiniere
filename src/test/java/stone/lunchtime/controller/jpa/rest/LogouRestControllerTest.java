// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.init.AbstractInitDataBase;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Test for logout controller, using Mock.
 */
class LogouRestControllerTest extends AbstractJpaWebTest {
	private static final String URL = "/logout";

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogout01() throws Exception {
		final var email = AbstractInitDataBase.USER_EXISTING_EMAIL;

		// The call to controller
		var result = super.logMeIn(email);
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		result = super.mockMvc.perform(
				MockMvcRequestBuilders.put(LogouRestControllerTest.URL).contentType(MediaType.APPLICATION_JSON_VALUE)
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
