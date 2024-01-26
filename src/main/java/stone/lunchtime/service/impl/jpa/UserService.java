// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IRoleDao;
import stone.lunchtime.dao.jpa.IUserDao;
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.jpa.handler.UserDtoHandler;
import stone.lunchtime.dto.out.UserDtoOut;
import stone.lunchtime.entity.EntityStatus;
import stone.lunchtime.entity.RoleLabel;
import stone.lunchtime.entity.jpa.RoleEntity;
import stone.lunchtime.entity.jpa.UserEntity;
import stone.lunchtime.service.IDefaultImages;
import stone.lunchtime.service.IUserService;
import stone.lunchtime.service.exception.EntityAlreadySavedException;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentPasswordException;
import stone.lunchtime.service.exception.InconsistentRoleException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.LackOfMoneyException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * User service.
 */
@Service
public class UserService extends AbstractService<UserEntity, UserDtoOut> implements IUserService<UserEntity> {
	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	private final IUserDao userDao;

	private final IRoleDao roleDao;

	private final ImageService imageService;

	private final BCryptPasswordEncoder passwordEncoder;

	/**
	 * Constructor.
	 *
	 * @param pMapper          the json mapper.
	 * @param pUserDao         user dao
	 * @param pRoleDao         role dao
	 * @param pImageService    image service
	 * @param pPasswordEncoder password encoder
	 */
	protected UserService(ObjectMapper pMapper, IUserDao pUserDao, IRoleDao pRoleDao, ImageService pImageService,
			BCryptPasswordEncoder pPasswordEncoder) {
		super(pMapper);
		this.imageService = pImageService;
		this.userDao = pUserDao;
		this.passwordEncoder = pPasswordEncoder;
		this.roleDao = pRoleDao;
	}

	/**
	 * Will search a user that has the given email.
	 *
	 * @param pEmail an email (cannot be null)
	 * @return the user found, or throws an exception
	 * @throws EntityNotFoundException if entity was not found
	 */
	@Override
	public UserDtoOut find(String pEmail) throws EntityNotFoundException {
		UserService.LOG.atDebug().log("find - {}", pEmail);
		ValidationUtils.isNotEmpty(pEmail, "Email cannot be null or empty");
		var opResult = this.userDao.findOneByEmail(pEmail);
		if (opResult.isPresent()) {
			var result = opResult.get();
			UserService.LOG.atInfo().log("find - OK found entity for email={}", pEmail);
			return UserDtoHandler.dtoOutfromEntity(result);
		}
		UserService.LOG.atWarn().log("find - KO No user found with email={}", pEmail);
		throw new EntityNotFoundException("Utilisateur introuvable", pEmail);
	}

	/**
	 * Will check for a user password.
	 *
	 * @param pId       a user id (cannot be null)
	 * @param pPassword a password (cannot be null)
	 * @return the user found, or throws an exception
	 * @throws EntityNotFoundException       if entity was not found
	 * @throws InconsistentPasswordException if password are not the same
	 *
	 */
	@Override
	public UserDtoOut checkPassword(Integer pId, String pPassword)
			throws EntityNotFoundException, InconsistentPasswordException {
		UserService.LOG.atDebug().log("checkPassword - {}", pId);
		ValidationUtils.isNotNull(pId, "ID cannot be null");
		ValidationUtils.isNotEmpty(pPassword, "Password cannot be null or empty");
		var opResult = this.userDao.findById(pId);
		if (opResult.isPresent()) {
			var result = opResult.get();
			UserService.LOG.atInfo().log("checkPassword - OK found entity for id={}", pId);
			if (this.passwordEncoder.matches(pPassword, result.getPassword())) {
				return UserDtoHandler.dtoOutfromEntity(result);
			}
			throw new InconsistentPasswordException(
					"Les mots de passe ne sont pas identiques pour l'utilisateur " + pId);
		}
		UserService.LOG.atWarn().log("checkPassword - KO No user found with Id={}", pId);
		throw new EntityNotFoundException("Utilisateur introuvable", pId);
	}

