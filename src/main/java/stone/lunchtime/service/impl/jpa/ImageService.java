// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IImageDao;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.entity.jpa.ImageEntity;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Images service.
 */
@Service
public class ImageService extends AbstractService<ImageEntity, ImageDtoOut> {
	private static final Logger LOG = LoggerFactory.getLogger(ImageService.class);

	private final IImageDao imageDao;

	/**
	 * Constructor.
	 *
	 * @param pMapper   the json mapper.
	 * @param pImageDao image dao
	 */
	@Autowired
	protected ImageService(ObjectMapper pMapper, IImageDao pImageDao) {
		super(pMapper);
		this.imageDao = pImageDao;
	}

	@Transactional(rollbackFor = Exception.class)
	public ImageEntity saveIfNotInDataBase(ImageEntity pEntity) {
		ImageService.LOG.atDebug().log("saveIfNotInDataBase - {}", pEntity);
		ValidationUtils.isNotNull(pEntity, "Entity cannot be null");
		if (pEntity.getIsDefault().booleanValue()) {
			var img = this.imageDao.findOneByImagePath(pEntity.getImagePath());
			if (img.isPresent()) {
				ImageService.LOG.atWarn().log("saveIfNotInDataBase - image with path {} already in DB", pEntity.getImagePath());
				return img.get();
			}
			ImageService.LOG.atDebug().log("saveIfNotInDataBase - image with path {} not in DB, will save it",
					pEntity.getImagePath());
		}
		if (pEntity.getId() != null) {
			ImageService.LOG.atDebug().log("saveIfNotInDataBase - image has an id {}, will save it again if path changed",
					pEntity.getId());
		}

		var resultSave = this.imageDao.save(pEntity);
		ImageService.LOG.atInfo().log("saveIfNotInDataBase - OK with id={}", resultSave.getId());
		return resultSave;
	}

	@Override
	protected JpaRepository<ImageEntity, Integer> getTargetedDao() {
		return this.imageDao;
	}

	/**
	 * Deletes the constraint. <br>
	 *
	 * Caution: Data are completely removed from database.
	 *
	 * @param pId a constraint id
	 * @throws EntityNotFoundException if entity not found
	 * @throws ParameterException      if parameter is invalid
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public ImageDtoOut delete(Integer pId) throws EntityNotFoundException {
		ImageService.LOG.atDebug().log("delete - {}", pId);
		var entity = super.findEntity(pId);
		this.imageDao.delete(entity);
		return ImageDtoHandler.dtoOutfromEntity(entity);
	}

	@Override
	public ImageDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return ImageDtoHandler.dtoOutfromEntity(super.findEntity(pEntityPrimaryKey));
	}

	@Override
	public List<ImageDtoOut> findAll() {
		return ImageDtoHandler.dtosOutfromEntities(super.findAllEntities());
	}
}
