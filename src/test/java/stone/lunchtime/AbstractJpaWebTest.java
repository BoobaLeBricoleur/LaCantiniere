// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.entity.jpa.IngredientEntity;
import stone.lunchtime.entity.jpa.MealEntity;
import stone.lunchtime.entity.jpa.MenuEntity;
import stone.lunchtime.init.AbstractInitDataBase;
import stone.lunchtime.service.impl.jpa.OrderService;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Mother class of all tests that uses Web (for controller so).
 */
public abstract class AbstractJpaWebTest extends AbstractJpaTest {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractJpaWebTest.class);

	@Autowired
	protected ObjectMapper mapper;

	@Autowired
	protected MockMvc mockMvc;

	/**
	 * Logs the user as a Lunch Lady. Creates a session.
	 *
	 * @return the ResultActions
	 * @throws Exception if an error occurred
	 */
	protected ResultActions logOut(ResultActions result) throws Exception {
		return this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGOUT_URL)
				.header(SecurityConstants.TOKEN_HEADER, this.getJWT(result)));
	}

	/**
	 * Logs the user. Creates a session. Will assert the ok HttpStatus before
	 * returning the action.
	 *
	 * @param email an email
	 * @return the ResultActions
	 * @throws Exception if an error occurred
	 */
	protected ResultActions logMeIn(String email) throws Exception {
		var result = this.mockMvc.perform(MockMvcRequestBuilders.get(SecurityConstants.AUTH_LOGIN_URL)
				.param("email", email).param("password", AbstractInitDataBase.USER_DEFAULT_PWD));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		return result;
	}

	/**
	 * Logs the user as a Lunch Lady. Creates a session. Will assert the ok
	 * HttpStatus before returning the action.
	 *
	 * @return the ResultActions
	 * @throws Exception if an error occurred
	 */
	protected ResultActions logMeInAsLunchLady() throws Exception {
		var result = this.logMeIn(AbstractInitDataBase.USER_EXISTING_EMAIL);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		return result;
	}

	/**
	 * Gets the JWT inside the result.
	 *
	 * @param result where to find the JWT
	 * @return the JWT
	 */
	protected String getJWT(ResultActions result) {
		return result.andReturn().getResponse().getHeader(SecurityConstants.TOKEN_HEADER);
	}

	/**
	 * Gets the user dto inside the JWT.
	 *
	 * @param result where to find the JWT.
	 * @return the user dto inside the JWT
	 */
	protected UserDtoOut getUserInToken(ResultActions result) {
		var signingKey = this.env
				.getProperty("configuration.jwt.key",
						"-KaPdSgVkXp2s5v8y/B?E(H+MbQeThWmZq3t6w9z$C&F)J@NcRfUjXn2r5u7x!A%")
				.getBytes(StandardCharsets.UTF_8);
		var secretKey = Keys.hmacShaKeyFor(signingKey);

		var token = this.getJWT(result);
		var parsedToken = Jwts.parser().verifyWith(secretKey).build()
				.parseSignedClaims(token.replace(SecurityConstants.TOKEN_PREFIX, ""));

		var username = parsedToken.getPayload().getSubject();

		if (!ObjectUtils.isEmpty(username)) {
			@SuppressWarnings("unchecked")
			Map<String, ?> userDto = (Map<String, ?>) parsedToken.getPayload().get(SecurityConstants.TOKEN_USER);
			return new UserDtoOut(userDto);
		}
		return null;
	}

	/**
	 * Gets the user's id inside the JWT.
	 *
	 * @param result where to find the JWT.
	 * @return the user's id inside the JWT
	 */
	protected Integer getUserIdInToken(ResultActions result) {
		var userDtoOut = this.getUserInToken(result);
		return userDtoOut.getId();
	}

	/**
	 * Logs as a random user that is not a Lunch Lady. Creates a session.
	 *
	 * @param idsToAvoid an id to avoid
	 * @return the ResultActions
	 * @throws Exception if an error occurred
	 */
	protected ResultActions logMeInAsNormalRandomUser(Integer... idsToAvoid) throws Exception {
		var userNotLunchLady = super.findASimpleUser(idsToAvoid);
		var result = this.logMeIn(userNotLunchLady.getEmail());
		result.andExpect(MockMvcResultMatchers.status().isOk());
		return result;
	}

	/**
	 * Gets a valid menu for this week
	 *
	 * @param forThisWeek if true menu will be for this week only
	 * @return a valid menu for this week
	 */
	protected MenuEntity getValidMenu(boolean forThisWeek) {
		var resu = forThisWeek ? this.menuDao.findAllAvailableForWeek(String.valueOf(OrderService.getCurrentWeekId()))
				: this.menuDao.findAllEnabled();
		if (resu.isPresent()) {
			var menus = resu.get();
			var random = new Random();
			return menus.get(random.nextInt(menus.size()));
		}
		throw new RuntimeException("No valid menu found!");
	}

	/**
	 * Gets a valid meal for this week
	 *
	 * @param forThisWeek if true meal will be for this week only
	 * @return a valid meal for this week
	 */
	protected MealEntity getValidMeal(boolean forThisWeek) {
		var resu = forThisWeek ? this.mealDao.findAllAvailableForWeek(String.valueOf(OrderService.getCurrentWeekId()))
				: this.mealDao.findAllEnabled();
		if (resu.isPresent()) {
			var menus = resu.get();
			var random = new Random();
			return menus.get(random.nextInt(menus.size()));
		}
		throw new RuntimeException("No valid menu found!");
	}

	/**
	 * Gets a valid ingredient
	 *
	 * @return a valid ingredient
	 */
	protected IngredientEntity getValidIngredient() {
		var resu = this.ingredientDao.findAllEnabled();
		if (resu.isPresent()) {
			var menus = resu.get();
			var random = new Random();
			return menus.get(random.nextInt(menus.size()));
		}
		throw new RuntimeException("No valid Ingredient found!");
	}

	/**
	 * Indicates that the unsecured profile is on.
	 *
	 * @return true if the unsecured profile is on
	 */
	protected final boolean isProfileUnsecured() {
		var profiles = this.env.getActiveProfiles();
		for (String profile : profiles) {
			if ("unsecured".equalsIgnoreCase(profile)) {
				AbstractJpaWebTest.LOG.atWarn().log("We are un UNSECURED profile");
				return true;
			}
		}
		AbstractJpaWebTest.LOG.atInfo().log("We are un SECURE profile");
		return false;
	}

}
