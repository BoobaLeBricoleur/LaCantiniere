// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.gql;

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
import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.entity.jpa.ConstraintEntity;
import stone.lunchtime.service.IConstraintService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;

/**
 * Constraint controller.
 */
@Controller
public class ConstraintGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(ConstraintGqlController.class);

	private final IConstraintService<ConstraintEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public ConstraintGqlController(IConstraintService<ConstraintEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Adds a constraint. <br>
	 *
	 * You need to be connected as a lunch lady.
	 *
	 * @param pConstraint the constraint to be added
	 *
	 * @return the constraint added
	 */
	@MutationMapping
	@Observed(name = "graphql.constraint.add", contextualName = "graphql#constraint#add")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public ConstraintDtoOut addConstraint(@Argument("constraint") ConstraintDtoIn pConstraint) {
		ConstraintGqlController.LOG.atInfo().log("--> addConstraint - {}", pConstraint);
		var result = this.service.add(pConstraint);
		ConstraintGqlController.LOG.atInfo().log("<-- addConstraint - New constraint has id {}", result.getId());
		return result;
	}

	/**
	 * Updates a constraint. <br>
	 *
	 * You need to be connected as a lunch lady.
	 *
	 * @param pConstraintId id of the constraint to be updated
	 * @param pConstraint   the new data for this constraint
	 *
	 * @return the constraint updated
	 * @throws EntityNotFoundException if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.constraint.update", contextualName = "graphql#constraint#update")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public ConstraintDtoOut updateConstraint(@Argument("id") Integer pConstraintId,
			@Argument("constraint") ConstraintDtoIn pConstraint) throws EntityNotFoundException {
		ConstraintGqlController.LOG.atInfo().log("--> updateConstraint - {}", pConstraint);
		var result = this.service.update(pConstraintId, pConstraint);
		ConstraintGqlController.LOG.atInfo().log("<-- updateConstraint - Constraint {} is updated by lunch lady {}",
				result.getId(), this.getConnectedUserId());
		return result;
	}

	/**
	 * Deletes a constraint. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * Caution: constraint will be completely removed from database.
	 *
	 * @param pConstraintId id of the constraint to be deleted
	 *
	 * @return the HTTP Status regarding success or failure
	 * @throws EntityNotFoundException     if an error occurred
	 * @throws InconsistentStatusException if an error occurred
	 */
	@MutationMapping
	@Observed(name = "graphql.constraint.delete", contextualName = "graphql#constraint#delete")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public boolean deleteConstraint(@Argument("id") Integer pConstraintId)
			throws EntityNotFoundException, InconsistentStatusException {
		ConstraintGqlController.LOG.atInfo().log("--> deleteConstraint - {}", pConstraintId);
		this.service.delete(pConstraintId);
		ConstraintGqlController.LOG.atInfo().log("<-- deleteConstraint - Constraint {} is deleted by lunch lady {}",
				pConstraintId, this.getConnectedUserId());
		return true;
	}

	/**
	 * Gets a constraint. <br>
	 *
	 * Every one can use this method. <br>
	 *
	 * @param pConstraintId id of the constraint you are looking for
	 *
	 * @return the constraint found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 */
	@QueryMapping
	@Observed(name = "graphql.constraint.byid", contextualName = "graphql#constraint#byid")
	public ConstraintDtoOut constraintById(@Argument("id") Integer pConstraintId) throws EntityNotFoundException {
		ConstraintGqlController.LOG.atInfo().log("--> constraintById - {}", pConstraintId);
		var result = this.service.find(pConstraintId);
		ConstraintGqlController.LOG.atInfo().log("<-- constraintById - Has found constraint {}", pConstraintId);
		return result;
	}

	/**
	 * Gets all constraints. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @return all the constraint found or an empty list if none or if an error
	 *         occurred
	 */
	@QueryMapping
	@Observed(name = "graphql.constraint.findall", contextualName = "graphql#constraint#findall")
	public List<ConstraintDtoOut> findAllConstraints() {
		ConstraintGqlController.LOG.atInfo().log("--> findAllConstraints");
		var result = this.service.findAll();
		ConstraintGqlController.LOG.atInfo().log("<-- findAllConstraints - Has found {} constraints", result.size());
		return result;
	}
}
