// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IIngredientDao;
import stone.lunchtime.dao.jpa.IMealDao;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.jpa.handler.MealDtoHandler;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.MealCategory;
import stone.lunchtime.entity.jpa.ImageEntity;
import stone.lunchtime.entity.jpa.IngredientEntity;
import stone.lunchtime.entity.jpa.MealEntity;
import stone.lunchtime.service.IDefaultImages;
import stone.lunchtime.service.IMealService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Handle meal.
 */
@Service
public class MealService extends AbstractServiceForEatable<MealEntity, MealDtoOut> implements IMealService<MealEntity> {
	private static final Logger LOG = LoggerFactory.getLogger(MealService.class);

	private final IMealDao mealDao;

	private final IIngredientDao ingredientDao;

	/**
	 * Constructor.
	 *
	 * @param pMapper        the json mapper.
	 * @param pImageService  image service
	 * @param pIngredientDao ingredient dao
	 * @param pMealDao       meal dao
	 */
	@Autowired
	protected MealService(ObjectMapper pMapper, ImageService pImageService, IMealDao pMealDao,
			IIngredientDao pIngredientDao) {
		super(pMapper, pImageService);
		this.mealDao = pMealDao;
		this.ingredientDao = pIngredientDao;
	}

	@Override
	public MealDtoOut add(MealDtoIn pDto) {
		MealService.LOG.atDebug().log("add - {}", pDto);
		ValidationUtils.isNotNull(pDto, "DTO cannot be null");
		pDto.validate();

		var meal = MealDtoHandler.toEntity(pDto, super.getMapper());
		meal.setStatus(EntityStatus.ENABLED);
		this.handleIngredients(meal, pDto.getIngredientsId());

		super.handleImage(meal, pDto);

		var resultSave = this.mealDao.save(meal);
		MealService.LOG.atInfo().log("add - OK with new id={}", resultSave.getId());
		return MealDtoHandler.dtoOutfromEntity(resultSave, super.getMapper());
	}

	@Override
	public MealDtoOut update(Integer pIdToUpdate, MealDtoIn pNewDto) throws EntityNotFoundException {
		var entityInDateBase = super.beginUpdate(pIdToUpdate, pNewDto);
		this.handleIngredients(entityInDateBase, pNewDto.getIngredientsId());
		var resultUpdate = this.mealDao.save(entityInDateBase);
		MealService.LOG.atInfo().log("update - OK in {}", this.getClass().getSimpleName());
		return MealDtoHandler.dtoOutfromEntity(resultUpdate, super.getMapper());
	}

	/**
	 * Handles the join between meal and ingredients.
	 *
	 * @param pMealEntity    a meal. That will be changed during this method.
	 * @param pIngredientIds a list of ingredient's id
	 */
	private void handleIngredients(MealEntity pMealEntity, List<Integer> pIngredientIds) {
		if (pIngredientIds != null && !pIngredientIds.isEmpty()) {
			List<IngredientEntity> ingredients = new ArrayList<>();
			for (Integer ingredientId : pIngredientIds) {
				var opIngredient = this.ingredientDao.findById(ingredientId);
				if (opIngredient.isPresent()) {
					var entityFound = opIngredient.get();
					if (entityFound.isDeleted()) {
						MealService.LOG.atWarn().log(
								"handleIngredients - cannot add ingredient with id {} because it is deleted",
								ingredientId);
					} else {
						MealService.LOG.atTrace().log("handleIngredients - adding ingredient with id {}", ingredientId);
						ingredients.add(entityFound);
					}
				} else {
					MealService.LOG.atWarn().log("handleIngredients - cannot add ingredient with id {} because not found",
							ingredientId);
				}
			}
			MealService.LOG.atTrace().log("handleIngredients - adding {} ingredients", ingredients.size());
			pMealEntity.setIngredients(ingredients);
		} else {
			pMealEntity.setIngredients(null);
		}
	}

	@Override
	protected JpaRepository<MealEntity, Integer> getTargetedDao() {
		return this.mealDao;
	}

