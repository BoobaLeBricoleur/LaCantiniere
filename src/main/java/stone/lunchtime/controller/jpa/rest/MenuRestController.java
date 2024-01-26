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
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ExceptionDtoOut;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.MenuDtoOut;
import stone.lunchtime.entity.jpa.MenuEntity;
import stone.lunchtime.service.IMenuService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.impl.jpa.OrderService;

/**
 * Menu controller.
 */
@RestController
@RequestMapping("/menu")
@Tag(name = "Menu management API", description = "Menu management API")
public class MenuRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(MenuRestController.class);

	private final IMenuService<MenuEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public MenuRestController(IMenuService<MenuEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Adds a menu. <br>
	 *
	 * You need to be connected as a lunch lady.
	 *
	 * @param pMenu the menu to be added
	 * @return the menu added
	 */
	@PutMapping("/add")
	@Observed(name = "rest.menu.add", contextualName = "rest#menu#add")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Menu management API" }, summary = "Adds a menu.", description = "Will add a menu into the data base. Will return it with its id when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your menu was added and returned in the response body.", content = @Content(schema = @Schema(implementation = MenuDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your menu is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MenuDtoOut> addMenu(
			@Parameter(description = "Menu object that will be stored in database.", required = true) @RequestBody MenuDtoIn pMenu) {

		MenuRestController.LOG.atInfo().log("--> addMenu - {}", pMenu);
		var result = this.service.add(pMenu);
		MenuRestController.LOG.atInfo().log("<-- addMenu - New menu has id {}", result.getId());
		return ResponseEntity.ok(result);
	}

	/**
	 * Updates a menu. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * You cannot update status with this method.
	 *
	 * @param pMenu   the menu to be added
	 * @param pMenuId the menu id that need to be updated
	 * @return the menu updated
	 * @throws EntityNotFoundException if an error occurred
	 */
	@PatchMapping("/update/{menuId}")
	@Observed(name = "rest.menu.update", contextualName = "rest#menu#update")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Menu management API" }, summary = "Updates a menu.", description = "Will update a menu already present in the data base. Will return it when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your menu was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = MenuDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your menu or menuId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MenuDtoOut> updateMenu(
			@Parameter(description = "The menu's id", required = true) @PathVariable("menuId") Integer pMenuId,
			@Parameter(description = "Menu object that will be updated in database. All present values will be updated.", required = true) @RequestBody MenuDtoIn pMenu)
			throws EntityNotFoundException {

		MenuRestController.LOG.atInfo().log("--> updateMenu - {}", pMenu);
		var result = this.service.update(pMenuId, pMenu);
		MenuRestController.LOG.atInfo().log("<-- updateMenu - Menu {} is updated by lunch lady {}", result.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
	}

	/**
	 * Deletes a menu. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * Meal will still be in the database but its status will be deleted. This
	 * status is permanent.
	 *
	 * @param pMenuId the menu id that need to be deleted
	 * @return the menu deleted
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@DeleteMapping("/delete/{menuId}")
	@Observed(name = "rest.menu.delete", contextualName = "rest#menu#delete")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Menu management API" }, summary = "Deletes a menu.", description = "Will delete a menu already present in the data base. Will return true when done. Note that element is not realy deleted from database but will change its status to DELETE (2). You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your menu was deleted and true is returned in the response body.", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Your menuId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to delete does not exist or is not a deleteable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<Boolean> deleteMenu(
			@Parameter(description = "The menu's id", required = true) @PathVariable("menuId") Integer pMenuId)
			throws EntityNotFoundException, InconsistentStatusException {

		MenuRestController.LOG.atInfo().log("--> deleteMenu - {}", pMenuId);
		var result = this.service.delete(pMenuId);
		MenuRestController.LOG.atInfo().log("<-- deleteMenu - Menu {} is deleted by lunch lady {}", pMenuId,
				this.getConnectedUserId());
		return ResponseEntity.ok(result.isDeleted());
	}

	/**
	 * Gets a menu. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pMenuId id of the menu you are looking for
	 * @return the menu found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 */
	@GetMapping("/find/{menuId}")
	@Observed(name = "rest.menu.byid", contextualName = "rest#menu#byid")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds one menu.", description = "Will find a menu already present in the data base. Will return it when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your menu was found and returned in the response body.", content = @Content(schema = @Schema(implementation = MenuDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your menuId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MenuDtoOut> menuById(
			@Parameter(description = "The menu's id", required = true) @PathVariable("menuId") Integer pMenuId)
			throws EntityNotFoundException {

		MenuRestController.LOG.atInfo().log("--> menuById - {}", pMenuId);
		var result = this.service.find(pMenuId);
		MenuRestController.LOG.atInfo().log("<-- menuById - Has found menu {}", pMenuId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all menus. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @return all the menus found or an empty list if none
	 */
	@GetMapping("/findall")
	@Observed(name = "rest.menu.findall", contextualName = "rest#menu#findall")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds all menus.", description = "Will find all menus already present in the data base. Will return them when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All menus found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<MenuDtoOut>> findAllMenus() {

		MenuRestController.LOG.atInfo().log("--> findAllMenus");
		var result = this.service.findAll();
		MenuRestController.LOG.atInfo().log("<-- findAllMenus - Lunch lady has found {} menus", result.size());
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all menus available for the given week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pWeeknumber a week number between [1, 53]
	 * @return all the menus available for the given week number found or an empty
	 *         list if none
	 *
	 */
	@GetMapping("/findallavailableforweek/{weeknumber}")
	@Observed(name = "rest.menu.findallavailableforweek", contextualName = "rest#menu#findallavailableforweek")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds all menus for a specific week.", description = "Will find all menus already present in the data base and available for the specified week. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All menus found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuDtoOut.class)))),
			@ApiResponse(responseCode = "400", description = "Your weeknumber is not valid. Should be a number between [1..53].", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<MenuDtoOut>> findAllMenusForWeek(
			@Parameter(description = "The week's number. A number between 1 and 53.", required = true) @PathVariable("weeknumber") Integer pWeeknumber) {

		MenuRestController.LOG.atInfo().log("--> findAllMenusForWeek - week {}", pWeeknumber);
		var result = this.service.findAllAvailableForWeek(pWeeknumber);
		MenuRestController.LOG.atInfo().log("<-- findAllMenusForWeek - Has found {} menus", result.size());
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all menus available for the given week and day. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pWeeknumber a week number between [1, 53]
	 * @return all the menus available for the given week number found or an empty
	 *         list if none
	 *
	 */
	@GetMapping("/findallavailableforweekandday/{weeknumber}/{daynumber}")
	@Observed(name = "rest.menu.findallavailableforweekandday", contextualName = "rest#menu#findallavailableforweekandday")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds all menus for a specific week and day.", description = "Will find all menus already present in the data base and available for the specified week and day. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All menus found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuDtoOut.class)))),
			@ApiResponse(responseCode = "400", description = "Your weeknumber or daynumber is not valid. Weeknumber should be a number between [1..53], and daynumber should be a number between [1..7].", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<MenuDtoOut>> findAllMenusForWeekAndDay(
			@Parameter(description = "The week's number. A number between 1 and 53.", required = true) @PathVariable("weeknumber") Integer pWeeknumber,
			@Parameter(description = "The day's number. A number between 1 and 7.", required = true) @PathVariable("daynumber") Integer pDaynumber) {

		MenuRestController.LOG.atInfo().log("--> findAllMenusForWeekAndDay - week {} and day {}", pWeeknumber, pDaynumber);
		var result = this.service.findAllAvailableForWeekAndDay(pWeeknumber, pDaynumber);
		MenuRestController.LOG.atInfo().log("<-- findAllMenusForWeekAndDay - Has found {} menus for week {} and day {}",
				result.size(), pWeeknumber, pDaynumber);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all menus available for this week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @return all the menus available for this week, an empty list if none
	 */
	@GetMapping("/findallavailableforthisweek")
	@Observed(name = "rest.menu.findallavailableforthisweek", contextualName = "rest#menu#findallavailableforthisweek")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds all menus for this week (= all days in this week).", description = "Will find all menus already present in the data base and available for this current week. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All menus found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuDtoOut.class)))) })
	public ResponseEntity<List<MenuDtoOut>> findAllMenusForThisWeek() {

		MenuRestController.LOG.atInfo().log("--> findAllMenusForThisWeek");
		var weekId = OrderService.getCurrentWeekId();
		var result = this.service.findAllAvailableForWeek(weekId);
		MenuRestController.LOG.atInfo().log("<-- findAllMenusForThisWeek - Has found {} menus for week {}", result.size(),
				weekId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all menus available for today. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @return all the menus available for today, an empty list if none
	 */
	@GetMapping("/findallavailablefortoday")
	@Observed(name = "rest.menu.findallavailablefortoday", contextualName = "rest#menu#findallavailablefortoday")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds all menus for today (= this day in the week).", description = "Will find all menus already present in the data base and available for today (= this current day in the week). Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All menus found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuDtoOut.class)))) })
	public ResponseEntity<List<MenuDtoOut>> findAllMenusForToday() {

		MenuRestController.LOG.atInfo().log("--> findAllMenusForToday");
		var weekId = OrderService.getCurrentWeekId();
		var dayId = OrderService.getCurrentDayId();
		var result = this.service.findAllAvailableForWeekAndDay(weekId, dayId);
		MenuRestController.LOG.atInfo().log("<-- findAllMenusForToday - Has found {} menus for week {} and day {}",
				result.size(), weekId, dayId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Updates a menu image. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param id     id of the element to be updated
	 * @param pImage where to find the new information
	 * @return the element updated
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@PatchMapping("/updateimg/{id}")
	@Observed(name = "rest.menu.updateimg", contextualName = "rest#menu#updateimg")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Menu management API" }, summary = "Updates an element's image.", description = "Will update the image of an element already present in the data base.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your elements's image was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = MenuDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your element id is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist or has not the correct status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<MenuDtoOut> updateMenuImage(
			@Parameter(description = "The element's id", required = true) @PathVariable("id") Integer id,
			@Parameter(description = "Image object that will be updated in database. ", required = true) @RequestBody ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {

		MenuRestController.LOG.atInfo().log("--> updateMenuImage - {}", pImage);
		var result = this.service.updateImage(id, pImage);
		MenuRestController.LOG.atInfo().log("<-- updateMenuImage - Menu {} image is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
	}

	/**
	 * Finds an element's image. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param id id of the element's image you are looking for
	 * @return the image found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 *
	 */
	@GetMapping("/findimg/{menuid}")
	@Observed(name = "rest.menu.findimg", contextualName = "rest#menu#findimg")
	@Operation(tags = {
			"Menu management API" }, summary = "Finds an elements's image.", description = "Will find an element's image already present in the data base. Will return it when done. Every one can call this method.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The image was found and returned in the response body.", content = @Content(schema = @Schema(implementation = ImageDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your id is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to find does not exist or is not findable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ImageDtoOut> findMenuImage(
			@Parameter(description = "The menu's id", required = true) @PathVariable("menuid") Integer id)
			throws EntityNotFoundException {

		MenuRestController.LOG.atInfo().log("--> findMenuImage - {}", id);
		var result = this.service.findEntity(id);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		MenuRestController.LOG.atInfo().log("<-- findMenuImage - Menu's image {} found by user {}", dtoOut.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(dtoOut);
	}
}
