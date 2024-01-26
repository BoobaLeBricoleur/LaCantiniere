package stone.lunchtime.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;

@Service
public interface IIngredientService<E> extends IServiceForLabeled<E, IngredientDtoOut> {

	/**
	 * Will add an ingredient into the database.
	 *
	 * @param pDto information to be added.
	 * @return the entity added
	 */
	@Transactional(rollbackFor = Exception.class)
	IngredientDtoOut add(IngredientDtoIn pDto);

	/**
	 * Updates entity. <br>
	 *
	 * This method does not change status.
	 *
	 * @param pIdToUpdate an entity id. The one that needs update.
	 * @param pNewDto     the new values for this entity
	 * @return the updated entity
	 * @throws EntityNotFoundException if entity not found
	 */
	@Transactional(rollbackFor = Exception.class)
	IngredientDtoOut update(Integer pIdToUpdate, IngredientDtoIn pNewDto) throws EntityNotFoundException;

}
