// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import stone.lunchtime.AbstractJpaWebTest;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.jpa.handler.UserDtoHandler;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.RoleLabel;
import stone.lunchtime.entity.Sex;
import stone.lunchtime.entity.jpa.RoleEntity;
import stone.lunchtime.init.AbstractInitDataBase;
import stone.lunchtime.service.IDefaultImages;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Test for user controller, using Mock.
 */
class UserRestControllerTest extends AbstractJpaWebTest {
	private static final String URL_ROOT = "/user";
	private static final String URL_REGISTER = UserRestControllerTest.URL_ROOT + "/register";
	private static final String URL_DELETE = UserRestControllerTest.URL_ROOT + "/delete/";
	private static final String URL_UPDATE = UserRestControllerTest.URL_ROOT + "/update/";
	private static final String URL_FIND_IMG = UserRestControllerTest.URL_ROOT + "/findimg/";
	private static final String URL_UPDATE_IMG = UserRestControllerTest.URL_ROOT + "/updateimg/";
	private static final String URL_ACTIVATE = UserRestControllerTest.URL_ROOT + "/activate/";
	private static final String URL_DEACTIVATE = UserRestControllerTest.URL_ROOT + "/deactivate/";
	private static final String URL_CREDIT = UserRestControllerTest.URL_ROOT + "/credit/";
	private static final String URL_DEBIT = UserRestControllerTest.URL_ROOT + "/debit/";
	private static final String URL_CHECKPWD = UserRestControllerTest.URL_ROOT + "/checkpassword/";

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister01() throws Exception {
		var dtoIn = new UserDtoIn();
		dtoIn.setAddress("somewhere");
		dtoIn.setEmail("tyty@gmail.com");
		dtoIn.setFirstname("Jhon");
		dtoIn.setIsLunchLady(Boolean.FALSE);
		dtoIn.setName("Dupont");
		dtoIn.setPassword("xyz1234KIOuiop");
		dtoIn.setPhone("125346789");
		dtoIn.setPostalCode("78140");
		dtoIn.setSex(Sex.MAN);
		dtoIn.setTown("Versailles");
		dtoIn.setWallet(Float.valueOf(50F));

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		var result = super.mockMvc.perform(MockMvcRequestBuilders.put(UserRestControllerTest.URL_REGISTER)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertNotNull(dtoOut.getId(), "User must have an id");
		Assertions.assertEquals(dtoIn.getIsLunchLady(), dtoOut.getIsLunchLady(), "Must not be a lunch lady");
		Assertions.assertEquals(dtoIn.getEmail(), dtoOut.getEmail(), "Must have the correct email");
		Assertions.assertEquals(dtoIn.getWallet(), dtoOut.getWallet(), "Must have the same wallet");
		Assertions.assertEquals(dtoIn.getName(), dtoOut.getName(), "Must have the same name");
		Assertions.assertEquals(dtoIn.getFirstname(), dtoOut.getFirstname(), "Must have the same firstname");
		Assertions.assertEquals(dtoIn.getAddress(), dtoOut.getAddress(), "Must have the same adress");
		Assertions.assertEquals(dtoIn.getPhone(), dtoOut.getPhone(), "Must have the same phone");
		Assertions.assertEquals(dtoIn.getPostalCode(), dtoOut.getPostalCode(), "Must have the same pc");
		Assertions.assertEquals(dtoIn.getSex(), dtoOut.getSex(), "Must have the same sex");
		Assertions.assertEquals(dtoIn.getTown(), dtoOut.getTown(), "Must have the same town");

		Assertions.assertTrue(EntityStatus.isEnabled(dtoOut.getStatus()), "User must be enabled");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister02() throws Exception {
		var dtoIn = new UserDtoIn();
		dtoIn.setAddress("somewhere");
		dtoIn.setEmail("tyty@gmail.com");
		dtoIn.setFirstname("Jhon");
		dtoIn.setIsLunchLady(Boolean.TRUE);
		dtoIn.setName("Dupont");
		dtoIn.setPassword("xyz1234KIOuiop");
		dtoIn.setPhone("125346789");
		dtoIn.setPostalCode("78140");
		dtoIn.setSex(Sex.MAN);
		dtoIn.setTown("Versailles");
		dtoIn.setWallet(Float.valueOf(50F));

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		var result = super.mockMvc.perform(MockMvcRequestBuilders.put(UserRestControllerTest.URL_REGISTER)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts : cannot register as lunch lady if there is at least one
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testRegister03() throws Exception {
		var opAllLunchLadyRole = super.roleDao.findLunchLadyRoles();
		Assertions.assertTrue(opAllLunchLadyRole.isPresent(), "Lunch lady found");
		Assertions.assertFalse(opAllLunchLadyRole.get().isEmpty(), "Lunch lady found");
		var allLunchLadyRole = opAllLunchLadyRole.get();
		// Remove lunch lady role(s)
		for (RoleEntity lRoleEntity : allLunchLadyRole) {
			if (lRoleEntity.getLabel() == RoleLabel.ROLE_LUNCHLADY) {
				var user = lRoleEntity.getUser();
				user.setIsLunchLady(Boolean.FALSE);
				super.userDao.save(user);
			}
		}
		// Check that there is no more lunch lady
		Assertions.assertEquals(0, super.roleDao.countLunchLady(), "No more Lunch lady found");

		// Add a user with lunch lady role
		var dtoIn = new UserDtoIn();
		dtoIn.setAddress("somewhere");
		dtoIn.setEmail("tyty@gmail.com");
		dtoIn.setFirstname("Jhon");
		dtoIn.setIsLunchLady(Boolean.TRUE);
		dtoIn.setName("Dupont");
		dtoIn.setPassword("xyz1234KIOuiop");
		dtoIn.setPhone("125346789");
		dtoIn.setPostalCode("78140");
		dtoIn.setSex(Sex.MAN);
		dtoIn.setTown("Versailles");
		dtoIn.setWallet(Float.valueOf(50F));

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		var result = super.mockMvc.perform(MockMvcRequestBuilders.put(UserRestControllerTest.URL_REGISTER)
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertNotNull(dtoOut.getId(), "User must have an id");
		Assertions.assertEquals(dtoIn.getIsLunchLady(), dtoOut.getIsLunchLady(), "Must BE a lunch lady");

		Assertions.assertEquals(dtoIn.getEmail(), dtoOut.getEmail(), "Must have the correct email");
		Assertions.assertEquals(dtoIn.getWallet(), dtoOut.getWallet(), "Must have the same wallet");
		Assertions.assertEquals(dtoIn.getName(), dtoOut.getName(), "Must have the same name");
		Assertions.assertEquals(dtoIn.getFirstname(), dtoOut.getFirstname(), "Must have the same firstname");
		Assertions.assertEquals(dtoIn.getAddress(), dtoOut.getAddress(), "Must have the same adress");
		Assertions.assertEquals(dtoIn.getPhone(), dtoOut.getPhone(), "Must have the same phone");
		Assertions.assertEquals(dtoIn.getPostalCode(), dtoOut.getPostalCode(), "Must have the same pc");
		Assertions.assertEquals(dtoIn.getSex(), dtoOut.getSex(), "Must have the same sex");
		Assertions.assertEquals(dtoIn.getTown(), dtoOut.getTown(), "Must have the same town");

		Assertions.assertTrue(EntityStatus.isEnabled(dtoOut.getStatus()), "User must be enabled");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete01() throws Exception {
		// Connect as normal user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);

		Assertions.assertTrue(EntityStatus.isEnabled(user.getStatus()), "User must be enabled");

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(UserRestControllerTest.URL_DELETE + user.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var responseBool = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), Boolean.class);
		Assertions.assertTrue(responseBool, "Should be true");
		var entity = this.userService.findEntity(user.getId());
		Assertions.assertEquals(EntityStatus.DELETED, entity.getStatus(), "Status should be deleted");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDelete02() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var ue = super.findASimpleUser();
		Assertions.assertTrue(ue.isEnabled(), "User must be enabled");
		var ueId = ue.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.delete(UserRestControllerTest.URL_DELETE + ueId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var responseBool = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), Boolean.class);
		Assertions.assertTrue(responseBool, "Should be true");
		var entity = this.userService.findEntity(ue.getId());
		Assertions.assertEquals(EntityStatus.DELETED, entity.getStatus(), "Status should be deleted");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateImg00() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);
		var oldImgId = user.getImageId();

		var dtoIn = new ImageDtoIn();
		dtoIn.setImagePath("img/test.png");
		dtoIn.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		var userId = user.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_UPDATE_IMG + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);
		Assertions.assertNotNull(dtoOut.getImageId(), "Image must have an id");

		var ie = super.imageService.findEntity(dtoOut.getImageId());

		Assertions.assertNotNull(ie.getImagePath(), "Image must have a path");
		Assertions.assertFalse(ie.getIsDefault(), "Image should NOT be a default one");
		Assertions.assertNotNull(ie.getImage64(), "Image should have a base64");
		Assertions.assertNotEquals(oldImgId, ie.getId(), "Image id must not be the same");
		Assertions.assertEquals(dtoIn.getImagePath(), ie.getImagePath(), "Image should have the same path");
		Assertions.assertEquals(dtoIn.getImage64(), ie.getImage64(), "Image should have the same base 64");

		var ue = super.userService.findEntity(user.getId());
		ie = ue.getImage();
		Assertions.assertNotNull(ie.getImagePath(), "Image must have a path");
		Assertions.assertFalse(ie.getIsDefault(), "Image should NOT be a default one");
		Assertions.assertNotNull(ie.getImage64(), "Image should have a base64");
		Assertions.assertNotEquals(oldImgId, ie.getId(), "Image id must not be the same");
		Assertions.assertEquals(dtoIn.getImagePath(), ie.getImagePath(), "Image should have the same path");
		Assertions.assertEquals(dtoIn.getImage64(), ie.getImage64(), "Image should have the same base 64");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testUpdateImg01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var user = super.getUserInToken(result);
		// Use another id
		var anOtherUserId = super.findASimpleUser(user.getId()).getId();

		Assertions.assertNotEquals(user.getId(), anOtherUserId, "Users id must be different");

		var dtoIn = new ImageDtoIn();
		dtoIn.setImagePath("img/test.png");
		dtoIn.setImage64(
				"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAYAAAAGCAIAAABvrngfAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAB9SURBVBhXAXIAjf8Bsry+/fz7z7yx8/LxNEVNERUaAgMEBMSxpRXy1xr/6uLDsAIDAgQDAgIiEQc3HSwaICXa6fP7AwQCAQEBGwkF9vf97ebpFBMUBAIBAv/27sPMyeTj6urr9t3h4QEBAgNLLQv/9u/g6O319/Ts6uMMHyvQyzf6YLHUTAAAAABJRU5ErkJggg==");

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_UPDATE_IMG + anOtherUserId)
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
						.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testFindImg01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var user = super.getUserInToken(result);
		// Use another id
		var anOtherUserId = super.findASimpleUser(user.getId()).getId();

		Assertions.assertNotEquals(user.getId(), anOtherUserId, "Users id must be different");

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(UserRestControllerTest.URL_FIND_IMG + anOtherUserId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindImg02() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var user = super.getUserInToken(result);
		// Use another id
		var anOtherUserId = super.findASimpleUser(user.getId()).getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(UserRestControllerTest.URL_FIND_IMG + anOtherUserId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), ImageDtoOut.class);
		Assertions.assertNotNull(dtoOut.getId(), "Image must have an id");
		Assertions.assertNotNull(dtoOut.getImagePath(), "Image must have a path");
		Assertions.assertTrue(dtoOut.isDefault(), "Image should the default one");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testFindImg00() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		var user = super.getUserInToken(result);
		var userId = user.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(UserRestControllerTest.URL_FIND_IMG + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), ImageDtoOut.class);
		Assertions.assertNotNull(dtoOut.getId(), "Image must have an id");
		Assertions.assertNotNull(dtoOut.getImagePath(), "Image must have a path");
		Assertions.assertTrue(dtoOut.isDefault(), "Image should be a default one");
		Assertions.assertEquals(user.getImageId(), dtoOut.getId(), "Image id must be the same");

		if (Sex.isMan(user.getSex())) {
			Assertions.assertEquals(IDefaultImages.USER_DEFAULT_MAN_IMG_PATH, dtoOut.getImagePath(),
					"Image should be the same as sex");
		} else if (Sex.isWoman(user.getSex())) {
			Assertions.assertEquals(IDefaultImages.USER_DEFAULT_WOMAN_IMG_PATH, dtoOut.getImagePath(),
					"Image should be the same as sex");
		} else if (Sex.isOther(user.getSex())) {
			Assertions.assertEquals(IDefaultImages.USER_DEFAULT_OTHER_IMG_PATH, dtoOut.getImagePath(),
					"Image should be the same as sex");
		}
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testActivate00() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var ue = super.findASimpleUser();
		ue.setStatus(EntityStatus.DISABLED);
		super.userDao.save(ue);
		var ueId = ue.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_ACTIVATE + ueId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(ueId, dtoOut.getId(), "Id must be the same");
		Assertions.assertTrue(EntityStatus.isEnabled(dtoOut.getStatus()), "User must be enabled");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testActivate01() throws Exception {
		// Connect as a normal user
		var result = super.logMeInAsNormalRandomUser();
		var conectedId = super.getUserIdInToken(result);

		// get another user id
		var ue = super.findASimpleUser(conectedId);
		ue.setStatus(EntityStatus.DISABLED);
		super.userDao.save(ue);
		var ueId = ue.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_ACTIVATE + ueId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDisable00() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var ue = super.findASimpleUser();
		Assertions.assertTrue(ue.isEnabled(), "User must be enabled");
		var ueId = ue.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_DEACTIVATE + ueId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(ueId, dtoOut.getId(), "Id must be the same");
		Assertions.assertTrue(EntityStatus.isDisabled(dtoOut.getStatus()), "User must be disabled");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testDisable01() throws Exception {
		// Connect as a normal user
		var result = super.logMeInAsNormalRandomUser();
		var conectedId = super.getUserIdInToken(result);

		// get another user id
		var ue = super.findASimpleUser(conectedId);
		Assertions.assertTrue(ue.isEnabled(), "User must be enabled");
		var ueId = ue.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_DEACTIVATE + ueId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testDisable02() throws Exception {
		// Connect as a normal user
		var result = super.logMeInAsNormalRandomUser();
		var conectedId = super.getUserIdInToken(result);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_DEACTIVATE + conectedId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCredit01() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var user = super.findASimpleUser();
		final var oldWallet = user.getWallet().doubleValue();
		final var amount = 10D;
		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.post(UserRestControllerTest.URL_CREDIT + user.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).param("amount", String.valueOf(amount)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Id must be the same");
		Assertions.assertEquals(amount + oldWallet, dtoOut.getWallet().doubleValue(), 0.001D,
				"User wallet must be up of the amount");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testCredit02() throws Exception {
		var user = super.findASimpleUser();

		// Connect as normal user
		var result = super.logMeIn(user.getEmail());
		final var amount = 10D;

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.post(UserRestControllerTest.URL_CREDIT + user.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).param("amount", String.valueOf(amount)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDebit01() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var user = super.findASimpleUser();
		user.setWallet(BigDecimal.valueOf(99D));
		user = this.userDao.save(user);

		final var oldWallet = user.getWallet().doubleValue();
		final var amount = 10D;
		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.post(UserRestControllerTest.URL_DEBIT + user.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).param("amount", String.valueOf(amount)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Id must be the same");
		Assertions.assertEquals(Math.max(0D, oldWallet - amount), dtoOut.getWallet().doubleValue(), 0.001D,
				"User wallet must be up of the amount");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testDebit02() throws Exception {
		var user = super.findASimpleUser();

		// Connect as normal user
		var result = super.logMeIn(user.getEmail());
		final var amount = 10D;

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.post(UserRestControllerTest.URL_DEBIT + user.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).param("amount", String.valueOf(amount)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testDebit03() throws Exception {
		// Connect as lunch lady
		var result = super.logMeInAsLunchLady();

		var user = super.findASimpleUser();
		user.setWallet(BigDecimal.valueOf(1D));
		user = this.userDao.save(user);

		final var amount = 10D;
		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.post(UserRestControllerTest.URL_DEBIT + user.getId())
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).param("amount", String.valueOf(amount)));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isPreconditionFailed());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd01() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);
		final var userId = user.getId();

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(UserRestControllerTest.URL_CHECKPWD + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.param("password", AbstractInitDataBase.USER_DEFAULT_PWD));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(user.getName(), dtoOut.getName(), "Same name");
		Assertions.assertEquals(user.getAddress(), dtoOut.getAddress(), "Same address");
		Assertions.assertEquals(user.getEmail(), dtoOut.getEmail(), "Same email");
		Assertions.assertEquals(user.getFirstname(), dtoOut.getFirstname(), "Same first name");
		Assertions.assertEquals(user.getPhone(), dtoOut.getPhone(), "Same phone");
		Assertions.assertEquals(user.getPostalCode(), dtoOut.getPostalCode(), "Same postal code");
		Assertions.assertEquals(user.getTown(), dtoOut.getTown(), "Same town");
		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Same id");
		Assertions.assertEquals(user.getImageId(), dtoOut.getImageId(), "Same img id");
		Assertions.assertEquals(user.getSex(), dtoOut.getSex(), "Same sex");
		Assertions.assertEquals(user.getStatus(), dtoOut.getStatus(), "Same status");
		Assertions.assertEquals(user.getIsLunchLady(), dtoOut.getIsLunchLady(), "Same lunch lady");
		Assertions.assertEquals(user.getRegistrationDate(), dtoOut.getRegistrationDate(), "Same registration date");
		Assertions.assertEquals(user.getWallet().doubleValue(), dtoOut.getWallet().doubleValue(), 0.0001D,
				"Same wallet");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testCheckPwd03() throws Exception {
		// Connect as simple user
		var userIdNotLunchLady = super.findASimpleUser().getId();
		// Now become the lunch lady
		var result = super.logMeInAsLunchLady();
		// The call
		result = super.mockMvc
				.perform(MockMvcRequestBuilders.get(UserRestControllerTest.URL_CHECKPWD + userIdNotLunchLady)
						.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.param("password", AbstractInitDataBase.USER_DEFAULT_PWD));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	@DisabledIf(value = "isProfileUnsecured", disabledReason = "Have no reason when profile is 'unsecured'")
	void testCheckPwd02() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();

		// The call with another user id
		result = super.mockMvc.perform(MockMvcRequestBuilders.get(UserRestControllerTest.URL_CHECKPWD + 1)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.param("password", AbstractInitDataBase.USER_DEFAULT_PWD));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdatePwd() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);
		final var userId = user.getId();

		var dtoIn = UserDtoHandler.dtoInfromEntity(this.userService.findEntity(userId));
		final var newPwd = "myNewPwd503";
		dtoIn.setPassword(newPwd);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_UPDATE + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(user.getName(), dtoOut.getName(), "Same name");
		Assertions.assertEquals(user.getAddress(), dtoOut.getAddress(), "Same address");
		Assertions.assertEquals(user.getEmail(), dtoOut.getEmail(), "Same email");
		Assertions.assertEquals(user.getFirstname(), dtoOut.getFirstname(), "Same first name");
		Assertions.assertEquals(user.getPhone(), dtoOut.getPhone(), "Same phone");
		Assertions.assertEquals(user.getPostalCode(), dtoOut.getPostalCode(), "Same postal code");
		Assertions.assertEquals(user.getTown(), dtoOut.getTown(), "Same town");
		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Same id");
		Assertions.assertEquals(user.getImageId(), dtoOut.getImageId(), "Same img id");
		Assertions.assertEquals(user.getSex(), dtoOut.getSex(), "Same sex");
		Assertions.assertEquals(user.getStatus(), dtoOut.getStatus(), "Same status");
		Assertions.assertEquals(user.getIsLunchLady(), dtoOut.getIsLunchLady(), "Same lunch lady");
		Assertions.assertEquals(user.getRegistrationDate(), dtoOut.getRegistrationDate(), "Same registration date");
		Assertions.assertEquals(user.getWallet().doubleValue(), dtoOut.getWallet().doubleValue(), 0.0001D,
				"Same wallet");

		Assertions.assertNotNull(this.userService.checkPassword(userId, newPwd), "Same password");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateName() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);
		final var userId = user.getId();

		var dtoIn = UserDtoHandler.dtoInfromEntity(this.userService.findEntity(userId));
		final var newName = "my new name";
		dtoIn.setName(newName);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_UPDATE + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(user.getAddress(), dtoOut.getAddress(), "Same address");
		Assertions.assertEquals(user.getEmail(), dtoOut.getEmail(), "Same email");
		Assertions.assertEquals(user.getFirstname(), dtoOut.getFirstname(), "Same first name");
		Assertions.assertEquals(user.getPhone(), dtoOut.getPhone(), "Same phone");
		Assertions.assertEquals(user.getPostalCode(), dtoOut.getPostalCode(), "Same postal code");
		Assertions.assertEquals(user.getTown(), dtoOut.getTown(), "Same town");
		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Same id");
		Assertions.assertEquals(user.getImageId(), dtoOut.getImageId(), "Same img id");
		Assertions.assertEquals(user.getSex(), dtoOut.getSex(), "Same sex");
		Assertions.assertEquals(user.getStatus(), dtoOut.getStatus(), "Same status");
		Assertions.assertEquals(user.getIsLunchLady(), dtoOut.getIsLunchLady(), "Same lunch lady");
		Assertions.assertEquals(user.getRegistrationDate(), dtoOut.getRegistrationDate(), "Same registration date");
		Assertions.assertEquals(user.getWallet().doubleValue(), dtoOut.getWallet().doubleValue(), 0.0001D,
				"Same wallet");

		var fromDb = this.userService.find(userId);

		Assertions.assertEquals(newName, fromDb.getName(), "Same new name");
		Assertions.assertEquals(newName, dtoOut.getName(), "Same new name");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateSex() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);
		final var userId = user.getId();

		var dtoIn = UserDtoHandler.dtoInfromEntity(this.userService.findEntity(userId));
		final var newSex = Sex.isMan(user.getSex()) ? Sex.WOMAN : Sex.MAN;
		dtoIn.setSex(newSex);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_UPDATE + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);
		Assertions.assertEquals(user.getName(), dtoOut.getName(), "Same name");
		Assertions.assertEquals(user.getAddress(), dtoOut.getAddress(), "Same address");
		Assertions.assertEquals(user.getEmail(), dtoOut.getEmail(), "Same email");
		Assertions.assertEquals(user.getFirstname(), dtoOut.getFirstname(), "Same first name");
		Assertions.assertEquals(user.getPhone(), dtoOut.getPhone(), "Same phone");
		Assertions.assertEquals(user.getPostalCode(), dtoOut.getPostalCode(), "Same postal code");
		Assertions.assertEquals(user.getTown(), dtoOut.getTown(), "Same town");
		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Same id");
		Assertions.assertEquals(user.getImageId(), dtoOut.getImageId(), "Same img id");
		Assertions.assertEquals(user.getStatus(), dtoOut.getStatus(), "Same status");
		Assertions.assertEquals(user.getIsLunchLady(), dtoOut.getIsLunchLady(), "Same lunch lady");
		Assertions.assertEquals(user.getRegistrationDate(), dtoOut.getRegistrationDate(), "Same registration date");
		Assertions.assertEquals(user.getWallet().doubleValue(), dtoOut.getWallet().doubleValue(), 0.0001D,
				"Same wallet");

		var fromDb = this.userService.find(userId);

		Assertions.assertEquals(newSex, fromDb.getSex(), "Same new sex");
		Assertions.assertEquals(newSex, dtoOut.getSex(), "Same new sex");
	}

	/**
	 * Test
	 *
	 * @throws Exception if an error occurred
	 */
	@Test
	void testUpdateNoPwd() throws Exception {
		// Connect as simple user
		var result = super.logMeInAsNormalRandomUser();
		var user = super.getUserInToken(result);
		final var userId = user.getId();

		var dtoIn = UserDtoHandler.dtoInfromEntity(this.userService.findEntity(userId));
		final String newPwd = null; // Means we do not change it
		dtoIn.setPassword(newPwd);

		var dtoInAsJsonString = this.mapper.writeValueAsString(dtoIn);

		// The call
		result = super.mockMvc.perform(MockMvcRequestBuilders.patch(UserRestControllerTest.URL_UPDATE + userId)
				.header(SecurityConstants.TOKEN_HEADER, super.getJWT(result))
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(dtoInAsJsonString));

		// The asserts
		result.andExpect(MockMvcResultMatchers.status().isOk());

		var dtoOut = this.mapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDtoOut.class);

		Assertions.assertEquals(user.getName(), dtoOut.getName(), "Same name");
		Assertions.assertEquals(user.getAddress(), dtoOut.getAddress(), "Same address");
		Assertions.assertEquals(user.getEmail(), dtoOut.getEmail(), "Same email");
		Assertions.assertEquals(user.getFirstname(), dtoOut.getFirstname(), "Same first name");
		Assertions.assertEquals(user.getPhone(), dtoOut.getPhone(), "Same phone");
		Assertions.assertEquals(user.getPostalCode(), dtoOut.getPostalCode(), "Same postal code");
		Assertions.assertEquals(user.getTown(), dtoOut.getTown(), "Same town");
		Assertions.assertEquals(user.getId(), dtoOut.getId(), "Same id");
		Assertions.assertEquals(user.getImageId(), dtoOut.getImageId(), "Same img id");
		Assertions.assertEquals(user.getSex(), dtoOut.getSex(), "Same sex");
		Assertions.assertEquals(user.getStatus(), dtoOut.getStatus(), "Same status");
		Assertions.assertEquals(user.getIsLunchLady(), dtoOut.getIsLunchLady(), "Same lunch lady");
		Assertions.assertEquals(user.getRegistrationDate(), dtoOut.getRegistrationDate(), "Same registration date");
		Assertions.assertEquals(user.getWallet().doubleValue(), dtoOut.getWallet().doubleValue(), 0.0001D,
				"Same wallet");

		var fromDb = this.userService.findEntity(userId);

		Assertions.assertNotNull(fromDb.getPassword(), "Not null, same as before");
	}

}
