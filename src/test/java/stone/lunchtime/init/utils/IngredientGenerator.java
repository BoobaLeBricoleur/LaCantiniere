// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.init.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.Disabled;

import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.IngredientDtoIn;

/**
 * Not a test class. Will generate ingredients.
 */
@Disabled("Not for tests, used for data base generation.")
public final class IngredientGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(IngredientGenerator.class);

	/**
	 * Constructor of the object.
	 */
	private IngredientGenerator() {
		throw new IllegalAccessError("Not for use");
	}

	/**
	 * Generates ingredients.
	 *
	 * @param pHowMany the number to generate
	 * @return the list of ingredients
	 */
	public static List<IngredientDtoIn> generate(int pHowMany) {
		var rawList = TestFileReader.readIngredients();
		final var listSize = rawList.size();
		if (pHowMany > listSize) {
			IngredientGenerator.LOG.atWarn().log("Not enough ingredients in the file, will only generate {} ingredients",
					listSize);
			pHowMany = listSize;
		}

		List<IngredientDtoIn> result = new ArrayList<>(pHowMany);
		for (var i = 0; i < pHowMany; i++) {
			IngredientGenerator.LOG.atDebug().log("Creating ingredient {}/{}", i, pHowMany);
			var ingredient = new IngredientDtoIn();
			var rawLine = rawList.get(i);
			var splitedLine = rawLine.split("\t");
			if (splitedLine.length != 3) {
				IngredientGenerator.LOG.error("Error on line {} size found is [{}] {}", i, splitedLine.length, rawLine);
				continue;
			}
			ingredient.setLabel(splitedLine[0]);
			if (!"-".equals(splitedLine[1])) {
				ingredient.setDescription(splitedLine[1]);
			}
			var img = splitedLine[2];
			var imgDto = new ImageDtoIn();
			if (!"-".equals(img)) {
				imgDto.setImage64(img);
				imgDto.setImagePath("img/ingredient/" + ingredient.getLabel() + ".png");
				ingredient.setImage(imgDto);
			}

			result.add(ingredient);
		}
		return result;
	}
}
