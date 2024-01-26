// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JacksonException;

import stone.lunchtime.AbstractJpaTest;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Meal service test class.
 */
class MealServiceTest extends AbstractJpaTest {

	/**
	 * Test
	 *
	 * @throws JacksonException if json is not right
	 */
	@Test
	void testFindAllAvailableForWeek01() throws JacksonException {
		final var week = Integer.valueOf(52);
		var result = this.mealService.findAllAvailableForWeekAndCategory(week, null);
		Assertions.assertNotNull(result, "Result must exists");
		Assertions.assertFalse(result.isEmpty(), "Result must not be empty");
		for (MealDtoOut lMealDtoOut : result) {
			Assertions.assertNotNull(lMealDtoOut, "Meal must exists");
			var aad = lMealDtoOut.getAvailableForWeeksAndDays();
			Assertions.assertTrue(lMealDtoOut.getAvailableForWeeksAndDays() == null || aad.oneWeek(week),
					() -> "Week should be null or with week = " + week);
		}
	}

	@Test
	void testFindAllAvailableForWeek05() throws JacksonException {
		final var week = Integer.valueOf(31);
		var result = this.mealService.findAllAvailableForWeekAndCategory(week, null);
		Assertions.assertNotNull(result, "Result must exists");
		Assertions.assertFalse(result.isEmpty(), "Result must not be empty");
		var found17 = false;
		for (MealDtoOut lMealDtoOut : result) {
			Assertions.assertNotNull(lMealDtoOut, "Meal must exists");
			if (lMealDtoOut.getId().intValue() == 17) {
				found17 = true;
			}
			var aad = lMealDtoOut.getAvailableForWeeksAndDays();
			Assertions.assertTrue(lMealDtoOut.getAvailableForWeeksAndDays() == null || aad.oneWeek(week),
					() -> "Week should be null or with week = " + week);
		}
		Assertions.assertTrue(found17, () -> "Curry de porc au lait de coco is part of week " + week);
	}

	@Test
	void testFindAllAvailableForWeek06() throws JacksonException {
		final var week = Integer.valueOf(9);
		final var day = Integer.valueOf(7);
		var result = this.mealService.findAllAvailableForWeekAndDayAndCategory(week, day, null);
		Assertions.assertNotNull(result, "Result must exists");
		Assertions.assertFalse(result.isEmpty(), "Result must not be empty");
		var found7 = false;
		for (MealDtoOut lMealDtoOut : result) {
			Assertions.assertNotNull(lMealDtoOut, "Meal must exists");
			if (lMealDtoOut.getId().intValue() == 7) {
				found7 = true;
			}
			var aad = lMealDtoOut.getAvailableForWeeksAndDays();
			Assertions.assertTrue(lMealDtoOut.getAvailableForWeeksAndDays() == null || aad.oneWeekAndOneDay(week, day),
					() -> "Week should be null or with week = " + week + " and day " + day);
		}
		Assertions.assertTrue(found7, () -> "Pates carbonara is part of week " + week + " and day " + day);
	}

