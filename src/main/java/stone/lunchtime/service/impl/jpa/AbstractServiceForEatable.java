// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.math.BigDecimal;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dto.in.AbstractEatableDtoIn;
import stone.lunchtime.dto.out.AbstractEatableDtoOut;
import stone.lunchtime.entity.jpa.AbstractEatableEntity;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Mother class of all services that handle eatable entity.
 *
 * @param <E> Entity targeted by this service
 * @param <R> The targeted DTO out class
 */
@Service
abstract class AbstractServiceForEatable<E extends AbstractEatableEntity, R extends AbstractEatableDtoOut>
		extends AbstractServiceForLabeled<E, R> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceForEatable.class);

	/**
	 * Constructor.
	 *
	 * @param pMapper       the json mapper.
	 * @param pImageService image service
	 */
	@Autowired
	protected AbstractServiceForEatable(ObjectMapper pMapper, ImageService pImageService) {
		super(pMapper, pImageService);
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
	protected E beginUpdate(Integer pIdToUpdate, AbstractEatableDtoIn pNewDto) throws EntityNotFoundException {

		ValidationUtils.isNotNull(pIdToUpdate, "Key cannot be null");
		ValidationUtils.isNotNull(pNewDto, "DTO cannot be null");

		var entityInDateBase = super.beginUpdate(pIdToUpdate, pNewDto);

		if (pNewDto.getPriceDF() != null
				&& pNewDto.getPriceDF().floatValue() != entityInDateBase.getPriceDF().floatValue()) {
			AbstractServiceForEatable.LOG.atDebug().log("beginUpdate - Entity PrixHT has changed");
			entityInDateBase.setPriceDF(BigDecimal.valueOf(pNewDto.getPriceDF()));
		}

		if (pNewDto.getAvailableForWeeksAndDays() != null) {
			String aad = null;
			try {
				aad = pNewDto.getAvailableForWeeksAndDays().toJson(this.getMapper());
			} catch (JacksonException exc) {
				AbstractServiceForEatable.LOG.atError().log("Error with weeks and days format", exc);
			}
			if (!Objects.equals(aad, entityInDateBase.getAvailableForWeeksAndDays())) {
				AbstractServiceForEatable.LOG.atDebug().log("beginUpdate - Entity AvailableForWeeks has changed");
				entityInDateBase.setAvailableForWeeksAndDays(aad);
			}
		} else {
			entityInDateBase.setAvailableForWeeksAndDays(null);
		}

		return entityInDateBase;
	}

}
