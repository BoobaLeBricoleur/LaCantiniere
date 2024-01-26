// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ExceptionDtoOut;
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
@RestController
@RequestMapping("/meal")
@Tag(name = "Meal management API", description = "Meal management API")
public class MealRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(MealRestController.class);

	private final IMealService<MealEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public MealRestController(IMealService<MealEntity> pService) {
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
	@PutMapping("/add")
	@Observed(name = "rest.meal.add", contextualName = "rest#meal#add")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Meal management API" }, summary = "Adds a meal.", description = "Will add a meal into the data base. Will return it with its id when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your meal was added and returned in the response body.", content = @Content(schema = @Schema(implementation = MealDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your meal is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MealDtoOut> addMeal(
			@Parameter(description = "Meal object that will be stored in database.", required = true) @RequestBody MealDtoIn pMeal) {

		MealRestController.LOG.atInfo().log("--> addMeal - {}", pMeal);
		var result = this.service.add(pMeal);
		MealRestController.LOG.atInfo().log("<-- addMeal - New meal has id {}", result.getId());
		return ResponseEntity.ok(result);
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
	@PatchMapping("/update/{mealId}")
	@Observed(name = "rest.meal.update", contextualName = "rest#meal#update")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Meal management API" }, summary = "Updates a meal.", description = "Will update a meal already present in the data base. Will return it when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your meal was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = MealDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your meal or constraintId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MealDtoOut> updateMeal(
			@Parameter(description = "The meal's id", required = true) @PathVariable("mealId") Integer pMealId,
			@Parameter(description = "Meal object that will be updated in database. All present values will be updated.", required = true) @RequestBody MealDtoIn pMeal)
			throws EntityNotFoundException {

		MealRestController.LOG.atInfo().log("--> updateMeal - {}", pMeal);
		var result = this.service.update(pMealId, pMeal);
		MealRestController.LOG.atInfo().log("<-- updateMeal - Meal {} is updated by lunch lady {}", result.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@DeleteMapping("/delete/{mealId}")
	@Observed(name = "rest.meal.delete", contextualName = "rest#meal#delete")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Meal management API" }, summary = "Deletes a meal.", description = "Will delete a meal already present in the data base. Will return true when done. Note that element is not realy deleted from database but will change its status to DELETE (2). You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your meal was deleted and true is returned in the response body.", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Your mealId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to delete does not exist or is not a deleteable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<Boolean> deleteMeal(
			@Parameter(description = "The meal's id", required = true) @PathVariable("mealId") Integer pMealId)
			throws EntityNotFoundException, InconsistentStatusException {
		MealRestController.LOG.atInfo().log("--> deleteMeal - {}", pMealId);
		var result = this.service.delete(pMealId);
		MealRestController.LOG.atInfo().log("<-- deleteMeal - Meal {} is deleted by lunch lady {}", pMealId,
				this.getConnectedUserId());
		return ResponseEntity.ok(result.isDeleted());
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
	@GetMapping("/find/{mealId}")
	@Observed(name = "rest.meal.byid", contextualName = "rest#meal#byid")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds one meal.", description = "Will find a meal already present in the data base. Will return it when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your meal was found and returned in the response body.", content = @Content(schema = @Schema(implementation = MealDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your mealId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MealDtoOut> mealById(
			@Parameter(description = "The meal's id", required = true) @PathVariable("mealId") Integer pMealId)
			throws EntityNotFoundException {

		MealRestController.LOG.atInfo().log("--> mealById - {}", pMealId);
		var result = this.service.find(pMealId);
		MealRestController.LOG.atInfo().log("<-- mealById - Has found meal {}", pMealId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all meals. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 *
	 * @return all the meals found or an empty list if none
	 */
	@GetMapping("/findall")
	@Observed(name = "rest.meal.findall", contextualName = "rest#meal#findall")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds all meals.", description = "Will find all meals already present in the data base. Will return them when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All meals found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MealDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<MealDtoOut>> findAllMeals() {

		MealRestController.LOG.atInfo().log("--> findAllMeals");
		var result = this.service.findAll();
		MealRestController.LOG.atInfo().log("<-- findAllMeals - Lunch lady {} has found {} meals", this.getConnectedUserId(),
				result.size());
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all meals available for the given week and potential category (as
	 * parameter). <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pWeeknumber a week number between [1, 53]
	 * @param pCategory   a category, number between [0, 11] (see enum for values).
	 *                    Can be absent.
	 * @return all the meals available for the given week number, potentially its
	 *         category, found or an empty list if none
	 */
	@GetMapping("/findallavailableforweek/{weeknumber}")
	@Observed(name = "rest.meal.findallmealsforweekandcategory", contextualName = "rest#meal#findallmealsforweekandcategory")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds all meals for a specific week and potential category.", description = "Will find all meals already present in the data base and available for the specified week and the potential category. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All meals found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MealDtoOut.class)))),
			@ApiResponse(responseCode = "400", description = "Your weeknumber is not valid. Should be a number between [1..53].", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<MealDtoOut>> findAllMealsForWeekAndCategory(
			@Parameter(description = "The week's number. A number between 1 and 53.", required = true) @PathVariable("weeknumber") Integer pWeeknumber,
			@Parameter(description = "The meal category. See enum for possible values from 0 to 11.", required = false) @RequestParam(required = false, name = "category") Byte pCategory) {
		MealRestController.LOG.atInfo().log("--> findAllMealsForWeekAndCategory - week {} - category {}", pWeeknumber,
				pCategory);
		var result = this.service.findAllAvailableForWeekAndCategory(pWeeknumber, pCategory);
		MealRestController.LOG.atInfo().log("<-- findAllMealsForWeekAndCategory - Has found {} meals", result.size());
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all meals available for the given week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pWeeknumber a week number between [1, 53]
	 * @param pDaynumber  a day number between [1, 7]
	 * @param pCategory   a category, number between [0, 11] (see enum for values).
	 *                    Can be absent.
	 * @return all the meals available for the given week and day number,
	 *         potentially its category, found or an empty list if none
	 */
	@GetMapping("/findallavailableforweekandday/{weeknumber}/{daynumber}")
	@Observed(name = "rest.meal.findallavailableforweekanddayandcategory", contextualName = "rest#meal#findallavailableforweekanddayandcategory")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds all meals for a specific day in the week and potential category.", description = "Will find all meals already present in the data base and available for the specified day in the week and potential category. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All meals found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MealDtoOut.class)))),
			@ApiResponse(responseCode = "400", description = "Your weeknumber or daynumber is not valid. Weeknumber should be a number between [1..53], and daynumber should be a number between [1..7].", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<MealDtoOut>> findAllMealsForWeekAndDayAndCategory(
			@Parameter(description = "The week's number. A number between 1 and 53.", required = true) @PathVariable("weeknumber") Integer pWeeknumber,
			@Parameter(description = "The day's number. A number between 1 and 7.", required = true) @PathVariable("daynumber") Integer pDaynumber,
			@Parameter(description = "The meal category. See enum for possible values from 0 to 11.", required = false) @RequestParam(required = false, name = "category") Byte pCategory) {

		MealRestController.LOG.atInfo().log("--> findAllMealsForWeekAndDayAndCategory - week {} and day {} - category {}",
				pWeeknumber, pDaynumber, pCategory);
		var result = this.service.findAllAvailableForWeekAndDayAndCategory(pWeeknumber, pDaynumber, pCategory);
		MealRestController.LOG.atInfo().log(
				"<-- findAllMealsForWeekAndDayAndCategory - Has found {} meals for week {} and day {} - category {}",
				result.size(), pWeeknumber, pDaynumber, pCategory);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all meals available for this week and potential category. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pCategory a category, number between [0, 11] (see enum for values).
	 *                  Can be absent.
	 * @return all the meals available for this week, potentially its category,
	 *         found or an empty list if none
	 *
	 */
	@GetMapping("/findallavailableforthisweek")
	@Observed(name = "rest.meal.findallavailableforweekandcategory", contextualName = "rest#meal#findallavailableforweekandcategory")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds all meals for this current week and potential category.", description = "Will find all meals already present in the data base and available for this current week. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All meals found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MealDtoOut.class)))) })
	public ResponseEntity<List<MealDtoOut>> findAllForThisWeekAndCategory(
			@Parameter(description = "The meal category. See enum for possible values from 0 to 11.", required = false) @RequestParam(required = false, name = "category") Byte pCategory) {

		MealRestController.LOG.atInfo().log("--> findAllForThisWeekAndCategory with category {}", pCategory);
		var weekId = OrderService.getCurrentWeekId();
		var result = this.service.findAllAvailableForWeekAndCategory(weekId, pCategory);
		MealRestController.LOG.atInfo().log("<-- findAllMealsForThisWeek - Has found {} meals for week {} with category {}",
				result.size(), weekId, pCategory);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all meals available for today and potential category. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pCategory a category, number between [0, 11] (see enum for values).
	 *                  Can be absent.
	 * @return all the meals available for today, potentially its category, found or
	 *         an empty list if none
	 */
	@GetMapping("/findallavailablefortoday")
	@Observed(name = "rest.meal.findallmealsfortodayandcategory", contextualName = "rest#meal#findallmealsfortodayandcategory")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds all meals for this current day in the week.", description = "Will find all meals already present in the data base and available for this current day in the week. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All meals found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MealDtoOut.class)))) })
	public ResponseEntity<List<MealDtoOut>> findAllMealsForTodayAndCategory(
			@Parameter(description = "The meal category. See enum for possible values from 0 to 11.", required = false) @RequestParam(required = false, name = "category") Byte pCategory) {
		MealRestController.LOG.atInfo().log("--> findAllMealsForTodayAndCategory with category {}", pCategory);
		var weekId = OrderService.getCurrentWeekId();
		var dayId = OrderService.getCurrentDayId();
		var result = this.service.findAllAvailableForWeekAndDayAndCategory(weekId, dayId, pCategory);
		MealRestController.LOG.atInfo().log(
				"<-- findAllMealsForTodayAndCategory - Has found {} meals for week {} and day {} with category {}",
				result.size(), weekId, dayId, pCategory);
		return ResponseEntity.ok(result);
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
	@PatchMapping("/updateimg/{id}")
	@Observed(name = "rest.meal.updateimg", contextualName = "rest#meal#updateimg")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Meal management API" }, summary = "Updates an element's image.", description = "Will update the image of an element already present in the data base.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your elements's image was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = MealDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your element id is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist or has not the correct status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MealDtoOut> updateMealImage(
			@Parameter(description = "The element's id", required = true) @PathVariable("id") Integer id,
			@Parameter(description = "Image object that will be updated in database. ", required = true) @RequestBody ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {

		MealRestController.LOG.atInfo().log("--> updateMealImage - {}", pImage);
		var result = this.service.updateImage(id, pImage);
		MealRestController.LOG.atInfo().log("<-- updateMealImage - Meal {} image is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@GetMapping("/findimg/{mealid}")
	@Observed(name = "rest.meal.findimg", contextualName = "rest#meal#findimg")
	@Operation(tags = {
			"Meal management API" }, summary = "Finds an elements's image.", description = "Will find an element's image already present in the data base. Will return it when done. Every one can call this method.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The image was found and returned in the response body.", content = @Content(schema = @Schema(implementation = ImageDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your id is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to find does not exist or is not findable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ImageDtoOut> findMealImage(
			@Parameter(description = "The meal's id", required = true) @PathVariable("mealid") Integer id)
			throws EntityNotFoundException {

		MealRestController.LOG.atInfo().log("--> findMealImage - {}", id);
		var result = this.service.findEntity(id);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		MealRestController.LOG.atInfo().log("<-- findMealImage - Meal's image {} found by user {}", dtoOut.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(dtoOut);
	}

}
