package stone.lunchtime.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;

@Service
public interface IConstraintService<E> extends IService<E, ConstraintDtoOut> {

	/**
	 * Will add a constraint into the database.
	 *
	 * @param pDto information to be added.
	 * @return the entity added
	 */
	@Transactional(rollbackFor = Exception.class)
	ConstraintDtoOut add(ConstraintDtoIn pDto);

	/**
	 * Updates the entity.
	 *
	 * @param pIdToUpdate the id of the entity to update
	 * @param pNewDto     where to find new information
	 * @return the entity updated
	 * @throws EntityNotFoundException if an error occurred
	 */
	@Transactional(rollbackFor = Exception.class)
	ConstraintDtoOut update(Integer pIdToUpdate, ConstraintDtoIn pNewDto) throws EntityNotFoundException;

}
