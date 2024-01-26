// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.env.Environment;

/**
 * Main class for starting the application.
 */
@SpringBootApplication
public class SpringBootConfiguration extends SpringBootServletInitializer {
	private static final Logger LOG = LoggerFactory.getLogger(SpringBootConfiguration.class);

	private static final String SPRING_KEY_DRIVER = "spring.datasource.driver-class-name";
	private static final String SPRING_KEY_SSL = "server.ssl.enabled";

	/**
	 * Allow to deploy this application inside a JEE server as a WAR file. <br>
	 *
	 * @param application a builder for the application context
	 * @return the application builder
	 * @see SpringApplicationBuilder
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		SpringBootConfiguration.LOG.atInfo().log("-- Starting Lunch Time Project - Using Spring Security -- ");
		SpringBootConfiguration.LOG.atInfo().log(
				"-- Deploying on real server, so URL depends on your server configuration but context = nameofyourwar");
		return application.sources(SpringBootConfiguration.class);
	}

	/**
	 * Method that will start Spring Boot and the JEE server. <br>
	 *
	 * <ul>
	 * <li>For in memory H2 data base:
	 *
	 * <pre>
	 * java -jar -Dspring.profiles.active=h2 stone.lunchtime.war
	 * </pre>
	 *
	 * </li>
	 * <li>For MySQL standard data base:
	 *
	 * <pre>
	 * java -jar -Dspring.profiles.active=mysql stone.lunchtime.war --spring.datasource.username=yourlogin --spring.datasource.password=yourpwd
	 * </pre>
	 *
	 * </li>
	 *
	 * <li>For PostgreSQL standard data base:
	 *
	 * <pre>
	 * java -jar -Dspring.profiles.active=postgresql stone.lunchtime.war --spring.datasource.username=yourlogin --spring.datasource.password=yourpwd
	 * </pre>
	 *
	 * </li>
	 * </ul>
	 *
	 * @param args some parameters
	 */
	public static void main(String[] args) {
		var springApplication = new SpringApplication(SpringBootConfiguration.class);
		var context = springApplication.run(args);
		var insideEnv = context.getBean(Environment.class);
		SpringBootConfiguration.LOG.atInfo().log("-- Starting Lunch Time Project - Using Spring Security -- ");
		SpringBootConfiguration.LOG.atInfo()
				.log("-- Note : if using Docker, port can be different, see file .env and SPRING_LOCAL_PORT variable.");
		if (SpringBootConfiguration.usingH2(insideEnv)) {
			SpringBootConfiguration.LOG.atInfo().log("-- Using H2 in memory DBMS so:");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + Data base will be created at startup with some data (see data.sql script in src/main/resources directory)");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + image64 are not handled at initialization, you will only have the imagePath set in the data base.");
			SpringBootConfiguration.LOG.atInfo().log("--  + H2 console is available at http{}://localhost:{}{}{}",
					SpringBootConfiguration.usingSSL(insideEnv) ? "s" : "",
					insideEnv.getProperty("server.port", "8080"),
					insideEnv.getProperty("server.servlet.context-path", "/"),
					insideEnv.getProperty("spring.h2.console.path", ""));
			var h2Url = insideEnv.getProperty("spring.datasource.url", "NotFound!");
			if (h2Url.contains(";DB_CLOSE_ON_EXIT=FALSE")) {
				h2Url = h2Url.replace(";DB_CLOSE_ON_EXIT=FALSE", "");
			}
			SpringBootConfiguration.LOG.atInfo().log("--  + H2 console please use {} as JDBC URL", h2Url);
			SpringBootConfiguration.LOG.atInfo().log("--  + All information added will be lost at shutdown");
		} else if (SpringBootConfiguration.usingMySQL(insideEnv)) {
			SpringBootConfiguration.LOG.atInfo().log("-- Using MySQL DBMS, do not forget to verify that");
			SpringBootConfiguration.LOG.atInfo().log("--  + DBMS was started");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + You started the application with the right login/password (using --spring.datasource.username=yourlogin --spring.datasource.password=yourpwd)");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + You have created and loaded the lunchtime data base (see dump file in /database/dump directory)");
		} else if (SpringBootConfiguration.usingPostgreSQL(insideEnv)) {
			SpringBootConfiguration.LOG.atInfo().log("-- Using PostgreSQL DBMS, do not forget to verify that");
			SpringBootConfiguration.LOG.atInfo().log("--  + DBMS was started");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + You started the application with the right login/password (using --spring.datasource.username=yourlogin --spring.datasource.password=yourpwd)");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + You have created and loaded the lunchtime data base (see dump file in /database/dump directory)");
		} else if (SpringBootConfiguration.usingSQLServer(insideEnv)) {
			SpringBootConfiguration.LOG.atInfo().log("-- Using SQLServer DBMS, do not forget to verify that");
			SpringBootConfiguration.LOG.atInfo().log("--  + DBMS was started");
			SpringBootConfiguration.LOG.atInfo().log("--  + TCP/IP port is activated and set to 1433");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + You started the application with the right login/password (using --spring.datasource.username=yourlogin --spring.datasource.password=yourpwd)");
			SpringBootConfiguration.LOG.atInfo().log(
					"--  + You have created and loaded the lunchtime data base (see dump file in /database/dump directory)");
		} else {
			SpringBootConfiguration.LOG.atInfo().log(
					"-- Strange, you will be using DBMS {}, did you spell mysql or h2 properly?",
					insideEnv.getProperty(SpringBootConfiguration.SPRING_KEY_DRIVER, ""));
		}
		SpringBootConfiguration.LOG.atInfo().log("-- Project Lunch Time is up - Go to http{}://localhost:{}{} -- ",
				SpringBootConfiguration.usingSSL(insideEnv) ? "s" : "", insideEnv.getProperty("server.port", "8080"),
				insideEnv.getProperty("server.servlet.context-path", "/"));
	}

	/**
	 * Indicates if we are using H2 DBMS.
	 *
	 * @param pEnv environment
	 * @return true if we are using H2 DBMS
	 */
	public static boolean usingH2(Environment pEnv) {
		return pEnv.getProperty(SpringBootConfiguration.SPRING_KEY_DRIVER, "").contains("h2");
	}

	/**
	 * Indicates if we are using MySQL DBMS.
	 *
	 * @param pEnv environment
	 * @return true if we are using MySQL DBMS
	 */
	public static boolean usingMySQL(Environment pEnv) {
		return pEnv.getProperty(SpringBootConfiguration.SPRING_KEY_DRIVER, "").contains("mysql");
	}

	/**
	 * Indicates if we are using PostgreSQL DBMS.
	 *
	 * @param pEnv environment
	 * @return true if we are using PostgreSQL DBMS
	 */
	public static boolean usingPostgreSQL(Environment pEnv) {
		return pEnv.getProperty(SpringBootConfiguration.SPRING_KEY_DRIVER, "").contains("postgresql");
	}

	/**
	 * Indicates if we are using sqlserver DBMS.
	 *
	 * @param pEnv environment
	 * @return true if we are using sqlserver DBMS
	 */
	public static boolean usingSQLServer(Environment pEnv) {
		return pEnv.getProperty(SpringBootConfiguration.SPRING_KEY_DRIVER, "").contains("sqlserver");
	}

	/**
	 * Indicates if we are using SSL.
	 *
	 * @param pEnv environment
	 * @return true if we are using SSL
	 */
	public static boolean usingSSL(Environment pEnv) {
		return "true".equalsIgnoreCase(pEnv.getProperty(SpringBootConfiguration.SPRING_KEY_SSL, "false"));
	}

}
