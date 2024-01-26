// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stone.lunchtime.init.AbstractInitDataBase;

/**
 * Test for forgot pwd controller, using Mock.
 */
class ForgotPasswordGqlControllerTest extends AbstractJpaWebGqlTest {

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword01() throws Exception {
		var gqlRequest = "mutation {forgotPassword(email:\"" + AbstractInitDataBase.USER_EXISTING_EMAIL + "\")}";
		var gqlResult = super.getGqlTester().document(gqlRequest).execute();
		gqlResult.path("forgotPassword").entity(Boolean.class).isEqualTo(Boolean.TRUE);
		Assertions.assertTrue(true); // For Sonar
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword02() throws Exception {
		var gqlRequest = "mutation {forgotPassword(email:\"txxxxt@gmail.com\")}";
		var gqlResult = super.getGqlTester().document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Utilisateur introuvable".equals(e.getMessage()));
		Assertions.assertTrue(true); // For Sonar
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testForgotPassword03() throws Exception {
		var gqlRequest = "mutation {forgotPassword}";
		var gqlResult = super.getGqlTester().document(gqlRequest).execute();
		gqlResult.errors().expect(e -> e.getMessage().startsWith("Validation error"));
		Assertions.assertTrue(true); // For Sonar
	}
}
