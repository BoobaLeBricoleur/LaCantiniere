package stone.lunchtime.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.out.AbstractLabeledDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;

@Service
public interface IServiceForLabeled<E, R extends AbstractLabeledDtoOut> extends IService<E, R> {

	/**
	 * Changes the image of this element. <br>
	 *
	 * @param pElmId       an element id
	 * @param pNewImageDto the new image
	 * @return the user updated
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 * @throws ParameterException          if parameter is invalid
	 */
	@Transactional(rollbackFor = Exception.class)
	R updateImage(Integer pElmId, ImageDtoIn pNewImageDto) throws EntityNotFoundException, InconsistentStatusException;

}
