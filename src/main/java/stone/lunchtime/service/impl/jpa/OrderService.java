// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;

import stone.lunchtime.dao.jpa.IConstraintDao;
import stone.lunchtime.dao.jpa.IMealDao;
import stone.lunchtime.dao.jpa.IMenuDao;
import stone.lunchtime.dao.jpa.IOrderDao;
import stone.lunchtime.dto.AvailableForWeeksAndDays;
import stone.lunchtime.dto.in.OrderDtoIn;
import stone.lunchtime.dto.in.QuantityDtoIn;
import stone.lunchtime.dto.jpa.handler.OrderDtoHandler;
import stone.lunchtime.dto.out.OrderDtoOut;
import stone.lunchtime.dto.out.PriceDtoOut;
import stone.lunchtime.entity.OrderStatus;
import stone.lunchtime.entity.jpa.OrderEntity;
import stone.lunchtime.entity.jpa.QuantityEntity;
import stone.lunchtime.entity.jpa.UserEntity;
import stone.lunchtime.service.IOrderService;
import stone.lunchtime.service.IUserService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.LackOfMoneyException;
import stone.lunchtime.service.exception.NotAvailableForThisWeekException;
import stone.lunchtime.service.exception.OrderCanceledException;
import stone.lunchtime.service.exception.OrderDelivredException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.service.exception.TimeOutException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Handle orders.
 */
@Service
public class OrderService extends AbstractService<OrderEntity, OrderDtoOut> implements IOrderService<OrderEntity> {
	private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

	private final IOrderDao orderDao;

	private final IMealDao mealDao;

	private final IMenuDao menuDao;

	private final IConstraintDao constraintDao;

	private final IUserService<UserEntity> userSevice;

	/**
	 * Constructor.
	 *
	 * @param pMapper        the json mapper.
	 * @param pConstraintDao constraint DAO
	 * @param pMealDao       meal dao
	 * @param pMenuDao       menu dao
	 * @param pOrderDao      order dao
	 * @param pUserSevice    user service
	 */
	@Autowired
	protected OrderService(ObjectMapper pMapper, IOrderDao pOrderDao, IMealDao pMealDao, IMenuDao pMenuDao,
			IConstraintDao pConstraintDao, IUserService<UserEntity> pUserSevice) {
		super(pMapper);
		this.orderDao = pOrderDao;
		this.mealDao = pMealDao;
		this.menuDao = pMenuDao;
		this.constraintDao = pConstraintDao;
		this.userSevice = pUserSevice;
	}

	@Override
	public OrderDtoOut order(OrderDtoIn pDtoIn)
			throws TimeOutException, NotAvailableForThisWeekException, EntityNotFoundException {
		OrderService.LOG.atDebug().log("order - {}", pDtoIn);
		ValidationUtils.isNotNull(pDtoIn, "DTO cannot be null");
		pDtoIn.validate();

		var insertOrder = OrderDtoHandler.toEntity();

		// Handle join for transaction reason
		var constraintId = pDtoIn.getConstraintId();
		if (constraintId == null) {
			OrderService.LOG.atWarn().log("order - id constraint is null, will use first one");
			constraintId = Integer.valueOf(1);
		}

		insertOrder.setUser(this.userSevice.findEntity(pDtoIn.getUserId()));

		if (pDtoIn.hasQuantity()) {
			this.handleOrderQuantity(insertOrder, pDtoIn.getQuantity(), constraintId);
		}

		var doInsert = this.handleTime(insertOrder, constraintId);
		if (doInsert) {
			var resultSave = this.orderDao.save(insertOrder);
			OrderService.LOG.atInfo().log("order - OK with new id={}", resultSave.getId());
			return OrderDtoHandler.dtoOutfromEntity(resultSave, super.getMapper());
		}
		OrderService.LOG.atError().log("order - KO It is too late for ordering");
		throw new TimeOutException("L'heure authorisée pour passer une commande est dépassée");
	}

