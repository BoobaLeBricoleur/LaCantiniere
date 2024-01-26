// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dao.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import stone.lunchtime.entity.jpa.MenuEntity;

/**
 * Repository for menu.
 */
@Repository
public interface IMenuDao extends ILabeledDao<MenuEntity> {
	/**
	 * Resets all sequences for MySQL. <br>
	 *
	 * Used for testing only.
	 */
	@Override
	@Modifying
	@Query(nativeQuery = true, value = "ALTER TABLE ltmenu AUTO_INCREMENT = 1")
	void resetMySQLSequence();

	/**
	 * Finds all menu, created, and in the given week.
	 *
	 * @param pWeek a week id [1,53]
	 * @return all menu found in an Option object.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND (availableForWeeksAndDays IS NULL OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week,'}%'))")
	Optional<List<MenuEntity>> findAllAvailableForWeek(@Param("week") String pWeek);

	/**
	 * Finds all menu, created, and in the given week AND day.
	 *
	 * @param pWeek a week id [1,53]
	 * @param pDay  a day id [1,7]
	 * @return all menu found in an Option object.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND (availableForWeeksAndDays IS NULL OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week, CONCAT(',\"day\":',:day,'}%')) OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week,'}%'))")
	Optional<List<MenuEntity>> findAllAvailableForWeekAndDay(@Param("week") String pWeek, @Param("day") String pDay);
}