	@Override
	public List<MealDtoOut> findAllAvailableForWeekAndCategory(Integer pWeek, Byte pCategory) {
		MealService.LOG.atDebug().log("findAllAvailableForWeek - {} {}", pWeek, pCategory);

		ValidationUtils.isBetween(pWeek, 1, 53, "Le numero de semaine doit être compris entre [1, 53] !");

		Optional<List<MealEntity>> opResult;
		if (pCategory == null || !MealCategory.inRange(pCategory)) {
			MealService.LOG.atWarn().log(
					"findAllAvailableForWeekAndCategory  - pCategory is null or not in [0, 11], will not consider this information relevant");
			opResult = this.mealDao.findAllAvailableForWeek(pWeek.toString());
		} else {
			opResult = this.mealDao.findAllAvailableForWeekAndCategory(pWeek.toString(),
					MealCategory.fromValue(pCategory));
		}
		if (opResult.isPresent()) {
			var result = opResult.get();
			MealService.LOG.atDebug().log("findAllAvailableForWeekAndCategory - found {} values for week {} and category {}",
					result.size(), pWeek, pCategory);
			return MealDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		MealService.LOG.atDebug().log("findAllAvailableForWeekAndCategory - found NO value for week {} and category {}", pWeek,
				pCategory);
		return Collections.emptyList();
	}

	@Override
	public List<MealDtoOut> findAllAvailableForWeekAndDayAndCategory(Integer pWeek, Integer pDay, Byte pCategory) {
		MealService.LOG.atDebug().log("findAllAvailableForWeekAndDayAndCategory - {} {} {}", pWeek, pDay, pCategory);

		ValidationUtils.isBetween(pWeek, 1, 53, "Le numero de semaine doit être compris entre [1, 53] !");
		ValidationUtils.isBetween(pDay, 1, 7, "Le numero de jour doit être compris entre [1, 7] !");

		Optional<List<MealEntity>> opResult;
		if (pCategory == null || !MealCategory.inRange(pCategory)) {
			MealService.LOG.atWarn().log(
					"findAllAvailableForWeekAndDayAndCategory  - pCategory is null or not in [0, 11], will not consider this information relevant");
			opResult = this.mealDao.findAllAvailableForWeekAndDay(pWeek.toString(), pDay.toString());
		} else {
			opResult = this.mealDao.findAllAvailableForWeekAndDayAndCategory(pWeek.toString(), pDay.toString(),
					MealCategory.fromValue(pCategory));
		}

		if (opResult.isPresent()) {
			var result = opResult.get();
			MealService.LOG.atDebug().log(
					"findAllAvailableForWeekAndDayAndCategory - found {} values for week {} and day {} and category {}",
					result.size(), pWeek, pDay, pCategory);
			return MealDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		MealService.LOG.atDebug().log(
				"findAllAvailableForWeekAndDayAndCategory - found NO value for week {} and day {} and category {}",
				pWeek, pDay, pCategory);
		return Collections.emptyList();
	}

	@Override
	protected ImageEntity getDefault() {
		return super.getImageService().saveIfNotInDataBase(IDefaultImages.MEAL_DEFAULT_IMG);
	}

	@Override
	public MealDtoOut updateImage(Integer pElmId, ImageDtoIn pNewImageDto)
			throws EntityNotFoundException, InconsistentStatusException {
		return MealDtoHandler.dtoOutfromEntity(super.updateImageEntity(pElmId, pNewImageDto), super.getMapper());
	}

	@Override
	public MealDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return MealDtoHandler.dtoOutfromEntity(super.findEntity(pEntityPrimaryKey), super.getMapper());
	}

	@Override
	public List<MealDtoOut> findAll() {
		return MealDtoHandler.dtosOutfromEntities(super.findAllEntities(), super.getMapper());
	}

	@Override
	public MealDtoOut delete(Integer pId) throws EntityNotFoundException, InconsistentStatusException {
		return MealDtoHandler.dtoOutfromEntity(super.deleteEntity(pId), super.getMapper());
	}

}
