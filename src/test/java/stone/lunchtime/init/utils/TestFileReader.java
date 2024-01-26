// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.init.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.Disabled;

/**
 * Not a test class. Will read files. <br>
 *
 * Those files are used by generator in order to populate the database.
 */
@Disabled("Not for tests, used for data base generation.")
public final class TestFileReader {
	private static final Logger LOG = LoggerFactory.getLogger(TestFileReader.class);

	private static final String PATH_NAME = "names.txt";
	private static final String PATH_FIRSTNAME_MAN = "firstnameMen.txt";
	private static final String PATH_FIRSTNAME_WOMAN = "firstnameWomen.txt";
	private static final String PATH_TOWN = "towns.txt";
	private static final String PATH_ADDRESS = "addresses.txt";
	// TODO Handle
	private static final String PATH_INGREDIENT = "ingredients.txt";
	private static final String PATH_MEAL = "meals.txt";

	/** Default chraset used. */
	private static final Charset CHARSET = StandardCharsets.UTF_8;

	/**
	 * Constructor of the object.
	 */
	private TestFileReader() {
		throw new IllegalAccessError("Not for use");
	}

	/**
	 * Reads names.
	 *
	 * @return list of names
	 */
	public static List<String> readNames() {
		try {
			return TestFileReader.read(TestFileReader.PATH_NAME);
		} catch (IOException e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_NAME, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads ingredients.
	 *
	 * @return list of ingredients
	 */
	public static List<String> readIngredients() {
		try {
			return TestFileReader.read(TestFileReader.PATH_INGREDIENT);
		} catch (IOException e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_INGREDIENT, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads meals.
	 *
	 * @return list of meals
	 */
	public static List<String> readMeals() {
		try {
			return TestFileReader.read(TestFileReader.PATH_MEAL);
		} catch (IOException e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_MEAL, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads first names for man.
	 *
	 * @return list of first names for man.
	 */
	public static List<String> readManFirstname() {
		try {
			return TestFileReader.read(TestFileReader.PATH_FIRSTNAME_MAN);
		} catch (IOException e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_FIRSTNAME_MAN, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads first names for woman.
	 *
	 * @return list of first names for woman.
	 */
	public static List<String> readWomanFirstname() {
		try {
			return TestFileReader.read(TestFileReader.PATH_FIRSTNAME_WOMAN);
		} catch (IOException e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_FIRSTNAME_WOMAN, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads addresses
	 *
	 * @return list of addresses
	 */
	public static List<String> readAddresses() {
		try {
			return TestFileReader.read(TestFileReader.PATH_ADDRESS);
		} catch (IOException e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_ADDRESS, e);
		}
		return Collections.emptyList();
	}

	/**
	 * Reads towns
	 *
	 * @return a map of addresses
	 */
	public static Map<Integer, List<String>> readTowns() {
		Map<Integer, List<String>> result = new HashMap<>();
		try (var br = Files.newBufferedReader(Paths.get("src", "test", "resources", "data", TestFileReader.PATH_TOWN),
				TestFileReader.CHARSET)) {
			String line;
			Integer lastPostalCode = null;
			var id = 0;
			while ((line = br.readLine()) != null) {
				if (id % 2 == 0) {
					// un cp
					try {
						lastPostalCode = Integer.valueOf(line);
					} catch (NumberFormatException e) {
						TestFileReader.LOG.error("Line {}: The postal code {} is not a number", id, line, e);
						break;
					}
				} else {
					var towns = result.computeIfAbsent(lastPostalCode, k -> new ArrayList<>());
					towns.add(line);
				}
				id++;
			}
		} catch (Exception e) {
			TestFileReader.LOG.error("Error while reading file {}", TestFileReader.PATH_TOWN, e);
		}

		return result;
	}

	/**
	 * Reads a file line by line.
	 *
	 * @param pPath the file name in data directory
	 * @return all file's lines
	 * @throws IOException if an error occurred
	 */
	private static List<String> read(String pPath) throws IOException {
		List<String> result = new ArrayList<>();
		Path lPath;
		try {
			// lPath = Paths.get(ClassLoader.getSystemResource("data/" + pPath).toURI());
			lPath = Paths.get("src", "test", "resources", "data", pPath);
		} catch (Exception e) {
			throw new IOException("Path " + pPath + " invalide", e);
		}

		if (!Files.exists(lPath)) {
			throw new IOException("File " + lPath + " not found");
		}
		if (!Files.isReadable(lPath)) {
			throw new IOException("File " + lPath + " is not readable");
		}
		if (!Files.isRegularFile(lPath)) {
			throw new IOException("File " + lPath + " is not a file");
		}
		var id = 0;
		try (var br = Files.newBufferedReader(lPath, TestFileReader.CHARSET)) {
			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.startsWith("#")) {
					result.add(line);
				}
				id++;
			}
		} catch (Exception e) {
			throw new IOException("Error while reading file " + lPath + " line " + id, e);
		}

		return result;
	}

}
