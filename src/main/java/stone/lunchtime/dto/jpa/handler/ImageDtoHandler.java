package stone.lunchtime.dto.jpa.handler;

import java.util.ArrayList;
import java.util.List;


import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.entity.jpa.ImageEntity;

/**
 * Handle DTO & Entity relation.
 */
public interface ImageDtoHandler {

	/**
	 * Transforms a DTO into a simple entity.
	 *
	 * @param pDto the DTO
	 * @return the entity
	 */
	public static ImageEntity toEntity(ImageDtoIn pDto) {
		var result = new ImageEntity();
		result.setImage64(pDto.getImage64());
		result.setImagePath(pDto.getImagePath());
		return result;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static ImageDtoOut dtoOutfromEntity(ImageEntity pEntity) {
		var result = new ImageDtoOut(pEntity.getId());
		result.setImage64(pEntity.getImage64());
		result.setImagePath(pEntity.getImagePath());
		result.setDefault(pEntity.getIsDefault().booleanValue());
		return result;
	}

	/**
	 * Transforms some Entities into DTOs
	 *
	 * @param pEntities the entities
	 * @return the DTOs
	 */
	public static List<ImageDtoOut> dtosOutfromEntities(List<ImageEntity> pEntities) {
		List<ImageDtoOut> dtos = new ArrayList<>();
		if (pEntities != null && !pEntities.isEmpty()) {
			pEntities.forEach(elm -> dtos.add(ImageDtoHandler.dtoOutfromEntity(elm)));
		}
		return dtos;
	}

	/**
	 * Transforms an Entity into a DTO
	 *
	 * @param pEntity the entity
	 * @return the DTO
	 */
	public static ImageDtoIn dtoInfromEntity(ImageEntity pEntity) {
		if (pEntity != null) {
			var result = new ImageDtoIn();
			result.setImage64(pEntity.getImage64());
			result.setImagePath(pEntity.getImagePath());
			return result;
		}
		return null;
	}
}