	@Override
	public OrderDtoOut update(Integer pIdToUpdate, OrderDtoIn pNewDto)
			throws EntityNotFoundException, TimeOutException, NotAvailableForThisWeekException {
		OrderService.LOG.atDebug().log("update - {} with {}", pIdToUpdate, pNewDto);
		ValidationUtils.isNotNull(pNewDto, "DTO cannot be null");
		pNewDto.validate();

		var entityInDataBase = this.findEntity(pIdToUpdate);
		// Handle join for transaction reason
		var constraintId = pNewDto.getConstraintId();
		if (constraintId == null) {
			OrderService.LOG.atWarn().log("update - id constraint is null, will use first one");
			constraintId = Integer.valueOf(1);
		}

		if (pNewDto.getUserId() != null && !pNewDto.getUserId().equals(entityInDataBase.getUser().getId())) {
			OrderService.LOG.atError().log("update - Cannont change user");
		}

		if (pNewDto.hasQuantity()) {
			this.handleOrderQuantity(entityInDataBase, pNewDto.getQuantity(), constraintId);
		} else {
			var values = entityInDataBase.getQuantityEntities();
			if (values != null && !values.isEmpty()) {
				OrderService.LOG.atDebug().log("update - clear quantities");
				values.clear();
			}
		}

		var doInsert = this.handleTime(entityInDataBase, constraintId);
		if (doInsert) {
			var resultUpdate = this.orderDao.save(entityInDataBase);
			OrderService.LOG.atInfo().log("update - OK");
			return OrderDtoHandler.dtoOutfromEntity(resultUpdate, super.getMapper());
		}
		OrderService.LOG.atError().log("update - KO It is too late for ordering or updating an order");
		throw new TimeOutException("L'heure authorisée pour passer une commande est dépassée");
	}

	@Override
	public OrderDtoOut cancel(Integer pOrderId)
			throws EntityNotFoundException, OrderCanceledException, OrderDelivredException {
		OrderService.LOG.atDebug().log("cancel - {}", pOrderId);
		return OrderDtoHandler.dtoOutfromEntity(this.updateEntityStatus(pOrderId, OrderStatus.CANCELED),
				super.getMapper());
	}

	@Override
	public OrderDtoOut deliverAndPay(Integer pOrderId, Integer pConstraintId)
			throws EntityNotFoundException, LackOfMoneyException, OrderCanceledException, OrderDelivredException {
		OrderService.LOG.atDebug().log("deliverAndPay - {}", pOrderId);
		var result = this.updateEntityStatus(pOrderId, OrderStatus.DELIVERED);
		result.setUser(this.userSevice.debitEntity(result.getUser().getId(),
				this.computePrice(pOrderId, pConstraintId, new PriceDtoOut())));
		return OrderDtoHandler.dtoOutfromEntity(result, super.getMapper());
	}

	@Override
	public BigDecimal computePrice(Integer pOrderId, Integer pConstraintId, PriceDtoOut pOut)
			throws EntityNotFoundException {
		OrderService.LOG.atDebug().log("computePrice - {}, {}", pOrderId, pConstraintId);

		var order = this.findEntity(pOrderId);

		return this.computePrice(order, pConstraintId, pOut);
	}

	private BigDecimal computePrice(OrderEntity pOrder, Integer pConstraintId, PriceDtoOut pOut)
			throws EntityNotFoundException {
		OrderService.LOG.atDebug().log("computePrice - {}, {}", pOrder, pConstraintId);

		if (pConstraintId == null) {
			OrderService.LOG.atWarn().log("computePrice - id constraint is null, will use 1");
			pConstraintId = Integer.valueOf(1);
		}
		var tva = 0F;
		var pkC = pConstraintId;
		if (pkC.intValue() == -1) {
			OrderService.LOG.atWarn().log("computePrice - id constraint is -1, will not use constraint");
		} else {
			var opResultConstraint = this.constraintDao.findById(pkC);
			if (opResultConstraint.isEmpty()) {
				OrderService.LOG.atError().log("computePrice - KO constraint not found for id={}", pkC);
				throw new EntityNotFoundException("Contrainte introuvable", pkC);
			}
			var result = opResultConstraint.get();
			OrderService.LOG.atDebug().log("computePrice - OK found for id={}", pkC);
			tva = result.getRateVAT().floatValue();
		}
		pOut.setRateVAT(tva);
		var total = 0F;
		var quantities = pOrder.getQuantityEntities();
		if (quantities != null && !quantities.isEmpty()) {
			for (QuantityEntity qme : quantities) {
				if (qme.getMeal() != null) {
					total += qme.getMeal().getPriceDF().floatValue() * qme.getQuantity();
				}
				if (qme.getMenu() != null) {
					total += qme.getMenu().getPriceDF().floatValue() * qme.getQuantity();
				}
			}
		}
		pOut.setPriceDF(total);
		total += total * (tva / 100F);
		pOut.setPriceVAT(total);
		return BigDecimal.valueOf(total);
	}

