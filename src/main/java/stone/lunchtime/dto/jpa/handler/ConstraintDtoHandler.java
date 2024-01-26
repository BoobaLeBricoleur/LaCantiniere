package stone.lunchtime.dto.jpa.handler;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.entity.jpa.ConstraintEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface ConstraintDtoHandler {

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static ConstraintEntity toEntity(ConstraintDtoIn pDto) {
		var result = new ConstraintEntity();
		result.setOrderTimeLimit(pDto.getOrderTimeLimitAsTime());
		result.setMaximumOrderPerDay(pDto.getMaximumOrderPerDay());
		result.setRateVAT(BigDecimal.valueOf(pDto.getRateVAT()));
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static ConstraintDtoOut dtoOutfromEntity(ConstraintEntity pEntity) {
		var result = new ConstraintDtoOut(pEntity.getId());
		result.setOrderTimeLimit(pEntity.getOrderTimeLimit());
		result.setMaximumOrderPerDay(pEntity.getMaximumOrderPerDay());
		result.setRateVAT(pEntity.getRateVAT().floatValue());
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<ConstraintDtoOut> dtosOutfromEntities(List<ConstraintEntity> pEntities) {
		List<ConstraintDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(ConstraintDtoHandler.dtoOutfromEntity(elm)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static ConstraintDtoIn dtoInfromEntity(ConstraintEntity pEntity) {
		var result = new ConstraintDtoIn();
		result.setOrderTimeLimit(
				pEntity.getOrderTimeLimit().format(DateTimeFormatter.ofPattern(ConstraintDtoIn.PATTERN)));
		result.setMaximumOrderPerDay(pEntity.getMaximumOrderPerDay());
		result.setRateVAT(pEntity.getRateVAT().floatValue());
		return result;
	}
}
