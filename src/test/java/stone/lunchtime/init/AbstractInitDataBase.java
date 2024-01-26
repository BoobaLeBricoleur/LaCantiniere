// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.Rollback;

import stone.lunchtime.AbstractJpaTest;

/**
 * This is not a real test. <br>
 * Code used in order to initialize database with data. <br>
 * Cannot be used with H2, but works with MySQL, Postgres and SQLServer.
 */
@Rollback(false)
public abstract class AbstractInitDataBase extends AbstractJpaTest {
	/** Nb user to generate. */
	public static final int USER_NB = 100;
	/** Lunch lady email. */
	public static final String USER_EXISTING_EMAIL = "toto@gmail.com";
	/** Default password. */
	public static final String USER_DEFAULT_PWD = "bonjour";
	/** Default id for first user. */
	public static final Integer USER_EXISTING_ID = Integer.valueOf(1);

	/** Nb ingredient to generate. */
	public static final int INGREDIENT_NB = 50;
	/** Nb meal to generate. */
	public static final int MEAL_NB = 50;
	/** Nb menu to generate. */
	public static final int MENU_NB = 60;

	@Autowired
	protected Environment env;

}
