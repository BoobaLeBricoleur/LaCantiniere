// -#--------------------------------------
// -# ©Copyright Ferret Renaud 2019       -
// -# Email: admin@ferretrenaud.fr        -
// -# All Rights Reserved.                -
// -#--------------------------------------

package stone.lunchtime.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import stone.lunchtime.service.IEmailService;
import stone.lunchtime.service.exception.SendMailException;

/**
 * Email service.
 */
@Service
public class EmailService implements IEmailService {
	private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender emailSender;

	/** Option used to disable send mail */
	@Value("${configuration.allow.sendmail}")
	private boolean allowSendmail;

	/**
	 * Constructor of the object.
	 *
	 * @param pEmailSender the email sender
	 */
	@Autowired
	public EmailService(JavaMailSender pEmailSender) {
		this.emailSender = pEmailSender;
	}

	/**
	 * Sends an email.
	 *
	 * @param pTo      to address
	 * @param pSubject subject of the mail
	 * @param pText    body of the email
	 * @throws SendMailException if mail was not sent
	 */
	@Override
	public void sendSimpleMessage(String pTo, String pSubject, String pText) throws SendMailException {
		EmailService.LOG.atDebug().log("sendSimpleMessage - {} {} {}", pTo, pSubject, pText);
		if (this.allowSendmail) {
			try {
				var message = new SimpleMailMessage();
				message.setTo(pTo);
				message.setSubject(pSubject);
				message.setText(pText);
				this.emailSender.send(message);
				EmailService.LOG.atDebug().log("sendSimpleMessage - Message sent to {}", pTo);
			} catch (Exception lExp) {
				throw new SendMailException("Erreur lors de l'envoie de l'email", lExp);
			}
		} else {
			EmailService.LOG.atError().log("sendSimpleMessage - OK BUT send mail is deactivated (see configuration)");
		}
	}

	/**
	 * Activates send mail.
	 */
	@Override
	public void activateSendMail() {
		this.allowSendmail = true;
	}

	/**
	 * Deactivate send mail.
	 */
	@Override
	public void deactivateSendMail() {
		this.allowSendmail = false;
	}

	/**
	 * Indicates status for send mail.
	 *
	 * @return true if mail can be sent, false if not
	 */
	@Override
	public boolean getSendMail() {
		return this.allowSendmail;
	}
}