	/**
	 * Registers a user. <br>
	 *
	 * The user can be a Lunch Lady only if there is no Lunch Lady in the database.
	 * Otherwise, this role can only be given through update and by a user who is a
	 * Lunch Lady himself. <br>
	 *
	 * @param pUser a new user
	 * @return the user inserted
	 * @throws EntityAlreadySavedException if user is already in database
	 * @throws InconsistentRoleException   if pUser has the role Lunch Lady and
	 *                                     should not.
	 */

	@Override
	@Transactional(rollbackFor = Exception.class)
	public UserDtoOut register(UserDtoIn pUser) throws EntityAlreadySavedException, InconsistentRoleException {
		UserService.LOG.atDebug().log("register - {}", pUser);

		ValidationUtils.isNotNull(pUser, "DTO cannot be null");
		pUser.validate();

		if (this.exist(pUser.getEmail())) {
			UserService.LOG.atError().log("register - User already in data base {}", pUser);
			throw new EntityAlreadySavedException(
					"Utilisateur avec email=[" + pUser.getEmail() + "] est deja dans la base de donnees.");
		}

		// Can ONLY register as a LunchLady if there is NONE in the database
		if (Boolean.TRUE.equals(pUser.getIsLunchLady())) {
			if (this.roleDao.countLunchLady() != 0) {
				UserService.LOG.atError().log(
						"register -User cannot register as a Lunch Lady, this role can only be given using update BY a Lunch Lady");
				throw new InconsistentRoleException(
						"Vous ne pouvez pas vous auto-proclamer avec le rôle cantinière. Ce rôle doit être indiqué lors d'une mise à jour par une cantinière.");
			}
			UserService.LOG.atWarn()
					.log("register - User can register as a Lunch Lady, because there is none in the data base");
		}
		var entityToInsert = UserDtoHandler.toEntity(pUser);
		entityToInsert.setStatus(EntityStatus.ENABLED);
		if (entityToInsert.getRoles() == null || entityToInsert.getRoles().isEmpty()) {
			List<RoleEntity> roles = new ArrayList<>();
			roles.add(new RoleEntity(RoleLabel.ROLE_USER, entityToInsert));
			entityToInsert.setRoles(roles);
		}

		this.handleImage(entityToInsert, pUser);

		entityToInsert.setPassword(this.passwordEncoder.encode(entityToInsert.getPassword()));

		var resultSave = this.userDao.save(entityToInsert);
		UserService.LOG.atInfo().log("register - OK with new id={}", resultSave.getId());
		return UserDtoHandler.dtoOutfromEntity(resultSave);
	}

