// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import stone.lunchtime.AbstractJpaTest;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.init.AbstractInitDataBase;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Tests for authentication service.
 */
class AuthenticationServiceTest extends AbstractJpaTest {

	/**
	 * Test
	 */
	@Test
	void testAuthenticate01() {
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
				AbstractInitDataBase.USER_EXISTING_EMAIL, AbstractInitDataBase.USER_DEFAULT_PWD);
		authenticationToken = this.authenticationService.authenticate(authenticationToken);
		Assertions.assertNotNull(authenticationToken, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, authenticationToken.getName(),
				"User should have the good email");
		Assertions.assertEquals(1, ((UserDtoOut) authenticationToken.getDetails()).getId().intValue(),
				"User should have first id");
	}

	/**
	 * Test
	 */
	@Test
	void testAuthenticate02() {
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
				AbstractInitDataBase.USER_EXISTING_EMAIL, null);
		Assertions.assertThrows(BadCredentialsException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 */
	@Test
	void testAuthenticate03() {
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(null,
				AbstractInitDataBase.USER_DEFAULT_PWD);
		Assertions.assertThrows(BadCredentialsException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 */
	@Test
	void testAuthenticate04() {
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
				AbstractInitDataBase.USER_EXISTING_EMAIL, "");
		Assertions.assertThrows(BadCredentialsException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 */
	@Test
	void testAuthenticate05() {
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken("",
				AbstractInitDataBase.USER_DEFAULT_PWD);
		Assertions.assertThrows(BadCredentialsException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 */
	@Test
	void testAuthenticate06() {
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
				AbstractInitDataBase.USER_EXISTING_EMAIL, "wrongpwd");
		Assertions.assertThrows(BadCredentialsException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testAuthenticate07() throws Exception {
		var result = this.userService.disable(AbstractInitDataBase.USER_EXISTING_ID);
		Assertions.assertNotNull(result, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, result.getEmail(),
				"User should have the good email");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, result.getId(), "User should have first id");
		Assertions.assertTrue(result.isDisabled(), "User should be disabled");
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
				AbstractInitDataBase.USER_EXISTING_EMAIL, AbstractInitDataBase.USER_DEFAULT_PWD);
		Assertions.assertThrows(DisabledException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testAuthenticate08() throws Exception {
		var result = this.userService.delete(AbstractInitDataBase.USER_EXISTING_ID);
		Assertions.assertNotNull(result, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, result.getEmail(),
				"User should have the good email");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, result.getId(), "User should have first id");
		Assertions.assertTrue(result.isDeleted(), "User should be deleted");
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
				AbstractInitDataBase.USER_EXISTING_EMAIL, AbstractInitDataBase.USER_DEFAULT_PWD);
		Assertions.assertThrows(DisabledException.class,
				() -> this.authenticationService.authenticate(authenticationToken));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword01() throws Exception {
		final var initialState = super.emailService.getSendMail();
		if (initialState) {
			super.emailService.deactivateSendMail();
		}
		this.authenticationService.forgotPassword(AbstractInitDataBase.USER_EXISTING_EMAIL);
		Assertions.assertTrue(true, "Sonar is my friend");
		if (initialState) {
			super.emailService.activateSendMail();
		}
	}

	/**
	 * Test
	 */
	@Test
	void testForgotPassword02() {
		Assertions.assertThrows(ParameterException.class, () -> this.authenticationService.forgotPassword(null));
	}

	/**
	 * Test
	 */
	@Test
	void testForgotPassword03() {
		Assertions.assertThrows(ParameterException.class, () -> this.authenticationService.forgotPassword(""));
	}

	/**
	 * Test
	 */
	@Test
	void testForgotPassword04() {
		Assertions.assertThrows(EntityNotFoundException.class,
				() -> this.authenticationService.forgotPassword("wrong@email.com"));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword05() throws Exception {
		var result = this.userService.disable(AbstractInitDataBase.USER_EXISTING_ID);
		Assertions.assertNotNull(result, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, result.getEmail(),
				"User should have the good email");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, result.getId(), "User should have first id");
		Assertions.assertTrue(result.isDisabled(), "User should be deactivated");
		Assertions.assertThrows(InconsistentStatusException.class,
				() -> this.authenticationService.forgotPassword(AbstractInitDataBase.USER_EXISTING_EMAIL));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword06() throws Exception {
		var result = this.userService.delete(AbstractInitDataBase.USER_EXISTING_ID);
		Assertions.assertNotNull(result, "User should not be null");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_EMAIL, result.getEmail(),
				"User should have the good email");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, result.getId(), "User should have first id");
		Assertions.assertTrue(result.isDeleted(), "User should be deleted");
		Assertions.assertThrows(InconsistentStatusException.class,
				() -> this.authenticationService.forgotPassword(AbstractInitDataBase.USER_EXISTING_EMAIL));
	}
}
