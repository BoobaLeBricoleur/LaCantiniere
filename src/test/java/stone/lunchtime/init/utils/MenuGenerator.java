// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.init.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.Disabled;

import stone.lunchtime.dto.in.MenuDtoIn;

/**
 * Not a test class. Will generate menus.
 */
@Disabled("Not for tests, used for data base generation.")
public final class MenuGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(MenuGenerator.class);

	/**
	 * Constructor of the object.
	 */
	private MenuGenerator() {
		throw new IllegalAccessError("Not for use");
	}

	/**
	 * Generates menus
	 *
	 * @param pHowMany number of elements to generate
	 * @return list of menus
	 */
	public static List<MenuDtoIn> generate(int pHowMany) {
		var random = new Random();
		List<MenuDtoIn> result = new ArrayList<>(pHowMany);
		for (var i = 0; i < pHowMany; i++) {
			MenuGenerator.LOG.atDebug().log("Creating menu {}/{}", i, pHowMany);
			var menu = new MenuDtoIn();
			menu.setLabel("Menu - " + i);
			menu.setPriceDF(random.nextFloat() * 20F + 5F);
			result.add(menu);
		}
		return result;
	}
}
