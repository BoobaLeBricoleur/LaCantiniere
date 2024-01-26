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
import org.springframework.stereotype.Repository;

import stone.lunchtime.entity.jpa.RoleEntity;

/**
 * Repository for role.
 */
@Repository
public interface IRoleDao extends IJpaDao<RoleEntity> {
	/**
	 * Resets all sequences for MySQL. Used for testing only.
	 */
	@Override
	@Modifying
	@Query(nativeQuery = true, value = "ALTER TABLE ltrole AUTO_INCREMENT = 1")
	void resetMySQLSequence();

	/**
	 * Counts how many lunch lady are in the database.
	 *
	 * @return the number of lunch lady in the database
	 */
	@Query("SELECT COUNT(id) FROM #{#entityName} WHERE label='#{T(stone.lunchtime.entity.RoleLabel).ROLE_LUNCHLADY}' and user.status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value}")
	int countLunchLady();

	/**
	 * Find all lunch ladies role.
	 *
	 * @return all lunch ladies role.
	 */
	@Query("FROM #{#entityName} WHERE label='#{T(stone.lunchtime.entity.RoleLabel).ROLE_LUNCHLADY}' and user.status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value}")
	Optional<List<RoleEntity>> findLunchLadyRoles();
}
