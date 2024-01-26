// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.init.AbstractInitDataBase;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Test for login controller, using Mock.
 */
class LoginRestControllerTest extends AbstractJpaWebTest {

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin01() throws Exception {
		// The call to controller
		var result = super.logMeInAsLunchLady();
		result.andExpect(MockMvcResultMatchers.header().exists(SecurityConstants.TOKEN_HEADER));
		var user = super.getUserInToken(result);
		Assertions.assertNotNull(user, "Header shoud contain user");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, user.getEmail(),
				"Header shoud contain user with email");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin02() throws Exception {
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", AbstractInitDataBase.USER_EXISTING_EMAIL).param("password", new String[] { null }));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin03() throws Exception {
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", new String[] { null }).param("password", AbstractInitDataBase.USER_DEFAULT_PWD));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin04() throws Exception {
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", AbstractInitDataBase.USER_EXISTING_EMAIL).param("password", ""));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin05() throws Exception {
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", "").param("password", AbstractInitDataBase.USER_DEFAULT_PWD));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin06() throws Exception {
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", AbstractInitDataBase.USER_EXISTING_EMAIL).param("password", "wrongpwd"));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin07() throws Exception {
		var user = this.userService.disable(AbstractInitDataBase.USER_EXISTING_ID);
		Assertions.assertNotNull(user, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, user.getEmail(),
				"User should have the good email");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, user.getId(), "User should have first id");
		Assertions.assertTrue(user.isDisabled(), "User should be disabled");
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", AbstractInitDataBase.USER_EXISTING_EMAIL)
				.param("password", AbstractInitDataBase.USER_DEFAULT_PWD));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testLogin08() throws Exception {
		var user = this.userService.delete(AbstractInitDataBase.USER_EXISTING_ID);
		Assertions.assertNotNull(user, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, user.getEmail(),
				"User should have the good email");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, user.getId(), "User should have first id");
		Assertions.assertTrue(user.isDeleted(), "User should be delted");
		// The call to controller
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", AbstractInitDataBase.USER_EXISTING_EMAIL)
				.param("password", AbstractInitDataBase.USER_DEFAULT_PWD));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}
}
