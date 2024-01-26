// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.jpa.handler.ConstraintDtoHandler;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Test for constraint controller, using Mock.
 */
class ConstraintRestControllerTest extends AbstractJpaWebTest {
	private static final String URL_ROOT = "/constraint";
	private static final String URL_ADD = ConstraintRestControllerTest.URL_ROOT + "/add";
	private static final String URL_DELETE = ConstraintRestControllerTest.URL_ROOT + "/delete/";
	private static final String URL_UPDATE = ConstraintRestControllerTest.URL_ROOT + "/update/";
	private static final String URL_FIND = ConstraintRestControllerTest.URL_ROOT + "/find/";
	private static final String URL_FINDALL = ConstraintRestControllerTest.URL_ROOT + "/findall";

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		var elmId = Integer.valueOf(1);

		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(ConstraintRestControllerTest.URL_FIND + elmId));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(elmId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind02() throws Exception {
		var elmId = 10000;
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(ConstraintRestControllerTest.URL_FIND + elmId));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind03() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(ConstraintRestControllerTest.URL_FIND));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindAll01() throws Exception {
		var result = super.mockMvc.perform(MockMvcRequestBuilders.get(ConstraintRestControllerTest.URL_FINDALL));
		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(Integer.valueOf(1)));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		var elmId = Integer.valueOf(1);

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(ConstraintRestControllerTest.URL_DELETE + elmId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		// No status for constraint, element is no more in database
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.constraintService.find(elmId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testDelete02() throws Exception {
		// Connect a user
		var result = super.logMeInAsNormalRandomUser();

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.delete(ConstraintRestControllerTest.URL_DELETE + String.valueOf(1))
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	void testDelete03() throws Exception {
		var elmId = 10000;

		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(ConstraintRestControllerTest.URL_DELETE + elmId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testAdd01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();

		var dto = new ConstraintDtoIn();
		dto.setMaximumOrderPerDay(Integer.valueOf(20));
		dto.setRateVAT(Float.valueOf(20F));
		dto.setOrderTimeLimit("11:00:00");
		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.put(ConstraintRestControllerTest.URL_ADD)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoAsJsonString)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(),
				ConstraintDtoOut.class);

		var entity = this.constraintService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertNotNull(entity.getId(), "Result must have an id");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testAdd02() throws Exception {
		// Connect as lambda
		var result = super.logMeInAsNormalRandomUser();

		var dto = new ConstraintDtoIn();
		dto.setMaximumOrderPerDay(Integer.valueOf(20));
		dto.setRateVAT(Float.valueOf(20F));
		dto.setOrderTimeLimit("11:00:00");
		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.put(ConstraintRestControllerTest.URL_ADD)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoAsJsonString)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	void testUpdate01() throws Exception {
		// Connect as Lunch Lady
		var result = super.logMeInAsLunchLady();
		var elmId = Integer.valueOf(1);
		var dto = ConstraintDtoHandler.dtoInfromEntity(super.constraintService.findEntity(elmId));
		// Change mop
		dto.setMaximumOrderPerDay(Integer.valueOf(199));

		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(ConstraintRestControllerTest.URL_UPDATE + elmId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoAsJsonString)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(),
				ConstraintDtoOut.class);

		var entity = this.constraintService.find(dtoOut.getId());
		Assertions.assertNotNull(entity, "Result must exist");
		Assertions.assertEquals(elmId, entity.getId(), "Result must have same id");
		Assertions.assertEquals(199, entity.getMaximumOrderPerDay().intValue(), "Result must have same changed value");
		Assertions.assertEquals(dto.getRateVAT(), entity.getRateVAT(), "Result must have same unchanged value");
		Assertions.assertEquals(dto.getOrderTimeLimitAsTime(), entity.getOrderTimeLimit(),
				"Result must have same unchanged value");
	}

	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdate02() throws Exception {
		// Connect as lambda
		var result = super.logMeInAsNormalRandomUser();
		var elmId = Integer.valueOf(1);
		var dto = ConstraintDtoHandler.dtoInfromEntity(super.constraintService.findEntity(elmId));
		// Change mop
		dto.setMaximumOrderPerDay(Integer.valueOf(199));

		var dtoAsJsonString = this.mapper.writeValueAsString(dto);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(ConstraintRestControllerTest.URL_UPDATE + elmId)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoAsJsonString)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}
}
