// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dto.in.AbstractLabeledDtoIn;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.AbstractLabeledDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.jpa.AbstractLabeledEntity;
import stone.lunchtime.entity.jpa.ImageEntity;
import stone.lunchtime.service.IServiceForLabeled;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Mother class for all services that handle labeled entity.
 *
 * @param <E> Entity targeted by this service
 * @param <R> The targeted DTO out class
 */
@Service
abstract class AbstractServiceForLabeled<E extends AbstractLabeledEntity, R extends AbstractLabeledDtoOut>
		extends AbstractService<E, R> implements IServiceForLabeled<E, R> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceForLabeled.class);

	private final ImageService imageService;

	/**
	 * Constructor.
	 *
	 * @param pMapper       the json mapper.
	 * @param pImageService image service
	 */
	@Autowired
	protected AbstractServiceForLabeled(ObjectMapper pMapper, ImageService pImageService) {
		super(pMapper);
		this.imageService = pImageService;
	}

	/**
	 * Will begin the update process. <br>
	 *
	 * This method does not call save method.
	 *
	 * @param pIdToUpdate an entity id. The one that needs update.
	 * @param pNewDto     the new values for this entity
	 * @return the partially updated entity
	 * @throws EntityNotFoundException if entity not found
	 */
	protected E beginUpdate(Integer pIdToUpdate, AbstractLabeledDtoIn pNewDto) throws EntityNotFoundException {
		AbstractServiceForLabeled.LOG.atDebug().log("beginUpdate - {} with {} in {}", pIdToUpdate, pNewDto,
				this.getClass().getSimpleName());

		ValidationUtils.isNotNull(pIdToUpdate, "Key cannot be null");
		ValidationUtils.isNotNull(pNewDto, "DTO cannot be null");

		pNewDto.validate();

		var entityInDateBase = super.findEntity(pIdToUpdate);

		if (pNewDto.getDescription() != null) {
			if (!pNewDto.getDescription().equals(entityInDateBase.getDescription())) {
				AbstractServiceForLabeled.LOG.atDebug().log("beginUpdate - Entity description has changed");
				entityInDateBase.setDescription(pNewDto.getDescription());
			}
		} else {
			entityInDateBase.setDescription(null);
		}

		if (!pNewDto.getLabel().equals(entityInDateBase.getLabel())) {
			AbstractServiceForLabeled.LOG.atDebug().log("beginUpdate - Entity label has changed");
			entityInDateBase.setLabel(pNewDto.getLabel());
		}
		return entityInDateBase;
	}

	protected void handleImage(E pEntity, AbstractLabeledDtoIn pNewDto) {

		var imgDto = pNewDto.getImage();
		var imgE = this.getDefault();
		if (imgDto != null) {
			AbstractServiceForLabeled.LOG.atDebug().log("insertAndLinkImage - element has an image, will insert it");
			imgE = ImageDtoHandler.toEntity(imgDto);
			imgE = this.getImageService().saveIfNotInDataBase(imgE);
			AbstractServiceForLabeled.LOG.atDebug().log("insertAndLinkImage - elements's image was inserted with id {}",
					imgE.getId());

		}
		pEntity.setImage(imgE);
	}

	/**
	 * Will change entity status. <br>
	 *
	 * @param pEntityId  an entity id
	 * @param pNewStatus the new entity status
	 * @return the updated entity
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 * @throws ParameterException          if parameter is invalid
	 */
	private E updateStatus(Integer pEntityId, EntityStatus pNewStatus)
			throws InconsistentStatusException, EntityNotFoundException {
		AbstractServiceForLabeled.LOG.atDebug().log("updateStatus - {} for new state {} in service {}", pEntityId,
				pNewStatus, this.getClass().getSimpleName());
		if (pNewStatus.equals(EntityStatus.DISABLED)) {
			AbstractServiceForLabeled.LOG.atError().log("updateStatus - newState cannot be disabled");
			throw new ParameterException("Il n'est pas possible de désactiver une entité", "pNewStatus");
		}

		var entity = super.findEntity(pEntityId);
		if (pNewStatus.equals(entity.getStatus())) {
			throw new InconsistentStatusException(
					"Entite " + pEntityId + " est déjà dans l'état demandé " + pNewStatus);
		}

		if (entity.isDeleted()) {
			throw new InconsistentStatusException(
					"Entite " + pEntityId + " est supprimée, elle ne peut pas changer de status");
		}

		entity.setStatus(pNewStatus);
		var resultUpdate = this.getTargetedDao().save(entity);
		AbstractServiceForLabeled.LOG.atInfo().log("updateStatus - OK");
		return resultUpdate;
	}

	protected E deleteEntity(Integer pId) throws EntityNotFoundException, InconsistentStatusException {
		AbstractServiceForLabeled.LOG.atDebug().log("delete - {} for service {}", pId, this.getClass().getSimpleName());
		return this.updateStatus(pId, EntityStatus.DELETED);
	}

	/**
	 * Gets the default image for this element from the database.
	 *
	 * @return the default image for this element from the database.
	 */
	protected abstract ImageEntity getDefault();

	/**
	 * Gets the image service.
	 *
	 * @return the image service
	 */
	protected ImageService getImageService() {
		return this.imageService;
	}

	protected E updateImageEntity(Integer pElmId, ImageDtoIn pNewImageDto)
			throws EntityNotFoundException, InconsistentStatusException {
		AbstractServiceForLabeled.LOG.atDebug().log("updateImage - {} for new image {}", pElmId, pNewImageDto);
		ValidationUtils.isNotNull(pNewImageDto, "DTO cannot be null");
		var elm = this.findEntity(pElmId);
		if (elm.isDeleted()) {
			throw new InconsistentStatusException("Impossible de changer l'image d'un element supprimé");
		}
		var oldImg = elm.getImage();
		if (oldImg != null) {
			if (Boolean.TRUE.equals(oldImg.getIsDefault())) {
				oldImg = ImageDtoHandler.toEntity(pNewImageDto);
			} else {
				oldImg.setImage64(pNewImageDto.getImage64());
				oldImg.setImagePath(pNewImageDto.getImagePath());
			}
			oldImg = this.imageService.saveIfNotInDataBase(oldImg);
			elm.setImage(oldImg);
		}

		var resultUpdate = this.getTargetedDao().save(elm);
		AbstractServiceForLabeled.LOG.atInfo().log("updateImage - OK");
		return resultUpdate;
	}

}
