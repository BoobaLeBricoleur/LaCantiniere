// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import io.micrometer.observation.annotation.Observed;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.entity.jpa.MealEntity;
import stone.lunchtime.service.IMealService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.impl.jpa.OrderService;

/**
 * Meal controller.
 */
@Controller
public class MealGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(MealGqlController.class);

	private final IMealService<MealEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public MealGqlController(IMealService<MealEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Adds a meal. <br>
	 *
	 * You need to be connected as a lunch lady.
	 *
	 * @param pMeal the meal to be added
	 *
	 * @return the meal added
	 */
	@MutationMapping
	@Observed(name = "graphql.meal.add", contextualName = "graphql#meal#add")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public MealDtoOut addMeal(@Argument("meal") MealDtoIn pMeal) {
		MealGqlController.LOG.atInfo().log("--> addMeal - {}", pMeal);
		var result = this.service.add(pMeal);
		MealGqlController.LOG.atInfo().log("<-- addMeal - New meal has id {}", result.getId());
		return result;
	}

	/**
	 * Updates a meal. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * You cannot update status with this method.
	 *
	 * @param pMeal   the meal to be added
	 * @param pMealId the meal id that need to be updated
	 *
	 * @return the meal updated
	 * @throws EntityNotFoundException if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.meal.update", contextualName = "graphql#meal#update")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public MealDtoOut updateMeal(@Argument("id") Integer pMealId, @Argument("meal") MealDtoIn pMeal)
			throws EntityNotFoundException {

		MealGqlController.LOG.atInfo().log("--> updateMeal - {}", pMeal);
		var result = this.service.update(pMealId, pMeal);
		MealGqlController.LOG.atInfo().log("<-- updateMeal - Meal {} is updated by lunch lady {}", result.getId(),
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Deletes a meal. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * Meal will still be in the database but its status will be deleted. This
	 * status is permanent.
	 *
	 * @param pMealId the meal id that need to be deleted
	 *
	 * @return the meal deleted
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.meal.delete", contextualName = "graphql#meal#delete")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public boolean deleteMeal(@Argument("id") Integer pMealId)
			throws EntityNotFoundException, InconsistentStatusException {
		MealGqlController.LOG.atInfo().log("--> deleteMeal - {}", pMealId);
		var result = this.service.delete(pMealId);
		MealGqlController.LOG.atInfo().log("<-- deleteMeal - Meal {} is deleted by lunch lady {}", pMealId,
				this.getConnectedUserId());
		return result.isDeleted();
	}

	/**
	 * Gets a meal. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pMealId id of the meal you are looking for
	 *
	 * @return the meal found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.byid", contextualName = "graphql#meal#byid")
	public MealDtoOut mealById(@Argument("id") Integer pMealId) throws EntityNotFoundException {
		MealGqlController.LOG.atInfo().log("--> mealById - {}", pMealId);
		var result = this.service.find(pMealId);
		MealGqlController.LOG.atInfo().log("<-- mealById - Has found meal {}", pMealId);
		return result;
	}

	/**
	 * Gets all meals. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 *
	 * @return all the meals found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.findall", contextualName = "graphql#meal#findall")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public List<MealDtoOut> findAllMeals() {
		MealGqlController.LOG.atInfo().log("--> findAllMeals");
		var result = this.service.findAll();
		MealGqlController.LOG.atInfo().log("<-- findAllMeals - Lunch lady {} has found {} meals", this.getConnectedUserId(),
				result.size());
		return result;
	}

	/**
	 * Gets all meals available for the given week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pCategory   a category (0 .. 11). Can be null.
	 * @param pWeeknumber a week number between [1, 53]
	 * @return all the meals available for the given week number found or an empty
	 *         list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.findallavailableforweekandcategory", contextualName = "graphql#meal#findallavailableforweekandcategory")
	public List<MealDtoOut> findAllAvailableForWeekAndCategory(@Argument("weeknumber") Integer pWeeknumber,
			@Argument("catagory") Integer pCategory) {
		MealGqlController.LOG.atInfo().log("--> findAllAvailableForWeekAndCategory {} with category {}", pWeeknumber,
				pCategory);
		var dayId = OrderService.getCurrentDayId();
		var result = this.service.findAllAvailableForWeekAndDayAndCategory(pWeeknumber, dayId,
				pCategory != null ? pCategory.byteValue() : null);
		MealGqlController.LOG.atInfo().log(
				"<-- findAllAvailableForWeekAndCategory - Has found {} meals for week {} with category {}",
				result.size(), pWeeknumber, pCategory);
		return result;
	}

	/**
	 * Gets all meals available for the given week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pCategory   a category (0 .. 11). Can be null.
	 * @param pWeeknumber a week number between [1, 53]
	 * @return all the meals available for the given week number found or an empty
	 *         list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.findallavailableforweekanddayandcategory", contextualName = "graphql#meal#findallavailableforweekanddayandcategory")
	public List<MealDtoOut> findAllMealsForWeekAndDayAndCategory(@Argument("weeknumber") Integer pWeeknumber,
			@Argument("daynumber") Integer pDaynumber, @Argument("catagory") Integer pCategory) {

		MealGqlController.LOG.atInfo().log("--> findAllMealsForWeekAndDayAndCategory - week {} and day {} - category {}",
				pWeeknumber, pDaynumber, pCategory);
		var result = this.service.findAllAvailableForWeekAndDayAndCategory(pWeeknumber, pDaynumber,
				pCategory != null ? pCategory.byteValue() : null);
		MealGqlController.LOG.atInfo().log(
				"<-- findAllMealsForWeekAndDayAndCategory - Has found {} meals for week {} and day {} - category {}",
				result.size(), pWeeknumber, pDaynumber, pCategory);
		return result;
	}

	/**
	 * Gets all meals available for this week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pCategory a category (0 .. 11). Can be null.
	 * @return all the meals available for this week found or an empty list if none
	 *
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.findallavailableforweekandcategory", contextualName = "graphql#meal#findallavailableforweekandcategory")
	public List<MealDtoOut> findAllForThisWeekAndCategory(@Argument("catagory") Integer pCategory) {
		MealGqlController.LOG.atInfo().log("--> findAllForThisWeekAndCategory with category {}", pCategory);
		var weekId = OrderService.getCurrentWeekId();
		var result = this.service.findAllAvailableForWeekAndCategory(weekId,
				pCategory != null ? pCategory.byteValue() : null);
		MealGqlController.LOG.atInfo().log("<-- findAllMealsForThisWeek - Has found {} meals for week {} with category {}",
				result.size(), weekId, pCategory);
		return result;
	}

	/**
	 * Gets all meals available for today. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pCategory a category (0 .. 11). Can be null.
	 * @return all the meals available for today found or an empty list if none
	 *
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.findallavailableforweekanddayandcategory", contextualName = "graphql#meal#findallavailableforweekanddayandcategory")
	public List<MealDtoOut> findAllMealsForTodayAndCategory(@Argument("catagory") Integer pCategory) {
		MealGqlController.LOG.atInfo().log("--> findAllMealsForTodayAndCategory with category {}", pCategory);
		var weekId = OrderService.getCurrentWeekId();
		var dayId = OrderService.getCurrentDayId();
		var result = this.service.findAllAvailableForWeekAndDayAndCategory(weekId, dayId,
				pCategory != null ? pCategory.byteValue() : null);
		MealGqlController.LOG.atInfo().log(
				"<-- findAllMealsForTodayAndCategory - Has found {} meals for week {} and day {} with category {}",
				result.size(), weekId, dayId, pCategory);
		return result;
	}

	/**
	 * Updates a menu image. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param id     id of the element to be updated
	 * @param pImage where to find the new information
	 *
	 * @return the element updated
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.meal.updateimg", contextualName = "graphql#meal#updateimg")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public MealDtoOut updateMealImage(@Argument("id") Integer id, @Argument("image") ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {
		MealGqlController.LOG.atInfo().log("--> updateMealImage - {}", pImage);
		var result = this.service.updateImage(id, pImage);
		MealGqlController.LOG.atInfo().log("<-- updateMealImage - Meal {} image is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Finds an element's image. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param id id of the element's image you are looking for
	 *
	 * @return the image found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 *
	 */
	@QueryMapping
	@Observed(name = "graphql.meal.findimg", contextualName = "graphql#meal#findimg")
	public ImageDtoOut findMealImage(@Argument("id") Integer id) throws EntityNotFoundException {
		MealGqlController.LOG.atInfo().log("--> findMealImage - {}", id);
		var result = this.service.findEntity(id);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		MealGqlController.LOG.atInfo().log("<-- findMealImage - Meal's image {} found by user {}", dtoOut.getId(),
				this.getConnectedUserId());
		return dtoOut;
	}
}
