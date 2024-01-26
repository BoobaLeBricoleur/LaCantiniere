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

import stone.lunchtime.entity.MealCategory;
import stone.lunchtime.entity.jpa.MealEntity;

/**
 * Repository for meal.
 */
@Repository
public interface IMealDao extends ILabeledDao<MealEntity> {
	/**
	 * Resets all sequences for MySQL. <br>
	 *
	 * Used for testing only.
	 */
	@Override
	@Modifying
	@Query(nativeQuery = true, value = "ALTER TABLE ltmeal AUTO_INCREMENT = 1")
	void resetMySQLSequence();

	/**
	 * Finds all meal, created, and in the given week.
	 *
	 * @param pWeek a week id [1,53]
	 * @return all meal found in an Optional object.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND (availableForWeeksAndDays IS NULL OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week,'}%'))")
	Optional<List<MealEntity>> findAllAvailableForWeek(@Param("week") String pWeek);

	/**
	 * Finds all meal, created, and in the given week with the specified category.
	 *
	 * @param pWeek     a week id [1,53]
	 * @param pCategory a category
	 * @return all meal found in an Optional object.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND category=:category AND (availableForWeeksAndDays IS NULL OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week,'}%'))")
	Optional<List<MealEntity>> findAllAvailableForWeekAndCategory(@Param("week") String pWeek,
			@Param("category") MealCategory pCategory);

	/**
	 * Finds all meal, created, and in the given week AND day.
	 *
	 * @param pWeek a week id [1,53]
	 * @param pDay  a day id [1,7]
	 * @return all meal found in an Option object.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND (availableForWeeksAndDays IS NULL OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week, CONCAT(',\"day\":',:day,'}%')) OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week,'}%'))")
	Optional<List<MealEntity>> findAllAvailableForWeekAndDay(@Param("week") String pWeek, @Param("day") String pDay);

	/**
	 * Finds all meal, created, and in the given week AND day with the specified
	 * category.
	 *
	 * @param pWeek a week id [1,53]
	 * @param pDay  a day id [1,7]
	 * @return all meal found in an Option object.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND category=:category AND (availableForWeeksAndDays IS NULL OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week, CONCAT(',\"day\":',:day,'}%')) OR availableForWeeksAndDays LIKE CONCAT('%{\"week\":',:week,'}%'))")
	Optional<List<MealEntity>> findAllAvailableForWeekAndDayAndCategory(@Param("week") String pWeek,
			@Param("day") String pDay, @Param("category") MealCategory pCategory);

}
