// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.init.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.Disabled;

import stone.lunchtime.dto.in.ImageDtoIn;
import stone.lunchtime.dto.in.MealDtoIn;
import stone.lunchtime.entity.MealCategory;

/**
 * Not a test class. Will generate meals.
 */
@Disabled("Not for tests, used for data base generation.")
public final class MealGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(MealGenerator.class);

	/**
	 * Constructor of the object.
	 */
	private MealGenerator() {
		throw new IllegalAccessError("Not for use");
	}

	/**
	 * Generates meals.
	 *
	 * @param pHowMany the number to generate
	 * @return the list of meals
	 */
	public static List<MealDtoIn> generate(int pHowMany) {
		var rawList = TestFileReader.readMeals();
		final var listSize = rawList.size();
		if (pHowMany > listSize) {
			MealGenerator.LOG.atWarn().log("Not enough meals in the file, will only generate {} meals", listSize);
			pHowMany = listSize;
		}

		var random = new Random();
		List<MealDtoIn> result = new ArrayList<>(pHowMany);
		for (var i = 0; i < pHowMany; i++) {
			MealGenerator.LOG.atDebug().log("Creating meal {}/{}", i, pHowMany);
			var meal = new MealDtoIn();
			var rawLine = rawList.get(i);
			var splitedLine = rawLine.split("\t");
			if (splitedLine.length != 4) {
				MealGenerator.LOG.error("Error on line {} size found is [{}] {}", i, splitedLine.length, rawLine);
				continue;
			}
			meal.setLabel(splitedLine[0]);
			if (!"-".equals(splitedLine[1])) {
				meal.setDescription(splitedLine[1]);
			}
			if (!"-".equals(splitedLine[2])) {
				meal.setCategory(MealCategory.fromValue(Byte.parseByte(splitedLine[2])));
			} else {
				meal.setCategory(MealCategory.UNKNOWN);
			}

			var img = splitedLine[3];
			var imgDto = new ImageDtoIn();
			if (!"-".equals(img)) {
				imgDto.setImage64(img);
				imgDto.setImagePath("img/meal/" + meal.getLabel() + ".png");
				meal.setImage(imgDto);
			}

			meal.setPriceDF(random.nextFloat() * 15F + 0.5F);
			result.add(meal);
		}
		return result;
	}
}
