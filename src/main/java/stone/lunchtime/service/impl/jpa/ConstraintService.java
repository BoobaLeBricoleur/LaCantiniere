// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IConstraintDao;
import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.jpa.handler.ConstraintDtoHandler;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.entity.jpa.ConstraintEntity;
import stone.lunchtime.service.IConstraintService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Constraint service.
 */
@Service
public class ConstraintService extends AbstractService<ConstraintEntity, ConstraintDtoOut>
		implements IConstraintService<ConstraintEntity> {
	private static final Logger LOG = LoggerFactory.getLogger(ConstraintService.class);

	private final IConstraintDao constraintDao;

	/**
	 * Constructor.
	 *
	 * @param pMapper        the json mapper.
	 * @param pConstraintDao a dao
	 */
	@Autowired
	protected ConstraintService(ObjectMapper pMapper, IConstraintDao pConstraintDao) {
		super(pMapper);
		this.constraintDao = pConstraintDao;
	}

	@Override
	public ConstraintDtoOut add(ConstraintDtoIn pDto) {
		ConstraintService.LOG.atDebug().log("add - {}", pDto);

		ValidationUtils.isNotNull(pDto, "DTO cannot be null");

		pDto.validate();

		var resultSave = this.constraintDao.save(ConstraintDtoHandler.toEntity(pDto));
		ConstraintService.LOG.atInfo().log("add - OK with new id={}", resultSave.getId());
		return ConstraintDtoHandler.dtoOutfromEntity(resultSave);
	}

	@Override
	public ConstraintDtoOut update(Integer pIdToUpdate, ConstraintDtoIn pNewDto) throws EntityNotFoundException {
		ConstraintService.LOG.atDebug().log("update - {} with {}", pIdToUpdate, pNewDto);

		ValidationUtils.isNotNull(pNewDto, "DTO cannot be null");

		var entityInDataBase = super.findEntity(pIdToUpdate);

		pNewDto.validate();

		var newTime = pNewDto.getOrderTimeLimitAsTime();
		if (newTime != null && !newTime.equals(entityInDataBase.getOrderTimeLimit())) {
			ConstraintService.LOG.atDebug().log("update - Constraint OrderTimeLimit has changed");
			entityInDataBase.setOrderTimeLimit(newTime);
		}
		if (!pNewDto.getMaximumOrderPerDay().equals(entityInDataBase.getMaximumOrderPerDay())) {
			ConstraintService.LOG.atDebug().log("update - Constraint MaximumOrderPerDay has changed");
			entityInDataBase.setMaximumOrderPerDay(pNewDto.getMaximumOrderPerDay());
		}
		if (pNewDto.getRateVAT().floatValue() != entityInDataBase.getRateVAT().floatValue()) {
			ConstraintService.LOG.atDebug().log("update - Constraint RateVAT has changed");
			entityInDataBase.setRateVAT(BigDecimal.valueOf(pNewDto.getRateVAT()));
		}
		// entityInDataBase is updated with new values
		var resultUpdate = this.constraintDao.save(entityInDataBase);
		ConstraintService.LOG.atInfo().log("update - OK");
		return ConstraintDtoHandler.dtoOutfromEntity(resultUpdate);
	}

	@Override
	protected JpaRepository<ConstraintEntity, Integer> getTargetedDao() {
		return this.constraintDao;
	}

	@Override
	public ConstraintDtoOut delete(Integer pId) throws EntityNotFoundException {
		ConstraintService.LOG.atDebug().log("delete - {}", pId);
		var entity = super.findEntity(pId);
		this.constraintDao.delete(entity);
		return ConstraintDtoHandler.dtoOutfromEntity(entity);
	}

	@Override
	public ConstraintDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return ConstraintDtoHandler.dtoOutfromEntity(super.findEntity(pEntityPrimaryKey));
	}

	@Override
	public List<ConstraintDtoOut> findAll() {
		return ConstraintDtoHandler.dtosOutfromEntities(super.findAllEntities());
	}
}