	/**
	 * Changes the order status. <br>
	 *
	 * @param pOrderId   a user id
	 * @param pNewStatus the new status
	 * @return the order updated
	 * @throws EntityNotFoundException if entity not found
	 * @throws OrderCanceledException  if entity state is not valid
	 * @throws OrderDelivredException  if entity state is not valid
	 * @throws ParameterException      if parameter is invalid
	 */
	private OrderEntity updateEntityStatus(Integer pOrderId, OrderStatus pNewStatus)
			throws EntityNotFoundException, OrderCanceledException, OrderDelivredException {

		var result = this.findEntity(pOrderId);
		var doUpdate = false;
		if (pNewStatus == OrderStatus.CANCELED) {
			if (result.isCreated()) {
				result.setStatus(OrderStatus.CANCELED);
				doUpdate = true;
			} else if (result.isCanceled()) {
				OrderService.LOG.atWarn().log("changeStatus - KO order already canceled for id={}", pOrderId);
				throw new OrderCanceledException("La commande est déjà annulée, elle ne peut pas être re-annulée!");
			} else if (result.isDelivered()) {
				OrderService.LOG.atWarn().log("changeStatus - KO order already delivred for id={}", pOrderId);
				throw new OrderDelivredException("La commande est déjà délivrée, elle ne peut pas être annulée!");
			}
		}
		if (pNewStatus == OrderStatus.DELIVERED) {
			if (result.isCreated()) {
				result.setStatus(OrderStatus.DELIVERED);
				doUpdate = true;
			} else if (result.isCanceled()) {
				OrderService.LOG.atWarn().log("changeStatus - KO order already canceled for id={}", pOrderId);
				throw new OrderCanceledException("La commande est déjà annulée, elle ne peut pas être livrée!");
			} else if (result.isDelivered()) {
				OrderService.LOG.atWarn().log("changeStatus - KO order already delivred for id={}", pOrderId);
				throw new OrderDelivredException("La commande est déjà délivrée, elle ne peut pas être re-délivrée!");
			}
		}
		OrderEntity resultUpdate;
		if (doUpdate) {
			resultUpdate = this.orderDao.save(result);
			OrderService.LOG.atInfo().log("changeStatus - OK");
		} else {
			OrderService.LOG.atError().log("changeStatus - KO order is in strange state id={}", pOrderId);
			resultUpdate = this.findEntity(pOrderId);
		}
		return resultUpdate;
	}

