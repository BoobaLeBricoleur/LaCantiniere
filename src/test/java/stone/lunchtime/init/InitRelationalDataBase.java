// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.init;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;

import stone.lunchtime.SpringBootConfiguration;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.AbstractEatableDtoIn;
import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.in.QuantityDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.init.utils.IngredientGenerator;
import stone.lunchtime.init.utils.MealGenerator;
import stone.lunchtime.init.utils.MenuGenerator;
import stone.lunchtime.init.utils.UserGenerator;

/**
 * This is not a real test. <br>
 * Code used in order to initialize database with data. <br>
 */
@Rollback(false)
public class InitRelationalDataBase extends AbstractInitDataBase {
	private static final Logger LOG = LoggerFactory.getLogger(InitRelationalDataBase.class);

	/**
	 * Removes all values from data base and reset sequences.
	 */
	private void init() {
		InitRelationalDataBase.LOG.atInfo().log("Delete All");
		this.constraintDao.deleteAll();
		this.quantityMealDao.deleteAll();
		this.orderDao.deleteAll();
		this.menuDao.deleteAll();
		this.mealDao.deleteAll();
		this.ingredientDao.deleteAll();
		this.userDao.deleteAll();
		this.roleDao.deleteAll();
		this.imageDao.deleteAll();

		if (SpringBootConfiguration.usingMySQL(this.env)) {
			InitRelationalDataBase.LOG.atInfo().log("Reset Sequence");
			this.constraintDao.resetMySQLSequence();
			this.orderDao.resetMySQLSequence();
			this.menuDao.resetMySQLSequence();
			this.mealDao.resetMySQLSequence();
			this.ingredientDao.resetMySQLSequence();
			this.userDao.resetMySQLSequence();
			this.quantityMealDao.resetMySQLSequence();
			this.roleDao.resetMySQLSequence();
			this.imageDao.resetMySQLSequence();
		} else {
			InitRelationalDataBase.LOG.atWarn().log("init test is only for MySQL, but using {}",
					this.env.getProperty("spring.datasource.driver-class-name"));
		}
	}

	/**
	 * Inserts users in data base.
	 *
	 * @throws Exception if an error occurred
	 */
	private void initUser() throws Exception {
		InitRelationalDataBase.LOG.atInfo().log("initUser");

		var users = UserGenerator.generate(AbstractInitDataBase.USER_NB, AbstractInitDataBase.USER_DEFAULT_PWD);

		// First user is LL
		var ll = users.get(0);
		ll.setIsLunchLady(Boolean.TRUE);
		ll.setEmail(AbstractInitDataBase.USER_EXISTING_EMAIL);
		var nb = 0;
		for (UserDtoIn dto : users) {
			var result = this.userService.register(dto);
			Assertions.assertNotNull(result, "Result must exist");
			Assertions.assertNotNull(result.getId(), "Result must have an id");
			Assertions.assertNotNull(result.getRegistrationDate(), "Result must have a registration date");
			if (nb == 0) {
				Assertions.assertTrue(result.getIsLunchLady().booleanValue(), "Result must be LunchLady");
			} else {
				Assertions.assertFalse(result.getIsLunchLady().booleanValue(), "Result must not be LunchLady");
			}
			nb++;
		}
	}

	/**
	 * Inserts ingredients in data base.
	 */
	private void initIngredient() {
		InitRelationalDataBase.LOG.atInfo().log("initIngredient");
		var ingredients = IngredientGenerator.generate(AbstractInitDataBase.INGREDIENT_NB);
		for (IngredientDtoIn dto : ingredients) {
			var result = this.ingredientService.add(dto);
			Assertions.assertNotNull(result, "Result must exist");
			Assertions.assertNotNull(result.getId(), "Result must have an id");
		}
	}

	/**
	 * Inserts meals in data base.
	 */
	private void initMeal() {
		InitRelationalDataBase.LOG.atInfo().log("initMeal");
		var meals = MealGenerator.generate(AbstractInitDataBase.MEAL_NB);
		var allIngredients = this.ingredientService.findAll();
		var random = new Random();
		for (MealDtoIn dto : meals) {
			List<IngredientDtoOut> somIng = this.generateList(random.nextInt(5), allIngredients);
			dto.setIngredientsId(this.transformInIdsList(somIng));
			this.handleAvailableForWeeksAndDays(random, dto);
			var result = this.mealService.add(dto);
			Assertions.assertNotNull(result, "Result must exist");
			Assertions.assertNotNull(result.getId(), "Result must have an id");
		}
	}

