// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.dto.in;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import stone.lunchtime.dto.DtoUtils;

/**
 * The dto class for the image database table. The default notion cannot be
 * handled by DTO In. It is more the system that does this.
 */
@Schema(description = "Image element (pictures for user, meal, ingredient, menu)")
public class ImageDtoIn extends AbstractDtoIn {
	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "The image path.", nullable = true, example = "img/toto.png")
	private String imagePath;

	@Schema(description = "The image encoded in base 64.", nullable = true, example = "see https://www.base64-image.de/")
	private String image64;

	/**
	 * Constructor of the object.
	 */
	public ImageDtoIn() {
		super();
	}

	/**
	 * Gets the attribute imagePath.
	 *
	 * @return the value of imagePath.
	 */
	public String getImagePath() {
		return this.imagePath;
	}

	/**
	 * Sets a new value for the attribute imagePath.
	 *
	 * @param pImagePath the new value for the attribute.
	 */
	public void setImagePath(String pImagePath) {
		this.imagePath = DtoUtils.checkAndClean(pImagePath);
	}

	/**
	 * Gets the attribute image64.
	 *
	 * @return the value of image64.
	 */
	public String getImage64() {
		return this.image64;
	}

	/**
	 * Sets a new value for the attribute image64.
	 *
	 * @param pImage64 the new value for the attribute.
	 */
	public void setImage64(String pImage64) {
		this.image64 = DtoUtils.checkAndClean(pImage64);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		var parent = super.toString();
		parent = parent.substring(0, parent.length() - 1);
		sb.append(parent);
		sb.append("path=");
		if (this.getImagePath() != null) {
			sb.append(this.getImagePath(), 0, Math.min(20, this.getImagePath().length()));
		} else {
			sb.append("null");
		}
		sb.append(",base64=");
		if (this.getImage64() != null) {
			sb.append(this.getImage64(), 0, Math.min(10, this.getImage64().length()));
		} else {
			sb.append("null");
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public void validate() {
		// No validation for this been
	}
}
