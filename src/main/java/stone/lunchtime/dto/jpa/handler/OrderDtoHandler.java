package stone.lunchtime.dto.jpa.handler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;


import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.in.QuantityDtoIn;
import stone.lunchtime.dto.out.OrderDtoOut;
import stone.lunchtime.dto.out.QuantityDtoOut;
import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.entity.jpa.OrderEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface OrderDtoHandler {

	/**
	 * Transforms a DTO into a simple entity. Since we need to get User and
	 * Quantities, this is not done in this method that takes no parameter
	 *
	 * @return the entity
	 */
	public static OrderEntity toEntity() {
		var result = new OrderEntity();
		result.setCreationDate(LocalDate.now());
		result.setCreationTime(LocalTime.now());
		result.setStatus(OrderStatus.CREATED);
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static OrderDtoOut dtoOutfromEntity(OrderEntity pEntity, ObjectMapper mapper) {
		var result = new OrderDtoOut(pEntity.getId());
		result.setCreationDate(pEntity.getCreationDate());
		result.setCreationTime(pEntity.getCreationTime());
		result.setStatus(pEntity.getStatus());

		result.setUser(UserDtoHandler.dtoOutfromEntity(pEntity.getUser()));
		var chp = pEntity.getQuantityEntities();
		if (chp != null && !chp.isEmpty()) {
			List<QuantityDtoOut> quantity = new ArrayList<>();
			chp.forEach(elm -> quantity.add(QuantityDtoHandler.dtoOutfromEntity(elm, mapper)));
			result.setQuantity(quantity);
		}
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<OrderDtoOut> dtosOutfromEntities(List<OrderEntity> pEntities, ObjectMapper mapper) {
		List<OrderDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(OrderDtoHandler.dtoOutfromEntity(elm, mapper)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static OrderDtoIn dtoInfromEntity(OrderEntity pEntity, Integer pConstraintId) {
		var result = new OrderDtoIn();
		result.setUserId(pEntity.getUser().getId());
		var chp = pEntity.getQuantityEntities();
		if (chp != null && !chp.isEmpty()) {
			List<QuantityDtoIn> quantity = new ArrayList<>();
			chp.forEach(elm -> quantity.add(QuantityDtoHandler.dtoInfromEntity(elm)));
			result.setQuantity(quantity);
		}
		result.setConstraintId(pConstraintId);
		return result;
	}
}