	private void handleAvailableForWeeksAndDays(Random random, AbstractEatableDtoIn dto) {
		var aad = new AvailableForWeeksAndDays();
		if (random.nextBoolean()) {
			for (var o = 0; o < 10; o++) {
				if (random.nextBoolean()) {
					// A week and a day
					aad.add(random.nextInt(53) + 1, random.nextInt(7) + 1);
				} else {
					// All day in a week
					aad.add(random.nextInt(53) + 1, null);
				}
			}
			dto.setAvailableForWeeksAndDays(aad);
		} else {
			// All weeks
			dto.setAvailableForWeeksAndDays(null);
		}
	}

	/**
	 * Inserts menus in data base.
	 */
	private void initMenu() {
		InitRelationalDataBase.LOG.atInfo().log("initMenu");
		var menus = MenuGenerator.generate(AbstractInitDataBase.MENU_NB);
		var allMeals = this.mealService.findAll();
		var random = new Random();
		var index = 1;
		for (MenuDtoIn dto : menus) {
			List<MealDtoOut> someMeals = this.generateList(random.nextInt(5), allMeals);
			dto.setMealIds(this.transformInIdsList(someMeals));
			this.handleAvailableForWeeksAndDays(random, dto);
			dto.setLabel("Menu - " + index);
			var result = this.menuService.add(dto);
			Assertions.assertNotNull(result, "Result must exist");
			Assertions.assertNotNull(result.getId(), "Result must have an id");
			index++;
		}
	}

	/**
	 * Inserts constraint in data base.
	 */
	private void initConstraint() {
		InitRelationalDataBase.LOG.atInfo().log("initConstraint");
		var dto = new ConstraintDtoIn();
		var result = this.constraintService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");

	}

	/**
	 * Inserts orders in database.
	 *
	 * @throws Exception if an error occurred
	 */
	private void initOrder() throws Exception {
		InitRelationalDataBase.LOG.atInfo().log("initOrder");
		var allMeals = this.mealService.findAll();
		var allMenus = this.menuService.findAll();
		var allUsers = this.userService.findAll();
		// Remove constraint
		final var constraintId = Integer.valueOf(-1);
		final var nbOrder = 100;
		var random = new Random();
		for (var i = 0; i < nbOrder; i++) {
			var dto = new OrderDtoIn();
			var user = allUsers.get(random.nextInt(allUsers.size()));
			dto.setUserId(user.getId());
			dto.setConstraintId(constraintId);

			List<QuantityDtoIn> qps = new ArrayList<>();
			if (random.nextBoolean()) {
				var qp = new QuantityDtoIn();
				var menu = allMenus.get(random.nextInt(allMenus.size()));
				qp.setQuantity(random.nextInt(2) + 1);
				qp.setMenuId(menu.getId());
				qps.add(qp);
			} else {
				List<MealDtoOut> someMeals = this.generateList(random.nextInt(3), allMeals);
				for (MealDtoOut meal : someMeals) {
					var qp = new QuantityDtoIn();
					qp.setMealId(meal.getId());
					qp.setQuantity(random.nextInt(2) + 1);
					qps.add(qp);
				}
			}
			dto.setQuantity(qps);

			var result = this.orderService.order(dto);
			Assertions.assertNotNull(result, "Result must exist");
			Assertions.assertNotNull(result.getId(), "Result must have an id");
		}
	}

	/**
	 * Initializes all data in data base.
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@Disabled("Use only when you want to randomly reset a real data base (and not H2).")
	void initDb() throws Exception {
		InitRelationalDataBase.LOG.atWarn().log("-- WILL INIT DATA BASE .....");
		if (!SpringBootConfiguration.usingH2(this.env)) {
			InitRelationalDataBase.LOG.atWarn().log("-- Write 'y' if you are sure and press enter");
			try (var scanner = new Scanner(System.in)) {
				var line = scanner.next();
				if (line != null && ("y".equalsIgnoreCase(line.trim()) || "yes".equalsIgnoreCase(line.trim()))) {
					InitRelationalDataBase.LOG.atWarn().log("-- Let's do it then");
					this.init();
					this.initConstraint();
					this.initUser();
					this.initIngredient();
					this.initMeal();
					this.initMenu();
					this.initOrder();
				} else {
					InitRelationalDataBase.LOG.atWarn().log("-- It will be for an other time then ...");
				}
			}
		} else {
			InitRelationalDataBase.LOG.atWarn().log("initDb test is not for H2");
		}
	}

}
