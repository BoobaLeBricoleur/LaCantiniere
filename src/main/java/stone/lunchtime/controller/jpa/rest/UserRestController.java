// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ExceptionDtoOut;
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
 * User controller.
 */
@RestController
@RequestMapping("/user")
@Tag(name = "User management API", description = "User management API")
public class UserRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(UserRestController.class);

	private final IUserService<UserEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public UserRestController(IUserService<UserEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Registers a user. <br>
	 *
	 * Post and Put are allowed. <br>
	 * You do not need to be connected. <br>
	 * Lunch lady role can only be set if there is none in the database. Otherwise
	 * you'll need to use update in order to set/unset the lunch lady role.
	 *
	 * @param pUser a new user
	 *
	 * @return the new subscribed user
	 * @throws InconsistentRoleException   if an error occurred
	 * @throws EntityAlreadySavedException if an error occurred
	 */
	@RequestMapping(value = "/register", method = { RequestMethod.PUT, RequestMethod.POST })
	@Observed(name = "rest.user.register", contextualName = "rest#user#register")
	@Operation(tags = {
			"User management API" }, summary = "Adds a user.", description = "Will add a user into the data base. Will return it with its id when done. You do not need to be connected in order to execute this action. Lunch lady role can only be set if there is none in the data base. Otherwise you'll need to use update in order to set/unset the lunch lady role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user was added and returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your user is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "User is already in the data base (email must be unique) or there is a role problem.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> registerUser(
			@Parameter(description = "User object that will be stored in database", required = true) @RequestBody UserDtoIn pUser)
			throws EntityAlreadySavedException, InconsistentRoleException {

		UserRestController.LOG.atInfo().log("--> registerUser - {}", pUser);
		var result = this.service.register(pUser);
		UserRestController.LOG.atInfo().log("<-- registerUser - New user has id {}", result.getId());
		return ResponseEntity.ok(result);
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
	@PatchMapping("/update/{userId}")
	@Observed(name = "rest.user.update", contextualName = "rest#user#update")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Updates a user.", description = "Will update a user already present in the data base. Will return it when done. You must be connected in order to update yourself, or have the lunch lady role in order to update someone. You need to set in the DTO in ONLY the values that you want to update, set the others to null. Note that status and image cannot be changed and only a lunch lady can add/remove the lunch lady role.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your user or userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot update this user (because you are not a lunch lady or it is not you).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> updateUser(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId,
			@Parameter(description = "User object that will be updated in database. ONLY not null values will be updated (status and image cannot be changed and only a lunch lady can add/remove the lunch lady role).", required = true) @RequestBody UserDtoIn pUser)
			throws EntityNotFoundException, EntityAlreadySavedException, InconsistentRoleException {

		UserRestController.LOG.atInfo().log("--> updateUser - {} - {}", pUserId, pUser);
		// Check for role, since only LunchLady can change LunchLady
		var result = this.service.update(pUserId, pUser, super.hasLunchLadyRole());
		UserRestController.LOG.atInfo().log("<-- updateUser - User {} is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@GetMapping("/checkpassword/{userId}")
	@Observed(name = "rest.user.checkpassword", contextualName = "rest#user#checkpassword")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Updates a user.", description = "Will update a user already present in the data base. Will return it when done. You must be connected in order to update yourself, or have the lunch lady role in order to update someone. You need to set in the DTO in ONLY the values that you want to update, set the others to null. Note that status and image cannot be changed and only a lunch lady can add/remove the lunch lady role.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user gave the right password.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your user or userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot check this user (because you are not a lunch lady or it is not you).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to check does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> checkPasswordUser(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId,
			@Parameter(description = "User actual password.", required = true) @RequestParam("password") String pPassword)
			throws EntityNotFoundException, InconsistentPasswordException {

		UserRestController.LOG.atInfo().log("--> checkPasswordUser - {}", pUserId);
		var result = this.service.checkPassword(pUserId, pPassword);
		UserRestController.LOG.atInfo().log(
				"<-- checkPasswordUser - Checked password with succes for user {}, request made by user user {}",
				result.getId(), this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@DeleteMapping("/delete/{userId}")
	@Observed(name = "rest.user.delete", contextualName = "rest#user#delete")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Deletes a user.", description = "Will delete a user already present in the data base. Will return true when done. Note that element is not realy deleted from database but will change its status to DELETE (2). You must be connected in order to delete yourself, or have the lunch lady role in order to delete someone.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user was deleted and true is returned in the response body.", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot delete this user (because you are not a lunch lady or it is not you).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to delete does not exist or is not a deleteable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<Boolean> deleteUserById(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId)
			throws EntityNotFoundException, InconsistentStatusException {

		UserRestController.LOG.atInfo().log("--> deleteUserById - {}", pUserId);
		var result = this.service.delete(pUserId);
		UserRestController.LOG.atInfo().log("<-- deleteUserById - User {} is deleted by user {}", pUserId,
				this.getConnectedUserId());
		return ResponseEntity.ok(result.isDeleted());
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
	@PatchMapping("/deactivate/{userId}")
	@Observed(name = "rest.user.deactivate", contextualName = "rest#user#deactivate")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Deactivates a user.", description = "Will deactivate a user already present in the data base. Will return it when done. Will change the user's status to DISABLED (1). You must be connected and have the lunch lady role in order to deactivate someone.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user was deactivated and returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or you are not a lunch lady.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to deactivate does not exist or is not a deactivable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> deactivateUserById(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId)
			throws EntityNotFoundException, InconsistentStatusException {

		UserRestController.LOG.atInfo().log("--> deactivateUserById - {}", pUserId);
		var result = this.service.disable(pUserId);
		UserRestController.LOG.atInfo().log("<-- deactivateUserById - User {} is deactivated by lunch lady {}", pUserId,
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@PatchMapping("/activate/{userId}")
	@Observed(name = "rest.user.activate", contextualName = "rest#user#activate")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Activates a user.", description = "Will activate a user already present in the data base. Will return it when done. Will change the user's status to ENABLED (0). You must be connected and have the lunch lady role in order to activate someone.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user was activated and returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or you are not a lunch lady.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to activate does not exist or is not an activable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> activateUserById(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId)
			throws EntityNotFoundException, InconsistentStatusException {

		UserRestController.LOG.atInfo().log("--> activateUserById - {}", pUserId);
		var result = this.service.enable(pUserId);
		UserRestController.LOG.atInfo().log("<-- activateUserById - User {} is activated by lunch lady {}", pUserId,
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@PostMapping("/credit/{userId}")
	@Observed(name = "rest.user.credit", contextualName = "rest#user#credit")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Adds money to a user's wallet.", description = "Will add money to a user's wallet already present in the data base. Will return it when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user's wallet was updated and user is returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or you are not a lunch lady.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "User was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> creditUserById(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId,
			@Parameter(description = "The amount of money to add to the user's wallet.", required = true) @RequestParam("amount") BigDecimal pAmount)
			throws EntityNotFoundException {

		UserRestController.LOG.atInfo().log("--> creditUserById - {} of {}", pUserId, pAmount);
		var result = this.service.credit(pUserId, pAmount);
		UserRestController.LOG.atInfo().log("<-- creditUserById - User {} is credited of {} by lunch lady {}", pUserId, pAmount,
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@PostMapping("/debit/{userId}")
	@Observed(name = "rest.user.debit", contextualName = "rest#user#debit")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Removes money to a user's wallet.", description = "Will remove money to a user's wallet already present in the data base. Will return it when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user's wallet was updated and user is returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or you are not a lunch lady.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "User was not found or the wallet does not have enought money.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> debitUserById(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId,
			@Parameter(description = "The amount of money to remove from the user's wallet.", required = true) @RequestParam("amount") BigDecimal pAmount)
			throws EntityNotFoundException, LackOfMoneyException {

		UserRestController.LOG.atInfo().log("--> debitUserById - {} of {}", pUserId, pAmount);
		var result = this.service.debit(pUserId, pAmount);
		UserRestController.LOG.atInfo().log("<-- debitUserById - User {} is credited of {} by user {}", pUserId, pAmount,
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@GetMapping("/find/{userId}")
	@Observed(name = "rest.user.byid", contextualName = "rest#user#byid")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Finds one user.", description = "Will find a user already present in the data base. Will return it when done. You must be connected in order to find yourself, or have the lunch lady role in order to find someone.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The user was found and returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot find this user (because you are not a lunch lady or it is not you).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to find does not exist or is not findable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> userById(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId)
			throws EntityNotFoundException {

		UserRestController.LOG.atInfo().log("--> userById - {}", pUserId);
		var result = this.service.find(pUserId);
		UserRestController.LOG.atInfo().log("<-- userById - User {} has found user {}", this.getConnectedUserId(), pUserId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Finds all users. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 *
	 * @return all the users found or an empty list if none
	 */
	@GetMapping("/findall")
	@Observed(name = "rest.user.findall", contextualName = "rest#user#findall")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Finds all users.", description = "Will find all users already present in the data base. Will return them when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All users found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<UserDtoOut>> findAllUsers() {

		UserRestController.LOG.atInfo().log("--> findAllUsers");
		var result = this.service.findAll();
		UserRestController.LOG.atInfo().log("<-- findAllUsers - Lunch Lady {} has found {} users", this.getConnectedUserId(),
				result.size());
		return ResponseEntity.ok(result);
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
	@PatchMapping("/updateimg/{userId}")
	@Observed(name = "rest.user.updateimg", contextualName = "rest#user#updateimg")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Updates a user image.", description = "Will update the image of a user already present in the data base. Will return it when done. You must be connected in order to update yourself, or have the lunch lady role in order to update someone.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your user's image was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = UserDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot update this user (because you are not a lunch lady or it is not you).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist or has not the correct status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<UserDtoOut> updateUserImage(
			@Parameter(description = "The user's id", required = true) @PathVariable("userId") Integer pUserId,
			@Parameter(description = "Image object that will be updated in database. ", required = true) @RequestBody ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {

		UserRestController.LOG.atInfo().log("--> updateUserImage - {}", pImage);
		// Check for role, since only LunchLady can change LunchLady
		var result = this.service.updateImage(pUserId, pImage);
		UserRestController.LOG.atInfo().log("<-- updateUserImage - User {} image is updated by user {}", result.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	@GetMapping("/findimg/{userid}")
	@Observed(name = "rest.user.findimg", contextualName = "rest#user#findimg")
	@PreAuthorize("#pUserId == authentication.details.id or hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"User management API" }, summary = "Finds the user's image.", description = "Will find a user's image already present in the data base. Will return it when done. You must be connected in order to find your image, or have the lunch lady role in order to find someone's image.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The image was found and returned in the response body.", content = @Content(schema = @Schema(implementation = ImageDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your userId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or cannot find this user (because you are not a lunch lady or it is not you).", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to find does not exist or is not findable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ImageDtoOut> findUserImage(
			@Parameter(description = "The user's id", required = true) @PathVariable("userid") Integer pUserId)
			throws EntityNotFoundException {

		UserRestController.LOG.atInfo().log("--> findUserImage - {}", pUserId);
		var result = this.service.findEntity(pUserId);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		UserRestController.LOG.atInfo().log("<-- findUserImage - User's image {} found by user {}", dtoOut.getId(),
				this.getConnectedUserId());
		return ResponseEntity.ok(dtoOut);
	}

}
