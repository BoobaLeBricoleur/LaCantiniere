package stone.lunchtime.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.service.exception.EntityAlreadySavedException;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentPasswordException;
import stone.lunchtime.service.exception.InconsistentRoleException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.LackOfMoneyException;

@Service
public interface IUserService<E> extends IService<E, UserDtoOut> {

	/**
	 * Will search a user that has the given email.
	 *
	 * @param pEmail an email (cannot be null)
	 * @return the user found, or throws an exception
	 * @throws EntityNotFoundException if entity was not found
	 */
	@Transactional(readOnly = true)
	UserDtoOut find(String pEmail) throws EntityNotFoundException;

	/**
	 * Will check for a user password.
	 *
	 * @param pId       a user id (cannot be null)
	 * @param pPassword a password (cannot be null)
	 * @return the user found, or throws an exception
	 * @throws EntityNotFoundException       if entity was not found
	 * @throws InconsistentPasswordException if password are not the same
	 */
	@Transactional(readOnly = true)
	UserDtoOut checkPassword(Integer pId, String pPassword)
			throws EntityNotFoundException, InconsistentPasswordException;

	/**
	 * Registers a user. <br>
	 *
	 * The user can be a Lunch Lady only if there is no Lunch Lady in the database.
	 * Otherwise, this role can only be given through update and by a user who is a
	 * Lunch Lady himself. <br>
	 *
	 * @param pUser a new user
	 * @return the user inserted
	 * @throws EntityAlreadySavedException if user is already in data base
	 * @throws InconsistentRoleException   if pUser has the role Lunch Lady and
	 *                                     should not.
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut register(@Valid UserDtoIn pUser) throws EntityAlreadySavedException, InconsistentRoleException;

	/**
	 * Indicates if user with given email is already in database.
	 *
	 * @param pEmail an email.
	 * @return true if user with given email is in database, false if not
	 */
	@Transactional(readOnly = true)
	boolean exist(String pEmail);

	/**
	 * Updates a user. <br>
	 *
	 * You cannot update status, wallet nor image with this method. <br>
	 * But you can become or lose the Lunch Lady status.
	 *
	 * @param pIdToUpdate         the id of the user to update
	 * @param pNewDto             some new info for the user.
	 * @param pCanHandleLunchLady if true then lunch lady role can be changed,
	 *                            otherwise it will not be change.
	 * @return the updated user
	 * @throws EntityNotFoundException     if user with given id was not found
	 * @throws EntityAlreadySavedException if user changes its email and is already
	 *                                     in database
	 * @throws InconsistentRoleException   if try to change the Lunch Lady role and
	 *                                     cannot
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut update(Integer pIdToUpdate, UserDtoIn pNewDto, boolean pCanHandleLunchLady)
			throws EntityNotFoundException, EntityAlreadySavedException, InconsistentRoleException;

	/**
	 * Will remove money from user's wallet.
	 *
	 * @param pUserId a user id
	 * @param pAmount an amount of money
	 * @return the user updated
	 * @throws EntityNotFoundException if entity was not found
	 * @throws LackOfMoneyException    if user has not enough money in its wallet
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut debit(Integer pUserId, @Positive BigDecimal pAmount)
			throws EntityNotFoundException, LackOfMoneyException;

	/**
	 * Will remove money from user's wallet.
	 *
	 * @param pUserId a user id
	 * @param pAmount an amount of money
	 * @return the user updated
	 * @throws EntityNotFoundException if entity was not found
	 * @throws LackOfMoneyException    if user has not enough money in its wallet
	 */
	@Transactional(rollbackFor = Exception.class)
	E debitEntity(Integer pUserId, @Positive BigDecimal pAmount) throws EntityNotFoundException, LackOfMoneyException;

	/**
	 * Will add money to user's wallet.
	 *
	 * @param pUserId a user id
	 * @param pAmount an amount of money
	 * @return the user updated
	 * @throws EntityNotFoundException if entity was not found
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut credit(Integer pUserId, @Positive BigDecimal pAmount) throws EntityNotFoundException;

	/**
	 * Disables the user. <br>
	 *
	 * A disabled user cannot log in not change anything. Only a lunch lady can
	 * change this state.
	 *
	 * @param pUserId a user id
	 * @return the user disabled
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut disable(Integer pUserId) throws EntityNotFoundException, InconsistentStatusException;

	/**
	 * Enables the user. <br>
	 *
	 * A user enable can log in or change anything. Only a lunch lady can change
	 * this state.
	 *
	 * @param pUserId a user id
	 * @return the user enabled
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut enable(Integer pUserId) throws EntityNotFoundException, InconsistentStatusException;

	/**
	 * Changes the image of this element. <br>
	 *
	 * @param pUserId      a user id
	 * @param pNewImageDto the new image
	 * @return the user updated
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	@Transactional(rollbackFor = Exception.class)
	UserDtoOut updateImage(Integer pUserId, ImageDtoIn pNewImageDto)
			throws EntityNotFoundException, InconsistentStatusException;

}
