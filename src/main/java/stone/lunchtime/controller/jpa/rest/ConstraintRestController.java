// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller.jpa.rest;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import stone.lunchtime.dto.in.ConstraintDtoIn;
import stone.lunchtime.dto.out.ConstraintDtoOut;
import stone.lunchtime.dto.out.ExceptionDtoOut;
import stone.lunchtime.entity.jpa.ConstraintEntity;
import stone.lunchtime.service.IConstraintService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;

/**
 * Constraint controller.
 */
@RestController
@RequestMapping("/constraint")
@Tag(name = "Constraint management API", description = "Constraint management API")
public class ConstraintRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(ConstraintRestController.class);

	private final IConstraintService<ConstraintEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public ConstraintRestController(IConstraintService<ConstraintEntity> pService) {
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
	@PutMapping("/add")
	@Observed(name = "rest.constraint.add", contextualName = "rest#constraint#add")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Constraint management API" }, summary = "Adds a constraint.", description = "Will add a constraint into the data base. Will return it with its id when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your constraint was added and returned in the response body.", content = @Content(schema = @Schema(implementation = ConstraintDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your constraint is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ConstraintDtoOut> addConstraint(
			@Parameter(description = "Constraint object that will be stored in database.", required = true) @RequestBody ConstraintDtoIn pConstraint) {

		ConstraintRestController.LOG.atInfo().log("--> addConstraint - {}", pConstraint);
		var result = this.service.add(pConstraint);
		ConstraintRestController.LOG.atInfo().log("<-- addConstraint - New constraint has id {}", result.getId());
		return ResponseEntity.ok(result);
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
	@PatchMapping("/update/{constraintId}")
	@Observed(name = "rest.constraint.update", contextualName = "rest#constraint#update")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Constraint management API" }, summary = "Updates a constraint.", description = "Will update a constraint already present in the data base. Will return it when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your constraint was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = ConstraintDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your constraint or constraintId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ConstraintDtoOut> updateConstraint(
			@Parameter(description = "The contraint's id", required = true) @PathVariable("constraintId") Integer pConstraintId,
			@Parameter(description = "Constraint object that will be updated in database. All present values will be updated.", required = true) @RequestBody ConstraintDtoIn pConstraint)
			throws EntityNotFoundException {
		ConstraintRestController.LOG.atInfo().log("--> updateConstraint - {}", pConstraint);
		var result = this.service.update(pConstraintId, pConstraint);
		ConstraintRestController.LOG.atInfo().log("<-- updateConstraint - Constraint {} is updated by lunch lady {}",
				result.getId(), this.getConnectedUserId());
		return ResponseEntity.ok(result);
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
	 * @throws InconsistentStatusException is status is not right
	 */
	@DeleteMapping("/delete/{constraintId}")
	@Observed(name = "rest.constraint.delete", contextualName = "rest#constraint#delete")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Constraint management API" }, summary = "Deletes a constraint.", description = "Will delete a constraint already present in the data base. Will not return it when done. Will realy delete it from data base. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your constraint was deleted and true is returned in the response body.", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Your constraintId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to delete does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<Boolean> deleteConstraint(
			@Parameter(description = "The constraint's id", required = true) @PathVariable("constraintId") Integer pConstraintId)
			throws EntityNotFoundException, InconsistentStatusException {
		ConstraintRestController.LOG.atInfo().log("--> deleteConstraint - {}", pConstraintId);
		this.service.delete(pConstraintId);
		ConstraintRestController.LOG.atInfo().log("<-- deleteConstraint - Constraint {} is deleted by lunch lady {}",
				pConstraintId, this.getConnectedUserId());
		return ResponseEntity.ok(Boolean.TRUE);
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
	@GetMapping("/find/{constraintId}")
	@Observed(name = "rest.constraint.byid", contextualName = "rest#constraint#byid")
	@Operation(tags = {
			"Constraint management API" }, summary = "Finds one constraint.", description = "Will find a constraint already present in the data base. Will return it when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your constraint was found and returned in the response body.", content = @Content(schema = @Schema(implementation = ConstraintDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your constraintId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ConstraintDtoOut> constraintById(
			@Parameter(description = "The constraint's id", required = true) @PathVariable("constraintId") Integer pConstraintId)
			throws EntityNotFoundException {

		ConstraintRestController.LOG.atInfo().log("--> constraintById - {}", pConstraintId);
		var result = this.service.find(pConstraintId);
		ConstraintRestController.LOG.atInfo().log("<-- constraintById - Has found constraint {}", pConstraintId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all constraints. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @return all the constraint found or an empty list if none or if an error
	 *         occurred
	 */
	@GetMapping("/findall")
	@Observed(name = "rest.constraint.findall", contextualName = "rest#constraint#findall")
	@Operation(tags = {
			"Constraint management API" }, summary = "Finds all constraints.", description = "Will find all constraints already present in the data base. Will return them when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All constraints found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConstraintDtoOut.class)))) })
	public ResponseEntity<List<ConstraintDtoOut>> findAllConstraints() {
		ConstraintRestController.LOG.atInfo().log("--> findAllConstraints");
		var result = this.service.findAll();
		ConstraintRestController.LOG.atInfo().log("<-- findAllConstraints - Has found {} constraints", result.size());
		return ResponseEntity.ok(result);
	}
}
