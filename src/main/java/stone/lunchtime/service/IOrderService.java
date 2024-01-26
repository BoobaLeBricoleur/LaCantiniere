package stone.lunchtime.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.out.OrderDtoOut;
import stone.lunchtime.dto.out.PriceDtoOut;
import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.LackOfMoneyException;
import stone.lunchtime.service.exception.NotAvailableForThisWeekException;
import stone.lunchtime.service.exception.OrderCanceledException;
import stone.lunchtime.service.exception.OrderDelivredException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.service.exception.TimeOutException;

@Service
public interface IOrderService<E> extends IService<E, OrderDtoOut> {

	/**
	 * Passes an order.
	 *
	 * @param pDtoIn Information that will be used for the order
	 * @return the order
	 * @throws TimeOutException                 if time is too late for passing the
	 *                                          order. Will depends on constraint
	 *                                          given.
	 * @throws NotAvailableForThisWeekException if meal or menu is not available for
	 *                                          this week. Will depends on
	 *                                          constraint given.
	 * @throws EntityNotFoundException          if entity was not found
	 */
	@Transactional(rollbackFor = Exception.class)
	OrderDtoOut order(OrderDtoIn pDtoIn)
			throws TimeOutException, NotAvailableForThisWeekException, EntityNotFoundException;

	/**
	 * Updates entity. <br>
	 *
	 * This method does not change status nor user that made the order.
	 *
	 * @param pIdToUpdate an entity id. The one that needs update.
	 * @param pNewDto     the new values for this entity
	 * @return the updated entity
	 * @throws EntityNotFoundException          if entity not found
	 * @throws TimeOutException                 If time is not right
	 * @throws NotAvailableForThisWeekException if some meals or menu are not
	 *                                          available for this week
	 */
	@Transactional(rollbackFor = Exception.class)
	OrderDtoOut update(Integer pIdToUpdate, OrderDtoIn pNewDto)
			throws EntityNotFoundException, TimeOutException, NotAvailableForThisWeekException;

	/**
	 * Cancel an order. <br>
	 *
	 * A canceled order cannot be un-canceled.
	 *
	 * @param pOrderId an order id
	 * @return the order canceled
	 * @throws EntityNotFoundException if entity not found
	 * @throws OrderCanceledException  if entity state is not valid
	 * @throws OrderDelivredException  if entity state is not valid
	 */
	@Transactional(rollbackFor = Exception.class)
	OrderDtoOut cancel(Integer pOrderId) throws EntityNotFoundException, OrderCanceledException, OrderDelivredException;

	/**
	 * Will deliver order. <br>
	 *
	 * This will remove money from user and change the order status.
	 *
	 * @param pOrderId      an order id
	 * @param pConstraintId the constraint id. Can be null or -1.
	 * @return the order delivered
	 * @throws LackOfMoneyException    if user has not enough money
	 * @throws EntityNotFoundException if entity not found
	 * @throws OrderCanceledException  if entity state is not valid
	 * @throws OrderDelivredException  if entity state is not valid
	 */
	@Transactional(rollbackFor = Exception.class)
	OrderDtoOut deliverAndPay(Integer pOrderId, Integer pConstraintId)
			throws EntityNotFoundException, LackOfMoneyException, OrderCanceledException, OrderDelivredException;

	/**
	 * Will compute the order price. <br>
	 *
	 * This will NOT remove money from user NOR change the order status.
	 *
	 * @param pOrderId      the order id
	 * @param pConstraintId the constraint to use, can be
	 *                      <ul>
	 *                      <li>null: will use constraint with id 1 in data
	 *                      base</li>
	 *                      <li>-1: will not use constraint at all</li>
	 *                      </ul>
	 * @param pOut          the result of the computation
	 * @return the VAT price
	 * @throws EntityNotFoundException if entity was not found
	 * @throws ParameterException      if parameter is invalid
	 */
	@Transactional(readOnly = true)
	BigDecimal computePrice(Integer pOrderId, Integer pConstraintId, PriceDtoOut pOut) throws EntityNotFoundException;

	/**
	 * Selects all orders made by the given user.
	 *
	 * @param pUserId a user id
	 * @return all orders found for this user (all status) ordered by creation date.
	 *         Empty list if none.
	 */
	@Transactional(readOnly = true)
	List<OrderDtoOut> findAllByUserId(Integer pUserId);

	/**
	 * Selects all orders made between two dates and having the given status.
	 *
	 * @param pBeginDate a start date. Can be null, will use now-20years.
	 * @param pEndDate   an end date. Can be null, will use now.
	 * @param pStatus    a status. Can be null will use OrderStatus.CREATED
	 * @return all orders found ordered by creation date. Empty list if none.
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<OrderDtoOut> findAllBetweenDateInStatus(LocalDate pBeginDate, LocalDate pEndDate, OrderStatus pStatus);

	/**
	 * Selects all orders made by a given user between two dates whatever status.
	 *
	 * @param pUserId    a user id
	 * @param pBeginDate a start date. Can be null, will use now-20years.
	 * @param pEndDate   an end date. Can be null, will use now.
	 * @return all orders found ordered by creation date. Empty list if none.
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<OrderDtoOut> findAllBetweenDateForUser(Integer pUserId, LocalDate pBeginDate, LocalDate pEndDate);

	/**
	 * Selects all orders made by a given user between two dates and respecting the
	 * given status.
	 *
	 * @param pUserId    a user id
	 * @param pBeginDate a start date. Can be null, will use now-20years.
	 * @param pEndDate   an end date. Can be null, will use now.
	 * @param pStatus    a status. Can be null will use OrderStatus.CREATED
	 * @return all orders found ordered by creation date. Empty list if none.
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<OrderDtoOut> findAllBetweenDateForUserInStatus(Integer pUserId, LocalDate pBeginDate, LocalDate pEndDate,
			OrderStatus pStatus);

	/**
	 * Selects all orders made by a given user with the given status.
	 *
	 * @param pUserId a user id
	 * @param pStatus a status. Can be null will use OrderStatus.CREATED
	 * @return all orders found ordered by creation date. Empty list if none.
	 * @throws ParameterException if parameter is invalid
	 */
	@Transactional(readOnly = true)
	List<OrderDtoOut> findAllForUserInStatus(Integer pUserId, OrderStatus pStatus);

}