	/**
	 * Indicates if user with given email is already in database.
	 *
	 * @param pEmail an email.
	 * @return true if user with given email is in database, false if not
	 */
	@Override
	public boolean exist(String pEmail) {
		ValidationUtils.isNotEmpty(pEmail, "Email cannot be null or empty");
		var foundUser = this.userDao.findOneByEmail(pEmail);
		return foundUser.isPresent();
	}

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
	@Override
	public UserDtoOut update(Integer pIdToUpdate, UserDtoIn pNewDto, boolean pCanHandleLunchLady)
			throws EntityNotFoundException, EntityAlreadySavedException, InconsistentRoleException {
		UserService.LOG.atDebug().log("update - {} with {}", pIdToUpdate, pNewDto);
		ValidationUtils.isNotNull(pNewDto, "DTO cannot be null");
		var entityInDataBase = super.findEntity(pIdToUpdate);

		// We do not validate the dtoin because we are in update
		// pNewDto.validate();

		if (pNewDto.getEmail() != null && !pNewDto.getEmail().equals(entityInDataBase.getEmail())) {
			UserService.LOG.atDebug().log("update - User email has changed");
			if (this.exist(pNewDto.getEmail())) {
				UserService.LOG.atError().log("update - User already in data base {}) ", pNewDto);
				throw new EntityAlreadySavedException(
						"Utilisateur avec email=[" + pNewDto.getEmail() + "] est deja dans la base de donnees.");
			}
			entityInDataBase.setEmail(pNewDto.getEmail());
		} else {
			UserService.LOG.atError().log("update - No or eq value for new User email will do nothing");
		}
		if (pNewDto.getPassword() != null) {
			pNewDto.setPassword(this.passwordEncoder.encode(pNewDto.getPassword()));
			entityInDataBase.setPassword(pNewDto.getPassword());
			UserService.LOG.atDebug().log("update - User mdp has changed");
		} else {
			UserService.LOG.atDebug().log("update - No value for new User password, will do nothing");
		}

		if (pNewDto.getWallet() != null) {
			UserService.LOG.atError().log("update - User wallet has changed but will not be updated use credit/debit");
		}

		if (entityInDataBase.getRoles() == null || entityInDataBase.getRoles().isEmpty()) {
			List<RoleEntity> roles = new ArrayList<>();
			roles.add(new RoleEntity(RoleLabel.ROLE_USER, entityInDataBase));
			entityInDataBase.setRoles(roles);
		}
		if (pCanHandleLunchLady && !pNewDto.getIsLunchLady().equals(entityInDataBase.getIsLunchLady())) {
			// If role is lost, check that there is still one lunch lady in the database
			if (Boolean.TRUE.equals(entityInDataBase.getIsLunchLady()) && Boolean.FALSE.equals(pNewDto.getIsLunchLady())
					&& this.roleDao.countLunchLady() - 1 <= 0) {
				// Cannot remove the role
				UserService.LOG.atError()
						.log("update - User IsLunchLady has changed but this is the last one, so will not be change");
				throw new InconsistentRoleException(
						"Vous ne pouvez pas vous retirer le rôle de cantinière. Vous êtes la seule dans la base de données.");
			}
			UserService.LOG.atDebug().log("update - User IsLunchLady has changed");
			entityInDataBase.setIsLunchLady(pNewDto.getIsLunchLady()); // Will handle ROLE
		} else {
			UserService.LOG.atDebug().log("update - No value for new User isLunchLady, will do nothing");
		}

		if (pNewDto.getAddress() != null && !pNewDto.getAddress().equals(entityInDataBase.getAddress())) {
			UserService.LOG.atDebug().log("update - User Address has changed");
			entityInDataBase.setAddress(pNewDto.getAddress());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User adress, will do nothing");
		}
		if (pNewDto.getPostalCode() != null && !pNewDto.getPostalCode().equals(entityInDataBase.getPostalCode())) {
			UserService.LOG.atDebug().log("update - User PostalCode has changed");
			entityInDataBase.setPostalCode(pNewDto.getPostalCode());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User postal code, will do nothing");
		}

		if (pNewDto.getName() != null && !pNewDto.getName().equals(entityInDataBase.getName())) {
			UserService.LOG.atDebug().log("update - User Name has changed");
			entityInDataBase.setName(pNewDto.getName());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User name, will do nothing");
		}
		if (pNewDto.getFirstname() != null && !pNewDto.getFirstname().equals(entityInDataBase.getFirstname())) {
			UserService.LOG.atDebug().log("update - User Firstname has changed");
			entityInDataBase.setFirstname(pNewDto.getFirstname());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User firstname, will do nothing");
		}
		if (pNewDto.getTown() != null && !pNewDto.getTown().equals(entityInDataBase.getTown())) {
			UserService.LOG.atDebug().log("update - User Town has changed");
			entityInDataBase.setTown(pNewDto.getTown());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User town, will do nothing");
		}
		if (pNewDto.getPhone() != null && !pNewDto.getPhone().equals(entityInDataBase.getPhone())) {
			UserService.LOG.atDebug().log("update - User Phone has changed");
			entityInDataBase.setPhone(pNewDto.getPhone());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User phone, will do nothing");
		}

		if (pNewDto.getSex() != null && !pNewDto.getSex().equals(entityInDataBase.getSex())) {
			UserService.LOG.atDebug().log("update - User Sex has changed");
			entityInDataBase.setSex(pNewDto.getSex());
		} else {
			UserService.LOG.atDebug().log("update - No or eq value for new User sex, will do nothing");
		}

		// userInDataBase is updated with new values
		var resultUpdate = this.userDao.save(entityInDataBase);
		UserService.LOG.atInfo().log("update - OK");
		return UserDtoHandler.dtoOutfromEntity(resultUpdate);
	}

