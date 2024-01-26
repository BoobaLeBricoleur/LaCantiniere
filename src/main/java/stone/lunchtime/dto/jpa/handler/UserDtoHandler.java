package stone.lunchtime.dto.jpa.handler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.entity.jpa.UserEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface UserDtoHandler {

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static UserEntity toEntity(UserDtoIn pDto) {
		var result = new UserEntity();
		result.setAddress(pDto.getAddress());
		result.setWallet(BigDecimal.valueOf(pDto.getWallet()));
		result.setPostalCode(pDto.getPostalCode());
		result.setRegistrationDate(LocalDateTime.now());
		result.setEmail(pDto.getEmail());
		result.setIsLunchLady(pDto.getIsLunchLady());
		result.setPassword(pDto.getPassword());
		result.setName(pDto.getName());
		result.setFirstname(pDto.getFirstname());
		result.setPhone(pDto.getPhone());
		result.setTown(pDto.getTown());
		result.setSex(pDto.getSex());
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static UserDtoOut dtoOutfromEntity(UserEntity pEntity) {
		var result = new UserDtoOut(pEntity.getId());
		result.setAddress(pEntity.getAddress());
		if (pEntity.getWallet() != null) {
			result.setWallet(pEntity.getWallet().floatValue());
		} else {
			result.setWallet(0F);
		}
		result.setPostalCode(pEntity.getPostalCode());
		result.setRegistrationDate(pEntity.getRegistrationDate());
		result.setEmail(pEntity.getEmail());
		result.setIsLunchLady(pEntity.getIsLunchLady());
		if (pEntity.getImage() != null) {
			result.setImageId(pEntity.getImage().getId());
		}
		result.setName(pEntity.getName());
		result.setFirstname(pEntity.getFirstname());
		result.setPhone(pEntity.getPhone());
		result.setTown(pEntity.getTown());
		result.setSex(pEntity.getSex());
		result.setStatus(pEntity.getStatus());
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<UserDtoOut> dtosOutfromEntities(List<UserEntity> pEntities) {
		List<UserDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(UserDtoHandler.dtoOutfromEntity(elm)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static UserDtoIn dtoInfromEntity(UserEntity pEntity) {
		var result = new UserDtoIn();
		result.setAddress(pEntity.getAddress());
		result.setWallet(pEntity.getWallet().floatValue());
		result.setPostalCode(pEntity.getPostalCode());
		result.setEmail(pEntity.getEmail());
		result.setIsLunchLady(pEntity.getIsLunchLady());
		result.setImage(ImageDtoHandler.dtoInfromEntity(pEntity.getImage()));
		result.setName(pEntity.getName());
		result.setFirstname(pEntity.getFirstname());
		result.setPhone(pEntity.getPhone());
		result.setTown(pEntity.getTown());
		result.setSex(pEntity.getSex());
		// Do not set password since it is hached in entity
		// result.setPassword(pEntity.getPassword());
		return result;
	}
}
