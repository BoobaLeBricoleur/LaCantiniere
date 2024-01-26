package stone.lunchtime.dto.jpa.handler;

import java.util.ArrayList;
import java.util.List;


import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.entity.jpa.IngredientEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface IngredientDtoHandler {

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static IngredientEntity toEntity(IngredientDtoIn pDto) {
		var result = new IngredientEntity();
		result.setDescription(pDto.getDescription());
		result.setLabel(pDto.getLabel());
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static IngredientDtoOut dtoOutfromEntity(IngredientEntity pEntity) {
		var result = new IngredientDtoOut(pEntity.getId());
		result.setDescription(pEntity.getDescription());
		result.setLabel(pEntity.getLabel());
		if (pEntity.getImage() != null) {
			result.setImageId(pEntity.getImage().getId());
		}
		result.setStatus(pEntity.getStatus());
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<IngredientDtoOut> dtosOutfromEntities(List<IngredientEntity> pEntities) {
		List<IngredientDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(IngredientDtoHandler.dtoOutfromEntity(elm)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static IngredientDtoIn dtoInfromEntity(IngredientEntity pEntity) {
		var result = new IngredientDtoIn();
		result.setDescription(pEntity.getDescription());
		result.setLabel(pEntity.getLabel());
		if (pEntity.getImage() != null) {
			result.setImage(ImageDtoHandler.dtoInfromEntity(pEntity.getImage()));
		}
		return result;
	}
}
