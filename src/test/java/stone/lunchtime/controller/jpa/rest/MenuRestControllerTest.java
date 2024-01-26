// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.JavaType;

import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.jpa.handler.MenuDtoHandler;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.MenuDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.service.impl.jpa.OrderService;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Test for ingredient controller, using Mock.
 */
class MenuRestControllerTest extends AbstractJpaWebTest {
	private static final String URL_ROOT = "/menu";
	private static final String URL_ADD = MenuRestControllerTest.URL_ROOT + "/add";
	private static final String URL_DELETE = MenuRestControllerTest.URL_ROOT + "/delete/";
	private static final String URL_UPDATE = MenuRestControllerTest.URL_ROOT + "/update/";
	private static final String URL_FIND = MenuRestControllerTest.URL_ROOT + "/find/";
	private static final String URL_FINDALL = MenuRestControllerTest.URL_ROOT + "/findall";
	private static final String URL_FINDALLFORTODAY = MenuRestControllerTest.URL_ROOT + "/findallavailablefortoday";
	private static final String URL_FINDALLFORWEEK = MenuRestControllerTest.URL_ROOT + "/findallavailableforweek/";
	private static final String URL_FINDALLFORWEEKANDDAY = MenuRestControllerTest.URL_ROOT
			+ "/findallavailableforweekandday/";
	private static final String URL_FIND_IMG = MenuRestControllerTest.URL_ROOT + "/findimg/";
	private static final String URL_UPDATE_IMG = MenuRestControllerTest.URL_ROOT + "/updateimg/";

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		var elmId = Integer.valueOf(1);

		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FIND + elmId));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(elmId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind02() throws Exception {
		var elmId = 10000;
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FIND + elmId));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind03() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FIND));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
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

		result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FINDALL)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = MenuDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<MenuDtoOut> elements = this.mapper.readValue(content, type);

		Assertions.assertNotNull(elements, "List cannot be null");
		Assertions.assertFalse(elements.isEmpty(), "List cannot be empty");
		Assertions.assertEquals(60, elements.size(), "List size is 60");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindAll02() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FINDALL));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindAll03() throws Exception {
		// Connect as lambda
		var result = super.logMeInAsNormalRandomUser();

		result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FINDALL)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		var elmId = Integer.valueOf(1);
		var entity = this.menuService.find(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertNotEquals(EntityStatus.DELETED, entity.getStatus(), "Status is not deleted");

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(MenuRestControllerTest.URL_DELETE + elmId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		entity = this.menuService.find(elmId);
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
		var elmId = 1;
		// Connect a user
		var result = super.logMeInAsNormalRandomUser();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(MenuRestControllerTest.URL_DELETE + elmId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	void testDelete03() throws Exception {
		var elmId = Integer.valueOf(1);
		var entity = this.menuService.find(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertNotEquals(EntityStatus.DELETED, entity.getStatus(), "Status is not deleted");
		entity = super.menuService.delete(elmId);
		Assertions.assertNotNull(entity, "Entity is still in data base");
		Assertions.assertEquals(EntityStatus.DELETED, entity.getStatus(), "Status is not deleted");

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(MenuRestControllerTest.URL_DELETE + elmId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	@Test
	void testDelete04() throws Exception {
		var elmId = 10000;

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(MenuRestControllerTest.URL_DELETE + elmId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
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

		var dto = new MenuDtoIn();
		dto.setDescription("test a description");
		dto.setLabel("A nice label");
		dto.setPriceDF(Float.valueOf(50F));
		var aad = new AvailableForWeeksAndDays();
		var weekNow = OrderService.getCurrentWeekId();
		var dayNow = OrderService.getCurrentDayId();
		aad.add(1, null);
		aad.add(10, null);
		aad.add(weekNow, dayNow);
		dto.setAvailableForWeeksAndDays(aad);

		List<Integer> mealIds = new ArrayList<>();
		mealIds.add(super.getValidMeal(true).getId());
		mealIds.add(super.getValidMeal(true).getId());
		dto.setMealIds(mealIds);

		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.put(MenuRestControllerTest.URL_ADD).contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(dtoAsJsonString).header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), MenuDtoOut.class);

		var entity = this.menuService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
		Assertions.assertNotNull(entity.getMeals(), "Result must have meals");
		Assertions.assertEquals(mealIds.size(), entity.getMeals().size(), "Result must have 2 meals");
		var aweeks = dto.getAvailableForWeeksAndDays();
		Assertions.assertNotNull(aweeks, "Week & day should be not null");
		Assertions.assertTrue(aweeks.oneWeekAndOneDay(weekNow, dayNow), "Week & day should be inside");
		Assertions.assertTrue(aweeks.oneWeekAndOneDay(1, dayNow), "Week 1 & day should be inside");
		Assertions.assertTrue(aweeks.oneWeekAndOneDay(10, dayNow), "Week 1 & day should be inside");
		Assertions.assertTrue(aweeks.oneWeek(1), "Week should be inside");
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

		var dto = new MenuDtoIn();
		dto.setDescription("test a description");
		dto.setLabel("A nice label");
		dto.setPriceDF(Float.valueOf(50F));
		var aad = new AvailableForWeeksAndDays();
		var weekNow = OrderService.getCurrentWeekId();
		var dayNow = OrderService.getCurrentDayId();
		aad.add(1, null);
		aad.add(10, null);
		aad.add(weekNow, dayNow);
		dto.setAvailableForWeeksAndDays(aad);
		List<Integer> mealIds = new ArrayList<>();
		mealIds.add(super.getValidMeal(true).getId());
		mealIds.add(super.getValidMeal(true).getId());
		dto.setMealIds(mealIds);
		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.put(MenuRestControllerTest.URL_ADD).contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(dtoAsJsonString).header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	void testAdd03() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		var dto = new MenuDtoIn();
		dto.setDescription("test a description");
		dto.setLabel("A nice label");
		dto.setPriceDF(Float.valueOf(50F));
		var aad = new AvailableForWeeksAndDays();
		aad.add(1, null);
		aad.add(100, null); // wrong
		dto.setAvailableForWeeksAndDays(aad);
		List<Integer> mealIds = new ArrayList<>();
		mealIds.add(super.getValidMeal(true).getId());
		mealIds.add(super.getValidMeal(true).getId());
		dto.setMealIds(mealIds);

		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.put(MenuRestControllerTest.URL_ADD).contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(dtoAsJsonString).header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	void testUpdate01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();
		var elmId = Integer.valueOf(1);
		var dto = MenuDtoHandler.dtoInfromEntity(super.menuService.findEntity(elmId));
		// Change label
		dto.setLabel("test new label");

		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(MenuRestControllerTest.URL_UPDATE + elmId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoAsJsonString)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), MenuDtoOut.class);

		var entity = this.menuService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertEquals(elmId, entity.getId(), "Result must have same id");
		Assertions.assertEquals("test new label", entity.getLabel(), "Result must have same changed value");
		Assertions.assertEquals(dto.getDescription(), entity.getDescription(), "Result must have same unchanged value");
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdate02() throws Exception {
		// Connect as lambda
		var result = super.logMeInAsNormalRandomUser();
		var elmId = Integer.valueOf(1);
		var dto = MenuDtoHandler.dtoInfromEntity(super.menuService.findEntity(elmId));
		// Change label
		dto.setLabel("test new label");

		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(MenuRestControllerTest.URL_UPDATE + elmId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoAsJsonString)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	void testFindAllForToday01() throws Exception {

		var dtoIn = new MenuDtoIn();
		dtoIn.setDescription("test a description");
		dtoIn.setLabel("A nice label");
		dtoIn.setPriceDF(Float.valueOf(50F));
		var aad = new AvailableForWeeksAndDays();
		var weekNow = OrderService.getCurrentWeekId();
		var dayNow = OrderService.getCurrentDayId();
		aad.add(weekNow, dayNow);
		dtoIn.setAvailableForWeeksAndDays(aad);
		List<Integer> mealIds = new ArrayList<>();
		mealIds.add(super.getValidMeal(true).getId());
		mealIds.add(super.getValidMeal(true).getId());
		dtoIn.setMealIds(mealIds);

		var entity = super.menuService.add(dtoIn);
		Assertions.assertEquals(EntityStatus.ENABLED, entity.getStatus(), "Status must enabled");

		// The call
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FINDALLFORTODAY));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = MenuDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<MenuDtoOut> elements = this.mapper.readValue(content, type);

		Assertions.assertNotNull(elements, "List cannot be null");
		Assertions.assertFalse(elements.isEmpty(), "List cannot be empty");
		Assertions.assertTrue(elements.size() >= 1, "List size should be >= 1");
		var found = false;
		for (MenuDtoOut dto : elements) {
			Assertions.assertNotNull(dto, "Element cannot be null");
			Assertions.assertEquals(EntityStatus.ENABLED, dto.getStatus(), "Element status is as searched");
			var aweeks = dto.getAvailableForWeeksAndDays();
			if (aweeks != null) {
				Assertions.assertTrue(aweeks.oneWeek(weekNow), "Week should be inside");
			}
			if (Objects.equals(dto.getId(), entity.getId())) {
				Assertions.assertTrue(aweeks.oneWeekAndOneDay(weekNow, dayNow), "Week & day should be inside");
				found = true;
			}
		}
		Assertions.assertTrue(found, "Entity should have been found");
	}

	@Test
	void testFindAllForWeekAndDay01() throws Exception {

		var dtoIn = new MenuDtoIn();
		dtoIn.setDescription("test a description");
		dtoIn.setLabel("A nice label");
		dtoIn.setPriceDF(Float.valueOf(50F));
		var aad = new AvailableForWeeksAndDays();
		var weekNow = OrderService.getCurrentWeekId();
		var dayNow = OrderService.getCurrentDayId();
		aad.add(weekNow, dayNow);
		dtoIn.setAvailableForWeeksAndDays(aad);
		List<Integer> mealIds = new ArrayList<>();
		mealIds.add(super.getValidMeal(true).getId());
		mealIds.add(super.getValidMeal(true).getId());
		dtoIn.setMealIds(mealIds);

		var entity = super.menuService.add(dtoIn);
		Assertions.assertEquals(EntityStatus.ENABLED, entity.getStatus(), "Status must enabled");

		// The call
		var result = super.mockMvc.perform(
				MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FINDALLFORWEEKANDDAY + weekNow + "/" + dayNow));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = MenuDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<MenuDtoOut> elements = this.mapper.readValue(content, type);

		Assertions.assertNotNull(elements, "List cannot be null");
		Assertions.assertFalse(elements.isEmpty(), "List cannot be empty");
		Assertions.assertTrue(elements.size() >= 1, "List size should be >= 1");
		var found = false;
		for (MenuDtoOut dto : elements) {
			Assertions.assertNotNull(dto, "Element cannot be null");
			Assertions.assertEquals(EntityStatus.ENABLED, dto.getStatus(), "Element status is as searched");
			var aweeks = dto.getAvailableForWeeksAndDays();
			if (aweeks != null) {
				Assertions.assertTrue(aweeks.oneWeek(weekNow), "Week should be inside");
			}
			if (Objects.equals(dto.getId(), entity.getId())) {
				Assertions.assertTrue(aweeks.oneWeekAndOneDay(weekNow, dayNow), "Week & day should be inside");
				found = true;
			}
		}
		Assertions.assertTrue(found, "Entity should have been found");
	}

	@Test
	void testFindAllForWeek01() throws Exception {

		var dtoIn = new MenuDtoIn();
		dtoIn.setDescription("test a description");
		dtoIn.setLabel("A nice label");
		dtoIn.setPriceDF(Float.valueOf(50F));
		var aad = new AvailableForWeeksAndDays();
		var weekNow = OrderService.getCurrentWeekId();
		aad.add(weekNow, null);
		dtoIn.setAvailableForWeeksAndDays(aad);
		List<Integer> mealIds = new ArrayList<>();
		mealIds.add(super.getValidMeal(true).getId());
		mealIds.add(super.getValidMeal(true).getId());
		dtoIn.setMealIds(mealIds);

		var entity = super.menuService.add(dtoIn);
		Assertions.assertEquals(EntityStatus.ENABLED, entity.getStatus(), "Status must enabled");

		// The call
		var result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FINDALLFORWEEK + weekNow));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = MenuDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<MenuDtoOut> elements = this.mapper.readValue(content, type);

		Assertions.assertNotNull(elements, "List cannot be null");
		Assertions.assertFalse(elements.isEmpty(), "List cannot be empty");
		Assertions.assertTrue(elements.size() >= 1, "List size should be >= 1");
		var found = false;
		for (MenuDtoOut dto : elements) {
			Assertions.assertNotNull(dto, "Element cannot be null");
			Assertions.assertEquals(EntityStatus.ENABLED, dto.getStatus(), "Element status is as searched");
			var aweeks = dto.getAvailableForWeeksAndDays();
			if (aweeks != null) {
				Assertions.assertTrue(aweeks.oneWeek(weekNow), "Week should be inside");
			}
			if (Objects.equals(dto.getId(), entity.getId())) {
				found = true;
			}
		}
		Assertions.assertTrue(found, "Entity should have been found");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindImg00() throws Exception {
		var elmId = super.getValidIngredient().getId();

		// The call
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(MenuRestControllerTest.URL_FIND_IMG + elmId)
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), ImageDtoOut.class);
		Assertions.assertNotNull(dtoOut.getId(), "Image must have an id");
		Assertions.assertNotNull(dtoOut.getImagePath(), "Image must have a path");
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

		var elm = super.getValidMenu(false);
		var oldImgId = elm.getImage().getId();

		var dtoIn = new ImageDtoIn();
		dtoIn.setImagePath("img/test.png");
		dtoIn.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(MenuRestControllerTest.URL_UPDATE_IMG + elm.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), MenuDtoOut.class);
		Assertions.assertNotNull(dtoOut.getImageId(), "Image must have an id");

		var ie = super.imageService.findEntity(dtoOut.getImageId());

		Assertions.assertNotNull(ie.getImagePath(), "Image must have a path");
		Assertions.assertFalse(ie.getIsDefault(), "Image should NOT be a default one");
		Assertions.assertNotNull(ie.getImage64(), "Image should have a base64");
		// We updated a default image, so id is != same
		Assertions.assertNotEquals(oldImgId, ie.getId(), "Image id must be the same");
		Assertions.assertEquals(dtoIn.getImagePath(), ie.getImagePath(), "Image should have the same path");
		Assertions.assertEquals(dtoIn.getImage64(), ie.getImage64(), "Image should have the same base 64");
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

		var elm = super.getValidMenu(false);

		var dtoIn = new ImageDtoIn();
		dtoIn.setImagePath("img/test.png");
		dtoIn.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(MenuRestControllerTest.URL_UPDATE_IMG + elm.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

}
