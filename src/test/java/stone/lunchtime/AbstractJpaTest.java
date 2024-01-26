// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import stone.lunchtime.dao.jpa.IConstraintDao;
import stone.lunchtime.dao.jpa.IImageDao;
import stone.lunchtime.dao.jpa.IIngredientDao;
import stone.lunchtime.dao.jpa.IMealDao;
import stone.lunchtime.dao.jpa.IMenuDao;
import stone.lunchtime.dao.jpa.IOrderDao;
import stone.lunchtime.dao.jpa.IQuantityDao;
import stone.lunchtime.dao.jpa.IRoleDao;
import stone.lunchtime.dao.jpa.IUserDao;
import stone.lunchtime.dto.out.AbstractDtoOut;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.jpa.ConstraintEntity;
import stone.lunchtime.entity.jpa.IngredientEntity;
import stone.lunchtime.entity.jpa.MealEntity;
import stone.lunchtime.entity.jpa.MenuEntity;
import stone.lunchtime.entity.jpa.OrderEntity;
import stone.lunchtime.entity.jpa.UserEntity;
import stone.lunchtime.service.IAuthenticationService;
import stone.lunchtime.service.IConstraintService;
import stone.lunchtime.service.IEmailService;
import stone.lunchtime.service.IIngredientService;
import stone.lunchtime.service.IMealService;
import stone.lunchtime.service.IMenuService;
import stone.lunchtime.service.IOrderService;
import stone.lunchtime.service.IUserService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.impl.jpa.ImageService;

/**
 * Mother class of all tests.
 */
@SpringBootTest
@Transactional
@Rollback(true)
@AutoConfigureMockMvc // Keep here for spring loader ...
public abstract class AbstractJpaTest {
	@Autowired
	protected Environment env;

	@Autowired
	protected IAuthenticationService authenticationService;
	@Autowired
	protected IUserService<UserEntity> userService;
	@Autowired
	protected IIngredientService<IngredientEntity> ingredientService;
	@Autowired
	protected IMealService<MealEntity> mealService;
	@Autowired
	protected IMenuService<MenuEntity> menuService;
	@Autowired
	protected IConstraintService<ConstraintEntity> constraintService;
	@Autowired
	protected IOrderService<OrderEntity> orderService;
	@Autowired
	protected IEmailService emailService;
	@Autowired
	protected ImageService imageService;

	@Autowired
	protected IUserDao userDao;
	@Autowired
	protected IOrderDao orderDao;
	@Autowired
	protected IConstraintDao constraintDao;
	@Autowired
	protected IIngredientDao ingredientDao;
	@Autowired
	protected IMenuDao menuDao;
	@Autowired
	protected IMealDao mealDao;
	@Autowired
	protected IQuantityDao quantityMealDao;
	@Autowired
	protected IRoleDao roleDao;
	@Autowired
	protected IImageDao imageDao;

	/**
	 * Takes random values from the given list and return them.
	 *
	 * @param pHowMany how many elements to give back
	 * @param pFrom    where to take elements
	 * @return a list with elements taken from the given list
	 */
	@Disabled("Not a test, used for simplification.")
	protected <T> List<T> generateList(int pHowMany, List<T> pFrom) {
		if (pHowMany != 0 && pFrom != null && !pFrom.isEmpty()) {
			final var fromSize = pFrom.size();
			if (pHowMany > fromSize) {
				List<T> copy = new ArrayList<>();
				Collections.copy(copy, pFrom);
				Collections.shuffle(copy);
				return copy;
			}
			Set<T> resu = new HashSet<>();
			var random = new Random();
			while (pHowMany > 0) {
				if (resu.add(pFrom.get(random.nextInt(fromSize)))) {
					pHowMany--;
				}
			}
			return new ArrayList<>(resu);
		}
		return Collections.emptyList();
	}

	/**
	 * Transforms an entity list into there id's.
	 *
	 * @param pFrom where to take elements
	 * @return a list with entity's id
	 */
	@Disabled("Not a test, used for simplification.")
	protected List<Integer> transformInIdsList(List<? extends AbstractDtoOut> pFrom) {
		if (pFrom != null && !pFrom.isEmpty()) {
			List<Integer> resu = new ArrayList<>();
			for (AbstractDtoOut dto : pFrom) {
				resu.add(dto.getId());
			}
			return resu;
		}
		return Collections.emptyList();
	}

	/**
	 * Finds a simple user in the database. A user in an Enabled state and not a
	 * lunch lady (will do the asser).
	 *
	 * @param idsToAvoid some ids to avoid
	 * @return a simpler user
	 */
	protected UserEntity findASimpleUser(Integer... idsToAvoid) {
		var allUsers = this.userService.findAll();
		Collections.shuffle(allUsers);
		allUser: for (UserDtoOut user : allUsers) {
			Assertions.assertNotNull(user, "User must exist");
			if (user.isEnabled() && Boolean.FALSE.equals(user.getIsLunchLady())) {
				Assertions.assertEquals(EntityStatus.ENABLED, user.getStatus(), "User status must be ok");
				Assertions.assertFalse(user.getIsLunchLady(), "User must not be a lunch lady");
				if (idsToAvoid != null && idsToAvoid.length > 0) {
					for (Integer idToAvoid : idsToAvoid) {
						if (user.getId().equals(idToAvoid)) {
							continue allUser;
						}
					}
				}
				try {
					return this.userService.findEntity(user.getId());
				} catch (EntityNotFoundException e) {
					throw new IllegalStateException("Should not happend, no simple user found !", e);
				}
			} // End if not lunch lady
		} // for all users
		throw new IllegalStateException("No simple user found !");
	}

}
