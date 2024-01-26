package stone.lunchtime.dto.jpa.handler;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;


import stone.lunchtime.dto.in.QuantityDtoIn;
import stone.lunchtime.dto.out.QuantityDtoOut;
import stone.lunchtime.entity.jpa.QuantityEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface QuantityDtoHandler {

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static QuantityEntity toEntity(QuantityDtoIn pDto) {
		var result = new QuantityEntity();
		result.setQuantity(pDto.getQuantity());
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static QuantityDtoOut dtoOutfromEntity(QuantityEntity pEntity, ObjectMapper mapper) {
		var result = new QuantityDtoOut(pEntity.getId());
		result.setQuantity(pEntity.getQuantity());
		if (pEntity.getMeal() != null) {
			result.setMeal(MealDtoHandler.dtoOutfromEntity(pEntity.getMeal(), mapper));
		}
		if (pEntity.getMenu() != null) {
			result.setMenu(MenuDtoHandler.dtoOutfromEntity(pEntity.getMenu(), mapper));
		}
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<QuantityDtoOut> dtosOutfromEntities(List<QuantityEntity> pEntities,
			ObjectMapper mapper) {
		List<QuantityDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(QuantityDtoHandler.dtoOutfromEntity(elm, mapper)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static QuantityDtoIn dtoInfromEntity(QuantityEntity pEntity) {
		var result = new QuantityDtoIn();
		result.setQuantity(pEntity.getQuantity());
		if (pEntity.getMeal() != null) {
			result.setMealId(pEntity.getMeal().getId());
		}
		if (pEntity.getMenu() != null) {
			result.setMenuId(pEntity.getMenu().getId());
		}
		return result;
	}

	/**
	 * Used for unit test.
	 *
	 * @param dtoOut a dto out
	 * @return a new dto in
	 */
	public static QuantityDtoIn dtoInfromDtoOut(QuantityDtoOut dtoOut) {
		var result = new QuantityDtoIn();
		result.setQuantity(dtoOut.getQuantity());
		if (dtoOut.getMeal() != null) {
			result.setMealId(dtoOut.getMeal().getId());
		}
		if (dtoOut.getMenu() != null) {
			result.setMenuId(dtoOut.getMenu().getId());
		}
		return result;
	}

	/**
	 * Used for unit test.
	 *
	 * @param pDtosOut some dto out
	 * @return a new dto in
	 */
	public static List<QuantityDtoIn> dtosInfromDtosOut(List<QuantityDtoOut> pDtosOut) {
		List<QuantityDtoIn> dtos = new ArrayList<>();
		if (pDtosOut != null && !pDtosOut.isEmpty()) {
			pDtosOut.forEach(elm -> dtos.add(QuantityDtoHandler.dtoInfromDtoOut(elm)));
		}
		return dtos;
	}
}
