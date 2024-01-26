package stone.lunchtime.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.ParameterException;

@Service
public interface IMealService<E> extends IServiceForLabeled<E, MealDtoOut> {

	/**
	 * Will add a meal into the database.
	 *
	 * @param pDto information to be added.
	 * @return the entity added
	 */
	@Transactional(rollbackFor = Exception.class)
	MealDtoOut add(MealDtoIn pDto);

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
	MealDtoOut update(Integer pIdToUpdate, MealDtoIn pNewDto) throws EntityNotFoundException;

	/**
	 * Finds all meal available for the given week and the given category.
	 *
	 * @param pWeek     a week id [1, 53]
	 * @param pCategory a category [0, 11] (can be null)
	 * @return all meals available for this week and this category
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<MealDtoOut> findAllAvailableForWeekAndCategory(Integer pWeek, Byte pCategory);

	/**
	 * Finds all meal available for the given week AND day and the given category.
	 *
	 * @param pWeek     a week id [1, 53]
	 * @param pDay      a day [1, 7]
	 * @param pCategory a category [0, 11] (can be null)
	 * @return all meal available for this week and day
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<MealDtoOut> findAllAvailableForWeekAndDayAndCategory(Integer pWeek, Integer pDay, Byte pCategory);

}