	/**
	 * Handles the join between order and quantity.
	 *
	 * @param pOrder        an order. That will be changed during this method.
	 * @param pQuantity     a list of QuantityDtoIn
	 * @param pConstraintId a constraint id. Can be null or -1 for no constraint
	 * @throws EntityNotFoundException          if entity was not found
	 * @throws NotAvailableForThisWeekException if this menu is not available for
	 *                                          this week. Depending on the
	 *                                          constraint.
	 */
	private void handleOrderQuantity(OrderEntity pOrder, List<QuantityDtoIn> pQuantity, Integer pConstraintId)
			throws EntityNotFoundException, NotAvailableForThisWeekException {
		List<QuantityEntity> quantities = new ArrayList<>();
		for (QuantityDtoIn qmd : pQuantity) {
			var mealId = qmd.getMealId();
			var menuId = qmd.getMenuId();
			var mealQuantity = qmd.getQuantity();
			if (mealQuantity.intValue() == 0) {
				OrderService.LOG.atDebug().log("handleOrderQuantity - Found 0 quantity for MealId={}", mealId);
				continue;
			}

			if (mealId != null) {
				var opResult = this.mealDao.findById(mealId);
				if (opResult.isEmpty()) {
					OrderService.LOG.atError().log("handleOrderQuantity - KO meal not found for id={}", mealId);
					throw new EntityNotFoundException("Plat introuvable", mealId);
				}
				var result = opResult.get();
				OrderService.LOG.atDebug().log("handleOrderQuantity - OK found for id={}  Meal={}", mealId, result);
				if (pConstraintId.intValue() == -1) {
					OrderService.LOG.atDebug().log("handleOrderHasMeals - constraint is disabled");
					var chp = new QuantityEntity();
					chp.setMeal(result);
					chp.setQuantity(mealQuantity);
					quantities.add(chp);
				} else {
					AvailableForWeeksAndDays dispo = null;
					try {
						dispo = new AvailableForWeeksAndDays(result.getAvailableForWeeksAndDays(), this.getMapper());
					} catch (JacksonException exc) {
						OrderService.LOG.atError().log("Error with weeks and day format", exc);
						dispo = null;
					}
					final var thisWeek = OrderService.getCurrentWeekId();
					final var thisDay = OrderService.getCurrentDayId();
					if (dispo != null && !dispo.oneWeekAndOneDay(thisWeek, thisDay)) {
						// KO
						OrderService.LOG.atError().log(
								"handleOrderQuantity - KO meal {} is NOT available for this week {} and day {}", mealId,
								thisWeek, thisDay);
						throw new NotAvailableForThisWeekException("Plat " + mealId + " indisponible pour la semaine "
								+ thisWeek + " et le jour " + thisDay);
					}
					// Ok
					OrderService.LOG.atDebug().log(
							"handleOrderQuantity - OK meal {} is available for this week {} and day {}", mealId,
							thisWeek, thisDay);
					var chp = new QuantityEntity();
					chp.setMeal(result);
					chp.setQuantity(mealQuantity);
					quantities.add(chp);
				}
			} // This was a meal link
			else if (menuId != null) {
				var opResult = this.menuDao.findById(menuId);
				if (opResult.isEmpty()) {
					OrderService.LOG.atError().log("handleOrderQuantity - KO menu not found for id={}", menuId);
					throw new EntityNotFoundException("Menu introuvable", menuId);
				}
				var result = opResult.get();
				OrderService.LOG.atDebug().log("handleOrderQuantity - OK found for id={}  Menu={}", menuId, result);
				if (pConstraintId.intValue() == -1) {
					OrderService.LOG.atDebug().log("handleOrderQuantity - constraint is disabled");
					var chp = new QuantityEntity();
					chp.setMenu(result);
					chp.setQuantity(mealQuantity);
					quantities.add(chp);
				} else {
					AvailableForWeeksAndDays dispo = null;
					try {
						dispo = new AvailableForWeeksAndDays(result.getAvailableForWeeksAndDays(), this.getMapper());
					} catch (JacksonException exc) {
						OrderService.LOG.atError().log("Error with weeks and day format", exc);
						dispo = null;
					}
					final var thisWeek = OrderService.getCurrentWeekId();
					final var thisDay = OrderService.getCurrentDayId();
					if (dispo != null && !dispo.oneWeekAndOneDay(thisWeek, thisDay)) {
						// KO
						OrderService.LOG.atError().log(
								"handleOrderQuantity - KO menu {} is NOT available for this week {} and day {}", menuId,
								thisWeek, thisDay);
						throw new NotAvailableForThisWeekException(
								"Menu " + menuId + " indisponible pour la semaine " + thisWeek);
					}
					// Ok
					OrderService.LOG.atDebug().log(
							"handleOrderQuantity - OK menu {} is available for this week {} and day {}", menuId,
							thisWeek, thisDay);
					var chp = new QuantityEntity();
					chp.setMenu(result);
					chp.setQuantity(mealQuantity);
					quantities.add(chp);
				}
			} // this was a menu link
		}
		OrderService.LOG.atDebug().log("handleOrderQuantity - nb element for quantities={}", quantities.size());
		pOrder.setQuantityEntities(quantities);
	}

