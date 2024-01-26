// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.init.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.jupiter.api.Disabled;

import stone.lunchtime.dto.in.UserDtoIn;
import stone.lunchtime.entity.Sex;

/**
 * Not a test class. Will generate users.
 */
@Disabled("Not for tests, used for data base generation.")
public final class UserGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(UserGenerator.class);

	private static final String[] MAIL_EXT = { "gmail.com", "free.fr", "aol.com", "orange.fr", "stone.fr" };

	/**
	 * Constructor of the object.
	 */
	private UserGenerator() {
		throw new IllegalAccessError("Not for use");
	}

	/**
	 * Generates users.
	 *
	 * @param pHowMany    how many user do you want
	 * @param pDefaultPwd the default password to use. If null will generate one.
	 * @return a user list
	 */
	public static List<UserDtoIn> generate(int pHowMany, String pDefaultPwd) {
		var names = TestFileReader.readNames();
		var firstnamesForMen = TestFileReader.readManFirstname();
		var firstnamesForWomen = TestFileReader.readWomanFirstname();
		var addresses = TestFileReader.readAddresses();
		var towns = TestFileReader.readTowns();
		var random = new Random();
		List<UserDtoIn> result = new ArrayList<>(pHowMany);
		for (var i = 0; i < pHowMany; i++) {
			UserGenerator.LOG.atDebug().log("Creating user {}/{}", i, pHowMany);
			var userDto = new UserDtoIn();
			userDto.setName(names.get(random.nextInt(names.size())));
			var sex = Sex.fromValue((byte) random.nextInt(3));
			userDto.setSex(sex);
			if (userDto.isMan()) {
				userDto.setFirstname(firstnamesForMen.get(random.nextInt(firstnamesForMen.size())));
			} else if (userDto.isWoman()) {
				userDto.setFirstname(firstnamesForWomen.get(random.nextInt(firstnamesForWomen.size())));
			} else if (random.nextBoolean()) {
				userDto.setFirstname(firstnamesForWomen.get(random.nextInt(firstnamesForWomen.size())));
			} else {
				userDto.setFirstname(firstnamesForMen.get(random.nextInt(firstnamesForMen.size())));
			}

			if (random.nextBoolean()) {
				userDto.setAddress(addresses.get(random.nextInt(addresses.size())));
				// 1000 a 98890
				List<String> towNames = null;
				var cp = -1;
				while (towNames == null) {
					cp = random.nextInt(98890 + 1);
					towNames = towns.get(Integer.valueOf(cp));
				}
				userDto.setPostalCode(String.valueOf(cp));
				userDto.setTown(towNames.get(random.nextInt(towNames.size())));
			}
			userDto.setEmail(UserGenerator.generateEmail(userDto.getName(), userDto.getFirstname(), random));
			if (random.nextBoolean()) {
				userDto.setWallet(Float.valueOf(random.nextFloat() * random.nextInt(100)));
			}
			userDto.setPassword(
					Objects.requireNonNullElseGet(pDefaultPwd, () -> UserGenerator.generatePassword(8, false, random)));
			if (random.nextBoolean()) {
				var nu = new StringBuilder();
				for (var j = 0; j < 10; j++) {
					nu.append(random.nextInt(10));
				}
				userDto.setPhone(nu.toString());
			}
			result.add(userDto);
		}
		return result;
	}

	/**
	 * Generates an email.
	 *
	 * @param pName      the name
	 * @param pFirstname the first name
	 * @param pRandom    random generator
	 * @return an email
	 */
	private static String generateEmail(String pName, String pFirstname, Random pRandom) {
		var email = new StringBuilder();
		pFirstname = pFirstname.replace(' ', '_');
		if (pFirstname.length() > 5) {
			email.append(pFirstname, 0, 5);
		} else {
			email.append(pFirstname);
		}
		email.append('_');
		email.append(String.valueOf(pRandom.nextInt(99999)));
		email.append('.');
		pName = pName.replace(' ', '_');
		if (pName.length() > 5) {
			email.append(pName, 0, 5);
		} else {
			email.append(pName);
		}
		email.append('@');
		email.append(UserGenerator.MAIL_EXT[pRandom.nextInt(UserGenerator.MAIL_EXT.length)]);
		return email.toString().toLowerCase();
	}

	/**
	 * Generates a password.
	 *
	 * @param pSize             amount of char to generate
	 * @param pAllowSpecialChar if true will also use special chars
	 * @param pRandom           random generator
	 * @return the password
	 */
	private static String generatePassword(int pSize, boolean pAllowSpecialChar, Random pRandom) {
		// http://www.asciitable.com/
		// 33-46 [special char]
		// 48-57 [0,...,9]
		// 65-90 [A,B,...,Z]
		// 97-122 [a,b,...,z]
		var sb = new StringBuilder();
		for (var i = 0; i < pSize; i++) {
			final var val = pAllowSpecialChar ? pRandom.nextInt(4) : pRandom.nextInt(3);
			switch (val) {
			case 0:
				sb.append((char) (pRandom.nextInt(91 - 65) + 65));
				break;
			case 1:
				sb.append((char) (pRandom.nextInt(58 - 48) + 48));
				break;
			case 2:
				sb.append((char) (pRandom.nextInt(123 - 97) + 97));
				break;
			case 3:
				var c = (char) (pRandom.nextInt(47 - 33) + 33);
				while (c == '"' || c == '\'' || c == ',' || c == '.') {
					c = (char) (pRandom.nextInt(47 - 33) + 33);
				}
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
}
