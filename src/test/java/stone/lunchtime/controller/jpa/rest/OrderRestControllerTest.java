// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.JavaType;

import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.in.QuantityDtoIn;
import stone.lunchtime.dto.jpa.handler.OrderDtoHandler;
import stone.lunchtime.dto.out.OrderDtoOut;
import stone.lunchtime.dto.out.PriceDtoOut;
import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Test for order controller, using Mock.
 */
class OrderRestControllerTest extends AbstractJpaWebTest {
	private static final String URL_ROOT = "/order";
	private static final String URL_FINDALLBETWEENDATEINSTATUS = OrderRestControllerTest.URL_ROOT
			+ "/findallbetweendateinstatus";
	private static final String URL_FINDALLFORUSER = OrderRestControllerTest.URL_ROOT + "/findallforuser/";
	private static final String URL_FINDALLFORUSERTODAY = OrderRestControllerTest.URL_ROOT + "/findallforusertoday/";
	private static final String URL_ADD = OrderRestControllerTest.URL_ROOT + "/add";
	private static final String URL_CANCEL = OrderRestControllerTest.URL_ROOT + "/cancel/";
	private static final String URL_COMPUTEPRICE = OrderRestControllerTest.URL_ROOT + "/computeprice/";
	private static final String URL_FIND = OrderRestControllerTest.URL_ROOT + "/find/";
	private static final String URL_FINDALL = OrderRestControllerTest.URL_ROOT + "/findall";
	private static final String URL_PAY = OrderRestControllerTest.URL_ROOT + "/deliverandpay/";
	private static final String URL_UPDATE = OrderRestControllerTest.URL_ROOT + "/update/";

