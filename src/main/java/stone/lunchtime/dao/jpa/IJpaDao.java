// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dao.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;

import stone.lunchtime.entity.jpa.AbstractJpaEntity;

/**
 * Default DAO parent.
 *
 * @param <T> The targeted entity
 */
@NoRepositoryBean
public interface IJpaDao<T extends AbstractJpaEntity> extends JpaRepository<T, Integer> {

	/**
	 * Resets all sequences for MySQL. <br>
	 *
	 * Used for testing only.
	 */
	@Modifying
	void resetMySQLSequence();
}
