// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019 -
// -# Email: admin@ferretrenaud.fr -
// -# All Rights Reserved. -
// -#--------------------------------------

package stone.lunchtime.service.impl.jpa;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import stone.lunchtime.dao.jpa.IUserDao;
import stone.lunchtime.dto.jpa.handler.UserDtoHandler;
import stone.lunchtime.entity.jpa.RoleEntity;
import stone.lunchtime.entity.jpa.UserEntity;
import stone.lunchtime.service.IAuthenticationService;
import stone.lunchtime.service.IEmailService;
import stone.lunchtime.service.exception.EntityNotFoundException;
import stone.lunchtime.service.exception.InconsistentStatusException;
import stone.lunchtime.service.exception.ParameterException;
import stone.lunchtime.service.exception.SendMailException;
import stone.lunchtime.utils.ValidationUtils;

/**
 * Authentication service.
 */
@Service
public class AuthenticationService implements IAuthenticationService {
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

	private final IUserDao userDao;
	private final PasswordEncoder passwordEncoder;
	private final IEmailService emailService;

	@Value("${configuration.forgot.password.email.body}")
	private String mailBody;
	@Value("${configuration.forgot.password.email.subject}")
	private String mailSubject;

	/**
	 * Constructor.
	 *
	 * @param userDao         user dao
	 * @param emailService    email service
	 * @param passwordEncoder password encoder
	 */
	@Autowired
	public AuthenticationService(IUserDao userDao, IEmailService emailService, PasswordEncoder passwordEncoder) {
		this.userDao = userDao;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Spring Security method.
	 *
	 * @param pAuthentication authentication object with login and password
	 * @return authentication object with role
	 */
	@Override
	public Authentication authenticate(Authentication pAuthentication) {
		var name = pAuthentication.getName();
		var password = pAuthentication.getCredentials() != null ? pAuthentication.getCredentials().toString() : null;

		try {
			ValidationUtils.isNotEmpty(name, "Email cannot be null or empty");
			ValidationUtils.isNotEmpty(password, "Password cannot be null or empty");
		} catch (Exception e) {
			throw new BadCredentialsException("email ou mot de passe est vide ou null !", e);
		}

		AuthenticationService.LOG.atDebug().log("Spring Security Authenticate name={}", name);
		var user = this.authenticate(name, password);
		if (user != null) {
			AuthenticationService.LOG.atDebug().log("Spring Security Authenticate found {}", user);
			Collection<GrantedAuthority> springSecurityRoles = new ArrayList<>(2);
			// Get role in data base, become a role in SS
			for (RoleEntity role : user.getRoles()) {
				GrantedAuthority ga = new SimpleGrantedAuthority(role.getLabel().toString());
				springSecurityRoles.add(ga);
			}
			var upat = new UsernamePasswordAuthenticationToken(name, password, springSecurityRoles);
			upat.setDetails(UserDtoHandler.dtoOutfromEntity(user));
			return upat;
		}
		return null;
	}

	/**
	 * Spring Security method.
	 *
	 * @param pAuthentication authentication object
	 * @return true if parameter belong to Authentication family
	 */
	@Override
	public boolean supports(Class<?> pAuthentication) {
		AuthenticationService.LOG.atDebug().log("support : {} ?", pAuthentication);
		var resu = Authentication.class.isAssignableFrom(pAuthentication);
		AuthenticationService.LOG.atDebug().log("support : {}={}", pAuthentication, resu);
		return resu;
	}

	/**
	 * Authenticates a user (for internal use).
	 *
	 * @param pEmail    an email
	 * @param pPassword a password
	 * @return the user found, throws an exception if an error occurred
	 *
	 * @throws BadCredentialsException   if parameter is invalid
	 * @throws DisabledException         if user status is not enabled
	 * @throws UsernameNotFoundException if authentication is wrong
	 */
	protected UserEntity authenticate(String pEmail, String pPassword) {
		AuthenticationService.LOG.atDebug().log("authentifier - {}, XXX", pEmail);

		var result = this.userDao.findOneByEmail(pEmail);
		if (result.isPresent()) {
			var user = result.get();
			if (!user.isEnabled()) {
				AuthenticationService.LOG.atWarn().log("authentifier - {}, Status {}", pEmail, user.getStatus());
				throw new DisabledException(
						"Erreur d'authentification, l'utilisateur est dans l'état [" + user.getStatus() + "]");
			}
			AuthenticationService.LOG.atDebug().log("authentifier - {},XXX found user with id={}", pEmail,
					user.getId());
			if (this.passwordEncoder.matches(pPassword, user.getPassword())) {
				AuthenticationService.LOG.atDebug().log("authentifier - {} password is OK", pEmail);
				return user;
			}
			AuthenticationService.LOG.atWarn().log("authentifier - {} password is KO {}", pEmail, pPassword);
			throw new BadCredentialsException("Erreur d'authentification");
		}
		AuthenticationService.LOG.atWarn().log("authentifier - User with email {} was not found", pEmail);
		throw new UsernameNotFoundException("Erreur d'authentification");
	}

	/**
	 * Sends an email to the user with its new generated password.
	 *
	 * @param pEmail an email
	 * @throws EntityNotFoundException     if user was not found
	 * @throws ParameterException          if parameter is invalid
	 * @throws InconsistentStatusException if user status is not enabled
	 * @throws SendMailException           if mail was not sent
	 */
	@Override
	public void forgotPassword(String pEmail)
			throws EntityNotFoundException, SendMailException, InconsistentStatusException {
		AuthenticationService.LOG.atDebug().log("forgotPassword - {}", pEmail);

		ValidationUtils.isNotEmpty(pEmail, "Email cannot be null or empty");

		var result = this.userDao.findOneByEmail(pEmail);
		if (result.isPresent()) {
			var user = result.get();
			if (user.isEnabled()) {
				AuthenticationService.LOG.atDebug().log("forgotPassword - found user with id {}", user.getId());
				var newPwd = AuthenticationService.generateCommonLangPassword();
				user.setPassword(this.passwordEncoder.encode(newPwd));
				// We update the user in DB
				this.userDao.save(user);
				this.mailBody = MessageFormat.format(this.mailBody, newPwd);
				this.emailService.sendSimpleMessage(user.getEmail(), this.mailSubject, this.mailBody);
				return;
			}
			AuthenticationService.LOG.atWarn().log("forgotPassword - {}, Status {}", pEmail, user.getStatus());
			throw new InconsistentStatusException(
					"Erreur d'authentification, l'utilisateur est dans l'état [" + user.getStatus() + "]");
		}
		AuthenticationService.LOG.atWarn().log("forgotPassword - No user found with email={}", pEmail);
		throw new EntityNotFoundException("Utilisateur introuvable", pEmail);
	}

	/**
	 * Generates a pwd.
	 *
	 * @return the new password (not in bcrypt)
	 * @see <a href="https://www.baeldung.com/java-generate-secure-password">the
	 *      tuto</a>
	 */
	@SuppressWarnings("java:S2245") // For Sonar Qube
	private static String generateCommonLangPassword() {
		var upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
		var lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
		var numbers = RandomStringUtils.randomNumeric(2);
		var specialChar = RandomStringUtils.random(2, 33, 47, false, false);
		var totalChars = RandomStringUtils.randomAlphanumeric(2);
		var combinedChars = upperCaseLetters.concat(lowerCaseLetters).concat(numbers).concat(specialChar)
				.concat(totalChars);
		// DO NOT call toList() instead of collect(Collectors.toList()), since it
		// returns an immutable list
		List<Character> pwdChars = combinedChars.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
		Collections.shuffle(pwdChars);
		return pwdChars.stream().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
	}
}
