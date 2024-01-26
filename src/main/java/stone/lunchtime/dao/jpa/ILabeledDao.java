// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dao.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import stone.lunchtime.entity.jpa.AbstractLabeledEntity;

/**
 * Repository for labeled entity. <br>
 *
 * Not a real repository.
 *
 * @param <T> The targeted entity
 */
@NoRepositoryBean
public interface ILabeledDao<T extends AbstractLabeledEntity> extends IJpaDao<T> {

	/**
	 * Definition for a search method on labels.
	 *
	 * @param pLabel a label.
	 * @return all entity that look like this label
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value} AND label LIKE CONCAT('%',:label,'%')")
	public Optional<List<T>> findLikeLabel(@Param("label") String pLabel);

	/**
	 * Finds all none deleted entity.
	 *
	 * @return all none deleted entity.
	 */
	@Query("FROM #{#entityName} where status<>#{T(stone.lunchtime.entity.EntityStatus).DELETED.value}")
	public Optional<List<T>> findAllNotDeleted();

	/**
	 * Finds all enabled entity.
	 *
	 * @return all enabled entity.
	 */
	@Query("FROM #{#entityName} where status=#{T(stone.lunchtime.entity.EntityStatus).ENABLED.value}")
	public Optional<List<T>> findAllEnabled();
}
