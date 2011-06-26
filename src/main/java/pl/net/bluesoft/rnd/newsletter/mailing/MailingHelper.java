package pl.net.bluesoft.rnd.newsletter.mailing;

import com.liferay.mail.service.MailService;
import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.util.InfrastructureUtil;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class MailingHelper {

    public void sendEmail(String rcpt, String from, String subject, String body, boolean sendHtml) {

        try {
            MailMessage mailMessage = new MailMessage();
            mailMessage.setBody(body);
            mailMessage.setHTMLFormat(sendHtml);
            mailMessage.setFrom(new InternetAddress(from));
            mailMessage.setTo(new InternetAddress(rcpt));
            mailMessage.setSubject(subject);
            MailServiceUtil.sendEmail(mailMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