	/**
	 * Will remove money from user's wallet.
	 *
	 * @param pUserId a user id
	 * @param pAmount an amount of money
	 * @return the user updated
	 * @throws EntityNotFoundException if entity was not found
	 * @throws LackOfMoneyException    if user has not enough money in its wallet
	 */
	@Override
	public UserDtoOut debit(Integer pUserId, BigDecimal pAmount) throws EntityNotFoundException, LackOfMoneyException {
		return UserDtoHandler.dtoOutfromEntity(this.debitEntity(pUserId, pAmount));
	}

	@Override
	public UserEntity debitEntity(Integer pUserId, BigDecimal pAmount)
			throws EntityNotFoundException, LackOfMoneyException {
		UserService.LOG.atDebug().log("debit - {} of {}", pUserId, pAmount);
		ValidationUtils.isStrictlyPositive(pAmount, "Amount muste be > 0");
		var user = super.findEntity(pUserId);

		var actualSolde = user.getWallet().doubleValue();
		if (actualSolde - pAmount.doubleValue() >= 0) {
			user.setWallet(BigDecimal.valueOf(actualSolde - pAmount.doubleValue()));
			var resultUpdate = this.userDao.save(user);
			UserService.LOG.atInfo().log("debit - OK new wallet is {}", user.getWallet().doubleValue());
			return resultUpdate;
		}
		UserService.LOG.atError().log("debit - User with id={} has not enought money (left {})", user.getId(),
				actualSolde);
		throw new LackOfMoneyException("Utilisateur avec id=[" + user.getId() + "] n'a pas assez d'argent.");

	}

	/**
	 * Will add money to user's wallet.
	 *
	 * @param pUserId a user id
	 * @param pAmount an amount of money
	 * @return the user updated
	 * @throws EntityNotFoundException if entity was not found
	 */
	@Override
	public UserDtoOut credit(Integer pUserId, BigDecimal pAmount) throws EntityNotFoundException {
		UserService.LOG.atDebug().log("credit - {} of {}", pUserId, pAmount);
		ValidationUtils.isStrictlyPositive(pAmount, "Amount muste be > 0");
		var user = super.findEntity(pUserId);
		var actualSolde = user.getWallet().doubleValue();
		user.setWallet(BigDecimal.valueOf(actualSolde + pAmount.doubleValue()));
		var resultUpdate = this.userDao.save(user);
		UserService.LOG.atInfo().log("credit - OK new wallet is {}", user.getWallet().doubleValue());
		return UserDtoHandler.dtoOutfromEntity(resultUpdate);
	}

