// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.dao.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.entity.jpa.OrderEntity;

/**
 * Repository for order.
 */
@Repository
public interface IOrderDao extends IJpaDao<OrderEntity> {
	/**
	 * Resets all sequences for MySQL. <br>
	 *
	 * Used for testing only.
	 */
	@Override
	@Modifying
	@Query(nativeQuery = true, value = "ALTER TABLE ltorder AUTO_INCREMENT=1")
	void resetMySQLSequence();

	/**
	 * Selects all orders made by the given user.
	 *
	 * @param pUserId a user id
	 * @return all orders found for this user (all status) ordered by creation date
	 */
	Optional<List<OrderEntity>> findByUserIdOrderByCreationDateAsc(Integer pUserId);

	/**
	 * Selects all orders made between two dates and having the given status.
	 *
	 * @param pBeginDate a start date.
	 * @param pEndDate   an end date.
	 * @param pStatus    a status.
	 * @return all orders found ordered by creation date.
	 */
	Optional<List<OrderEntity>> findByCreationDateBetweenAndStatusOrderByCreationDateAsc(LocalDate pBeginDate,
			LocalDate pEndDate, OrderStatus pStatus);

	/**
	 * Selects all orders made by a given user between two dates whatever status.
	 *
	 * @param pUserId    a user id
	 * @param pBeginDate a start date.
	 * @param pEndDate   an end date.
	 * @return all orders found ordered by creation date.
	 */
	Optional<List<OrderEntity>> findByCreationDateBetweenAndUserIdOrderByCreationDateAsc(LocalDate pBeginDate,
			LocalDate pEndDate, Integer pUserId);

	/**
	 * Selects all orders made by a given user between two dates and respecting the
	 * given status.
	 *
	 * @param pUserId    a user id
	 * @param pBeginDate a start date.
	 * @param pEndDate   an end date.
	 * @param pStatus    a status.
	 * @return all orders found ordered by creation
	 */
	Optional<List<OrderEntity>> findByCreationDateBetweenAndUserIdAndStatusOrderByCreationDateAsc(LocalDate pBeginDate,
			LocalDate pEndDate, Integer pUserId, OrderStatus pStatus);

	/**
	 * Selects all orders made by a given user with the given status.
	 *
	 * @param pUserId a user id
	 * @param pStatus a status.
	 * @return all orders found ordered by creation date.
	 */
	Optional<List<OrderEntity>> findByUserIdAndStatusOrderByCreationDateAsc(Integer pUserId, OrderStatus pStatus);
}