	/**
	 * Handles the time constraint on order method.
	 *
	 * @param pOrder        an order. That will be changed during this method.
	 * @param pConstraintId a constraint id. Can be null or -1 for no constraint
	 * @throws EntityNotFoundException if entity was not found
	 * @return true if constraint is ok (means in the accepted hours), false
	 *         otherwise
	 */
	private boolean handleTime(OrderEntity pOrder, Integer pConstraintId) throws EntityNotFoundException {
		if (pConstraintId == null) {
			OrderService.LOG.atWarn().log("handleTime - id constraint is null, will user");
			pConstraintId = Integer.valueOf(1);
		}

		if (pConstraintId.intValue() == -1) {
			OrderService.LOG.atWarn().log("handleTime - id constraint is -1, will not use constraint");
			return true;
		}
		// Handle time limit!
		var opResult = this.constraintDao.findById(pConstraintId);
		LocalTime heureLimit = null;
		if (opResult.isEmpty()) {
			OrderService.LOG.atError().log("handleTime - KO constraint not found for id={}", pConstraintId);
			throw new EntityNotFoundException("Contrainte introuvable", pConstraintId);
		}
		var result = opResult.get();
		OrderService.LOG.atDebug().log("handleTime - OK found for id={}", pConstraintId);
		heureLimit = result.getOrderTimeLimit();
		var orderTime = pOrder.getCreationTime();
		var doInsert = orderTime.isBefore(heureLimit);
		OrderService.LOG.atDebug().log("handleTime - Time order={}  Time limit={} ==> {})", orderTime, heureLimit,
				Boolean.valueOf(doInsert));

		return doInsert;
	}

