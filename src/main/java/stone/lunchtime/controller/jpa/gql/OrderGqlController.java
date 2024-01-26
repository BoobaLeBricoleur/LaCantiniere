// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;

import io.micrometer.observation.annotation.Observed;
import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.out.OrderDtoOut;
import stone.lunchtime.dto.out.PriceDtoOut;
import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.entity.jpa.OrderEntity;
import stone.lunchtime.service.IOrderService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.LackOfMoneyException;
import stone.lunchtime.service.exception.NotAvailableForThisWeekException;
import stone.lunchtime.service.exception.OrderCanceledException;
import stone.lunchtime.service.exception.OrderDelivredException;
import stone.lunchtime.service.exception.TimeOutException;

/**
 * Order controller.
 */
@Controller
public class OrderGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(OrderGqlController.class);

	private final IOrderService<OrderEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public OrderGqlController(IOrderService<OrderEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Passes an order. <br>
	 *
	 * You need to be connected.
	 *
	 * @param pOrder the order to be added
	 *
	 * @return the order added
	 * @throws EntityNotFoundException          if an error occurred
	 * @throws NotAvailableForThisWeekException if an error occurred
	 * @throws TimeOutException                 if an error occurred
	 */
	@PreAuthorize("isAuthenticated()")
	@Observed(name = "graphql.order.add", contextualName = "graphql#order#add")
	@MutationMapping
	public OrderDtoOut addOrder(@Argument(name = "order") OrderDtoIn pOrder)
			throws TimeOutException, NotAvailableForThisWeekException, EntityNotFoundException {

		OrderGqlController.LOG.atInfo().log("--> addOrder - {}", pOrder);
		if (pOrder.getUserId() == null) {
			OrderGqlController.LOG.atWarn().log(
					"--- addOrder - New order had no user id so will use the one who is connected {}",
					super.getConnectedUserId());
			pOrder.setUserId(super.getConnectedUserId());
		}
		var result = this.service.order(pOrder);
		OrderGqlController.LOG.atInfo().log("<-- addOrder - New order made by {} has id {}", super.getConnectedUserId(),
				result.getId());
		return result;
	}

	/**
	 * Deliver and pay the order. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * Order status will change and money will be removed from the user's wallet.
	 *
	 * @param pOrderId      the order id to be delivered and paid
	 * @param pConstraintId a constraint id. If null will use first one in database,
	 *                      if -1 will not use any constraint
	 * @return the order updated
	 * @throws OrderDelivredException  if an error occurred
	 * @throws OrderCanceledException  if an error occurred
	 * @throws LackOfMoneyException    if an error occurred
	 * @throws EntityNotFoundException if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.order.pay", contextualName = "graphql#order#pay")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public OrderDtoOut payOrder(@Argument("id") Integer pOrderId, @Argument("constraintId") Integer pConstraintId)
			throws EntityNotFoundException, LackOfMoneyException, OrderCanceledException, OrderDelivredException {

		OrderGqlController.LOG.atInfo().log("--> pay - {} with constraint {}", pOrderId, pConstraintId);
		var result = this.service.deliverAndPay(pOrderId, pConstraintId);
		OrderGqlController.LOG.atInfo().log("<-- pay - order {} is payed by lunch lady {}", result.getId(),
				super.getConnectedUserId());
		return result;
	}

	/**
	 * Computes order price. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to compute price of
	 * order that you have made.
	 *
	 * @param pOrderId      the order targeted
	 * @param pConstraintId a constraint id. If null will use first one in database,
	 *                      if -1 will not use any constraint
	 * @return the order price
	 * @throws EntityNotFoundException if an error occurred
	 */
	@PreAuthorize("isAuthenticated()")
	@Observed(name = "graphql.order.computeprice", contextualName = "graphql#order#computeprice")
	@QueryMapping
	public PriceDtoOut computeOrderPrice(@Argument("id") Integer pOrderId,
			@Argument("constraintId") Integer pConstraintId) throws EntityNotFoundException {

		OrderGqlController.LOG.atInfo().log("--> computeOrderPrice - {} with constraint {}", pOrderId, pConstraintId);
		var order = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(order.getUser().getId())) {
			var dtoOut = new PriceDtoOut();
			this.service.computePrice(pOrderId, pConstraintId, dtoOut);
			OrderGqlController.LOG.atInfo().log("<-- computeOrderPrice - order {} has a price of {}", pOrderId, dtoOut);
			return dtoOut;
		}
		OrderGqlController.LOG.atError().log(
				"<-- computeOrderPrice - User {} not allowed to compute Price for order {}", super.getConnectedUserId(),
				pOrderId);
		throw new InsufficientAuthenticationException("Vous n'avez pas le droit de voir cette commande!");
	}

	/**
	 * Cancels an order. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to cancel order that you
	 * have made. <br>
	 * A canceled order cannot be change later.
	 *
	 * @param pOrderId id of the order to be deleted
	 * @return the order canceled
	 * @throws OrderDelivredException  if an error occurred
	 * @throws OrderCanceledException  if an error occurred
	 * @throws EntityNotFoundException if an error occurred
	 */
	@PreAuthorize("isAuthenticated()")
	@Observed(name = "graphql.order.cancel", contextualName = "graphql#order#cancel")
	@MutationMapping
	public OrderDtoOut cancelOrder(@Argument("id") Integer pOrderId)
			throws EntityNotFoundException, OrderCanceledException, OrderDelivredException {

		OrderGqlController.LOG.atInfo().log("--> cancelOrder - {}", pOrderId);
		var orderToCancel = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(orderToCancel.getUser().getId())) {
			var result = this.service.cancel(pOrderId);
			OrderGqlController.LOG.atInfo().log("<-- cancelOrder - order {} is cancel by user {}", pOrderId,
					super.getConnectedUserId());
			return result;
		}
		OrderGqlController.LOG.atError().log("<-- cancelOrder - User {} not allowed to cancel command {}",
				super.getConnectedUserId(), pOrderId);
		throw new InsufficientAuthenticationException("Vous n'avez pas le droit d'annuler cette commande!");
	}

	/**
	 * Updates an order. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to update order that you
	 * have made. <br>
	 * You cannot change status or user with this method
	 *
	 * @param pOrderId id of the order to be updated
	 * @param pOrder   where to find the new order information
	 * @return the order updated
	 * @throws NotAvailableForThisWeekException if an error occurred
	 * @throws TimeOutException                 if an error occurred
	 * @throws EntityNotFoundException          if an error occurred
	 */
	@PreAuthorize("isAuthenticated()")
	@Observed(name = "graphql.order.update", contextualName = "graphql#order#update")
	@MutationMapping
	public OrderDtoOut updateOrder(@Argument("id") Integer pOrderId, @Argument("order") OrderDtoIn pOrder)
			throws EntityNotFoundException, TimeOutException, NotAvailableForThisWeekException {

		OrderGqlController.LOG.atInfo().log("--> updateOrder - {}", pOrder);
		if (pOrder.getUserId() == null) {
			OrderGqlController.LOG.atWarn().log(
					"--- updateOrder - Order to updateOrder had no user id so will use the one who is connected {}",
					super.getConnectedUserId());
			pOrder.setUserId(super.getConnectedUserId());
		}
		var orderToUpdate = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(orderToUpdate.getUser().getId())) {
			var result = this.service.update(pOrderId, pOrder);
			OrderGqlController.LOG.atInfo().log("<-- updateOrder - order {} is updated by user {}", result.getId(),
					super.getConnectedUserId());
			return result;
		}
		OrderGqlController.LOG.atError().log("<-- updateOrder - User {} not allowed to update order {}",
				super.getConnectedUserId(), pOrderId);
		throw new InsufficientAuthenticationException("Vous n'avez pas le droit de mettre à jour cette commande!");
	}

	/**
	 * Finds an order. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to find order that you
	 * have made. <br>
	 *
	 * @param pOrderId id of the order to be found
	 * @return the order found
	 * @throws EntityNotFoundException if an error occurred
	 */
	@PreAuthorize("isAuthenticated()")
	@Observed(name = "graphql.order.byid", contextualName = "graphql#order#byid")
	@QueryMapping
	public OrderDtoOut orderById(@Argument("id") Integer pOrderId) throws EntityNotFoundException {
		OrderGqlController.LOG.atInfo().log("--> find - {}", pOrderId);
		var result = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(result.getUser().getId())) {
			OrderGqlController.LOG.atInfo().log("<-- find - Has found order {}", pOrderId);
			return result;
		}
		OrderGqlController.LOG.atError().log("<-- find - User {} not allowed to see order {}",
				super.getConnectedUserId(), pOrderId);
		throw new InsufficientAuthenticationException("Vous n'avez pas le droit de voir cette commande!");
	}

	/**
	 * Gets all orders. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @return all the orders found or an empty list if none
	 */
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	@Observed(name = "graphql.order.findall", contextualName = "graphql#order#findall")
	@QueryMapping
	public List<OrderDtoOut> findAllOrders() {
		OrderGqlController.LOG.atInfo().log("--> findAllOrders");
		var result = this.service.findAll();
		OrderGqlController.LOG.atInfo().log("<-- findAllOrders - Lunch Lady {} has found {} orders",
				super.getConnectedUserId(), result.size());
		return result;
	}

	/**
	 * Gets all orders for a specific user and the given parameters. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to find order that you
	 * have made. <br>
	 *
	 * @param pUserId    a user id. Cannot be null.
	 * @param pBeginDate a start date. If null will use 1970
	 * @param pEndDate   an end date. If null will use now
	 * @param pStatus    an order status. If null will use CREATED.
	 * @return all the orders found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.order.findallforuser", contextualName = "graphql#order#findallforuser")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public List<OrderDtoOut> findAllOrdersForUser(@Argument("userId") Integer pUserId,
			@Argument("status") OrderStatus pStatus, @Argument("beginDate") String pBeginDate,
			@Argument("endDate") String pEndDate) {

		OrderGqlController.LOG.atInfo().log("--> findAllOrdersForUser - {} {} {} {}", pUserId, pStatus, pBeginDate,
				pEndDate);
		List<OrderDtoOut> result;
		var beginDate = super.getDate(pBeginDate);
		var endDate = super.getDate(pEndDate);
		if (pStatus == null) {
			if (pBeginDate == null && pEndDate == null) {
				result = this.service.findAllByUserId(pUserId);
			} else {
				result = this.service.findAllBetweenDateForUser(pUserId, beginDate, endDate);
			}
		} else if (pBeginDate == null && pEndDate == null) {
			result = this.service.findAllForUserInStatus(pUserId, pStatus);
		} else {
			result = this.service.findAllBetweenDateForUserInStatus(pUserId, beginDate, endDate, pStatus);
		}

		OrderGqlController.LOG.atInfo().log("<-- findAllOrdersForUser - Has found {} orders", result.size());
		return result;
	}

	/**
	 * Gets all orders (not delivered nor canceled) for a specific user and today.
	 * <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to find order that you
	 * have made. <br>
	 *
	 *
	 * @param pUserId a user id. Cannot be null.
	 * @return all the orders found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.order.findallforusertoday", contextualName = "graphql#order#findallforusertoday")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public List<OrderDtoOut> findAllOrdersForUserToday(@Argument("userId") Integer pUserId) {
		OrderGqlController.LOG.atInfo().log("--> findAllOrdersForUserToday - {}", pUserId);
		var todayBegin = LocalDate.now();
		var result = this.service.findAllBetweenDateForUserInStatus(pUserId, todayBegin, todayBegin,
				OrderStatus.CREATED);
		OrderGqlController.LOG.atInfo().log("<-- findAllOrdersForUserToday - Has found {} orders", result.size());
		return result;
	}

	/**
	 * Gets all orders for all users with the given parameters. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param pBeginDate a start date. If null will use 1970
	 * @param pEndDate   an end date. If null will use now
	 * @param pStatus    an order status. If null will use CREATED.
	 * @return all the orders found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.order.findallbetweendateinstatus", contextualName = "graphql#order#findallbetweendateinstatus")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public List<OrderDtoOut> findAllOrdersBetweenDateInStatus(@Argument("status") OrderStatus pStatus,
			@Argument("beginDate") String pBeginDate, @Argument("endDate") String pEndDate) {
		OrderGqlController.LOG.atInfo().log("--> findAllOrdersBetweenDateInStatus - {} {} {}", pStatus, pBeginDate,
				pEndDate);
		var beginDate = super.getDate(pBeginDate);
		var endDate = super.getDate(pEndDate);
		var result = this.service.findAllBetweenDateInStatus(beginDate, endDate, pStatus);
		OrderGqlController.LOG.atInfo().log("<-- findAllOrdersBetweenDateInStatus - Lunch lady {} has found {} orders",
				super.getConnectedUserId(), result.size());
		return result;
	}
}
