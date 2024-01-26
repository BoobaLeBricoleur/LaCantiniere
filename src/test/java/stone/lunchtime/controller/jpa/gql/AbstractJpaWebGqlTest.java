// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;


import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Mother class of all tests that uses Web and GQL
 */
abstract class AbstractJpaWebGqlTest extends AbstractJpaWebTest {

	/**
	 * Build a GqlTester in order to handle GQL request.
	 *
	 * @return a GqlTester
	 */
	protected HttpGraphQlTester getGqlTester() {
		var client = MockMvcWebTestClient.bindTo(super.mockMvc).baseUrl("/graphql").build();
		return HttpGraphQlTester.create(client);
	}

	/**
	 * Build a GqlTester in order to handle GQL request with a JWT in the header.
	 *
	 * @param aJwtToken a JWT
	 * @return a GqlTester
	 */
	protected HttpGraphQlTester getGqlTester(String aJwtToken) {
		var client = MockMvcWebTestClient.bindTo(super.mockMvc).baseUrl("/graphql")
				.defaultHeader(SecurityConstants.TOKEN_HEADER, aJwtToken).build();
		return HttpGraphQlTester.create(client);
	}

}