	@Override
	public List<OrderDtoOut> findAllByUserId(Integer pUserId) {
		OrderService.LOG.atDebug().log("findAllByUserId - {}", pUserId);
		ValidationUtils.isNotNull(pUserId, "ID cannot be null");
		var opResult = this.orderDao.findByUserIdOrderByCreationDateAsc(pUserId);
		if (opResult.isPresent()) {
			var result = opResult.get();
			OrderService.LOG.atDebug().log("findAllByUserId - found {} values for user {}", result.size(), pUserId);
			return OrderDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		OrderService.LOG.atDebug().log("findAllByUserId - found NO value for user {}", pUserId);
		return Collections.emptyList();
	}

	@Override
	public List<OrderDtoOut> findAllBetweenDateInStatus(LocalDate pBeginDate, LocalDate pEndDate, OrderStatus pStatus) {
		OrderService.LOG.atDebug().log("findAllByBetweenDate - {} and {} for state {}", pBeginDate, pEndDate, pStatus);
		if (pBeginDate != null && pEndDate != null && pBeginDate.isAfter(pEndDate)) {
			OrderService.LOG.atError().log("findAllBetweenDateInStatus  - Begin date is after end date");
			throw new ParameterException("Les dates ne sont pas valides", "dates");
		}
		if (pStatus == null) {
			pStatus = OrderStatus.CREATED;
		}
		if (pBeginDate == null) {
			pBeginDate = LocalDate.now().minusYears(20);
		}
		if (pEndDate == null) {
			pEndDate = LocalDate.now();
		}

		var opResult = this.orderDao.findByCreationDateBetweenAndStatusOrderByCreationDateAsc(pBeginDate, pEndDate,
				pStatus);
		if (opResult.isPresent()) {
			var result = opResult.get();
			OrderService.LOG.atDebug().log("findAllBetweenDateInStatus - found {} values", result.size());
			return OrderDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		OrderService.LOG.atDebug().log("findAllBetweenDateInStatus - found NO value");
		return Collections.emptyList();
	}

	@Override
	public List<OrderDtoOut> findAllBetweenDateForUser(Integer pUserId, LocalDate pBeginDate, LocalDate pEndDate) {
		OrderService.LOG.atDebug().log("findAllBetweenDateForUser - {} and {} for user {}", pBeginDate, pEndDate,
				pUserId);
		if (pBeginDate != null && pEndDate != null && pBeginDate.isAfter(pEndDate)) {
			OrderService.LOG.atError().log("findAllBetweenDateForUser - Begin date is after end date");
			throw new ParameterException("Les dates ne sont pas valides", "dates");
		}

		if (pBeginDate == null) {
			pBeginDate = LocalDate.now().minusYears(20);
		}
		if (pEndDate == null) {
			pEndDate = LocalDate.now();
		}
		ValidationUtils.isNotNull(pUserId, "ID cannot be null");
		var opResult = this.orderDao.findByCreationDateBetweenAndUserIdOrderByCreationDateAsc(pBeginDate, pEndDate,
				pUserId);
		if (opResult.isPresent()) {
			var result = opResult.get();
			OrderService.LOG.atDebug().log("findAllBetweenDateForUser - found {} values", result.size());
			return OrderDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		OrderService.LOG.atDebug().log("findAllBetweenDateForUser - found NO value");
		return Collections.emptyList();
	}

	@Override
	public List<OrderDtoOut> findAllBetweenDateForUserInStatus(Integer pUserId, LocalDate pBeginDate,
			LocalDate pEndDate, OrderStatus pStatus) {
		OrderService.LOG.atDebug().log("findAllBetweenDateForUserInStatus - {} and {} for user {} with state {}",
				pBeginDate, pEndDate, pUserId, pStatus);
		ValidationUtils.isNotNull(pUserId, "ID cannot be null");
		if (pBeginDate != null && pEndDate != null && pBeginDate.isAfter(pEndDate)) {
			OrderService.LOG.atError().log("findAllBetweenDateForUserInStatus  - Begin date is after end date");
			throw new ParameterException("Les dates ne sont pas valides", "dates");
		}

		if (pStatus == null) {
			pStatus = OrderStatus.CREATED;
		}

		if (pBeginDate == null) {
			pBeginDate = LocalDate.now().minusYears(20);
		}
		if (pEndDate == null) {
			pEndDate = LocalDate.now();
		}

		var opResult = this.orderDao.findByCreationDateBetweenAndUserIdAndStatusOrderByCreationDateAsc(pBeginDate,
				pEndDate, pUserId, pStatus);
		if (opResult.isPresent()) {
			var result = opResult.get();
			OrderService.LOG.atDebug().log("findAllBetweenDateForUserInStatus - found {} values", result.size());
			return OrderDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		OrderService.LOG.atDebug().log("findAllBetweenDateForUserInStatus - found NO value");
		return Collections.emptyList();
	}

	@Override
	public List<OrderDtoOut> findAllForUserInStatus(Integer pUserId, OrderStatus pStatus) {
		OrderService.LOG.atDebug().log("findAllForUserInStatus - for user {} with state {}", pUserId, pStatus);

		if (pStatus == null) {
			pStatus = OrderStatus.CREATED;
		}
		ValidationUtils.isNotNull(pUserId, "ID cannot be null");
		var opResult = this.orderDao.findByUserIdAndStatusOrderByCreationDateAsc(pUserId, pStatus);
		if (opResult.isPresent()) {
			var result = opResult.get();
			OrderService.LOG.atDebug().log("findAllForUserInStatus - found {} values", result.size());
			return OrderDtoHandler.dtosOutfromEntities(result, super.getMapper());
		}
		OrderService.LOG.atDebug().log("findAllForUserInStatus - found NO value");
		return Collections.emptyList();
	}

	@Override
	protected JpaRepository<OrderEntity, Integer> getTargetedDao() {
		return this.orderDao;
	}

	/**
	 * Gets the id of the current week.
	 *
	 * @return the id of the current week.
	 */
	public static Integer getCurrentWeekId() {
		return LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
	}

	/**
	 * Gets the id of the current day.
	 *
	 * @return the id of the current day.
	 */
	public static Integer getCurrentDayId() {
		return LocalDate.now().get(WeekFields.ISO.dayOfWeek());
	}

	@Override
	public OrderDtoOut find(Integer pEntityPrimaryKey) throws EntityNotFoundException {
		return OrderDtoHandler.dtoOutfromEntity(super.findEntity(pEntityPrimaryKey), super.getMapper());
	}

	@Override
	public List<OrderDtoOut> findAll() {
		return OrderDtoHandler.dtosOutfromEntities(super.findAllEntities(), super.getMapper());
	}

	@Override
	public OrderDtoOut delete(Integer pId) throws EntityNotFoundException, InconsistentStatusException {
		try {
			return OrderDtoHandler.dtoOutfromEntity(this.updateEntityStatus(pId, OrderStatus.CANCELED),
					super.getMapper());
		} catch (OrderCanceledException | OrderDelivredException e) {
			throw new InconsistentStatusException(e);
		}
	}

}
