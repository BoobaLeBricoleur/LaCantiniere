// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IMealDao;
import stone.lunchtime.dao.jpa.IMenuDao;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.jpa.handler.MenuDtoHandler;
import stone.lunchtime.dto.out.MenuDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.jpa.ImageEntity;
import stone.lunchtime.entity.jpa.MealEntity;
import stone.lunchtime.entity.jpa.MenuEntity;
import stone.lunchtime.service.IDefaultImages;
import stone.lunchtime.service.IMenuService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Handle menu.
 */
@Service
public class MenuService extends AbstractServiceForEatable<MenuEntity, MenuDtoOut> implements IMenuService<MenuEntity> {
	private static final Logger LOG = LoggerFactory.getLogger(MenuService.class);

	private final IMealDao mealDao;

	private final IMenuDao menuDao;

	/**
	 * Constructor.
	 *
	 * @param pMapper       the json mapper.
	 * @param pImageService image service
	 * @param pMealDao      meal dao
	 * @param pMenuDao      menu dao
	 */
	@Autowired
	protected MenuService(ObjectMapper pMapper, ImageService pImageService, IMealDao pMealDao, IMenuDao pMenuDao) {
		super(pMapper, pImageService);
		this.menuDao = pMenuDao;
		this.mealDao = pMealDao;
	}

	@Override
	public MenuDtoOut add(MenuDtoIn pDto) {
		MenuService.LOG.atDebug().log("add - {}", pDto);
		ValidationUtils.isNotNull(pDto, "DTO cannot be null");
		pDto.validate();
		var menuInsert = MenuDtoHandler.toEntity(pDto, super.getMapper());
		menuInsert.setStatus(EntityStatus.ENABLED);

		this.handleMeals(menuInsert, pDto.getMealIds());

		super.handleImage(menuInsert, pDto);

		var resultSave = this.menuDao.save(menuInsert);
		MenuService.LOG.atInfo().log("add - OK with new id={}", resultSave.getId());
		return MenuDtoHandler.dtoOutfromEntity(resultSave, super.getMapper());
	}

	@Override
	public MenuDtoOut update(Integer pIdToUpdate, MenuDtoIn pNewDto) throws EntityNotFoundException {
		var entityInDateBase = super.beginUpdate(pIdToUpdate, pNewDto);

		this.handleMeals(entityInDateBase, pNewDto.getMealIds());

		var resultUpdate = this.menuDao.save(entityInDateBase);
		MenuService.LOG.atInfo().log("update - OK in {}", this.getClass().getSimpleName());
		return MenuDtoHandler.dtoOutfromEntity(resultUpdate, super.getMapper());

	}

	@Override
	public List<MenuDtoOut> findAllAvailableForWeek(Integer pWeek) {
		MenuService.LOG.atDebug().log("findAllAvailableForWeek - {}", pWeek);
		ValidationUtils.isBetween(pWeek, 1, 53, "Le numero de semaine doit être compris entre [1, 53] !");
		var opResult = this.menuDao.findAllAvailableForWeek(pWeek.toString());
		if (opResult.isPresent()) {
			var result = opResult.get();
			MenuService.LOG.atDebug().log("findAllAvailableForWeek - found {} values for week {}", result.size(), pWeek);
			return MenuDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		MenuService.LOG.atDebug().log("findAllAvailableForWeek - found NO value for week {}", pWeek);
		return Collections.emptyList();
	}

	@Override
	public List<MenuDtoOut> findAllAvailableForWeekAndDay(Integer pWeek, Integer pDay) {
		MenuService.LOG.atDebug().log("findAllAvailableForWeekAndDay - {} {}", pWeek, pDay);
		ValidationUtils.isBetween(pWeek, 1, 53, "Le numero de semaine doit être compris entre [1, 53] !");
		ValidationUtils.isBetween(pDay, 1, 7, "Le numero de jour doit être compris entre [1, 7] !");
		var opResult = this.menuDao.findAllAvailableForWeekAndDay(pWeek.toString(), pDay.toString());
		if (opResult.isPresent()) {
			var result = opResult.get();
			MenuService.LOG.atDebug().log("findAllAvailableForWeekAndDay - found {} values for week {} and day {}",
					result.size(), pWeek, pDay);
			return MenuDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		MenuService.LOG.atDebug().log("findAllAvailableForWeekAndDay - found NO value for week {} and day {}", pWeek, pDay);
		return Collections.emptyList();
	}

	/**
	 * Handles the join between menu and meal.
	 *
	 * @param pMenuEntity a menu
	 * @param pMealIds    a list of meal's id
	 */
	private void handleMeals(MenuEntity pMenuEntity, List<Integer> pMealIds) {
		if (pMealIds != null && !pMealIds.isEmpty()) {
			List<MealEntity> meals = new ArrayList<>();
			for (Integer mealId : pMealIds) {
				var oldMeal = this.mealDao.findById(mealId);
				if (oldMeal.isPresent()) {
					MenuService.LOG.atTrace().log("handleMeals - adding meal with id {}", mealId);
					meals.add(oldMeal.get());
				} else {
					MenuService.LOG.atWarn().log("handleMeals - cannot add meal with id {} because not found", mealId);
				}
			}
			pMenuEntity.setMeals(meals);
		} else {
			MenuService.LOG.atWarn().log("handleMeals (no meal)");
			pMenuEntity.setMeals(null);
		}
	}

	@Override
	protected JpaRepository<MenuEntity, Integer> getTargetedDao() {
		return this.menuDao;
	}

	@Override
	protected ImageEntity getDefault() {
		return super.getImageService().saveIfNotInDataBase(IDefaultImages.MENU_DEFAULT_IMG);
	}

	@Override
	public MenuDtoOut updateImage(Integer pElmId, ImageDtoIn pNewImageDto)
			throws EntityNotFoundException, InconsistentStatusException {
		return MenuDtoHandler.dtoOutfromEntity(super.updateImageEntity(pElmId, pNewImageDto), super.getMapper());
	}

	@Override
	public MenuDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return MenuDtoHandler.dtoOutfromEntity(super.findEntity(pEntityPrimaryKey), super.getMapper());
	}

	@Override
	public List<MenuDtoOut> findAll() {
		return MenuDtoHandler.dtosOutfromEntities(super.findAllEntities(), super.getMapper());
	}

	@Override
	public MenuDtoOut delete(Integer pId) throws EntityNotFoundException, InconsistentStatusException {
		return MenuDtoHandler.dtoOutfromEntity(super.deleteEntity(pId), super.getMapper());
	}
}
