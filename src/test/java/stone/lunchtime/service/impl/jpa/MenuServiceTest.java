// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stone.lunchtime.AbstractJpaTest;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Menu service test class.
 */
class MenuServiceTest extends AbstractJpaTest {

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek01() {
		var result = this.menuService.findAllAvailableForWeek(Integer.valueOf(1));
		Assertions.assertNotNull(result, "Result must exists");
		Assertions.assertFalse(result.isEmpty(), "Result must not be empty");
	}

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek02() {
		final var w = Integer.valueOf(60);
		Assertions.assertThrows(ParameterException.class, () -> this.menuService.findAllAvailableForWeek(w));
	}

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek03() {
		final var w = Integer.valueOf(0);
		Assertions.assertThrows(ParameterException.class, () -> this.menuService.findAllAvailableForWeek(w));
	}

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek04() {
		Assertions.assertThrows(ParameterException.class, () -> this.menuService.findAllAvailableForWeek(null));
	}

	@Test
	void testFindAllAvailableForWeek05() {
		final var week = 0; // bad week
		final var day = Integer.valueOf(7);
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndDayAndCategory(week, day, null));

	}

	@Test
	void testFindAllAvailableForWeek06() {
		final var week = 9;
		final var day = 8; // bad day
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndDayAndCategory(week, day, null));

	}

	/**
	 * Test
	 */
	@Test
	void testAdd01() {
		var allMeals = super.mealService.findAll();
		var allMealsId = super.transformInIdsList(allMeals);
		var dto = new MenuDtoIn();
		dto.setLabel("Test Menu");
		dto.setPriceDF(2F);
		dto.setMealIds(super.generateList(3, allMealsId));
		var result = this.menuService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals(result.getMeals().size(), dto.getMealIds().size(),
				"Result must have the correct number of meal");

	}

	/**
	 * Test
	 */
	@Test
	void testAdd02() {
		var dto = new MenuDtoIn();
		dto.setLabel("Test Menu");
		dto.setPriceDF(2F);
		dto.setMealIds(null);
		var result = this.menuService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertNull(result.getMeals(), "Result must have the correct number of meal");

	}

	/**
	 * Test
	 */
	@Test
	void testAdd04() {
		var allMeals = super.mealService.findAll();
		var allMealsId = super.transformInIdsList(allMeals);
		var dto = new MenuDtoIn();
		dto.setLabel("Test Menu");
		dto.setPriceDF(-2F);
		dto.setMealIds(super.generateList(3, allMealsId));
		var aad = new AvailableForWeeksAndDays();
		aad.add(5, null);
		dto.setAvailableForWeeksAndDays(aad);
		Assertions.assertThrows(ParameterException.class, () -> this.menuService.add(dto));
	}

	/**
	 * Test
	 */
	@Test
	void testAdd05() {
		Assertions.assertThrows(ParameterException.class, () -> this.menuService.add(null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate01() throws Exception {
		var allMeals = super.mealService.findAll();
		var allMealsId = super.transformInIdsList(allMeals);
		final var mealId = Integer.valueOf(1);
		var result = this.menuService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MenuDtoIn();
		dto.setLabel("New Test Menu");
		dto.setPriceDF(2F);
		dto.setMealIds(super.generateList(3, allMealsId));
		result = this.menuService.update(mealId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), "Result must have the same id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals("New Test Menu", result.getLabel(), "Result must have the correct label");
		Assertions.assertEquals(result.getMeals().size(), dto.getMealIds().size(),
				"Result must have the correct number of meal");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate02() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.menuService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MenuDtoIn();
		dto.setLabel("New Test Menu");
		dto.setPriceDF(2F);
		result = this.menuService.update(mealId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), "Result must have the same id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals("New Test Menu", result.getLabel(), "Result must have the correct label");
		Assertions.assertNull(result.getMeals(), "Result must have the correct number of meal");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate03() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.menuService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MenuDtoIn();
		dto.setLabel("New Test Menu");
		dto.setPriceDF(-2F);
		Assertions.assertThrows(ParameterException.class, () -> this.menuService.update(mealId, dto));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate04() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.menuService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MenuDtoIn();
		dto.setLabel("New Test Menu");
		dto.setPriceDF(2F);
		var aad = new AvailableForWeeksAndDays();
		aad.add(5, null);
		dto.setAvailableForWeeksAndDays(aad);
		result = this.menuService.update(mealId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), "Result must have the same id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals("New Test Menu", result.getLabel(), "Result must have the correct label");
		Assertions.assertNull(result.getMeals(), "Result must have the correct number of meal");
		var aweeks = result.getAvailableForWeeksAndDays();
		Assertions.assertNotNull(aweeks, "Week & day should be not null");
		Assertions.assertTrue(aweeks.oneWeek(5), "Week should be inside");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.menuService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertFalse(result.isDeleted(), "Result must not be deleted");
		result = this.menuService.delete(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertTrue(result.isDeleted(), "Result must be deleted");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete02() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.menuService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertFalse(result.isDeleted(), "Result must not be deleted");
		result = this.menuService.delete(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertTrue(result.isDeleted(), "Result must be deleted");
		Assertions.assertThrows(InconsistentStatusException.class, () -> this.menuService.delete(mealId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), () -> "Result must have " + mealId + " as id");
	}

	/**
	 * Test
	 */
	@Test
	void testFind02() {
		final var id = Integer.valueOf(1000000);
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.mealService.find(id));
	}

	/**
	 * Test
	 */
	@Test
	void testFind03() {
		final Integer id = null;
		Assertions.assertThrows(ParameterException.class, () -> this.mealService.find(id));
	}

}
