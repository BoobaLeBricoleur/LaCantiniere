// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
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
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ExceptionDtoOut;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.entity.jpa.IngredientEntity;
import stone.lunchtime.service.IIngredientService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;

/**
 * Ingredient controller.
 */
@RestController
@RequestMapping("/ingredient")
@Tag(name = "Ingredient management API", description = "Ingredient management API")
public class IngredientRestController extends AbstractRestController {
	private static final Logger LOG = LoggerFactory.getLogger(IngredientRestController.class);

	private final IIngredientService<IngredientEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public IngredientRestController(IIngredientService<IngredientEntity> pService) {
		super();
		this.service = pService;
	}

	/**
	 * Adds an ingredient. <br>
	 *
	 * You need to be connected as a lunch lady.
	 *
	 * @param pIngredient the ingredient to be added
	 *
	 * @return the ingredient added
	 */
	@PutMapping("/add")
	@Observed(name = "rest.ingredient.add", contextualName = "rest#ingredient#add")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Adds an ingredient.", description = "Will add an ingredient into the data base. Will return it with its id when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your ingredient was added and returned in the response body.", content = @Content(schema = @Schema(implementation = IngredientDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your ingredient is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<IngredientDtoOut> addIngredient(
			@Parameter(description = "Ingredient object that will be stored in database", required = true) @RequestBody IngredientDtoIn pIngredient) {
		IngredientRestController.LOG.atInfo().log("--> addIngredient - {}", pIngredient);
		var result = this.service.add(pIngredient);
		IngredientRestController.LOG.atInfo().log("<-- addIngredient - New ingredient has id {}", result.getId());
		return ResponseEntity.ok(result);
	}

	/**
	 * Updates an ingredient. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * You cannot update status with this method.
	 *
	 * @param pIngredientId id of the ingredient to be updated
	 * @param pIngredient   the new data for this ingredient
	 * @return the ingredient updated
	 * @throws EntityNotFoundException if an error occurred
	 */
	@PatchMapping("/update/{ingredientId}")
	@Observed(name = "rest.ingredient.update", contextualName = "rest#ingredient#update")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Updates an ingredient.", description = "Will update an ingredient already present in the data base. Will return it when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your ingredient was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = IngredientDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your ingredient or ingredientId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<IngredientDtoOut> updateIngredient(
			@Parameter(description = "The ingredient's id", required = true) @PathVariable("ingredientId") Integer pIngredientId,
			@Parameter(description = "Ingredient object that will be updated in database. All present values will be updated.", required = true) @RequestBody IngredientDtoIn pIngredient)
			throws EntityNotFoundException {
		IngredientRestController.LOG.atInfo().log("--> updateIngredient - {}", pIngredient);
		var result = this.service.update(pIngredientId, pIngredient);
		IngredientRestController.LOG.atInfo().log("<-- updateIngredient - Ingredient {} is updated by lunch lady {}",
				result.getId(), this.getConnectedUserId());
		return ResponseEntity.ok(result);
	}

	/**
	 * Deletes an ingredient. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 * Ingredient will still be in the database but its status will be deleted. This
	 * status is permanent.
	 *
	 * @param pIngredientId id of the ingredient to be deleted
	 * @return the ingredient deleted
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@DeleteMapping("/delete/{ingredientId}")
	@Observed(name = "rest.ingredient.delete", contextualName = "rest#ingredient#delete")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Deletes an ingredient.", description = "Will delete an ingredient already present in the data base. Will return true when done. Note that element is not realy deleted from database but will change its status to DELETE (2). You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your ingredient was deleted and true is returned in the response body.", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Your ingredientId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to delete does not exist or is not a deleteable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<Boolean> deleteIngredient(
			@Parameter(description = "The ingredient's id", required = true) @PathVariable("ingredientId") Integer pIngredientId)
			throws EntityNotFoundException, InconsistentStatusException {
		IngredientRestController.LOG.atInfo().log("--> deleteIngredient - {}", pIngredientId);
		var result = this.service.delete(pIngredientId);
		IngredientRestController.LOG.atInfo().log("<-- deleteIngredient - Ingredient {} is deleted by lunch lady {}",
				pIngredientId, this.getConnectedUserId());
		return ResponseEntity.ok(result.isDeleted());
	}

	/**
	 * Gets an ingredient. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param pIngredientId id of the ingredient you are looking for
	 * @return the ingredient found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 */
	@GetMapping("/find/{ingredientId}")
	@Observed(name = "rest.ingredient.byid", contextualName = "rest#ingredient#byid")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Finds one ingredient.", description = "Will find an ingredient already present in the data base. Will return it when done. You do not need to be connected in order to execute this action.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your ingredient was found and returned in the response body.", content = @Content(schema = @Schema(implementation = IngredientDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your ingredientId is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element was not found.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<IngredientDtoOut> ingredientById(
			@Parameter(description = "The ingredient's id", required = true) @PathVariable("ingredientId") Integer pIngredientId)
			throws EntityNotFoundException {
		IngredientRestController.LOG.atInfo().log("--> ingredientById - {}", pIngredientId);
		var result = this.service.find(pIngredientId);
		IngredientRestController.LOG.atInfo().log("<-- ingredientById - Has found meal {}", pIngredientId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Gets all ingredients. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @return all the ingredients found or an empty list if none
	 */
	@GetMapping("/findall")
	@Observed(name = "rest.ingredient.findall", contextualName = "rest#ingredient#findall")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Finds all ingredients.", description = "Will find all ingredients already present in the data base. Will return them when done. You must be connected and have the lunch lady role in order to execute this action.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All ingredients found will be in the response body.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = IngredientDtoOut.class)))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<List<IngredientDtoOut>> findAllIngredients() {
		IngredientRestController.LOG.atInfo().log("--> findAllIngredients");
		var result = this.service.findAll();
		IngredientRestController.LOG.atInfo().log("<-- findAllIngredients - Lunch lady {} has found {} ingredients",
				this.getConnectedUserId(), result.size());
		return ResponseEntity.ok(result);
	}

	/**
	 * Updates a menu image. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @param id     id of the element to be updated
	 * @param pImage where to find the new information
	 * @return the element updated
	 * @throws InconsistentStatusException if an error occurred
	 * @throws EntityNotFoundException     if an error occurred
	 */
	@PatchMapping("/updateimg/{id}")
	@Observed(name = "rest.ingredient.updateimg", contextualName = "rest#ingredient#updateimg")
	@PreAuthorize("hasRole('ROLE_LUNCHLADY')")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Updates an element's image.", description = "Will update the image of an element already present in the data base.", security = {
					@SecurityRequirement(name = "bearer-key") })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Your elements's image was updated and returned in the response body.", content = @Content(schema = @Schema(implementation = IngredientDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your element id is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "401", description = "You are not connected or do not have the LunchLady role.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to update does not exist or has not the correct status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<IngredientDtoOut> updateIngredientImage(
			@Parameter(description = "The element's id", required = true) @PathVariable("id") Integer id,
			@Parameter(description = "Image object that will be updated in database. ", required = true) @RequestBody ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {
		IngredientRestController.LOG.atInfo().log("--> updateIngredientImage - {}", pImage);
		var result = this.service.updateImage(id, pImage);
		IngredientRestController.LOG.atInfo().log("<-- updateIngredientImage - Ingredient {} image is updated by user {}",
				result.getId(), this.getConnectedUserId());
		return ResponseEntity.ok(result);
	}

	/**
	 * Finds an element's image. <br>
	 *
	 * Every one can use this method. No need to be connected. <br>
	 *
	 * @param id id of the element's image you are looking for
	 * @return the image found or an error if none
	 * @throws EntityNotFoundException if an error occurred
	 *
	 */
	@GetMapping("/findimg/{ingredientid}")
	@Observed(name = "rest.ingredient.findimg", contextualName = "rest#ingredient#findimg")
	@Operation(tags = {
			"Ingredient management API" }, summary = "Finds an elements's image.", description = "Will find an element's image already present in the data base. Will return it when done. Every one can call this method.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The image was found and returned in the response body.", content = @Content(schema = @Schema(implementation = ImageDtoOut.class))),
			@ApiResponse(responseCode = "400", description = "Your id is not valid.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))),
			@ApiResponse(responseCode = "412", description = "The element to find does not exist or is not findable status.", content = @Content(schema = @Schema(implementation = ExceptionDtoOut.class))) })
	public ResponseEntity<ImageDtoOut> findIngredientImage(
			@Parameter(description = "The ingredient's id", required = true) @PathVariable("ingredientid") Integer id)
			throws EntityNotFoundException {
		IngredientRestController.LOG.atInfo().log("--> findIngredientImage - {}", id);
		var result = this.service.findEntity(id);
		var dtoOut = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		IngredientRestController.LOG.atInfo().log("<-- findIngredientImage - Ingredient's image {} found by user {}",
				dtoOut.getId(), this.getConnectedUserId());
		return ResponseEntity.ok(dtoOut);
	}
}
