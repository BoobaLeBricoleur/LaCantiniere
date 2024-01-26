package stone.lunchtime.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.out.AbstractDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * A service default definition. All services will be validated, means all
 * validation on parameters will be followed.
 *
 * @param <E> the entity handled by the service
 * @param <R> the dto out handled by the service
 */

@Service
public interface IService<E, R extends AbstractDtoOut> {

	/**
	 * Finds an entity giving its pk.
	 *
	 * @param pEntityPrimaryKey an entity primary key value
	 * @return the dto found, throws an exception if not found
	 * @throws EntityNotFoundException if entity was not found
	 */
	@Transactional(readOnly = true)
	R find(Integer pEntityPrimaryKey) throws EntityNotFoundException;

	/**
	 * Finds all entities.
	 *
	 * @return the entities found, an empty list if none
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<R> findAll();

	/**
	 * Deletes the entity. <br>
	 *
	 * Not all data are removed from database, depending on the entity, only it's
	 * status will change, or it will be really deleted.
	 *
	 * @param pId an entity id
	 * @return the entity deleted
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	@Transactional(rollbackFor = Exception.class)
	R delete(Integer pId) throws EntityNotFoundException, InconsistentStatusException;

	/**
	 * Finds an entity giving its pk.
	 *
	 * @param pEntityPrimaryKey an entity primary key value
	 * @return the dto found, throws an exception if not found
	 * @throws EntityNotFoundException if entity was not found
	 */
	@Transactional(readOnly = true)
	E findEntity(Integer pEntityPrimaryKey) throws EntityNotFoundException;

	/**
	 * Finds all entities.
	 *
	 * @return the entities found, an empty list if none
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<E> findAllEntities();

}