	/**
	 * Deletes the user. <br>
	 *
	 * Data are not removed from database, only user's status will change.
	 *
	 * @param pUserId a user id
	 * @return the user deleted
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	@Override
	public UserDtoOut delete(Integer pUserId) throws EntityNotFoundException, InconsistentStatusException {
		UserService.LOG.atDebug().log("delete - {}", pUserId);
		return UserDtoHandler.dtoOutfromEntity(this.updateStatus(pUserId, EntityStatus.DELETED));
	}

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
	@Override
	public UserDtoOut disable(Integer pUserId) throws EntityNotFoundException, InconsistentStatusException {
		UserService.LOG.atDebug().log("disable - {}", pUserId);
		return UserDtoHandler.dtoOutfromEntity(this.updateStatus(pUserId, EntityStatus.DISABLED));
	}

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
	@Override
	public UserDtoOut enable(Integer pUserId) throws EntityNotFoundException, InconsistentStatusException {
		UserService.LOG.atDebug().log("enable - {}", pUserId);
		return UserDtoHandler.dtoOutfromEntity(this.updateStatus(pUserId, EntityStatus.ENABLED));
	}

	/**
	 * Changes the user status. <br>
	 *
	 * @param pUserId        a user id
	 * @param pNewUserStatus the new status
	 * @return the user updated
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	private UserEntity updateStatus(Integer pUserId, EntityStatus pNewUserStatus)
			throws EntityNotFoundException, InconsistentStatusException {
		UserService.LOG.atDebug().log("updateStatus - {} for new state {}", pUserId, pNewUserStatus);

		var user = super.findEntity(pUserId);
		if (pNewUserStatus == user.getStatus()) {
			throw new InconsistentStatusException(
					"Utilisateur ayant l'id " + pUserId + " est déjà dans l'état demandé (" + pNewUserStatus + ")");
		}
		user.setStatus(pNewUserStatus);
		var resultUpdate = this.userDao.save(user);
		UserService.LOG.atInfo().log("updateStatus - OK");
		return resultUpdate;
	}

	/**
	 * Changes the image of this element. <br>
	 *
	 * @param pUserId      a user id
	 * @param pNewImageDto the new image
	 * @return the user updated
	 * @throws EntityNotFoundException     if entity not found
	 * @throws InconsistentStatusException if entity state is not valid
	 */
	@Override
	public UserDtoOut updateImage(Integer pUserId, ImageDtoIn pNewImageDto)
			throws EntityNotFoundException, InconsistentStatusException {
		UserService.LOG.atDebug().log("updateImage - {} for new image {}", pUserId, pNewImageDto);

		var user = super.findEntity(pUserId);
		if (user.isDeleted()) {
			throw new InconsistentStatusException("Impossible de changer l'image d'un utilisateur supprimé");
		}
		var oldImg = user.getImage();
		if (oldImg != null) {
			if (Boolean.TRUE.equals(oldImg.getIsDefault())) {
				oldImg = ImageDtoHandler.toEntity(pNewImageDto);
			} else {
				oldImg.setImage64(pNewImageDto.getImage64());
				oldImg.setImagePath(pNewImageDto.getImagePath());
			}
			oldImg = this.imageService.saveIfNotInDataBase(oldImg);
			user.setImage(oldImg);
		}

		var resultUpdate = this.userDao.save(user);
		UserService.LOG.atInfo().log("updateImage - OK");
		return UserDtoHandler.dtoOutfromEntity(resultUpdate);
	}

	@Override
	protected JpaRepository<UserEntity, Integer> getTargetedDao() {
		return this.userDao;
	}

	private void handleImage(UserEntity pEntity, UserDtoIn pNewDto) {
		var imgDto = pNewDto.getImage();
		var imgE = switch (pEntity.getSex()) {
		case MAN -> this.imageService.saveIfNotInDataBase(IDefaultImages.USER_DEFAULT_MAN_IMG);
		case WOMAN -> this.imageService.saveIfNotInDataBase(IDefaultImages.USER_DEFAULT_WOMAN_IMG);
		default -> this.imageService.saveIfNotInDataBase(IDefaultImages.USER_DEFAULT_OTHER_IMG);
		};
		if (imgDto != null) {
			UserService.LOG.atDebug().log("insertAndLinkImage - element has an image, will insert it");
			imgE = ImageDtoHandler.toEntity(imgDto);
			imgE = this.imageService.saveIfNotInDataBase(imgE);
			UserService.LOG.atDebug().log("insertAndLinkImage - elements's image was inserted with id {}",
					imgE.getId());

		}
		pEntity.setImage(imgE);
	}

	@Override
	public UserDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return UserDtoHandler.dtoOutfromEntity(this.findEntity(pEntityPrimaryKey));
	}

	@Override
	public List<UserDtoOut> findAll() {
		return UserDtoHandler.dtosOutfromEntities(super.findAllEntities());
	}

}
