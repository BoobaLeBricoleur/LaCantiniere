// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import io.micrometer.observation.annotation.Observed;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.entity.jpa.UserEntity;
import stone.lunchtime.service.IUserService;
import stone.lunchtime.service.exception.EntityAlreadySavedException;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentPasswordException;
import stone.lunchtime.service.exception.InconsistentRoleException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.LackOfMoneyException;

/**
 * User controller using GraphQL.
 */
@Controller
public class UserGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(UserGqlController.class);

	private final IUserService<UserEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public UserGqlController(IUserService<UserEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Registers a user. <br>
	 *
	 * You do not need to be connected. <br>
	 * Lunch lady role can only be set if there is none in the database. Otherwise,
	 * you'll need to use update in order to set/unset the lunch lady role.
	 *
	 * @param pUser a new user
	 *
	 * @return the new subscribed user
	 * @throws InconsistentRoleException   if an error occurred
	 * @throws EntityAlreadySavedException if an error occurred
	 */
	@PreAuthorize("isAuthenticated()")
	@Observed(name = "graphql.user.register", contextualName = "graphql#user#register")
	@MutationMapping
	public UserDtoOut registerUser(@Argument(name = "user") UserDtoIn pUser)
			throws EntityAlreadySavedException, InconsistentRoleException {

		UserGqlController.LOG.atInfo().log("--> registerUser - {}", pUser);
		var result = this.service.register(pUser);
		UserGqlController.LOG.atInfo().log("<-- registerUser - New user has id {}", result.getId());
		return result;
		// Sample Use :
		// mutation RegisterUser($myUser: UserDtoIn!) {
		// registerUser(user: $myUser) {
		// id
		// }
		// }
		// // Variables
		// {
		// "myUser": {
		// "password": "lolssss45_",
		// "wallet": 5,
		// "email": "atoto@aol.com"
		// }
		// }
	}

	/**
	 * Updates a user. <br>
	 *
	 * You need to set in the DTO in ONLY the values that you want to update, set
	 * the others to null. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to update yourself. <br>
	 * You cannot change status with this method. <br>
	 * Only Lunch Lady can update the lunch lady role.
	 *
	 * @param pUserId id of the user to be updated
	 * @param pUser   where to find the new information
	 *
	 * @return the user updated
	 * @throws InconsistentRoleException   if an error occurred
	 * @throws EntityAlreadySavedException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.user.update", contextualName = "graphql#user#update")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public UserDtoOut updateUser(@Argument("id") Integer pUserId, @Argument("user") UserDtoIn pUser)
			throws EntityNotFoundException, EntityAlreadySavedException, InconsistentRoleException {

		UserGqlController.LOG.atInfo().log("--> updateUser - {} - {}", pUserId, pUser);
		// Check for role, since only LunchLady can change LunchLady
		var result = this.service.update(pUserId, pUser, super.hasLunchLadyRole());
		UserGqlController.LOG.atInfo().log("<-- updateUser - User {} is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Checks a user actual password. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to check for yourself.
	 * <br>
	 *
	 * @param pUserId   id of the user to be updated
	 * @param pPassword the actual user password
	 * @return the user if password are the same or an exception
	 * @throws InconsistentPasswordException if pwd is not right
	 * @throws EntityNotFoundException       if entity was not found
	 */
	@QueryMapping
	@Observed(name = "graphql.user.checkpassword", contextualName = "graphql#user#checkpassword")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public UserDtoOut checkPasswordUser(@Argument("id") Integer pUserId, @Argument("password") String pPassword)
			throws EntityNotFoundException, InconsistentPasswordException {

		UserGqlController.LOG.atInfo().log("--> checkPasswordUser - {}", pUserId);
		var result = this.service.checkPassword(pUserId, pPassword);
		UserGqlController.LOG.atInfo().log(
				"<-- checkPasswordUser - Checked password with succes for user {}, request made by user user {}",
				result.getId(), this.getConnectedUserId());
		return result;
	}

	/**
	 * Deletes a user. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to delete yourself. <br>
	 * User will still be in the database but its status will be deleted. He will be
	 * able to do nothing. This status is permanent.
	 *
	 * @param pUserId id of the user to be deleted
	 *
	 * @return the user deleted
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.user.delete", contextualName = "graphql#user#delete")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public boolean deleteUserById(@Argument("id") Integer pUserId)
			throws EntityNotFoundException, InconsistentStatusException {

		UserGqlController.LOG.atInfo().log("--> deleteUserById - {}", pUserId);
		var result = this.service.delete(pUserId);
		UserGqlController.LOG.atInfo().log("<-- deleteUserById - User {} is deleted by user {}", pUserId,
				this.getConnectedUserId());
		return result.isDeleted();
	}

	/**
	 * Deactivates a user. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * User deactivated cannot do anything until they are reactivated.
	 *
	 * @param pUserId id of the user to be deactivated
	 *
	 * @return the user deactivated
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.user.deactivate", contextualName = "graphql#user#deactivate")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public UserDtoOut deactivateUserById(@Argument("id") Integer pUserId)
			throws EntityNotFoundException, InconsistentStatusException {

		UserGqlController.LOG.atInfo().log("--> deactivateUserById - {}", pUserId);
		var result = this.service.disable(pUserId);
		UserGqlController.LOG.atInfo().log("<-- deactivateUserById - User {} is deactivated by lunch lady {}", pUserId,
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Activates a user. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param pUserId id of the user to be activated
	 *
	 * @return the user activated
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.user.activate", contextualName = "graphql#user#activate")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public UserDtoOut activateUserById(@Argument("id") Integer pUserId)
			throws EntityNotFoundException, InconsistentStatusException {

		UserGqlController.LOG.atInfo().log("--> activateUserById - {}", pUserId);
		var result = this.service.enable(pUserId);
		UserGqlController.LOG.atInfo().log("<-- activateUserById - User {} is activated by lunch lady {}", pUserId,
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Will add money to the user's wallet. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param pUserId id of the user
	 * @param pAmount amount of money that will be added to user's wallet
	 *
	 * @return the user credited
	 * @throws EntityNotFoundException if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.user.credit", contextualName = "graphql#user#credit")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public UserDtoOut creditUserById(@Argument("id") Integer pUserId, @Argument("amount") Float pAmount)
			throws EntityNotFoundException {

		UserGqlController.LOG.atInfo().log("--> creditUserById - {} of {}", pUserId, pAmount);
		var result = this.service.credit(pUserId, BigDecimal.valueOf(pAmount));
		UserGqlController.LOG.atInfo().log("<-- creditUserById - User {} is credited of {} by lunch lady {}", pUserId, pAmount,
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Will remove money to the user's wallet. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param pUserId id of the user
	 * @param pAmount amount of money that will be removed of user's wallet
	 *
	 * @return the user debited
	 * @throws LackOfMoneyException    if an error occurred
	 * @throws EntityNotFoundException if an error occurred
	 *
	 */
	@MutationMapping
	@Observed(name = "graphql.user.debit", contextualName = "graphql#user#debit")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public UserDtoOut debitUserById(@Argument("id") Integer pUserId, @Argument("amount") Float pAmount)
			throws EntityNotFoundException, LackOfMoneyException {

		UserGqlController.LOG.atInfo().log("--> debitUserById - {} of {}", pUserId, pAmount);
		var result = this.service.debit(pUserId, BigDecimal.valueOf(pAmount));
		UserGqlController.LOG.atInfo().log("<-- debitUserById - User {} is credited of {} by user {}", pUserId, pAmount,
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Finds a user. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to find yourself. <br>
	 *
	 * @param pUserId id of the user you are looking for
	 *
	 * @return the user found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 *
	 */
	@QueryMapping
	@Observed(name = "graphql.user.byid", contextualName = "graphql#user#byid")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public UserDtoOut userById(@Argument(name = "id") Integer pUserId) throws EntityNotFoundException {
		UserGqlController.LOG.atInfo().log("--> userById - {}", pUserId);
		var result = this.service.find(pUserId);
		UserGqlController.LOG.atInfo().log("<-- userById - User {} has found user {}", this.getConnectedUserId(), pUserId);
		return result;
	}

	/**
	 * Finds all users. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 *
	 * @return all the users found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.user.findall", contextualName = "graphql#user#findall")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public List<UserDtoOut> findAllUsers() {

		UserGqlController.LOG.atInfo().log("--> findAllUsers");
		var result = this.service.findAll();
		UserGqlController.LOG.atInfo().log("<-- findAllUsers - Lunch Lady {} has found {} users", this.getConnectedUserId(),
				result.size());
		return result;
	}

	/**
	 * Updates a user's image. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to update yourself. <br>
	 *
	 * @param pUserId id of the user to be updated
	 * @param pImage  where to find the new information
	 *
	 * @return the user updated
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.user.updateimg", contextualName = "graphql#user#updateimg")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public UserDtoOut updateUserImage(@Argument(name = "id") Integer pUserId,
			@Argument(name = "image") ImageDtoIn pImage) throws EntityNotFoundException, InconsistentStatusException {

		UserGqlController.LOG.atInfo().log("--> updateImage - {}", pImage);
		// Check for role, since only LunchLady can change LunchLady
		var result = this.service.updateImage(pUserId, pImage);
		UserGqlController.LOG.atInfo().log("<-- updateImage - User {} image is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return result;
	}

	/**
	 * Finds a user's image. <br>
	 *
	 * You need to be connected. <br>
	 * If you are not the lunch lady, you will only be able to find yourself. <br>
	 *
	 * @param pUserId id of the user's image you are looking for
	 *
	 * @return the image found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 *
	 */
	@QueryMapping
	@Observed(name = "graphql.user.findimg", contextualName = "graphql#user#findimg")
	@PreAuthorize("isAuthenticated() and (#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY'))")
	public ImageDtoOut findUserImage(@Argument("id") Integer pUserId) throws EntityNotFoundException {

		UserGqlController.LOG.atInfo().log("--> findUserImage - {}", pUserId);
		var result = this.service.findEntity(pUserId);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		UserGqlController.LOG.atInfo().log("<-- findUserImage - User's image {} found by user {}", dtoOut.getId(),
				this.getConnectedUserId());
		return dtoOut;
	}

}
