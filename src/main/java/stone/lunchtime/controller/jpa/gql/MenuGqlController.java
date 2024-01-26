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
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
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
@Controller
public class MenuGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(MenuGqlController.class);

	private final IMenuService<MenuEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public MenuGqlController(IMenuService<MenuEntity> pService) {
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
	@MutationMapping
	@Observed(name = "graphql.menu.add", contextualName = "graphql#menu#add")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public MenuDtoOut addMenu(@Argument("menu") MenuDtoIn pMenu) {

		MenuGqlController.LOG.atInfo().log("--> addMenu - {}", pMenu);
		var result = this.service.add(pMenu);
		MenuGqlController.LOG.atInfo().log("<-- addMenu - New menu has id {}", result.getId());
		return result;
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
	@MutationMapping
	@Observed(name = "graphql.menu.update", contextualName = "graphql#menu#update")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public MenuDtoOut updateMenu(@Argument("id") Integer pMenuId, @Argument("menu") MenuDtoIn pMenu)
			throws EntityNotFoundException {

		MenuGqlController.LOG.atInfo().log("--> updateMenu - {}", pMenu);
		var result = this.service.update(pMenuId, pMenu);
		MenuGqlController.LOG.atInfo().log("<-- updateMenu - Menu {} is updated by lunch lady {}", result.getId(),
				this.getConnectedUserId());
		return result;
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
	@MutationMapping
	@Observed(name = "graphql.menu.delete", contextualName = "graphql#menu#delete")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public boolean deleteMenu(@Argument("id") Integer pMenuId)
			throws EntityNotFoundException, InconsistentStatusException {
		MenuGqlController.LOG.atInfo().log("--> deleteMenu - {}", pMenuId);
		var result = this.service.delete(pMenuId);
		MenuGqlController.LOG.atInfo().log("<-- deleteMenu - Menu {} is deleted by lunch lady {}", pMenuId,
				this.getConnectedUserId());
		return result.isDeleted();
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
	@QueryMapping
	@Observed(name = "graphql.menu.byid", contextualName = "graphql#menu#byid")
	public MenuDtoOut menuById(@Argument("id") Integer pMenuId) throws EntityNotFoundException {
		MenuGqlController.LOG.atInfo().log("--> menuById - {}", pMenuId);
		var result = this.service.find(pMenuId);
		MenuGqlController.LOG.atInfo().log("<-- menuById - Has found menu {}", pMenuId);
		return result;
	}

	/**
	 * Gets all menus. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @return all the menus found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.menu.findall", contextualName = "graphql#menu#findall")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public List<MenuDtoOut> findAllMenus() {
		MenuGqlController.LOG.atInfo().log("--> findAllMenus");
		var result = this.service.findAll();
		MenuGqlController.LOG.atInfo().log("<-- findAllMenus - Lunch lady has found {} menus", result.size());
		return result;
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
	@QueryMapping
	@Observed(name = "graphql.menu.findallavailableforweek", contextualName = "graphql#menu#findallavailableforweek")
	public List<MenuDtoOut> findAllMenusForWeek(@Argument("weeknumber") Integer pWeeknumber) {
		MenuGqlController.LOG.atInfo().log("--> findAllMenusForWeek - week {}", pWeeknumber);
		var result = this.service.findAllAvailableForWeek(pWeeknumber);
		MenuGqlController.LOG.atInfo().log("<-- findAllMenusForWeek - Has found {} menus", result.size());
		return result;
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
	@QueryMapping
	@Observed(name = "graphql.menu.findallavailableforweekandday", contextualName = "graphql#menu#findallavailableforweekandday")
	public List<MenuDtoOut> findAllMenusForWeekAndDay(@Argument("weeknumber") Integer pWeeknumber,
			@Argument("daynumber") Integer pDaynumber) {
		MenuGqlController.LOG.atInfo().log("--> findAllMenusForWeekAndDay - week {} and day {}", pWeeknumber, pDaynumber);
		var result = this.service.findAllAvailableForWeekAndDay(pWeeknumber, pDaynumber);
		MenuGqlController.LOG.atInfo().log("<-- findAllMenusForWeekAndDay - Has found {} menus for week {} and day {}",
				result.size(), pWeeknumber, pDaynumber);
		return result;
	}

	/**
	 * Gets all menus available for this week. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @return all the menus available for this week, an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.menu.findallavailableforthisweek", contextualName = "graphql#menu#findallavailableforthisweek")
	public List<MenuDtoOut> findAllMenusForThisWeek() {
		MenuGqlController.LOG.atInfo().log("--> findAllMenusForThisWeek");
		var weekId = OrderService.getCurrentWeekId();
		var result = this.service.findAllAvailableForWeek(weekId);
		MenuGqlController.LOG.atInfo().log("<-- findAllMenusForThisWeek - Has found {} menus for week {}", result.size(),
				weekId);
		return result;
	}

	/**
	 * Gets all menus available for today. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @return all the menus available for today, an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.menu.findallavailablefortoday", contextualName = "graphql#menu#findallavailablefortoday")
	public List<MenuDtoOut> findAllMenusForToday() {
		MenuGqlController.LOG.atInfo().log("--> findAllMenusForToday");
		var weekId = OrderService.getCurrentWeekId();
		var dayId = OrderService.getCurrentDayId();
		var result = this.service.findAllAvailableForWeekAndDay(weekId, dayId);
		MenuGqlController.LOG.atInfo().log("<-- findAllMenusForToday - Has found {} menus for week {} and day {}",
				result.size(), weekId, dayId);
		return result;
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
	@MutationMapping
	@Observed(name = "graphql.menu.updateimg", contextualName = "graphql#menu#updateimg")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public MenuDtoOut updateMenuImage(@Argument("id") Integer id, @Argument("image") ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {
		MenuGqlController.LOG.atInfo().log("--> updateMenuImage - {}", pImage);
		var result = this.service.updateImage(id, pImage);
		MenuGqlController.LOG.atInfo().log("<-- updateMenuImage - Menu {} image is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return result;
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
	@QueryMapping
	@Observed(name = "graphql.menu.findimg", contextualName = "graphql#menu#findimg")
	public ImageDtoOut findMenuImage(@Argument("id") Integer id) throws EntityNotFoundException {
		MenuGqlController.LOG.atInfo().log("--> findMenuImage - {}", id);
		var result = this.service.findEntity(id);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		MenuGqlController.LOG.atInfo().log("<-- findMenuImage - Menu's image {} found by user {}", dtoOut.getId(),
				this.getConnectedUserId());
		return dtoOut;
	}
}
