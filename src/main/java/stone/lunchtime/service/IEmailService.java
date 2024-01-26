package stone.lunchtime.service;

import org.springframework.stereotype.Service;

import stone.lunchtime.service.exception.SendMailException;

/**
 * Send Email service.
 */
@Service
public interface IEmailService {

	/**
	 * Sends an email.
	 *
	 * @param pTo      to address
	 * @param pSubject subject of the mail
	 * @param pText    body of the email
	 * @throws SendMailException if mail was not sent
	 */
	void sendSimpleMessage(String pTo, String pSubject, String pText) throws SendMailException;

	/**
	 * Activates send mail.
	 */
	void activateSendMail();

	/**
	 * Deactivate send mail.
	 */
	void deactivateSendMail();

	/**
	 * Indicates status for send mail.
	 *
	 * @return true if mail can be sent, false if not
	 */
	boolean getSendMail();

}