	@Value("${configuration.date.pattern}")
	private String datePattern;

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindAllBetweenDateInStatus01() throws Exception {
		final var status = OrderStatus.CREATED;

		var begin = LocalDate.of(2019, Month.JANUARY, 1);
		var end = LocalDate.now();

		final var beginDate = begin.format(DateTimeFormatter.ofPattern(this.datePattern));
		final var endDate = end.format(DateTimeFormatter.ofPattern(this.datePattern));

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders
				.get(OrderRestControllerTest.URL_FINDALLBETWEENDATEINSTATUS)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)).param("status", String.valueOf(status))
				.param("beginDate", beginDate).param("endDate", endDate));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(status, dto.getStatus(), "Order status is as searched");
			var creationDate = dto.getCreationDate();
			Assertions.assertTrue(creationDate.isEqual(end) || creationDate.isBefore(end),
					"Order date is before the specified end date");
			Assertions.assertTrue(creationDate.isEqual(begin) || creationDate.isAfter(begin),
					"Order date is after the specified begin date");
		}
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindAllBetweenDateInStatus02() throws Exception {
		final var status = OrderStatus.CREATED;
		final var beginDate = "2019-01-01";
		final var endDate = "2019-05-01";

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders
				.get(OrderRestControllerTest.URL_FINDALLBETWEENDATEINSTATUS)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)).param("status", String.valueOf(status))
				.param("beginDate", beginDate).param("endDate", endDate));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testAdd01() throws Exception {

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.put(OrderRestControllerTest.URL_ADD)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CREATED, dtoOut.getStatus(), "Order status must created");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, dtoOut.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testAdd02() throws Exception {

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with quantity meal
		List<QuantityDtoIn> dtoInQuantity = new ArrayList<>();
		var q1 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q1);
		var q2 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q2);
		var q3 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q3);
		dtoIn.setQuantity(dtoInQuantity);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.put(OrderRestControllerTest.URL_ADD)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CREATED, dtoOut.getStatus(), "Order status must created");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");
		var dtoOutQuantityMeals = dtoOut.getQuantity();
		Assertions.assertNotNull(dtoOutQuantityMeals, "No quantity meals is not null");
		Assertions.assertEquals(dtoInQuantity.size(), dtoOutQuantityMeals.size(), "Quantity meals has the good size");
		for (var i = 0; i < dtoInQuantity.size(); i++) {
			var qmdout = dtoOutQuantityMeals.get(i);
			var qmdin = dtoInQuantity.get(i);
			Assertions.assertEquals(qmdin.getQuantity(), qmdout.getQuantity(), "Quantity meals has the good quantity");
			Assertions.assertEquals(qmdin.getMealId(), qmdout.getMeal().getId(), "Quantity meals has the good meal");
		}
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testAdd03() throws Exception {

		// Not connected

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(Integer.valueOf(1));
		// Order with quantity meal
		List<QuantityDtoIn> dtoInQuantity = new ArrayList<>();
		var q1 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q1);
		var q2 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q2);
		var q3 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q3);
		dtoIn.setQuantity(dtoInQuantity);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		var result = super.mockMvc.perform(MockMvcRequestBuilders.put(OrderRestControllerTest.URL_ADD)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testAdd04() throws Exception {

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var userId = super.getUserIdInToken(result);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Do not set the user id
		// dtoIn.setUserId(userId);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.put(OrderRestControllerTest.URL_ADD)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CREATED, dtoOut.getStatus(), "Order status must created");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, dtoOut.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		// User id should be the one of the connected user
		Assertions.assertEquals(userId, dtoOut.getUser().getId(), "User id must be the one selected");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCancel01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, orderCreated.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// The call, cancel the order passed
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_CANCEL + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CANCELED, dtoOut.getStatus(), "Order status must canceled");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, dtoOut.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testCancel02() throws Exception {

		final var userIdPassingOrder = Integer.valueOf(1);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Pass the order as user 1
		dtoIn.setUserId(userIdPassingOrder);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, orderCreated.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(userIdPassingOrder, orderCreated.getUser().getId(), "User id must be the one selected");

		// Now, Connect as simple user that is not the one passing the order
		var result = super.logMeInAsNormalRandomUser();
		// The asserts
		Assertions.assertNotEquals(userIdPassingOrder, super.getUserIdInToken(result),
				"Connected user is not the one that ordered");

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_CANCEL + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCancel03() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		final var userIdPassingOrder = super.getUserIdInToken(result);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Pass the order as a simple user
		dtoIn.setUserId(userIdPassingOrder);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, orderCreated.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(userIdPassingOrder, orderCreated.getUser().getId(), "User id must be the one selected");

		// Now, Connect as lunch lady
		result = super.logMeInAsLunchLady();
		// The asserts
		Assertions.assertNotEquals(userIdPassingOrder, super.getUserIdInToken(result),
				"Connected user is not the one that ordered");

		// Call a cancel as a lunch lady
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_CANCEL + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CANCELED, dtoOut.getStatus(), "Order status must canceled");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(1, dtoOut.getQuantity().size(), "Quantity muste have the right size");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testComputePrice01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// The call
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.get(OrderRestControllerTest.URL_COMPUTEPRICE + orderCreated.getId() + "/-1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), PriceDtoOut.class);
		Assertions.assertNotNull(dtoOut.getPriceDF(), "Price have value");
		Assertions.assertTrue(dtoOut.getPriceDF().doubleValue() > 0, "Price must be > 0");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testComputePrice02() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// The call, with constraint
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.get(OrderRestControllerTest.URL_COMPUTEPRICE + orderCreated.getId() + "/1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), PriceDtoOut.class);
		Assertions.assertNotNull(dtoOut.getPriceDF(), "Price have value");
		Assertions.assertTrue(dtoOut.getPriceDF().doubleValue() > 0, "Price must be > 0");
		Assertions.assertNotNull(dtoOut.getPriceVAT(), "Price have value");
		Assertions.assertTrue(dtoOut.getPriceVAT().doubleValue() > 0, "Price must be > 0");
		Assertions.assertTrue(dtoOut.getPriceDF().doubleValue() < dtoOut.getPriceVAT().doubleValue(),
				"Price must respect DF<VAT");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testComputePrice03() throws Exception {

		final var userIdPassingOrder = Integer.valueOf(1);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Pass the order as user 1
		dtoIn.setUserId(userIdPassingOrder);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Menu cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(userIdPassingOrder, orderCreated.getUser().getId(), "User id must be the one selected");

		// Now, Connect as simple user that is not the one passing the order
		var result = super.logMeInAsNormalRandomUser();
		// The asserts
		Assertions.assertNotEquals(userIdPassingOrder, super.getUserIdInToken(result),
				"Connected user is not the one that ordered");

		// The call
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.get(OrderRestControllerTest.URL_COMPUTEPRICE + orderCreated.getId() + "/1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testComputePrice04() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		final var userIdPassingOrder = super.getUserIdInToken(result);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Pass the order as a simple user
		dtoIn.setUserId(userIdPassingOrder);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(userIdPassingOrder, orderCreated.getUser().getId(), "User id must be the one selected");

		// Now, Connect as lunch lady
		result = super.logMeInAsLunchLady();
		// The asserts
		Assertions.assertNotEquals(userIdPassingOrder, super.getUserIdInToken(result),
				"Connected user is not the one that ordered");

		// Call a cancel as a lunch lady
		result = super.mockMvc.perform(
				MockMvcRequestBuilders.get(OrderRestControllerTest.URL_COMPUTEPRICE + orderCreated.getId() + "/1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), PriceDtoOut.class);
		Assertions.assertNotNull(dtoOut.getPriceDF(), "Price have value");
		Assertions.assertTrue(dtoOut.getPriceDF().doubleValue() > 0, "Price must be > 0");
		Assertions.assertNotNull(dtoOut.getPriceVAT(), "Price have value");
		Assertions.assertTrue(dtoOut.getPriceVAT().doubleValue() > 0, "Price must be > 0");
		Assertions.assertTrue(dtoOut.getPriceDF().doubleValue() < dtoOut.getPriceVAT().doubleValue(),
				"Price must respect DF<VAT");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// The call, cancel the order passed
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FIND + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFind02() throws Exception {

		final var userIdPassingOrder = Integer.valueOf(1);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Pass the order as user 1
		dtoIn.setUserId(userIdPassingOrder);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(userIdPassingOrder, orderCreated.getUser().getId(), "User id must be the one selected");

		// Now, Connect as simple user that is not the one passing the order
		var result = super.logMeInAsNormalRandomUser();
		// The asserts
		Assertions.assertNotEquals(userIdPassingOrder, super.getUserIdInToken(result),
				"Connected user is not the one that ordered");

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FIND + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind03() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		final var userIdPassingOrder = super.getUserIdInToken(result);
		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		// Pass the order as a simple user
		dtoIn.setUserId(userIdPassingOrder);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(userIdPassingOrder, orderCreated.getUser().getId(), "User id must be the one selected");

		// Now, Connect as lunch lady
		result = super.logMeInAsLunchLady();
		// The asserts
		Assertions.assertNotEquals(userIdPassingOrder, super.getUserIdInToken(result),
				"Connected user is not the one that ordered");

		// Call a cancel as a lunch lady
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FIND + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testPay01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_PAY + orderCreated.getId() + "/-1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testPay02() throws Exception {
		// Remove all money to a user
		var user = super.findASimpleUser();
		user.setWallet(BigDecimal.valueOf(0F));
		super.userDao.save(user);

		// Connect as the broke user
		var result = super.logMeIn(user.getEmail());

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with quantity meal
		List<QuantityDtoIn> dtoInQuantity = new ArrayList<>();
		var q1 = new QuantityDtoIn(Integer.valueOf(10), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q1);
		var q2 = new QuantityDtoIn(Integer.valueOf(10), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q2);
		var q3 = new QuantityDtoIn(Integer.valueOf(10), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q3);
		dtoIn.setQuantity(dtoInQuantity);

		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");

		// Connect as lunch lady
		result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_PAY + orderCreated.getId() + "/-1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));
		// Not enough money
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testPay04() throws Exception {
		// Remove all money to a user
		var user = super.findASimpleUser();
		user.setWallet(BigDecimal.valueOf(200D));
		super.userDao.save(user);

		// Connect as the user
		var result = super.logMeIn(user.getEmail());

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with quantity meal
		List<QuantityDtoIn> dtoInQuantity = new ArrayList<>();
		var q1 = new QuantityDtoIn(Integer.valueOf(2), Integer.valueOf(27), null);
		dtoInQuantity.add(q1);
		dtoIn.setQuantity(dtoInQuantity);

		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");

		// Connect as lunch lady
		result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_PAY + orderCreated.getId() + "/-1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));
		// Not enough money
		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testPay03() throws Exception {
		// Give a lot of money to a user
		var user = super.findASimpleUser();
		// Decimal(5,2) = 3 dig max
		user.setWallet(BigDecimal.valueOf(999D));
		super.userDao.save(user);

		// Connect as simple user
		var result = super.logMeIn(user.getEmail());
		var userId = super.getUserIdInToken(result);
		var walletBefore = super.getUserInToken(result).getWallet();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// Connect as lunch lady
		result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_PAY + orderCreated.getId() + "/-1")
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.DELIVERED, dtoOut.getStatus(), "Order status must delivered");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");

		user = super.userService.findEntity(userId);

		var walletAfter = user.getWallet();
		Assertions.assertTrue(walletBefore.doubleValue() >= walletAfter.doubleValue(),
				"Wallet must be less than before");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindAll01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALL)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");
		Assertions.assertTrue(100 <= orders.size(), "Order list size is at least 100");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindAll02() throws Exception {
		// Connect as standard user
		var result = super.logMeInAsNormalRandomUser();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALL)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	void testUpdate01() throws Exception {

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		dtoIn = OrderDtoHandler.dtoInfromEntity(this.orderService.findEntity(orderCreated.getId()),
				dtoIn.getConstraintId());
		// Add meal
		var dtoInQuantity = dtoIn.getQuantity();
		var q1 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q1);
		var q2 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q2);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_UPDATE + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CREATED, dtoOut.getStatus(), "Order status must created");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");
		var dtoOutQuantityMeals = dtoOut.getQuantity();
		Assertions.assertNotNull(dtoOutQuantityMeals, "No quantity meals is not null");
		Assertions.assertEquals(dtoInQuantity.size(), dtoOutQuantityMeals.size(), "Quantity meals has the good size");
		// Start at one in order to jump the menu
		for (var i = 1; i < dtoInQuantity.size(); i++) {
			Assertions.assertEquals(dtoInQuantity.get(i).getQuantity(), dtoOutQuantityMeals.get(i).getQuantity(),
					"Quantity meals has the good quantity");
			Assertions.assertEquals(dtoInQuantity.get(i).getMealId(), dtoOutQuantityMeals.get(i).getMeal().getId(),
					"Quantity meals has the good meal");
		}
	}

	@Test
	void testUpdate02() throws Exception {

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(super.getUserIdInToken(result));
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		dtoIn = OrderDtoHandler.dtoInfromEntity(this.orderService.findEntity(orderCreated.getId()),
				dtoIn.getConstraintId());
		// Add meal
		var dtoInQuantity = dtoIn.getQuantity();
		var q1 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q1);
		var q2 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q2);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// Connect as lunch lady
		result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_UPDATE + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), OrderDtoOut.class);
		Assertions.assertEquals(OrderStatus.CREATED, dtoOut.getStatus(), "Order status must created");
		Assertions.assertNotNull(dtoOut.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, dtoOut.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), dtoOut.getUser().getId(), "User id must be the one selected");
		var dtoOutQuantityMeals = dtoOut.getQuantity();
		Assertions.assertNotNull(dtoOutQuantityMeals, "No quantity meals is not null");
		Assertions.assertEquals(dtoInQuantity.size(), dtoOutQuantityMeals.size(), "Quantity meals has the good size");
		// Start at one in order to jump the menu
		for (var i = 1; i < dtoInQuantity.size(); i++) {
			Assertions.assertEquals(dtoInQuantity.get(i).getQuantity(), dtoOutQuantityMeals.get(i).getQuantity(),
					"Quantity meals has the good quantity");
			Assertions.assertEquals(dtoInQuantity.get(i).getMealId(), dtoOutQuantityMeals.get(i).getMeal().getId(),
					"Quantity meals has the good meal");
		}
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdate03() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var userId = super.getUserIdInToken(result);

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(userId);
		final var menuId = super.getValidMenu(false).getId();
		// Order with a menu
		dtoIn.addMenu(1, menuId);
		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Quantity cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		dtoIn = OrderDtoHandler.dtoInfromEntity(this.orderService.findEntity(orderCreated.getId()),
				dtoIn.getConstraintId());
		// Add meal
		List<QuantityDtoIn> dtoInQuantity = new ArrayList<>();
		var q1 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q1);
		var q2 = new QuantityDtoIn(Integer.valueOf(1), super.getValidMeal(false).getId(), null);
		dtoInQuantity.add(q2);
		dtoIn.setQuantity(dtoInQuantity);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// Connect as another simple user
		result = super.logMeInAsNormalRandomUser(userId);
		Assertions.assertNotEquals(userId, super.getUserIdInToken(result), "Not the same user");

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(OrderRestControllerTest.URL_UPDATE + orderCreated.getId())
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindAllForUser01() throws Exception {
		var user = super.findASimpleUser();
		while (super.orderService.findAllByUserId(user.getId()).isEmpty()) {
			user = super.findASimpleUser();
		}

		// Connect as simple user
		var result = super.logMeIn(user.getEmail());

		var userId = super.getUserIdInToken(result);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSER + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(userId, dto.getUser().getId(), "Order has the specified user id");
		}
	}

	@Test
	void testFindAllForUser02() throws Exception {
		final var status = OrderStatus.CREATED;
		var user = super.findASimpleUser();
		while (super.orderService.findAllByUserId(user.getId()).isEmpty()) {
			user = super.findASimpleUser();
		}

		// Connect as simple user
		var result = super.logMeIn(user.getEmail());
		var userId = super.getUserIdInToken(result);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSER + userId)
				.param("status", String.valueOf(status)).header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(userId, dto.getUser().getId(), "Order has the specified user id");
			Assertions.assertEquals(status, dto.getStatus(), "Order status is as searched");
		}
	}

	@Test
	void testFindAllForUser03() throws Exception {
		final var status = OrderStatus.CREATED;

		var begin = LocalDate.of(2019, Month.JANUARY, 1);
		var end = LocalDate.now();

		final var beginDate = begin.format(DateTimeFormatter.ofPattern(this.datePattern));
		final var endDate = end.format(DateTimeFormatter.ofPattern(this.datePattern));

		var user = super.findASimpleUser();
		while (super.orderService.findAllByUserId(user.getId()).isEmpty()) {
			user = super.findASimpleUser();
		}

		// Connect as simple user
		var result = super.logMeIn(user.getEmail());
		var userId = super.getUserIdInToken(result);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSER + userId)
				.param("status", String.valueOf(status)).param("beginDate", beginDate).param("endDate", endDate)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(status, dto.getStatus(), "Order status is as searched");
			var creationDate = dto.getCreationDate();
			Assertions.assertTrue(creationDate.isEqual(end) || creationDate.isBefore(end),
					"Order date is before the specified end date");
			Assertions.assertTrue(creationDate.isEqual(begin) || creationDate.isAfter(begin),
					"Order date is after the specified begin date");
		}
	}

	@Test
	void testFindAllForUser04() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		// The call with no user id
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSER)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts, userid is mandatory
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindAllForUser05() throws Exception {
		// Connect as simple user that had some orders
		var result = super.logMeInAsLunchLady();
		var userId = super.findASimpleUser().getId();
		var ordersEnt = super.orderService.findAllByUserId(userId);
		while (ordersEnt == null || ordersEnt.isEmpty()) {
			userId = super.findASimpleUser().getId();
			ordersEnt = super.orderService.findAllByUserId(userId);
		}

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSER + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(userId, dto.getUser().getId(), "Order has the specified user id");
		}
	}

	@Test
	void testFindAllForUserToday01() throws Exception {
		final var status = OrderStatus.CREATED;

		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var userId = super.getUserIdInToken(result);

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(userId);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);

		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Menu cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSERTODAY + userId)
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(status, dto.getStatus(), "Order status is as searched");
			var creationDate = dto.getCreationDate();
			Assertions.assertEquals(LocalDate.now(), creationDate, "Order date is today the specified date");
		}
	}

	@Test
	void testFindAllForUserToday02() throws Exception {
		final var status = OrderStatus.CREATED;

		var userId = Integer.valueOf(50);

		var dtoIn = new OrderDtoIn();
		dtoIn.setConstraintId(Integer.valueOf(-1));
		dtoIn.setUserId(userId);
		// Order with a menu
		final var menuId = super.getValidMenu(false).getId();
		dtoIn.addMenu(1, menuId);

		var orderCreated = super.orderService.order(dtoIn);
		Assertions.assertEquals(OrderStatus.CREATED, orderCreated.getStatus(), "Order status must created");
		Assertions.assertNotNull(orderCreated.getQuantity(), "Menu cannot be null");
		Assertions.assertEquals(menuId, orderCreated.getQuantity().get(0).getMenu().getId(),
				"Menu id must be the one selected");
		Assertions.assertEquals(dtoIn.getUserId(), orderCreated.getUser().getId(), "User id must be the one selected");

		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();
		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(OrderRestControllerTest.URL_FINDALLFORUSERTODAY + userId)
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var content = result.andReturn().getResponse().getContentAsString();

		Class<?> clz = OrderDtoOut.class;
		JavaType type = this.mapper.getTypeFactory().constructCollectionType(List.class, clz);
		List<OrderDtoOut> orders = this.mapper.readValue(content, type);

		Assertions.assertNotNull(orders, "Order list cannot be null");
		Assertions.assertFalse(orders.isEmpty(), "Order list cannot be empty");

		for (OrderDtoOut dto : orders) {
			Assertions.assertNotNull(dto, "Order cannot be null");
			Assertions.assertEquals(status, dto.getStatus(), "Order status is as searched");
			var creationDate = dto.getCreationDate();
			Assertions.assertEquals(LocalDate.now(), creationDate, "Order date is today the specified date");
		}
	}

}
