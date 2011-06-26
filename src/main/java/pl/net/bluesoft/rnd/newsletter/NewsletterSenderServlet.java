package pl.net.bluesoft.rnd.newsletter;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil;
import pl.net.bluesoft.rnd.newsletter.mailing.MailingHelper;
import pl.net.bluesoft.rnd.newsletter.model.HibernateUtil;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterJob;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterJobRecipient;
import pl.net.bluesoft.rnd.newsletter.portlets.VaadinApplicationPortlet2;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple single-threaded newsletter servlet-based sending mechanism.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class NewsletterSenderServlet extends HttpServlet implements Runnable {

    private Logger logger = Logger.getLogger(NewsletterSenderServlet.class.getName());

    private Thread t;
    private MailingHelper mailingHelper = new MailingHelper();
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        t = new Thread(this);
        t.start();
        logger.fine("NewsletterSenderServlet succesfully initialized");
    }

    @Override
    public void destroy() {
        super.destroy();
        t = null;
    }

    public void run() {
        logger.fine("NewsletterSenderServlet thread started");
        while (t != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.throwing("run()", e.getMessage(), e);
            }
            //look for a new job to process
            List<NewsletterJobRecipient> recipientList = new ArrayList<NewsletterJobRecipient>();
            Session session = HibernateUtil.getSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                List<NewsletterJob> jobs = session.createCriteria(NewsletterJob.class)
                        .add(Restrictions.eq("state", NewsletterJob.STATE_NEW))
                        .setFetchSize(1) //one at a time, but in theory it would be possible to process more
                        .list();

                if (jobs.isEmpty()) continue;
                NewsletterJob job = jobs.get(0); //job state is a possible redundancy, but makes the code simplier
                logger.info("Processing job #" + job.getId());
                job.setState(NewsletterJob.STATE_PROCESSING);
                recipientList.addAll(job.getRecipients());
                tx.commit();

                logger.info("Aquired mail session");
                for (NewsletterJobRecipient rcpt :recipientList) {
                    if (!rcpt.getStatus().equals(NewsletterJobRecipient.STATUS_NEW)) continue; //ignore other statuses
                    session = HibernateUtil.getSession();
                    tx = session.beginTransaction();
                    session.refresh(rcpt);
                    rcpt.setStatus(NewsletterJobRecipient.STATUS_PROCESSING);
                    tx.commit();

                    try {
                        session = HibernateUtil.getSession();
                        tx = session.beginTransaction();
                        logger.info("Sending email to: " + rcpt.getRecipient() + " for job #" + job.getId());
                        mailingHelper.sendEmail(rcpt.getRecipient(),
                                job.getMessageSender(),
                                job.getMessageTitle(),
                                job.getMessageBody(),
                                true);
                        session.refresh(rcpt);
                        rcpt.setStatus(NewsletterJobRecipient.STATUS_SENT);
                        tx.commit();
                        logger.info("Sent email to: " + rcpt.getRecipient() + " for job #" + job.getId());

                    }
                    catch (Exception e) {
                        logger.log(Level.SEVERE,
                                "Error sending email to: " + rcpt.getRecipient() + " for job #" + job.getId(),
                                e);
                        if (tx != null && tx.isActive()) tx.rollback();
                        session = HibernateUtil.getSession();
                        tx = session.beginTransaction();
                        session.refresh(rcpt);

                        rcpt.setStatus(NewsletterJobRecipient.STATUS_ERROR);
                        rcpt.setErrorMessage(e.getMessage());
                        tx.commit();

                    }
                }
                session = HibernateUtil.getSession();
                tx = session.beginTransaction();
                logger.info("Finished job #" + job.getId());
                session.refresh(job);
                job.setState(NewsletterJob.STATE_FINISHED);
                tx.commit();

            }
            catch (Exception e) {
	            logger.log(Level.SEVERE, e.getMessage(), e);
                if (tx != null && tx.isActive()) tx.rollback();
            }

        }
        logger.fine("NewsletterSenderServlet thread terminated");

    }
}
