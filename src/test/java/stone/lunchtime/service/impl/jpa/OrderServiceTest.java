// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stone.lunchtime.AbstractJpaTest;
import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.in.QuantityDtoIn;
import stone.lunchtime.dto.jpa.handler.OrderDtoHandler;
import stone.lunchtime.dto.jpa.handler.QuantityDtoHandler;
import stone.lunchtime.dto.out.MealDtoOut;
import stone.lunchtime.dto.out.OrderDtoOut;
import stone.lunchtime.dto.out.PriceDtoOut;
import stone.lunchtime.dto.out.QuantityDtoOut;
import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.service.exception.LackOfMoneyException;
import stone.lunchtime.service.exception.OrderCanceledException;
import stone.lunchtime.service.exception.OrderDelivredException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.service.exception.TimeOutException;

/**
 * Test class for order service.
 */
class OrderServiceTest extends AbstractJpaTest {

	private final static int THIS_WEEK = OrderService.getCurrentWeekId();

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testOrder01() throws Exception {
		var user = super.findASimpleUser();
		var allMenus = this.menuService.findAllAvailableForWeek(OrderServiceTest.THIS_WEEK);
		// Remove constraint
		final var constraintId = Integer.valueOf(-1);
		var dto = new OrderDtoIn();
		dto.setUserId(user.getId());
		dto.setConstraintId(constraintId);
		// Add a menu to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		var q = new QuantityDtoIn(1, null, allMenus.get(0).getId());
		qs.add(q);
		dto.setQuantity(qs);

		var result = this.orderService.order(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isCreated(), "Result must have the correct status");
		Assertions.assertNotNull(result.getQuantity(), "Result must have some quantity");
		Assertions.assertEquals(1, result.getQuantity().size(), "Result must have one quantity");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testOrder02() throws Exception {
		var allMeals = this.mealService.findAllAvailableForWeekAndCategory(OrderServiceTest.THIS_WEEK, null);
		var user = super.findASimpleUser();
		var allMenus = this.menuService.findAllAvailableForWeek(OrderServiceTest.THIS_WEEK);
		// Remove constraint
		final var constraintId = Integer.valueOf(-1);
		var dto = new OrderDtoIn();
		dto.setUserId(user.getId());
		dto.setConstraintId(constraintId);

		// Add a menu to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		var q = new QuantityDtoIn(1, null, allMenus.get(0).getId());
		qs.add(q);
		// Add some meals to the order
		List<MealDtoOut> someMeals = super.generateList(3, allMeals);
		for (MealDtoOut lMealDtoOut : someMeals) {
			var qp = new QuantityDtoIn(1, lMealDtoOut.getId(), null);
			qs.add(qp);
		}

		dto.setQuantity(qs);
		var result = this.orderService.order(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isCreated(), "Result must have the correct status");
		Assertions.assertNotNull(result.getQuantity(), "Result must have some quantity");
		Assertions.assertEquals(1 + someMeals.size(), result.getQuantity().size(), "Result must have correct quantity");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testOrder03() throws Exception {
		var allMeals = this.mealService.findAllAvailableForWeekAndCategory(OrderServiceTest.THIS_WEEK, null);
		var user = super.findASimpleUser();
		// Remove constraint
		final var constraintId = Integer.valueOf(-1);
		var dto = new OrderDtoIn();
		dto.setUserId(user.getId());
		dto.setConstraintId(constraintId);
		// Add some meals to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		List<MealDtoOut> someMeals = super.generateList(3, allMeals);
		for (MealDtoOut lMealDtoOut : someMeals) {
			var qp = new QuantityDtoIn(1, lMealDtoOut.getId(), null);
			qs.add(qp);
		}
		dto.setQuantity(qs);

		var result = this.orderService.order(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isCreated(), "Result must have the correct status");
		Assertions.assertNotNull(result.getQuantity(), "Result must have some quantity");
		Assertions.assertEquals(someMeals.size(), result.getQuantity().size(), "Result must have correct quantity");
	}

	/**
	 * Test
	 */
	@Test
	void testOrder04() {
		Assertions.assertThrows(ParameterException.class, () -> this.orderService.order(null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testOrder05() throws Exception {
		// update constraint for testing
		var ce = super.constraintService.findEntity(Integer.valueOf(1));
		Assertions.assertNotNull(ce, "Result must exist");
		ce.setOrderTimeLimit(LocalTime.now().plusMinutes(20));
		super.constraintDao.save(ce);

		var allMeals = this.mealService.findAllAvailableForWeekAndCategory(OrderServiceTest.THIS_WEEK, null);
		var user = super.findASimpleUser();
		// Remove constraint
		var dto = new OrderDtoIn();
		dto.setUserId(user.getId());
		dto.setConstraintId(null); // Set to null here <=> will be set to 1
		// Add some meals to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		List<MealDtoOut> someMeals = super.generateList(3, allMeals);
		for (MealDtoOut lMealDtoOut : someMeals) {
			var qp = new QuantityDtoIn(1, lMealDtoOut.getId(), null);
			qs.add(qp);
		}
		dto.setQuantity(qs);

		var result = this.orderService.order(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertTrue(result.isCreated(), "Result must have the correct status");
		Assertions.assertNotNull(result.getQuantity(), "Result must have some quantity");
		Assertions.assertEquals(someMeals.size(), result.getQuantity().size(), "Result must have correct quantity");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testOrder06() throws Exception {
		// update constraint for testing
		var ce = super.constraintService.findEntity(Integer.valueOf(1));
		Assertions.assertNotNull(ce, "Result must exist");
		ce.setOrderTimeLimit(LocalTime.now().minusMinutes(20));
		super.constraintDao.save(ce);

		var allMeals = this.mealService.findAllAvailableForWeekAndCategory(OrderServiceTest.THIS_WEEK, null);
		var user = super.findASimpleUser();
		var dto = new OrderDtoIn();
		dto.setUserId(user.getId());
		dto.setConstraintId(ce.getId());
		// Add some meals to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		List<MealDtoOut> someMeals = super.generateList(3, allMeals);
		for (MealDtoOut lMealDtoOut : someMeals) {
			var qp = new QuantityDtoIn(1, lMealDtoOut.getId(), null);
			qs.add(qp);
		}
		dto.setQuantity(qs);
		Assertions.assertThrows(TimeOutException.class, () -> this.orderService.order(dto));
	}

	private OrderDtoOut createAnOrder(Integer aConstraintId) throws Exception {
		var user = super.findASimpleUser();
		var allMenus = this.menuService.findAllAvailableForWeek(OrderServiceTest.THIS_WEEK);
		var dto = new OrderDtoIn();
		dto.setUserId(user.getId());
		dto.setConstraintId(aConstraintId);
		// Add a menu to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		var q = new QuantityDtoIn(1, null, allMenus.get(0).getId());
		qs.add(q);
		dto.setQuantity(qs);
		return this.orderService.order(dto);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDeliverAndPay01() throws Exception {
		final var constraintId = Integer.valueOf(-1);
		// Give money to user first, in case
		var order = this.createAnOrder(constraintId);
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		var user = order.getUser();
		user = this.userService.credit(user.getId(), BigDecimal.valueOf(500D));
		Assertions.assertTrue(user.getWallet().doubleValue() > 0, "User must have money");
		order = this.orderService.deliverAndPay(order.getId(), constraintId);
		Assertions.assertNotNull(order, "Result must exist");
		Assertions.assertTrue(order.isDelivered(), "Result must have the correct status");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDeliverAndPay02() throws Exception {
		final var constraintId = Integer.valueOf(-1);
		// Remove money to user first
		var order = this.createAnOrder(constraintId);
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		var user = order.getUser();
		if (user.getWallet().doubleValue() > 0) {
			this.userService.debit(user.getId(), BigDecimal.valueOf(user.getWallet().doubleValue() - 0.01D));
		}
		Assertions.assertThrows(LackOfMoneyException.class,
				() -> this.orderService.deliverAndPay(order.getId(), constraintId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDeliverAndPay03() throws Exception {
		final var constraintId = Integer.valueOf(-1);
		// cancel command first
		var order = this.createAnOrder(constraintId);
		var canceledOrder = this.orderService.cancel(order.getId());
		Assertions.assertTrue(canceledOrder.isCanceled(), "Result must have the correct status");
		Assertions.assertThrows(OrderCanceledException.class,
				() -> this.orderService.deliverAndPay(canceledOrder.getId(), constraintId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDeliverAndPay04() throws Exception {
		final var constraintId = Integer.valueOf(-1);
		// Give money to user first, in case
		var order = this.createAnOrder(constraintId);
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		var user = order.getUser();
		this.userService.credit(user.getId(), BigDecimal.valueOf(500D));
		var deliveredOrder = this.orderService.deliverAndPay(order.getId(), constraintId);
		Assertions.assertNotNull(deliveredOrder, "Result must exist");
		Assertions.assertTrue(deliveredOrder.isDelivered(), "Result must have the correct status");
		// Try to cancel
		final var deliveredId = deliveredOrder.getId();
		Assertions.assertThrows(OrderDelivredException.class, () -> this.orderService.cancel(deliveredId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDeliverAndPay05() throws Exception {
		final var constraintId = Integer.valueOf(-1);
		// Give money to user first, in case
		var order = this.createAnOrder(constraintId);
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		var user = order.getUser();
		this.userService.credit(user.getId(), BigDecimal.valueOf(500D));
		var deliveredOrder = this.orderService.deliverAndPay(order.getId(), constraintId);
		Assertions.assertNotNull(deliveredOrder, "Result must exist");
		Assertions.assertTrue(deliveredOrder.isDelivered(), "Result must have the correct status");
		// Try to deliver again
		Assertions.assertThrows(OrderDelivredException.class,
				() -> this.orderService.deliverAndPay(deliveredOrder.getId(), constraintId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate01() throws Exception {
		var allOrders = this.orderService.findAll();
		final var constraintId = Integer.valueOf(-1);
		var index = 0;
		var order = allOrders.get(index);
		while (order.getQuantity() == null || order.getQuantity().isEmpty()) {
			order = allOrders.get(index);
			index++;
		}
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertNotNull(order.getQuantity(), "Result must be have quantities");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must be have quantities");
		final var initialSize = order.getQuantity().size();
		var orderE = this.orderService.findEntity(order.getId());
		var dtoIn = OrderDtoHandler.dtoInfromEntity(orderE, constraintId);
		var allMenus = this.menuService.findAll();
		// Add a menu to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		var q = new QuantityDtoIn(1, null, allMenus.get(0).getId());
		qs.add(q);
		// re-add all other quantities
		for (QuantityDtoOut qe : order.getQuantity()) {
			qs.add(QuantityDtoHandler.dtoInfromDtoOut(qe));
		}
		dtoIn.setQuantity(qs);

		order = this.orderService.update(orderE.getId(), dtoIn);
		Assertions.assertNotNull(order, "Result must exist");
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertNotNull(order.getQuantity(), "Result must have some quantity");
		Assertions.assertEquals(initialSize + 1, order.getQuantity().size(), "Result must have correct quantity");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate02() throws Exception {
		var allOrders = this.orderService.findAll();
		final var constraintId = Integer.valueOf(-1);
		var index = 0;
		var order = allOrders.get(index);
		while (order.getQuantity().isEmpty()) {
			order = allOrders.get(index);
			index++;
		}
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must have meals");
		// Change meal - change quantity
		final var oldPrice = this.orderService.computePrice(order.getId(), constraintId, new PriceDtoOut());
		var orderE = this.orderService.findEntity(order.getId());
		var dtoIn = OrderDtoHandler.dtoInfromEntity(orderE, constraintId);
		var qps = dtoIn.getQuantity();
		var qpo = qps.get(0);
		qpo.setQuantity(Integer.valueOf(qpo.getQuantity().intValue() + 10));

		order = this.orderService.update(order.getId(), dtoIn);
		Assertions.assertNotNull(order, "Result must exist");
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must have meals");
		final var newPrice = this.orderService.computePrice(order.getId(), constraintId, new PriceDtoOut());
		Assertions.assertTrue(newPrice.doubleValue() > oldPrice.doubleValue(), "Price should be bigger");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate03() throws Exception {
		var allOrders = this.orderService.findAll();
		var allMeals = this.mealService.findAll();
		final var constraintId = Integer.valueOf(-1);
		var index = 0;
		var order = allOrders.get(index);
		while (order.getQuantity().isEmpty()) {
			order = allOrders.get(index);
			index++;
		}
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must have meals");
		// Change meal - Add one
		final var oldPrice = this.orderService.computePrice(order.getId(), constraintId, new PriceDtoOut());
		final var oldNbMeal = order.getQuantity().size();
		var orderE = this.orderService.findEntity(order.getId());
		var dtoIn = OrderDtoHandler.dtoInfromEntity(orderE, constraintId);
		var qps = dtoIn.getQuantity();
		qps.add(new QuantityDtoIn(1, allMeals.get(0).getId(), null));

		order = this.orderService.update(order.getId(), dtoIn);
		Assertions.assertNotNull(order, "Result must exist");
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must have meals");
		final var newPrice = this.orderService.computePrice(order.getId(), constraintId, new PriceDtoOut());
		Assertions.assertTrue(newPrice.doubleValue() > oldPrice.doubleValue(), "Price should be bigger");
		final var newNbMeal = order.getQuantity().size();
		Assertions.assertTrue(newNbMeal > oldNbMeal, "Nb meal should be bigger");
		Assertions.assertEquals(oldNbMeal + 1, newNbMeal, "Nb meal should +1 bigger");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate04() throws Exception {
		var allOrders = this.orderService.findAll();
		final var constraintId = Integer.valueOf(-1);
		var index = 0;
		var order = allOrders.get(index);
		while (order.getQuantity() == null || order.getQuantity().size() < 2) {
			order = allOrders.get(index);
			index++;
		}
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must have meals");
		// Change meal - Remove one
		final var oldPrice = this.orderService.computePrice(order.getId(), constraintId, new PriceDtoOut());
		final var oldNbMeal = order.getQuantity().size();
		var orderE = this.orderService.findEntity(order.getId());
		var dtoIn = OrderDtoHandler.dtoInfromEntity(orderE, constraintId);
		var qps = dtoIn.getQuantity();
		qps.remove(0);

		order = this.orderService.update(order.getId(), dtoIn);
		Assertions.assertNotNull(order, "Result must exist");
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertFalse(order.getQuantity().isEmpty(), "Result must have meals");
		final var newPrice = this.orderService.computePrice(order.getId(), constraintId, new PriceDtoOut());
		Assertions.assertTrue(newPrice.doubleValue() < oldPrice.doubleValue(), "Price should be smaller");
		final var newNbMeal = order.getQuantity().size();
		Assertions.assertTrue(newNbMeal < oldNbMeal, "Nb meal should be smaller");
		Assertions.assertEquals(oldNbMeal - 1, newNbMeal, "Nb meal should -1 smaller");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate07() throws Exception {
		var allOrders = this.orderService.findAll();
		final var constraintId = Integer.valueOf(-1);
		var index = 0;
		var order = allOrders.get(index);
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		final var userId = order.getUser().getId();
		var orderE = this.orderService.findEntity(order.getId());
		var dtoIn = OrderDtoHandler.dtoInfromEntity(orderE, constraintId);
		// Change user id
		dtoIn.setUserId(super.findASimpleUser(userId).getId()); // Changing user id should have no effect (except a log)
																// (must be a real one)
		order = this.orderService.update(order.getId(), dtoIn);
		Assertions.assertNotNull(order, "Result must exist");
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		Assertions.assertEquals(userId, order.getUser().getId(), "Result must have the correct user id");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate08() throws Exception {
		var ce = super.constraintService.findEntity(Integer.valueOf(1));
		Assertions.assertNotNull(ce, "Result must exist");

		var allOrders = this.orderService.findAll();
		final var constraintId = ce.getId();
		var index = 0;
		var order = allOrders.get(index);
		Assertions.assertTrue(order.isCreated(), "Result must have the correct status");
		// update constraint for testing
		ce.setOrderTimeLimit(order.getCreationTime().minusMinutes(20));
		super.constraintDao.save(ce);

		var orderE = this.orderService.findEntity(order.getId());
		var dtoIn = OrderDtoHandler.dtoInfromEntity(orderE, constraintId);
		final var orderId = order.getId();
		var allMenus = this.menuService.findAllAvailableForWeek(OrderServiceTest.THIS_WEEK);
		// Add a menu to the order
		List<QuantityDtoIn> qs = new ArrayList<>();
		var q = new QuantityDtoIn(1, null, allMenus.get(0).getId());
		qs.add(q);
		dtoIn.setQuantity(qs);

		Assertions.assertThrows(TimeOutException.class, () -> this.orderService.update(orderId, dtoIn));
	}

	/**
	 * Test
	 */
	@Test
	void findAllByUserId01() {
		var userId = super.findASimpleUser().getId();
		var orders = super.orderService.findAllByUserId(userId);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertNotNull(lOrderEntity.getUser(), "Orders must have a user");
			Assertions.assertEquals(userId, lOrderEntity.getUser().getId(), "Orders must have the correct user id");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllByUserId02() {
		Assertions.assertThrows(ParameterException.class, () -> super.orderService.findAllByUserId(null));
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateInStatus01() {
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = beginDate.minusDays(1);
		final var status = OrderStatus.CREATED;
		Assertions.assertThrows(ParameterException.class,
				() -> super.orderService.findAllBetweenDateInStatus(beginDate, endDate, status));
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateInStatus02() {
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = LocalDate.now().plusYears(1);
		final var status = OrderStatus.CREATED;
		var orders = super.orderService.findAllBetweenDateInStatus(beginDate, endDate, status);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertEquals(status, lOrderEntity.getStatus(), "Orders must have the correct status");
			Assertions.assertTrue(beginDate.isBefore(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
			Assertions.assertTrue(endDate.isAfter(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateInStatus03() {
		final var endDate = LocalDate.now().plusYears(1);
		final var status = OrderStatus.CREATED;
		var orders = super.orderService.findAllBetweenDateInStatus(null, endDate, status);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertEquals(status, lOrderEntity.getStatus(), "Orders must have the correct status");
			Assertions.assertTrue(endDate.isAfter(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateInStatus04() {
		final var beginDate = LocalDate.now().minusYears(20);
		final var status = OrderStatus.CREATED;
		var orders = super.orderService.findAllBetweenDateInStatus(beginDate, null, status);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertEquals(status, lOrderEntity.getStatus(), "Orders must have the correct status");
			Assertions.assertTrue(beginDate.isBefore(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateInStatus05() {
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = LocalDate.now().plusYears(1);
		var orders = super.orderService.findAllBetweenDateInStatus(beginDate, endDate, null);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertEquals(OrderStatus.CREATED, lOrderEntity.getStatus(),
					"Orders must have the correct status");
			Assertions.assertTrue(beginDate.isBefore(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
			Assertions.assertTrue(endDate.isAfter(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateForUser01() {
		final var userId = super.findASimpleUser().getId();
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = LocalDate.now().plusYears(1);
		var orders = super.orderService.findAllBetweenDateForUser(userId, beginDate, endDate);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertEquals(userId, lOrderEntity.getUser().getId(), "Orders must have the correct status");
			Assertions.assertTrue(beginDate.isBefore(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
			Assertions.assertTrue(endDate.isAfter(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateForUser02() {
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = LocalDate.now().plusYears(1);
		Assertions.assertThrows(ParameterException.class,
				() -> super.orderService.findAllBetweenDateForUser(null, beginDate, endDate));
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateForUser03() {
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = beginDate.minusDays(1);
		Assertions.assertThrows(ParameterException.class,
				() -> super.orderService.findAllBetweenDateForUser(null, beginDate, endDate));
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateForUserInStatus01() {
		final var userId = super.findASimpleUser().getId();
		final var status = OrderStatus.CREATED;
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = LocalDate.now().plusYears(1);
		var orders = super.orderService.findAllBetweenDateForUserInStatus(userId, beginDate, endDate, status);
		Assertions.assertNotNull(orders, "Orders must exists");
		for (OrderDtoOut lOrderEntity : orders) {
			Assertions.assertEquals(status, lOrderEntity.getStatus(), "Orders must have the correct status");
			Assertions.assertEquals(userId, lOrderEntity.getUser().getId(), "Orders must have the correct status");
			Assertions.assertTrue(beginDate.isBefore(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
			Assertions.assertTrue(endDate.isAfter(lOrderEntity.getCreationDate()),
					"Orders must have the correct creation date");
		}
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateForUserInStatus02() {
		final var status = OrderStatus.CREATED;
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = LocalDate.now().plusYears(1);
		Assertions.assertThrows(ParameterException.class,
				() -> super.orderService.findAllBetweenDateForUserInStatus(null, beginDate, endDate, status));
	}

	/**
	 * Test
	 */
	@Test
	void findAllBetweenDateForUserInStatus03() {
		final var userId = super.findASimpleUser().getId();
		final var status = OrderStatus.CREATED;
		final var beginDate = LocalDate.now().minusYears(20);
		final var endDate = beginDate.minusDays(1);
		Assertions.assertThrows(ParameterException.class,
				() -> super.orderService.findAllBetweenDateForUserInStatus(userId, beginDate, endDate, status));
	}
}
