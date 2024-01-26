// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IIngredientDao;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.jpa.handler.IngredientDtoHandler;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.jpa.ImageEntity;
import stone.lunchtime.entity.jpa.IngredientEntity;
import stone.lunchtime.service.IDefaultImages;
import stone.lunchtime.service.IIngredientService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Handle ingredient.
 */
@Service
public class IngredientService extends AbstractServiceForLabeled<IngredientEntity, IngredientDtoOut>
		implements IIngredientService<IngredientEntity> {
	private static final Logger LOG = LoggerFactory.getLogger(IngredientService.class);

	private final IIngredientDao ingredientDao;

	/**
	 * Constructor.
	 *
	 * @param pMapper        the json mapper.
	 * @param pImageService  image service
	 * @param pIngredientDao ingredient dao
	 */
	@Autowired
	protected IngredientService(ObjectMapper pMapper, ImageService pImageService, IIngredientDao pIngredientDao) {
		super(pMapper, pImageService);
		this.ingredientDao = pIngredientDao;
	}

	@Override
	public IngredientDtoOut add(IngredientDtoIn pDto) {
		IngredientService.LOG.atDebug().log("add - {}", pDto);
		ValidationUtils.isNotNull(pDto, "DTO cannot be null");
		pDto.validate();
		var ingredient = IngredientDtoHandler.toEntity(pDto);
		ingredient.setStatus(EntityStatus.ENABLED);

		super.handleImage(ingredient, pDto);

		var resultSave = this.ingredientDao.save(ingredient);
		IngredientService.LOG.atInfo().log("add - OK with new id={}", resultSave.getId());
		return IngredientDtoHandler.dtoOutfromEntity(resultSave);
	}

	@Override
	public IngredientDtoOut update(Integer pIdToUpdate, IngredientDtoIn pNewDto) throws EntityNotFoundException {
		var entityInDataBase = super.beginUpdate(pIdToUpdate, pNewDto);
		var resultUpdate = this.ingredientDao.save(entityInDataBase);
		IngredientService.LOG.atInfo().log("update - OK in {}", this.getClass().getSimpleName());
		return IngredientDtoHandler.dtoOutfromEntity(resultUpdate);
	}

	@Override
	protected JpaRepository<IngredientEntity, Integer> getTargetedDao() {
		return this.ingredientDao;
	}

	@Override
	protected ImageEntity getDefault() {
		return super.getImageService().saveIfNotInDataBase(IDefaultImages.INGREDIENT_DEFAULT_IMG);
	}

	@Override
	public IngredientDtoOut updateImage(Integer pElmId, ImageDtoIn pNewImageDto)
			throws EntityNotFoundException, InconsistentStatusException {
		return IngredientDtoHandler.dtoOutfromEntity(super.updateImageEntity(pElmId, pNewImageDto));
	}

	@Override
	public IngredientDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return IngredientDtoHandler.dtoOutfromEntity(super.findEntity(pEntityPrimaryKey));
	}

	@Override
	public List<IngredientDtoOut> findAll() {
		return IngredientDtoHandler.dtosOutfromEntities(super.findAllEntities());
	}

	@Override
	public IngredientDtoOut delete(Integer pId) throws EntityNotFoundException, InconsistentStatusException {
		return IngredientDtoHandler.dtoOutfromEntity(super.deleteEntity(pId));
	}

}
