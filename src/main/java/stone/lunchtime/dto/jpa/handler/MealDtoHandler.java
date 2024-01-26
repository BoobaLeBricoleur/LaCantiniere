package stone.lunchtime.dto.jpa.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.entity.MealCategory;
import stone.lunchtime.entity.jpa.MealEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface MealDtoHandler {
	public static final Logger LOG = LoggerFactory.getLogger(MealDtoHandler.class); // NOSONAR

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static MealEntity toEntity(MealDtoIn pDto, ObjectMapper mapper) {
		var result = new MealEntity();

		result.setDescription(pDto.getDescription());
		result.setLabel(pDto.getLabel());
		if (pDto.getAvailableForWeeksAndDays() != null) {
			try {
				result.setAvailableForWeeksAndDays(pDto.getAvailableForWeeksAndDays().toJson(mapper));
			} catch (JacksonException exc) {
				MealDtoHandler.LOG.atError().log("Error in week day format!", exc);
				result.setAvailableForWeeksAndDays(null);
			}
		} else {
			result.setAvailableForWeeksAndDays(null);
		}
		result.setPriceDF(BigDecimal.valueOf(pDto.getPriceDF()));
		result.setCategory(pDto.getCategory());

		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @param mapper  json object mapper
	 * @return the DTO
	 */
	public static MealDtoOut dtoOutfromEntity(MealEntity pEntity, ObjectMapper mapper) {
		var result = new MealDtoOut(pEntity.getId());
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
			MealDtoHandler.LOG.atError().log("Error with weeks and day format", exc);
			result.setAvailableForWeeksAndDays(null);
		}
		if (pEntity.getCategory() != null) {
			result.setCategory(pEntity.getCategory());
		} else {
			result.setCategory(MealCategory.UNKNOWN);
		}
		var ingredientsEntity = pEntity.getIngredients();
		if (ingredientsEntity != null && !ingredientsEntity.isEmpty()) {
			result.setIngredients(IngredientDtoHandler.dtosOutfromEntities(ingredientsEntity));
		}
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<MealDtoOut> dtosOutfromEntities(List<MealEntity> pEntities, ObjectMapper mapper) {
		List<MealDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(MealDtoHandler.dtoOutfromEntity(elm, mapper)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static MealDtoIn dtoInfromEntity(MealEntity pEntity) {
		var result = new MealDtoIn();
		result.setDescription(pEntity.getDescription());
		result.setLabel(pEntity.getLabel());
		result.setPriceDF(pEntity.getPriceDF().floatValue());
		if (pEntity.getImage() != null) {
			result.setImage(ImageDtoHandler.dtoInfromEntity(pEntity.getImage()));
		}
		if (pEntity.getCategory() != null) {
			result.setCategory(pEntity.getCategory());
		} else {
			result.setCategory(MealCategory.UNKNOWN);
		}
		var ingredients = pEntity.getIngredients();
		if (ingredients != null && !ingredients.isEmpty()) {
			List<Integer> ingredientsId = new ArrayList<>();
			ingredients.forEach(ingredient -> ingredientsId.add(ingredient.getId()));
			result.setIngredientsId(ingredientsId);
		}
		return result;
	}
}
