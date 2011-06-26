package pl.net.bluesoft.rnd.newsletter.portlets;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil;
import pl.net.bluesoft.rnd.newsletter.model.HibernateUtil;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Additional Portlet logic, supporting per-thread locale setting and simplistic Hibernate Session management.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class VaadinApplicationPortlet2 extends ApplicationPortlet2 {

    protected void handleRequest(final PortletRequest request, final PortletResponse response) throws PortletException, IOException {
        Session session = HibernateUtil.getSession();
        try {
            Transaction tx = session.beginTransaction();
            Locale locale = request.getLocale();
            VaadinUtil.setThreadLocale(locale);
            try {
                VaadinApplicationPortlet2.super.handleRequest(request, response);
            } finally {
                VaadinUtil.unsetThreadLocale();
            }
            tx.commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }
}
