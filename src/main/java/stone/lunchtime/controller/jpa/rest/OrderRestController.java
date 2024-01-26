// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.out.ExceptionDtoOut;
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
@RestController
@RequestMapping("/order")
@Tag(name = "Order management API", description = "Order management API")
public class OrderRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(OrderRestController.class);

	private final IOrderService<OrderEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public OrderRestController(IOrderService<OrderEntity> pService) {
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
	@PutMapping("/add")
	@Observed(name = "rest.order.add", contextualName = "rest#order#add")
	@Operation(tags = {
			"Order management API" }, summary = "Adds an order.", description = "Will add an order into the data base. Will return it with its id when done. You must be connected in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your order was added and returned in the response body.", content = @Content(schema = @Schema(implementation = OrderDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your order is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The meal or menu in this order is not available for this week or it is too late regarding the constraint's maximum time or this order is referencing invalid elements.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<OrderDtoOut> addOrder(
			@Parameter(description = "Order object that will be stored in database. Linked to a constraint's id that will be used for timeout here.", required = true) @RequestBody OrderDtoIn pOrder)
			throws TimeOutException, NotAvailableForThisWeekException, EntityNotFoundException {

		OrderRestController.LOG.atInfo().log("--> addOrder - {}", pOrder);
		if (pOrder.getUserId() == null) {
			OrderRestController.LOG.atWarn().log(
					"--- addOrder - New order had no user id so will use the one who is connected {}",
					super.getConnectedUserId());
			pOrder.setUserId(super.getConnectedUserId());
		}
		var result = this.service.order(pOrder);
		OrderRestController.LOG.atInfo().log("<-- addOrder - New order made by {} has id {}", super.getConnectedUserId(),
				result.getId());
		return ResponseEntity.ok(result);
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
	@PatchMapping("/deliverandpay/{orderId}/{constraintId}")
	@Observed(name = "rest.order.pay", contextualName = "rest#order#pay")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Order management API" }, summary = "Pays an order.", description = "Will pays an order into the data base. Will take money from user who ordered it and change the order status to DELIVRED(1). Will return it with its id when done. You must be connected and have the Lunch Lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your order was added and returned in the response body.", content = @Content(schema = @Schema(implementation = OrderDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your orderId or constraintId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "Your order is not in CREATED(0) state or the user has not enought money in its wallet or this order is referencing invalid elements.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<OrderDtoOut> payOrder(
			@Parameter(description = "Order's id that need to be delivred.", required = true) @PathVariable("orderId") Integer pOrderId,
			@Parameter(description = "Constraint's id that will be used for computing prices. May be -1 for no constraint, means DF only.", required = true) @PathVariable(required = false, name = "constraintId") Integer pConstraintId)
			throws EntityNotFoundException, LackOfMoneyException, OrderCanceledException, OrderDelivredException {

		OrderRestController.LOG.atInfo().log("--> payOrder - {} with constraint {}", pOrderId, pConstraintId);
		var result = this.service.deliverAndPay(pOrderId, pConstraintId);
		OrderRestController.LOG.atInfo().log("<-- payOrder - order {} is payed by lunch lady {}", result.getId(),
				super.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@GetMapping("/computeprice/{orderId}/{constraintId}")
	@Observed(name = "rest.order.computeprice", contextualName = "rest#order#computeprice")
	@Operation(tags = {
			"Order management API" }, summary = "Compute the prices (DF and VAT) of the order.", description = "Will compute the prices of an order (passed by you). Will not take money from user who ordered it nor change the order status. Will return it with its id when done. You must be connected, only Lunch Lady role can compute all orders, other wise you'll be able to compute your orders only.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Prices of the order was computed and are in the response body.", content = @Content(schema = @Schema(implementation = PriceDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your orderId or constraintId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot compute the specified order (because you are not a lunch lady or it is not your order).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "Your order was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<PriceDtoOut> computeOrderPrice(
			@Parameter(description = "Order's id.", required = true) @PathVariable("orderId") Integer pOrderId,
			@Parameter(description = "Constraint's id that will be used for computing prices. May be -1 for no constraint, means DF only.", required = true) @PathVariable(required = false, name = "constraintId") Integer pConstraintId)
			throws EntityNotFoundException {

		OrderRestController.LOG.atInfo().log("--> computeOrderPrice - {} with constraint {}", pOrderId, pConstraintId);
		var order = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(order.getUser().getId())) {
			var dtoOut = new PriceDtoOut();
			this.service.computePrice(pOrderId, pConstraintId, dtoOut);
			OrderRestController.LOG.atInfo().log("<-- computeOrderPrice - order {} has a price of {}", pOrderId, dtoOut);
			return ResponseEntity.ok(dtoOut);
		}
		OrderRestController.LOG.error("<-- computeOrderPrice - User {} not allowed to compute Price for order {}",
				super.getConnectedUserId(), pOrderId);
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
	@PatchMapping("/cancel/{orderId}")
	@Observed(name = "rest.order.cancel", contextualName = "rest#order#cancel")
	@Operation(tags = {
			"Order management API" }, summary = "Cancels an order.", description = "Will cancel an order (passed by you). Will change the order status, it will pass into CANCELED(2). Will return it when done. You must be connected, only Lunch Lady role can cancel any orders, other wise you'll be able to cancel your orders only.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Order was canceled and is in the response body.", content = @Content(schema = @Schema(implementation = OrderDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your orderId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot cancel the specified order (because you are not a lunch lady or it is not your order).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "Your order was not found or is not in the correct state.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<OrderDtoOut> cancelOrder(
			@Parameter(description = "Order's id.", required = true) @PathVariable("orderId") Integer pOrderId)
			throws EntityNotFoundException, OrderCanceledException, OrderDelivredException {

		OrderRestController.LOG.atInfo().log("--> cancelOrder - {}", pOrderId);
		var orderToCancel = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(orderToCancel.getUser().getId())) {
			var result = this.service.cancel(pOrderId);
			OrderRestController.LOG.atInfo().log("<-- cancelOrder - order {} is cancel by user {}", pOrderId,
					super.getConnectedUserId());
			return ResponseEntity.ok(result);
		}
		OrderRestController.LOG.error("<-- cancelOrder - User {} not allowed to cancel command {}",
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
	@PatchMapping("/update/{orderId}")
	@Observed(name = "rest.order.update", contextualName = "rest#order#update")
	@Operation(tags = {
			"Order management API" }, summary = "Updates an order.", description = "Will update an order (passed by you). Will change all available values except status and user responsible of the order. Will return it when done. You must be connected, only Lunch Lady role can update any orders, other wise you'll be able to update your orders only.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Order was updated and is in the response body.", content = @Content(schema = @Schema(implementation = OrderDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your orderId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot cancel the specified order (because you are not a lunch lady or it is not your order).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The meal or menu in this order is not available for this week or it is too late regarding the constraint's maximum time or this order is referencing invalid elements.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<OrderDtoOut> updateOrder(
			@Parameter(description = "Order's id.", required = true) @PathVariable("orderId") Integer pOrderId,
			@Parameter(description = "Order object that will be updated in database. All present values will be updated. You cannot change status nor user responsible of the order.", required = true) @RequestBody OrderDtoIn pOrder)
			throws EntityNotFoundException, TimeOutException, NotAvailableForThisWeekException {

		OrderRestController.LOG.atInfo().log("--> updateOrder - {}", pOrder);
		if (pOrder.getUserId() == null) {
			OrderRestController.LOG.atWarn().log(
					"--- updateOrder - Order to update had no user id so will use the one who is connected {}",
					super.getConnectedUserId());
			pOrder.setUserId(super.getConnectedUserId());
		}
		var orderToUpdate = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(orderToUpdate.getUser().getId())) {
			var result = this.service.update(pOrderId, pOrder);
			OrderRestController.LOG.atInfo().log("<-- updateOrder - order {} is updated by user {}", result.getId(),
					super.getConnectedUserId());
			return ResponseEntity.ok(result);
		}
		OrderRestController.LOG.error("<-- updateOrder - User {} not allowed to update order {}",
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
	@GetMapping("/find/{orderId}")
	@Observed(name = "rest.order.byid", contextualName = "rest#order#byid")
	@Operation(tags = {
			"Order management API" }, summary = "Finds one order.", description = "Will find an order already present in the data base (passed by you). Will return it when done. You must be connected, only Lunch Lady role can find any orders, other wise you'll be able to find your orders only.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your order was found and returned in the response body.", content = @Content(schema = @Schema(implementation = OrderDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your orderId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "Your are not connected or not allowed to see this order (because you are not a lunch lady or it is not your order).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<OrderDtoOut> orderById(
			@Parameter(description = "Order's id.", required = true) @PathVariable("orderId") Integer pOrderId)
			throws EntityNotFoundException {

		OrderRestController.LOG.atInfo().log("--> orderById - {}", pOrderId);
		var result = this.service.find(pOrderId);
		if (super.hasLunchLadyRole() || super.getConnectedUserId().equals(result.getUser().getId())) {
			OrderRestController.LOG.atInfo().log("<-- orderById - Has found order {}", pOrderId);
			return ResponseEntity.ok(result);
		}
		OrderRestController.LOG.error("<-- orderById - User {} not allowed to see order {}", super.getConnectedUserId(),
				pOrderId);
		throw new InsufficientAuthenticationException("Vous n'avez pas le droit de voir cette commande!");
	}

	/**
	 * Gets all orders. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 *
	 * @return all the orders found or an empty list if none
	 */
	@GetMapping("/findall")
	@Observed(name = "rest.order.findall", contextualName = "rest#order#findall")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Order management API" }, summary = "Finds all orders.", description = "Will find all orders already present in the data base. Will return them when done. You must be connected and have the Lunch Lady role.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All orders was found and returned in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<OrderDtoOut>> findAllOrders() {

		OrderRestController.LOG.atInfo().log("--> findAllOrders");
		var result = this.service.findAll();
		OrderRestController.LOG.atInfo().log("<-- findAllOrders - Lunch Lady {} has found {} orders",
				super.getConnectedUserId(), result.size());
		return ResponseEntity.ok(result);
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
	@GetMapping("/findallforuser/{userId}")
	@Observed(name = "rest.order.findallforuser", contextualName = "rest#order#findallforuser")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Order management API" }, summary = "Finds all orders made by a user and matching the criteria.", description = "Will find all orders already present in the data base made by a specific user and matching criteria. Will return them when done. You must be connected, you can retreive all your orders or if you have the Lunch Lady role any orders made by any users.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All orders was found and returned in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "Your are not connected or not allowed to see this order (because you are not a lunch lady or it is not your order).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<OrderDtoOut>> findAllOrdersForUser(
			@Parameter(description = "User's id.", required = true) @PathVariable("userId") Integer pUserId,
			@Parameter(description = "An order status. CREATED(0), DELIVERED(1), CANCELED(2)", allowEmptyValue = true) @RequestParam(required = false, name = "status") OrderStatus pStatus,
			@Parameter(description = "A start date. Format is linked with option configuration.date.pattern in application.properties file.", allowEmptyValue = true) @RequestParam(required = false, name = "beginDate") String pBeginDate,
			@Parameter(description = "An end date. Format is linked with option configuration.date.pattern in application.properties file.", allowEmptyValue = true) @RequestParam(required = false, name = "endDate") String pEndDate) {

		OrderRestController.LOG.atInfo().log("--> findAllOrdersForUser - {} {} {} {}", pUserId, pStatus, pBeginDate, pEndDate);
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

		OrderRestController.LOG.atInfo().log("<-- findAllOrdersForUser - Has found {} orders", result.size());
		return ResponseEntity.ok(result);
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
	@GetMapping("/findallforusertoday/{userId}")
	@Observed(name = "rest.order.findallforusertoday", contextualName = "rest#order#findallforusertoday")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Order management API" }, summary = "Finds all orders made by a user.", description = "Will find all orders already present in the data base made by a specific user. Will return them when done. You must be connected, you can retreive all your orders or if you have the Lunch Lady role any orders made by any users.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All orders was found and returned in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "Your are not connected or not allowed to see this order (because you are not a lunch lady or it is not your order).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<OrderDtoOut>> findAllOrdersForUserToday(
			@Parameter(description = "User's id.", required = true) @PathVariable("userId") Integer pUserId) {
		OrderRestController.LOG.atInfo().log("--> findAllOrdersForUserToday - {}", pUserId);
		var todayBegin = LocalDate.now();
		var result = this.service.findAllBetweenDateForUserInStatus(pUserId, todayBegin, todayBegin,
				OrderStatus.CREATED);
		OrderRestController.LOG.atInfo().log("<-- findAllOrdersForUserToday - Has found {} orders", result.size());
		return ResponseEntity.ok(result);
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
	@GetMapping("/findallbetweendateinstatus")
	@Observed(name = "rest.order.findallbetweendateinstatus", contextualName = "rest#order#findallbetweendateinstatus")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Order management API" }, summary = "Finds all orders matching criteria.", description = "Will find all orders already present in the data base and matching criteria. Will return them when done. You must be connected and have the Lunch Lady role.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All orders was found and returned in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<OrderDtoOut>> findAllOrdersBetweenDateInStatus(
			@Parameter(description = "An order status. CREATED(0), DELIVERED(1), CANCELED(2)", allowEmptyValue = true) @RequestParam(required = false, name = "status") OrderStatus pStatus,
			@Parameter(description = "A start date. Format is linked with option configuration.date.pattern in application.properties file.", allowEmptyValue = true) @RequestParam(required = false, name = "beginDate") String pBeginDate,
			@Parameter(description = "An end date. Format is linked with option configuration.date.pattern in application.properties file.", allowEmptyValue = true) @RequestParam(required = false, name = "endDate") String pEndDate) {

		OrderRestController.LOG.atInfo().log("--> findAllOrdersBetweenDateInStatus - {} {} {}", pStatus, pBeginDate, pEndDate);
		var beginDate = super.getDate(pBeginDate);
		var endDate = super.getDate(pEndDate);
		var result = this.service.findAllBetweenDateInStatus(beginDate, endDate, pStatus);
		OrderRestController.LOG.atInfo().log("<-- findAllOrdersBetweenDateInStatus - Lunch lady {} has found {} orders",
				super.getConnectedUserId(), result.size());
		return ResponseEntity.ok(result);
	}
}
