// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.graphql.test.tester.GraphQlTester.EntityList;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.jpa.handler.IngredientDtoHandler;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.entity.EntityStatus;

/**
 * Test for ingredient controller, using Mock.
 */
class IngredientGqlControllerTest extends AbstractJpaWebGqlTest {
	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		var elmId = Integer.valueOf(1);
		var gqlRequest = "query {ingredientById(id:" + elmId + ") {id,description,label,status,imageId}}";

		var result = super.getGqlTester().document(gqlRequest).execute();
		var dto = result.path("ingredientById").entity(IngredientDtoOut.class).get();
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
		var elmId = 10000;
		var gqlRequest = "query {ingredientById(id:" + elmId + ") {id,description,label,status,imageId}}";

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
		var gqlRequest = "query {ingredientById {id,description,label,status,imageId}}";
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
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		var gqlRequest = "query {findAllIngredients {id,description,label,status,imageId}}";

		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();

		EntityList<IngredientDtoOut> dtos = gqlResult.path("findAllIngredients").entityList(IngredientDtoOut.class);
		Assertions.assertNotNull(dtos, "List must not be null");
		dtos.hasSize(28);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindAll02() throws Exception {
		// Connect as Normal USer
		var result = super.logMeInAsNormalRandomUser();

		var gqlRequest = "query {findAllIngredients {id,description,label,status,imageId}}";
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindAll03() throws Exception {

		var gqlRequest = "query {findAllIngredients {id,description,label,status,imageId}}";
		var gqlResult = super.getGqlTester().document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Unauthorized".equals(e.getMessage()));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		var elmId = Integer.valueOf(1);
		var entity = this.ingredientService.find(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertNotEquals(EntityStatus.DELETED, entity.getStatus(), "Status is not deleted");

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();
		var gqlRequest = "mutation {deleteIngredient(id:" + elmId + ")}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();

		gqlResult.path("deleteIngredient").entity(Boolean.class).isEqualTo(Boolean.TRUE);

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		entity = this.ingredientService.find(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertNotNull(entity.getId(), "Entity still have an id");
		Assertions.assertEquals(EntityStatus.DELETED, entity.getStatus(), "Status is deleted");
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
		// Connect a user
		var result = super.logMeInAsNormalRandomUser();
		var gqlRequest = "mutation {deleteIngredient(id:" + elmId + ")}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

	@Test
	void testDelete03() throws Exception {
		// Already deleted
		var elmId = Integer.valueOf(1);
		var entity = this.ingredientService.find(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertNotEquals(EntityStatus.DELETED, entity.getStatus(), "Status is not deleted");
		entity = super.ingredientService.delete(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertEquals(EntityStatus.DELETED, entity.getStatus(), "Status is not deleted");

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		var gqlRequest = "mutation {deleteIngredient(id:" + elmId + ")}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();
		gqlResult.errors().expect(e -> e.getMessage().contains(EntityStatus.DELETED.toString()));
	}

	@Test
	void testDelete04() throws Exception {
		var elmId = Integer.valueOf(10000);

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();
		var gqlRequest = "mutation {deleteIngredient(id:" + elmId + ")}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).execute();
		gqlResult.errors().expect(e -> "Entite introuvable.".equals(e.getMessage()));
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

		var dto = new IngredientDtoIn();
		dto.setDescription("test a description");
		dto.setLabel("A nice label");

		var gqlRequest = "mutation addIngredient($dto: IngredientDtoIn!) {addIngredient(ingredient: $dto) {id,description,label,status,imageId}}";

		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		var dtoOut = gqlResult.path("addIngredient").entity(IngredientDtoOut.class).get();

		var entity = this.ingredientService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertEquals(dtoOut.getId(), entity.getId(), "Id must be the same");
		Assertions.assertEquals(dto.getDescription(), entity.getDescription(), "Attribute must be the same");
		Assertions.assertEquals(dto.getLabel(), entity.getLabel(), "Attribute must be the same");
		Assertions.assertEquals(EntityStatus.ENABLED, entity.getStatus(), "Status must be created");
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

		var dto = new IngredientDtoIn();
		dto.setDescription("test a description");
		dto.setLabel("A nice label");

		var gqlRequest = "mutation addIngredient($dto: IngredientDtoIn!) {addIngredient(ingredient: $dto) {id,description,label,status,imageId}}";

		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

	@Test
	void testUpdate01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();
		var elmId = Integer.valueOf(1);
		var dto = IngredientDtoHandler.dtoInfromEntity(super.ingredientService.findEntity(elmId));
		// Change label
		dto.setLabel("test new label");

		var gqlRequest = "mutation updateIngredient($dto: IngredientDtoIn!) {updateIngredient(id: " + elmId
				+ ", ingredient: $dto) {id,description,label,status,imageId}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		var dtoOut = gqlResult.path("updateIngredient").entity(ConstraintDtoOut.class).get();

		var entity = this.ingredientService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertEquals(dtoOut.getId(), entity.getId(), "Id must be the same");
		Assertions.assertEquals(dto.getDescription(), entity.getDescription(), "Attribute must be the same");
		Assertions.assertEquals(dto.getLabel(), entity.getLabel(), "Attribute must be the same");
		Assertions.assertEquals(EntityStatus.ENABLED, entity.getStatus(), "Status must be created");
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdate02() throws Exception {
		// Connect as lambda
		var result = super.logMeInAsNormalRandomUser();
		var elmId = Integer.valueOf(1);
		var dto = IngredientDtoHandler.dtoInfromEntity(super.ingredientService.findEntity(elmId));
		// Change label
		dto.setLabel("test new label");

		var gqlRequest = "mutation updateIngredient($dto: IngredientDtoIn!) {updateIngredient(id: " + elmId
				+ ", ingredient: $dto) {id,description,label,status,imageId}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindImg00() throws Exception {
		var elmId = super.getValidIngredient().getId();

		var gqlRequest = "query {findIngredientImage(id:" + elmId + ") {id,imagePath,image64,isDefault}}";

		var gqlResult = super.getGqlTester().document(gqlRequest).execute();

		var dtoOut = gqlResult.path("findIngredientImage").entity(ImageDtoOut.class).get();

		var entity = this.ingredientDao.findById(elmId).get().getImage();
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertEquals(dtoOut.getId(), entity.getId(), "Id must be the same");
		Assertions.assertEquals(dtoOut.getImage64(), entity.getImage64(), "Attribute must be the same");
		Assertions.assertEquals(dtoOut.getImagePath(), entity.getImagePath(), "Attribute must be the same");
		Assertions.assertEquals(dtoOut.isDefault(), entity.getIsDefault(), "Attribute must be the same");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateImg00() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var elm = super.getValidIngredient();
		var oldImgId = elm.getImage().getId();

		var dto = new ImageDtoIn();
		dto.setImagePath("img/test.png");
		dto.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");

		var gqlRequest = "mutation updateIngredientImage($dto: ImageDtoIn!) {updateIngredientImage(id: " + elm.getId()
				+ ", image: $dto) {id,description,label,status,imageId}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		var dtoOut = gqlResult.path("updateIngredientImage").entity(IngredientDtoOut.class).get();

		var entity = this.ingredientService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertEquals(dtoOut.getId(), entity.getId(), "Id must be the same");

		var ie = super.imageService.findEntity(dtoOut.getImageId());

		// We updated a non default image, so id is the same
		Assertions.assertEquals(oldImgId, ie.getId(), "Image id must be the same");
		Assertions.assertEquals(dto.getImagePath(), ie.getImagePath(), "Image should have the same path");
		Assertions.assertEquals(dto.getImage64(), ie.getImage64(), "Image should have the same base 64");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdateImg01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var elm = super.getValidIngredient();

		var dto = new ImageDtoIn();
		dto.setImagePath("img/test.png");
		dto.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");

		var gqlRequest = "mutation updateIngredientImage($dto: ImageDtoIn!) {updateIngredientImage(id: " + elm.getId()
				+ ", image: $dto) {id,description,label,status,imageId}}";
		// The call
		var gqlResult = super.getGqlTester(super.getJWT(result)).document(gqlRequest).variable("dto", dto).execute();

		gqlResult.errors().expect(e -> "Forbidden".equals(e.getMessage()));
	}

}