	@Test
	void testFindAllAvailableForWeek07() {
		final var week = 0; // bad week
		final var day = Integer.valueOf(7);
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndDayAndCategory(week, day, null));

	}

	@Test
	void testFindAllAvailableForWeek08() {
		final var week = 9;
		final var day = 8; // bad day
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndDayAndCategory(week, day, null));

	}

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek02() {
		final var week = Integer.valueOf(60); // bad week
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndCategory(week, null));
	}

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek03() {
		final var w = Integer.valueOf(0);
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndCategory(w, null));
	}

	/**
	 * Test
	 */
	@Test
	void testFindAllAvailableForWeek04() {
		Assertions.assertThrows(ParameterException.class,
				() -> this.mealService.findAllAvailableForWeekAndCategory(null, null));
	}

	/**
	 * Test
	 */
	@Test
	void testAdd01() {
		var allIngredients = super.ingredientService.findAll();
		var allIngredientsId = super.transformInIdsList(allIngredients);
		var dto = new MealDtoIn();
		dto.setLabel("Test Meal");
		dto.setPriceDF(2F);
		// dto.setIngredientsId(super.generateList(3, allIngredientsId));
		dto.setIngredientsId(super.generateList(3, allIngredientsId));
		var result = this.mealService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals(result.getIngredients().size(), dto.getIngredientsId().size(),
				"Result must have the correct number of ingredients");

	}

	/**
	 * Test
	 */
	@Test
	void testAdd02() {
		var dto = new MealDtoIn();
		dto.setLabel("Test Meal");
		dto.setPriceDF(2F);
		dto.setIngredientsId(null);
		var result = this.mealService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertNull(result.getIngredients(), "Result must have the correct number of ingredients");

	}

	/**
	 * Test
	 */
	@Test
	void testAdd04() {
		var allIngredients = super.ingredientService.findAll();
		var allIngredientsId = super.transformInIdsList(allIngredients);
		var dto = new MealDtoIn();
		dto.setLabel("Test Meal");
		dto.setPriceDF(-2F);
		dto.setIngredientsId(super.generateList(3, allIngredientsId));

		var aad = new AvailableForWeeksAndDays();
		aad.add(5, null);
		dto.setAvailableForWeeksAndDays(aad);
		Assertions.assertThrows(ParameterException.class, () -> this.mealService.add(dto));
	}

	/**
	 * Test
	 */
	@Test
	void testAdd05() {
		Assertions.assertThrows(ParameterException.class, () -> this.mealService.add(null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate01() throws Exception {
		var allIngredients = super.ingredientService.findAll();
		var allIngredientsId = super.transformInIdsList(allIngredients);
		final var mealId = Integer.valueOf(1);
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MealDtoIn();
		dto.setLabel("New Test Meal");
		dto.setPriceDF(2F);
		dto.setIngredientsId(super.generateList(3, allIngredientsId));
		result = this.mealService.update(mealId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), "Result must have the same id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals("New Test Meal", result.getLabel(), "Result must have the correct label");
		Assertions.assertEquals(result.getIngredients().size(), dto.getIngredientsId().size(),
				"Result must have the correct number of ingredients");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate02() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MealDtoIn();
		dto.setLabel("New Test Meal");
		dto.setPriceDF(2F);
		result = this.mealService.update(mealId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), "Result must have the same id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals("New Test Meal", result.getLabel(), "Result must have the correct label");
		Assertions.assertNull(result.getIngredients(), "Result must have the correct number of ingredients");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate03() throws Exception {
		final var mealId = Integer.valueOf(1);
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MealDtoIn();
		dto.setLabel("New Test Meal");
		dto.setPriceDF(-2F);
		Assertions.assertThrows(ParameterException.class, () -> this.mealService.update(mealId, dto));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate04() throws Exception {
		var allIngredients = super.ingredientService.findAll();
		var allIngredientsId = super.transformInIdsList(allIngredients);
		final var mealId = Integer.valueOf(1);
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new MealDtoIn();
		dto.setLabel("New Test Meal");
		dto.setPriceDF(2F);
		dto.setIngredientsId(super.generateList(3, allIngredientsId));

		var aad = new AvailableForWeeksAndDays();
		aad.add(5, null);
		dto.setAvailableForWeeksAndDays(aad);

		result = this.mealService.update(mealId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(mealId, result.getId(), "Result must have the same id");
		Assertions.assertTrue(result.isEnabled(), "Result must have the correct status");
		Assertions.assertEquals("New Test Meal", result.getLabel(), "Result must have the correct label");
		Assertions.assertEquals(result.getIngredients().size(), dto.getIngredientsId().size(),
				"Result must have the correct number of ingredients");
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
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertFalse(result.isDeleted(), "Result must not be deleted");
		result = this.mealService.delete(mealId);
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
		var result = this.mealService.find(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertFalse(result.isDeleted(), "Result must not be deleted");
		result = this.mealService.delete(mealId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertTrue(result.isDeleted(), "Result must be deleted");
		Assertions.assertThrows(InconsistentStatusException.class, () -> this.mealService.delete(mealId));
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
