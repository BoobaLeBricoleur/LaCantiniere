// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dto.out.AbstractDtoOut;
import stone.lunchtime.entity.jpa.AbstractJpaEntity;
import stone.lunchtime.service.IService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Mother class of all services that handle Entity.
 *
 * @param <E> Entity targeted by this service
 * @param <R> The targeted DTO out class
 */
@Service
abstract class AbstractService<E extends AbstractJpaEntity, R extends AbstractDtoOut> implements IService<E, R> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractService.class);

	private final ObjectMapper mapper;

	/**
	 * Constructor.
	 *
	 * @param pMapper the json mapper.
	 */
	@Autowired
	protected AbstractService(ObjectMapper pMapper) {
		super();
		this.mapper = pMapper;
	}

	@Override
	public E findEntity(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		AbstractService.LOG.atDebug().log("find - {} in {}", pEntityPrimaryKey, this.getClass().getSimpleName());

		ValidationUtils.isNotNull(pEntityPrimaryKey, "Key cannot be null");

		var optionalResult = this.getTargetedDao().findById(pEntityPrimaryKey);
		if (optionalResult.isEmpty()) {
			AbstractService.LOG.atError().log("Entity with id {} was not found for service {}", pEntityPrimaryKey,
					this.getClass().getSimpleName());
			throw new EntityNotFoundException("Entite introuvable.", pEntityPrimaryKey);
		}
		return optionalResult.get();
	}

	/**
	 * Finds all entities.
	 *
	 * @return the entities found, an empty list if none
	 * @throws ParameterException if parameter is invalid
	 */
	@Override
	public List<E> findAllEntities() {
		AbstractService.LOG.atDebug().log("findAll - for service {}", this.getClass().getSimpleName());
		var iterable = this.getTargetedDao().findAll();
		List<E> result = new ArrayList<>(iterable);
		AbstractService.LOG.atInfo().log("findAll - Found {} values for service {}", result.size(),
				this.getClass().getSimpleName());
		return result;
	}

	/**
	 * Gets the repository linked with this service.
	 *
	 * @return the repository linked with this service.
	 */
	protected abstract JpaRepository<E, Integer> getTargetedDao();

	/**
	 * Gets the property.
	 *
	 * @return the new value for property mapper
	 */
	protected ObjectMapper getMapper() {
		return this.mapper;
	}
}
