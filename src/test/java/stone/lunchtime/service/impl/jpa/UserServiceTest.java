// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import stone.lunchtime.AbstractJpaTest;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.jpa.handler.UserDtoHandler;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.RoleLabel;
import stone.lunchtime.entity.Sex;
import stone.lunchtime.init.AbstractInitDataBase;
import stone.lunchtime.service.exception.EntityAlreadySavedException;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentPasswordException;
import stone.lunchtime.service.exception.InconsistentRoleException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.LackOfMoneyException;
import stone.lunchtime.service.exception.ParameterException;

/**
 * User service test class.
 */
class UserServiceTest extends AbstractJpaTest {

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindEmail01() throws Exception {
		final var email = AbstractInitDataBase.USER_EXISTING_EMAIL;
		var result = this.userService.find(email);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(email, result.getEmail(), () -> "Result must have " + email + " as email");
	}

	/**
	 * Test
	 */
	@Test
	void testFindEmail02() {
		final var email = "nexistepas@gmail.com";
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.userService.find(email));
	}

	/**
	 * Test
	 */
	@Test
	void testFindEmail03() {
		final String email = null;
		Assertions.assertThrows(ParameterException.class, () -> this.userService.find(email));
	}

	/**
	 * Test
	 */
	@Test
	void testFindEmail04() {
		final var email = "";
		Assertions.assertThrows(ParameterException.class, () -> this.userService.find(email));
	}

	/**
	 * Test
	 */
	@Test
	void testExist01() {
		final var email = AbstractInitDataBase.USER_EXISTING_EMAIL;
		var result = this.userService.exist(email);
		Assertions.assertTrue(result, "Result must exist");
	}

	/**
	 * Test
	 */
	@Test
	void testExist02() {
		final var email = "jjj@aol.com";
		var result = this.userService.exist(email);
		Assertions.assertFalse(result, "Result must NOT exist");
	}

	/**
	 * Test
	 */
	@Test
	void testExist03() {
		final String email = null;
		Assertions.assertThrows(ParameterException.class, () -> this.userService.exist(email));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFind01() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.find(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
	}

	/**
	 * Test
	 */
	@Test
	void testFind02() {
		final var id = Integer.valueOf(1000000);
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.userService.find(id));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister01() throws Exception {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setWallet(Float.valueOf(50F));
		user.setEmail("newtoto@gmail.com");
		user.setPassword("alpha");
		user.setIsLunchLady(Boolean.FALSE);
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setSex(Sex.MAN);

		var result = this.userService.register(user);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getRegistrationDate(), "Result must have an registration date");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd01() throws Exception {
		var result = this.userService.checkPassword(AbstractInitDataBase.USER_EXISTING_ID,
				AbstractInitDataBase.USER_DEFAULT_PWD);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getRegistrationDate(), "Result must have an registration date");
		Assertions.assertEquals(AbstractInitDataBase.USER_EXISTING_ID, result.getId(), "ID must be the same");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd02() throws Exception {
		Assertions.assertThrows(ParameterException.class,
				() -> this.userService.checkPassword(null, AbstractInitDataBase.USER_DEFAULT_PWD));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd03() throws Exception {
		Assertions.assertThrows(ParameterException.class,
				() -> this.userService.checkPassword(AbstractInitDataBase.USER_EXISTING_ID, null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd04() throws Exception {
		Assertions.assertThrows(ParameterException.class,
				() -> this.userService.checkPassword(AbstractInitDataBase.USER_EXISTING_ID, ""));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd05() throws Exception {
		Assertions.assertThrows(EntityNotFoundException.class,
				() -> this.userService.checkPassword(100000, AbstractInitDataBase.USER_DEFAULT_PWD));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd07() throws Exception {
		Assertions.assertThrows(InconsistentPasswordException.class,
				() -> this.userService.checkPassword(AbstractInitDataBase.USER_EXISTING_ID, "Toto"));
	}

	/**
	 * Test
	 */
	@Test
	void testRegister02() {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setWallet(Float.valueOf(50F));
		user.setPassword("alpha");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setSex(Sex.MAN);

		user.setEmail(AbstractInitDataBase.USER_EXISTING_EMAIL);
		Assertions.assertThrows(EntityAlreadySavedException.class, () -> this.userService.register(user));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister03() throws Exception {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setWallet(Float.valueOf(50F));
		user.setEmail("newtoto@gmail.com");
		user.setPassword("alpha");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setSex(Sex.MAN);

		var result = this.userService.register(user);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getRegistrationDate(), "Result must have an registration date");
		Assertions.assertFalse(result.getIsLunchLady().booleanValue(), "Result must not be LunchLady");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister04() throws Exception {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setEmail("newtoto@gmail.com");
		user.setPassword("alpha");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setSex(Sex.MAN);
		user.setWallet(Float.valueOf(200F));

		var result = this.userService.register(user);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getRegistrationDate(), "Result must have an registration date");
		Assertions.assertEquals(200F, result.getWallet().floatValue(), 0.01F, "Result must not a wallet with 0");
	}

	/**
	 * Test
	 */
	@Test
	void testRegister05() {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setPassword("alpha");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setSex(Sex.MAN);

		Assertions.assertThrows(ParameterException.class, () -> this.userService.register(user));
	}

	/**
	 * Test
	 */
	@Test
	void testRegister06() {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setEmail("newtoto@gmail.com");
		user.setPassword(null);
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setSex(Sex.MAN);

		Assertions.assertThrows(ParameterException.class, () -> this.userService.register(user));
	}

	/**
	 * Test
	 */
	@Test
	void testRegister08() {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setEmail("newtoto@gmail.com");
		user.setPassword("toto");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		// user.setSexe(Sex.MAN);

		Assertions.assertThrows(ParameterException.class, () -> this.userService.register(user));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister09() throws Exception {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setEmail("newtoto@gmail.com");
		user.setPassword("alpha");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setWallet(null);
		user.setSex(Sex.MAN);

		var result = this.userService.register(user);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getRegistrationDate(), "Result must have an registration date");
		Assertions.assertEquals(0D, result.getWallet().doubleValue(), 0.01D, "Result must have a wallet with 0");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister10() throws Exception {
		var user = new UserDtoIn();
		user.setAddress("Somewhere in spain");
		user.setEmail("newtoto@gmail.com");
		user.setPassword("alpha");
		user.setName("Durant");
		user.setFirstname("Albert");
		user.setPhone("0148567897");
		user.setTown("Paris");
		user.setPostalCode("75000");
		user.setWallet(Float.valueOf(-10F));
		user.setSex(Sex.MAN);

		var result = this.userService.register(user);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertNotNull(result.getId(), "Result must have an id");
		Assertions.assertNotNull(result.getRegistrationDate(), "Result must have an registration date");
		Assertions.assertEquals(0D, result.getWallet().doubleValue(), 0.01D, "Result must have a wallet with 0");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate01() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newName = "new name";
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setName(newName);
		var resultDto = this.userService.update(id, dto, false);
		Assertions.assertNotNull(resultDto, "Result must exist");
		Assertions.assertEquals(id, resultDto.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(newName, resultDto.getName(), () -> "Result must have a name " + newName);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate02() throws Exception {
		final var id = Integer.valueOf(2);
		final var newEmail = AbstractInitDataBase.USER_EXISTING_EMAIL;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setEmail(newEmail);
		Assertions.assertThrows(EntityAlreadySavedException.class, () -> this.userService.update(id, dto, false));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate03() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newCagnote = Float.valueOf(555F);
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var currentCagnote = result.getWallet().doubleValue();
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setWallet(newCagnote);
		var resultDto = this.userService.update(id, dto, false);
		Assertions.assertNotNull(resultDto, "Result must exist");
		Assertions.assertEquals(id, resultDto.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(currentCagnote, resultDto.getWallet().doubleValue(), 0.01D,
				"Result must have a wallet unchanged");
	}

	/**
	 * Test
	 */
	@Test
	void testUpdate06() {
		final var id = Integer.valueOf(200000);
		var dto = new UserDtoIn();
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.userService.update(id, dto, false));
	}

	/**
	 * Test
	 */
	@Test
	void testUpdate07() {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		UserDtoIn dto = null;
		Assertions.assertThrows(ParameterException.class, () -> this.userService.update(id, dto, false));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate08() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertTrue(result.getIsLunchLady(), "Result must be a lunch lady");
		var din = UserDtoHandler.dtoInfromEntity(result);
		din.setIsLunchLady(Boolean.FALSE);
		Assertions.assertThrows(InconsistentRoleException.class, () -> this.userService.update(id, din, true));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate09() throws Exception {
		final var id1 = AbstractInitDataBase.USER_EXISTING_ID;
		final var id2 = Integer.valueOf(2);
		var result1 = this.userService.findEntity(id1);
		Assertions.assertNotNull(result1, "Result must exist");
		Assertions.assertEquals(id1, result1.getId(), () -> "Result must have " + id1 + " as id");
		Assertions.assertTrue(result1.getIsLunchLady(), "Result must be a lunch lady");
		var result2 = this.userService.findEntity(id2);
		Assertions.assertNotNull(result2, "Result must exist");
		Assertions.assertEquals(id2, result2.getId(), () -> "Result must have " + id2 + " as id");
		Assertions.assertFalse(result2.getIsLunchLady(), "Result must NOT be a lunch lady");
		var din = UserDtoHandler.dtoInfromEntity(result2);
		din.setIsLunchLady(Boolean.TRUE);
		var resultD2 = this.userService.update(id2, din, true);
		Assertions.assertTrue(resultD2.getIsLunchLady(), "Result must be a lunch lady");
		din = UserDtoHandler.dtoInfromEntity(result1);

		din.setIsLunchLady(Boolean.FALSE);
		this.userService.update(id1, din, true);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate10() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newAddr = "new address";
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setAddress(newAddr);
		var resultDto = this.userService.update(id, dto, false);
		Assertions.assertNotNull(resultDto, "Result must exist");
		Assertions.assertEquals(id, resultDto.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(newAddr, resultDto.getAddress(), () -> "Result must have an address " + newAddr);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate11() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newPostalCode = "new Postal Code";
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setPostalCode(newPostalCode);
		var dtoResult = this.userService.update(id, dto, false);
		Assertions.assertNotNull(dtoResult, "Result must exist");
		Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(newPostalCode, dtoResult.getPostalCode(),
				() -> "Result must have a postal code " + newPostalCode);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate12() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newFirstName = "new first name";
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setFirstname(newFirstName);
		var dtoResult = this.userService.update(id, dto, false);
		Assertions.assertNotNull(dtoResult, "Result must exist");
		Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(newFirstName, dtoResult.getFirstname(),
				() -> "Result must have a first name " + newFirstName);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate13() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newTown = "new town";
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setTown(newTown);
		var dtoResult = this.userService.update(id, dto, false);
		Assertions.assertNotNull(dtoResult, "Result must exist");
		Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(newTown, dtoResult.getTown(), () -> "Result must have a town " + newTown);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate14() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var newPhone = "new phone";
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		dto.setPhone(newPhone);
		var dtoResult = this.userService.update(id, dto, false);
		Assertions.assertNotNull(dtoResult, "Result must exist");
		Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertEquals(newPhone, dtoResult.getPhone(), () -> "Result must have a phone " + newPhone);
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdate15() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dto = UserDtoHandler.dtoInfromEntity(result);
		result.getRoles().clear();
		this.userService.update(id, dto, false);
		var afterUdpate = this.userService.findEntity(id);
		Assertions.assertNotNull(afterUdpate, "Result must exist");
		Assertions.assertEquals(id, afterUdpate.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertNotNull(afterUdpate.getRoles(), "Result must have role");
		Assertions.assertFalse(afterUdpate.getRoles().isEmpty(), "Result must have role");
		Assertions.assertEquals(1, afterUdpate.getRoles().size(), "Result must have one role");
		Assertions.assertEquals(RoleLabel.ROLE_USER, result.getRoles().get(0).getLabel(), "Result must have USER role");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDebit01() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		// Give money to the user if needed
		var currentCagnote = result.getWallet().doubleValue();
		if (currentCagnote <= 5D) {
			var dtoResult = this.userService.credit(id, BigDecimal.valueOf(50D));
			Assertions.assertNotNull(dtoResult, "Result must exist");
			Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
			currentCagnote = dtoResult.getWallet().doubleValue();
		}

		var dtoResult = this.userService.debit(id, BigDecimal.valueOf(5D));
		Assertions.assertNotNull(dtoResult, "Result must exist");
		Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertNotEquals(currentCagnote, dtoResult.getWallet().doubleValue(), 0.01D,
				"Result must have a wallet changed");
		Assertions.assertEquals(currentCagnote - 5D, dtoResult.getWallet().doubleValue(), 0.01D,
				"Result must have a wallet changed");

	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDebit02() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		final var m = BigDecimal.valueOf(500D);
		Assertions.assertThrows(LackOfMoneyException.class, () -> this.userService.debit(id, m));
	}

	/**
	 * Test
	 */
	@Test
	void testDebit03() {
		final Integer id = null;
		final var m = BigDecimal.valueOf(500D);
		Assertions.assertThrows(ParameterException.class, () -> this.userService.debit(id, m));
	}

	/**
	 * Test
	 */
	@Test
	void testDebit04() {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var m = BigDecimal.valueOf(-5D);
		Assertions.assertThrows(ParameterException.class, () -> this.userService.debit(id, m));
	}

	/**
	 * Test
	 */
	@Test
	void testDebit05() {
		final var id = Integer.valueOf(100000);
		final var m = BigDecimal.valueOf(5D);
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.userService.debit(id, m));
	}

	/**
	 * Test
	 */
	@Test
	void testDebit06() {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		Assertions.assertThrows(ParameterException.class, () -> this.userService.debit(id, null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCredit01() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var currentCagnote = result.getWallet().doubleValue();
		var dtoResult = this.userService.credit(id, BigDecimal.valueOf(50D));
		Assertions.assertNotNull(dtoResult, "Result must exist");
		Assertions.assertEquals(id, dtoResult.getId(), () -> "Result must have " + id + " as id");
		Assertions.assertNotEquals(currentCagnote, dtoResult.getWallet().doubleValue(), 0.01D,
				"Result must have a wallet changed");
		Assertions.assertEquals(currentCagnote + 50D, dtoResult.getWallet().doubleValue(), 0.01D,
				"Result must have a wallet changed");

	}

	/**
	 * Test
	 */
	@Test
	void testCredit03() {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		final var m = BigDecimal.valueOf(-5D);
		Assertions.assertThrows(ParameterException.class, () -> this.userService.credit(id, m));
	}

	/**
	 * Test
	 */
	@Test
	void testCredit04() {
		final var id = Integer.valueOf(10000);
		final var m = BigDecimal.valueOf(5D);
		Assertions.assertThrows(EntityNotFoundException.class, () -> this.userService.credit(id, m));
	}

	/**
	 * Test
	 */
	@Test
	void testCredit05() {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		Assertions.assertThrows(ParameterException.class, () -> this.userService.credit(id, null));
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateImage01() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var result = this.userService.findEntity(id);
		Assertions.assertNotNull(result, "Result must exist");
		Assertions.assertEquals(id, result.getId(), () -> "Result must have " + id + " as id");
		var dtoIn = new ImageDtoIn();
		dtoIn.setImagePath("img/test.png");
		dtoIn.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");
		this.userService.updateImage(id, dtoIn);
		var ie = result.getImage();
		Assertions.assertNotNull(ie.getImagePath(), "Image must have a path");
		Assertions.assertFalse(ie.getIsDefault(), "Image should NOT be a default one");
		Assertions.assertNotNull(ie.getImage64(), "Image should have a base64");
		Assertions.assertEquals(dtoIn.getImagePath(), ie.getImagePath(), "Image should have the same path");
		Assertions.assertEquals(dtoIn.getImage64(), ie.getImage64(), "Image should have the same base 64");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateImage04() throws Exception {
		final var id = AbstractInitDataBase.USER_EXISTING_ID;
		var ue = super.userService.findEntity(id);
		ue.setStatus(EntityStatus.DELETED);
		super.userDao.save(ue);
		var dtoIn = new ImageDtoIn();
		dtoIn.setImagePath("img/test.png");
		dtoIn.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");
		Assertions.assertThrows(InconsistentStatusException.class, () -> this.userService.updateImage(id, dtoIn));
	}
}
