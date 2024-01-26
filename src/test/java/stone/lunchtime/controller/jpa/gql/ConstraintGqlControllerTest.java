package stone.lunchtime.controller.jpa.gql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.graphql.test.tester.GraphQlTester.EntityList;

import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.jpa.handler.ConstraintDtoHandler;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;

class ConstraintGqlControllerTest extends AbstractJpaWebGqlTest {

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		var elmId = Integer.valueOf(1);
		var gqlRequest = "query {constraintById(id:" + elmId + ") {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";

		var result = super.getGqlTester().document(gqlRequest).execute();
		var dto = result.path("constraintById").entity(ConstraintDtoOut.class).get();
		Assertions.assertNotNull(dto, "Dto is not null");
		Assertions.assertEquals(elmId, dto.getId(), "Id must be the same");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind02() throws Exception {
		var elmId = 1000;
		var gqlRequest = "query {constraintById(id:" + elmId + ") {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";
		var result = super.getGqlTester().document(gqlRequest).execute();
		result.errors().expect(e -> "Entite introuvable.".equals(e.getMessage()));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind03() throws Exception {
		var gqlRequest = "query {constraintById {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";

		var result = super.getGqlTester().document(gqlRequest).execute();
		result.errors().expect(e -> e.getMessage().startsWith("Validation error"));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindAll01() throws Exception {
		var gqlRequest = "query {findAllConstraints {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";

		var result = super.getGqlTester().document(gqlRequest).execute();
		EntityList<ConstraintDtoOut> dtos = result.path("findAllConstraints").entityList(ConstraintDtoOut.class);
		Assertions.assertNotNull(dtos, "List must not be null");
		dtos.hasSizeGreaterThan(0);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		var elmId = Integer.valueOf(1);
		var gqlRequest = "mutation {deleteConstraint(id:" + elmId + ")}";
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();

		gqlResult.path("deleteConstraint").entity(Boolean.class).isEqualTo(Boolean.TRUE);

		// No status for constraint, element is no more in database
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.constraintService.find(elmId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testDelete02() throws Exception {
		var elmId = Integer.valueOf(1);
		var gqlRequest = "mutation {deleteConstraint(id:" + elmId + ")}";
		// Connect a user
		var result = super.logMeInAsNormalRandomUser();
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

	@Test
	void testDelete03() throws Exception {
		var elmId = Integer.valueOf(1000);
		var gqlRequest = "mutation {deleteConstraint(id:" + elmId + ")}";
		// Connect as LL
		var result = super.logMeInAsLunchLady();

		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();

		gqlResult.errors().expect(e -> "Entite introuvable.".equals(e.getMessage()));
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testDelete04() throws Exception {
		var elmId = Integer.valueOf(1);
		var gqlRequest = "mutation {deleteConstraint(id:" + elmId + ")}";
		// The call
		var gqlResult = super.getGqlTester().document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Unauthorized".equals(e.getMessage()));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testAdd01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		var dto = new ConstraintDtoIn();
		dto.setMaximumOrderPerDay(Integer.valueOf(20));
		dto.setRateVAT(Float.valueOf(20F));
		dto.setOrderTimeLimit("11:00:00");

		var gqlRequest = "mutation addConstraint($dto: ConstraintDtoIn!) {addConstraint(constraint: $dto) {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		var dtoOut = gqlResult.path("addConstraint").entity(ConstraintDtoOut.class).get();

		var entity = this.constraintService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertEquals(dtoOut.getId(), entity.getId(), "Id must be the same");
		Assertions.assertEquals(dto.getMaximumOrderPerDay(), entity.getMaximumOrderPerDay(),
				"Attribute must be the same");
		Assertions.assertEquals(dto.getRateVAT(), entity.getRateVAT(), "Attribute must be the same");
		Assertions.assertEquals(dto.getOrderTimeLimitAsTime(), entity.getOrderTimeLimit(),
				"Attribute must be the same");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testAdd02() throws Exception {
		// Connect as lambda
		var result = super.logMeInAsNormalRandomUser();

		var dto = new ConstraintDtoIn();
		dto.setMaximumOrderPerDay(Integer.valueOf(20));
		dto.setRateVAT(Float.valueOf(20F));
		dto.setOrderTimeLimit("11:00:00");

		var gqlRequest = "mutation addConstraint($dto: ConstraintDtoIn!) {addConstraint(constraint: $dto) {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

	@Test
	void testUpdate01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();
		var elmId = Integer.valueOf(1);
		var dto = ConstraintDtoHandler.dtoInfromEntity(super.constraintService.findEntity(elmId));
		// Change mop
		dto.setMaximumOrderPerDay(Integer.valueOf(199));

		var gqlRequest = "mutation updateConstraint($dto: ConstraintDtoIn!) {updateConstraint(id: " + elmId
				+ ", constraint: $dto) {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		var dtoOut = gqlResult.path("updateConstraint").entity(ConstraintDtoOut.class).get();

		var entity = this.constraintService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertEquals(dtoOut.getId(), entity.getId(), "Id must be the same");
		Assertions.assertEquals(dto.getMaximumOrderPerDay(), entity.getMaximumOrderPerDay(),
				"Attribute must be the same");
		Assertions.assertEquals(dto.getRateVAT(), entity.getRateVAT(), "Attribute must be the same");
		Assertions.assertEquals(dto.getOrderTimeLimitAsTime(), entity.getOrderTimeLimit(),
				"Attribute must be the same");
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdate02() throws Exception {
		// Connect as Standard User
		var result = super.logMeInAsNormalRandomUser();
		var elmId = Integer.valueOf(1);
		var dto = ConstraintDtoHandler.dtoInfromEntity(super.constraintService.findEntity(elmId));
		// Change mop
		dto.setMaximumOrderPerDay(Integer.valueOf(199));

		var gqlRequest = "mutation updateConstraint($dto: ConstraintDtoIn!) {updateConstraint(id: " + elmId
				+ ", constraint: $dto) {id,orderTimeLimit,maximumOrderPerDay,rateVAT}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));

	}
}
