// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import stone.lunchtime.AbstractJpaWebTest;

/**
 * Test for forgot pwd controller, using Mock.
 */
class ForgotPasswordRestControllerTest extends AbstractJpaWebTest {
	private static final String URL_ROOT = "/forgotpassword";

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword01() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.post(ForgotPasswordRestControllerTest.URL_ROOT)
				.param("email", "toto@gmail.com"));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword02() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.post(ForgotPasswordRestControllerTest.URL_ROOT)
				.param("email", "txxxtxxx@gmail.com"));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	@Test
	void testForgotPassword03() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.post(ForgotPasswordRestControllerTest.URL_ROOT));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

}
