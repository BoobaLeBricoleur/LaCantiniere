// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.spring;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import stone.lunchtime.SpringBootConfiguration;
import stone.lunchtime.spring.security.filter.SecurityConstants;

/**
 * Open API configuration class. <br>
 *
 * Same as Swagger, but works with Spring 2.2+
 *
 * @see <a href="https://springdoc.github.io/springdoc-openapi-demos/">tuto</a>
 */
@Configuration
public class SpringOpenApiConfiguration {
	private static final Logger LOG = LoggerFactory.getLogger(SpringOpenApiConfiguration.class);

	@Bean
	public OpenAPI customOpenAPI(Environment env) {
		SpringOpenApiConfiguration.LOG.atDebug().log("Loading Open API configuration");
		var openApi = new OpenAPI();
		openApi.setPaths(null);
		var rootUrl = "http" + (SpringBootConfiguration.usingSSL(env) ? "s" : "") + "://localhost:"
				+ env.getProperty("server.port", "8080") + env.getProperty("server.servlet.context-path", "");

		var info = new Info();
		info.setTitle("Lunch Time Rest API Information");
		info.setDescription(
				"Describes all services available for you in order to handle Lunch Time project. Will use JWT and Spring Security. The graphql API is available at "
						+ rootUrl + "/graphiql?path=" + env.getProperty("server.servlet.context-path", "")
						+ "/graphql");
		info.setTermsOfService("Free to use inside training sessions");
		info.setVersion("December 2023");

		var contact = new Contact();
		contact.setName("FERRET Renaud");
		contact.setUrl("https://ferretrenaud.ovh/renaud91/stone.lunchtime/wikis/home");
		contact.setEmail("admin@ferretrenaud.fr");
		info.setContact(contact);

		var license = new License();
		license.setName("Attribution Assurance License");
		license.setUrl(rootUrl + "/license.txt");

		info.setLicense(license);

		openApi.setInfo(info);

		List<Tag> tags = new ArrayList<>();

		var t = new Tag();
		t.setDescription("Let you add,update,delete,find ingredients. Ingredients are used in meal.");
		t.setName("Ingredient management API");
		tags.add(t);

		t = new Tag();
		t.setDescription("Let you add,update,delete,find constraints. Constraints are global informations.");
		t.setName("Constraint management API");
		tags.add(t);

		t = new Tag();
		t.setDescription("Used for sending a new user password");
		t.setName("Forgot password API");
		tags.add(t);

		t = new Tag();
		t.setDescription("Let you add,update,delete,find meals. Meals can be ordered.");
		t.setName("Meal management API");
		tags.add(t);

		t = new Tag();
		t.setDescription("Let you add,update,delete,find menus. Menus can be ordered.");
		t.setName("Menu management API");
		tags.add(t);

		t = new Tag();
		t.setDescription("Let you add,cancel,update,pay,cancel,find orders. Main functionality of the project.");
		t.setName("Order management API");
		tags.add(t);

		t = new Tag();
		t.setDescription("Let you activate,credit,debit,deactivate,delete,add,update,checkpassword,find users.");
		t.setName("User management API");
		tags.add(t);

		openApi.setTags(tags);

		// "bearer-key" will be used in the @Opertaion annotation, it will not be
		// handled by Spring
		openApi.components(new Components().addSecuritySchemes("bearer-key", new SecurityScheme()
				.type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat(SecurityConstants.TOKEN_TYPE)));

		return openApi;
	}

}
