package stone.lunchtime.dto.jpa.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.out.MenuDtoOut;
import stone.lunchtime.entity.jpa.MenuEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface MenuDtoHandler {
	public static final Logger LOG = LoggerFactory.getLogger(MenuDtoHandler.class); // NOSONAR

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static MenuEntity toEntity(MenuDtoIn pDto, ObjectMapper mapper) {
		var result = new MenuEntity();
		result.setDescription(pDto.getDescription());
		result.setLabel(pDto.getLabel());
		if (pDto.getAvailableForWeeksAndDays() != null) {
			try {
				result.setAvailableForWeeksAndDays(pDto.getAvailableForWeeksAndDays().toJson(mapper));
			} catch (JacksonException exc) {
				MenuDtoHandler.LOG.atError().log("Error in week dans day format!", exc);
				result.setAvailableForWeeksAndDays(null);
			}
		} else {
			result.setAvailableForWeeksAndDays(null);
		}
		result.setPriceDF(BigDecimal.valueOf(pDto.getPriceDF()));
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static MenuDtoOut dtoOutfromEntity(MenuEntity pEntity, ObjectMapper mapper) {
		var result = new MenuDtoOut(pEntity.getId());
		result.setDescription(pEntity.getDescription());
		result.setLabel(pEntity.getLabel());
		if (pEntity.getImage() != null) {
			result.setImageId(pEntity.getImage().getId());
		}
		result.setStatus(pEntity.getStatus());
		result.setPriceDF(pEntity.getPriceDF().floatValue());
		try {
			result.setAvailableForWeeksAndDays(
					new AvailableForWeeksAndDays(pEntity.getAvailableForWeeksAndDays(), mapper));
		} catch (JacksonException exc) {
			MenuDtoHandler.LOG.atError().log("Error with weeks and day format", exc);
			result.setAvailableForWeeksAndDays(null);
		}

		var lMeals = pEntity.getMeals();
		if (lMeals != null && !lMeals.isEmpty()) {
			result.setMeals(MealDtoHandler.dtosOutfromEntities(lMeals, mapper));
		}
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<MenuDtoOut> dtosOutfromEntities(List<MenuEntity> pEntities, ObjectMapper mapper) {
		List<MenuDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(MenuDtoHandler.dtoOutfromEntity(elm, mapper)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static MenuDtoIn dtoInfromEntity(MenuEntity pEntity) {
		var result = new MenuDtoIn();
		result.setDescription(pEntity.getDescription());
		result.setLabel(pEntity.getLabel());
		result.setPriceDF(pEntity.getPriceDF().floatValue());
		if (pEntity.getImage() != null) {
			result.setImage(ImageDtoHandler.dtoInfromEntity(pEntity.getImage()));
		}

		var meals = pEntity.getMeals();
		if (meals != null && !meals.isEmpty()) {
			List<Integer> mealIds = new ArrayList<>();
			meals.forEach(meal -> mealIds.add(meal.getId()));
			result.setMealIds(mealIds);
		}
		return result;
	}
}
