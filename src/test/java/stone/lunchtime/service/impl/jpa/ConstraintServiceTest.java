// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stone.lunchtime.AbstractJpaTest;
import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Constraint service test class.
 */
class ConstraintServiceTest extends AbstractJpaTest {

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		final var cId = Integer.valueOf(1);
		var result = this.constraintService.find(cId);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(cId, result.getId(), () -> "Result must have " + cId + " as id");
	}

	/**
	 * Test
	 */
	@Test
	void testFind02() {
		final var id = Integer.valueOf(1000000);
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.constraintService.find(id));
	}

	/**
	 * Test
	 */
	@Test
	void testFind03() {
		final Integer id = null;
		Assertions.assertThrows(ParameterException.class, () -> this.constraintService.find(id));
	}

	/**
	 * Test
	 */
	@Test
	void testAdd01() {
		var dto = new ConstraintDtoIn();
		dto.setMaximumOrderPerDay(Integer.valueOf(20));
		dto.setRateVAT(20F);
		dto.setOrderTimeLimit("11:00:00");
		var result = this.constraintService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
	}

	/**
	 * Test
	 */
	@Test
	void testAdd02() {
		var dto = new ConstraintDtoIn();
		var result = this.constraintService.add(dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getMaximumOrderPerDay(), "Result must have all constraint set");
		Assertions.assertNotNull(result.getOrderTimeLimit(), "Result must have all constraint set");
		Assertions.assertNotNull(result.getRateVAT(), "Result must have all constraint set");
	}

	/**
	 * Test
	 */
	@Test
	void testAdd03() {
		Assertions.assertThrows(ParameterException.class, () -> this.constraintService.add(null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		final var cId = Integer.valueOf(1);
		var result = this.constraintService.find(cId);
		Assertions.assertNotNull(result, "Result must exist");
		this.constraintService.delete(cId);
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.constraintService.find(cId));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate01() throws Exception {
		final var cId = Integer.valueOf(1);
		var result = this.constraintService.find(cId);
		Assertions.assertNotNull(result, "Result must exist");
		var dto = new ConstraintDtoIn();
		dto.setMaximumOrderPerDay(Integer.valueOf(20));
		var vat = Float.valueOf(50F);
		dto.setRateVAT(vat);
		dto.setOrderTimeLimit("11:00:00");
		result = this.constraintService.update(cId, dto);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(cId, result.getId(), "Result must have the same id");
		Assertions.assertEquals(vat, result.getRateVAT(), "Result must have the correct rate");
		// TODO FixMultiLine
		Assertions.assertEquals(result.getMaximumOrderPerDay(), dto.getMaximumOrderPerDay(),
				"Result must have the correct number of MaximumOrderPerDay");

	}
}
