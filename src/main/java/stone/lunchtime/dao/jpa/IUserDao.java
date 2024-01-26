// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dao.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import stone.lunchtime.entity.jpa.UserEntity;

/**
 * Repository for user.
 */
@Repository
public interface IUserDao extends IJpaDao<UserEntity> {
	/**
	 * Resets all sequences for MySQL. Used for testing only.
	 */
	@Override
	@Modifying
	@Query(nativeQuery = true, value = "ALTER TABLE ltuser AUTO_INCREMENT = 1")
	void resetMySQLSequence();

	/**
	 * Find a user with its email and password.
	 *
	 * @param pEmail an email
	 * @return the user found if any
	 */
	Optional<UserEntity> findOneByEmail(String pEmail);

}
