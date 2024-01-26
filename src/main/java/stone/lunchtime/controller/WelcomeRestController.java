// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.controller;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * default root controller.
 */
@RestController
@RequestMapping("/")
public class WelcomeRestController extends AbstractController {
	private static final Logger LOG = LoggerFactory.getLogger(WelcomeRestController.class);

	/**
	 * Redirect to Swagger URL
	 *
	 * @return the root page (swagger here)
	 */
	@GetMapping
	public ModelAndView welcome() {
		WelcomeRestController.LOG.atDebug().log("--> welcome");
		return new ModelAndView("redirect:/swagger-ui.html");
	}
}
