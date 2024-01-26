// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
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
import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.IngredientDtoIn;
import stone.lunchtime.dto.jpa.handler.ImageDtoHandler;
import stone.lunchtime.dto.out.ImageDtoOut;
import stone.lunchtime.dto.out.IngredientDtoOut;
import stone.lunchtime.entity.jpa.IngredientEntity;
import stone.lunchtime.service.IIngredientService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;

/**
 * Ingredient controller.
 */
@Controller
public class IngredientGqlController extends AbstractGqlController {
	private static final Logger LOG = LoggerFactory.getLogger(IngredientGqlController.class);

	private final IIngredientService<IngredientEntity> service;

	/**
	 * Constructor.
	 *
	 * @param pService the service
	 */
	@Autowired
	public IngredientGqlController(IIngredientService<IngredientEntity> pService) {
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
	@MutationMapping
	@Observed(name = "graphql.ingredient.add", contextualName = "graphql#ingredient#add")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public IngredientDtoOut addIngredient(@Argument("ingredient") IngredientDtoIn pIngredient) {
		IngredientGqlController.LOG.atInfo().log("--> addIngredient - {}", pIngredient);
		var result = this.service.add(pIngredient);
		IngredientGqlController.LOG.atInfo().log("<-- addIngredient - New ingredient has id {}", result.getId());
		return result;
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
	@MutationMapping
	@Observed(name = "graphql.ingredient.update", contextualName = "graphql#ingredient#update")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public IngredientDtoOut updateIngredient(@Argument("id") Integer pIngredientId,
			@Argument("ingredient") IngredientDtoIn pIngredient) throws EntityNotFoundException {
		IngredientGqlController.LOG.atInfo().log("--> updateIngredient - {}", pIngredient);
		var result = this.service.update(pIngredientId, pIngredient);
		IngredientGqlController.LOG.atInfo().log("<-- updateIngredient - Ingredient {} is updated by lunch lady {}",
				result.getId(), this.getConnectedUserId());
		return result;
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
	@MutationMapping
	@Observed(name = "graphql.ingredient.delete", contextualName = "graphql#ingredient#delete")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public boolean deleteIngredient(@Argument("id") Integer pIngredientId)
			throws EntityNotFoundException, InconsistentStatusException {
		IngredientGqlController.LOG.atInfo().log("--> deleteIngredient - {}", pIngredientId);
		var result = this.service.delete(pIngredientId);
		IngredientGqlController.LOG.atInfo().log("<-- deleteIngredient - Ingredient {} is deleted by lunch lady {}",
				pIngredientId, this.getConnectedUserId());
		return result.isDeleted();
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
	@QueryMapping
	@Observed(name = "graphql.ingredient.byid", contextualName = "graphql#ingredient#byid")
	public IngredientDtoOut ingredientById(@Argument("id") Integer pIngredientId) throws EntityNotFoundException {
		IngredientGqlController.LOG.atInfo().log("--> ingredientById - {}", pIngredientId);
		var result = this.service.find(pIngredientId);
		IngredientGqlController.LOG.atInfo().log("<-- ingredientById - Has found meal {}", pIngredientId);
		return result;
	}

	/**
	 * Gets all ingredients. <br>
	 *
	 * You need to be connected as a lunch lady. <br>
	 *
	 * @return all the ingredients found or an empty list if none
	 */
	@QueryMapping
	@Observed(name = "graphql.ingredient.findall", contextualName = "graphql#ingredient#findall")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public List<IngredientDtoOut> findAllIngredients() {
		IngredientGqlController.LOG.atInfo().log("--> findAllIngredients()");
		var result = this.service.findAll();
		IngredientGqlController.LOG.atInfo().log("<-- findAllIngredients - Lunch lady {} has found {} ingredients",
				this.getConnectedUserId(), result.size());
		return result;
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
	@MutationMapping
	@Observed(name = "graphql.ingredient.updateimg", contextualName = "graphql#ingredient#updateimg")
	@PreAuthorize("isAuthenticated() and hasRole('ROLE_LUNCHLADY')")
	public IngredientDtoOut updateIngredientImage(@Argument("id") Integer id, @Argument("image") ImageDtoIn pImage)
			throws EntityNotFoundException, InconsistentStatusException {
		IngredientGqlController.LOG.atInfo().log("--> updateIngredientImage(Integer, ImageDtoIn) - {}", pImage);
		var result = this.service.updateImage(id, pImage);
		IngredientGqlController.LOG.atInfo().log("<-- updateIngredientImage - Ingredient {} image is updated by user {}",
				result.getId(), this.getConnectedUserId());
		return result;
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
	@QueryMapping
	@Observed(name = "graphql.ingredient.findimg", contextualName = "graphql#ingredient#findimg")
	public ImageDtoOut findIngredientImage(@Argument("id") Integer id) throws EntityNotFoundException {
		IngredientGqlController.LOG.atInfo().log("--> findIngredientImage - {}", id);
		var result = this.service.findEntity(id);
		var dto = ImageDtoHandler.dtoOutfromEntity(result.getImage());
		IngredientGqlController.LOG.atInfo().log("<-- findIngredientImage - Ingredient's image {} found by user {}",
				dto.getId(), this.getConnectedUserId());
		return dto;
	}
}
