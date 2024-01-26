package stone.lunchtime.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.in.MenuDtoIn;
import stone.lunchtime.dto.out.MenuDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.ParameterException;

@Service
public interface IMenuService<E> extends IServiceForLabeled<E, MenuDtoOut> {

	/**
	 * Will add a menu into the database.
	 *
	 * @param pDto information to be added.
	 * @return the entity added
	 */
	@Transactional(rollbackFor = Exception.class)
	MenuDtoOut add(MenuDtoIn pDto);

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
	MenuDtoOut update(Integer pIdToUpdate, MenuDtoIn pNewDto) throws EntityNotFoundException;

	/**
	 * Finds all menu available for the given week.
	 *
	 * @param pWeek a week id [1, 53]
	 * @return all meal available for this week
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<MenuDtoOut> findAllAvailableForWeek(Integer pWeek);

	/**
	 * Finds all menu available for the given week and day.
	 *
	 * @param pWeek a week id [1, 53]
	 * @param pDay  a day if [1, 7]
	 * @return all meal available for this week and day
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<MenuDtoOut> findAllAvailableForWeekAndDay(Integer pWeek, Integer pDay);

}
