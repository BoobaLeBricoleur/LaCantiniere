// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.DtoUtils;
import stone.lunchtime.service.exception.ParameterException;

/**
 * Dto with label, description, image. Package visibility
 */
@Schema(description = "Represents a labeled element.", subTypes = { IngredientDtoIn.class, AbstractEatableDtoIn.class })
public abstract class AbstractLabeledDtoIn extends AbstractDtoIn {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AbstractLabeledDtoIn.class);

	@Schema(description = "A description of this element.", nullable = true)
	private String description;
	@Schema(description = "The visible label for this element.", requiredMode = Schema.RequiredMode.REQUIRED)
	private String label;

	@Schema(description = "The image.", nullable = true)
	private ImageDtoIn image;

	// status is not handled by DTO

	/**
	 * Constructor of the object. <br>
	 */
	protected AbstractLabeledDtoIn() {
		super();
	}

	/**
	 * Gets the attribute image.
	 *
	 * @return the value of image.
	 */
	public ImageDtoIn getImage() {
		return this.image;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pImage the new value for image attribute
	 */
	public void setImage(ImageDtoIn pImage) {
		this.image = pImage;
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the description value.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pDescription the new value for description attribute
	 */
	public void setDescription(String pDescription) {
		this.description = DtoUtils.checkAndClean(pDescription);
	}

	/**
	 * Gets the attribute value.
	 *
	 * @return the label value.
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Sets the attribute value.
	 *
	 * @param pLabel the new value for label attribute
	 */
	public void setLabel(String pLabel) {
		this.label = DtoUtils.checkAndClean(pLabel);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append("label=");
		sb.append(this.getLabel());
		if (this.getDescription() != null) {
			sb.append(",description=");
			sb.append(this.getDescription());
		}
		if (this.getImage() != null) {
			sb.append(",image=");
			sb.append(this.getImage());
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	@JsonIgnore
	public void validate() {
		if (this.getLabel() == null) {
			AbstractLabeledDtoIn.LOG.atError().log("validate - Label must be set");
			throw new ParameterException("Libelle ne doit pas être null", "label");
		}
	}
}
