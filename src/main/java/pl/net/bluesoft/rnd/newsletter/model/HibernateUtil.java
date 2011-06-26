package pl.net.bluesoft.rnd.newsletter.model;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;

/**
 * Simple Hibernate utility class.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    static {
        AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration();
        org.hibernate.cfg.Configuration cfg = annotationConfiguration.configure();
        sessionFactory = cfg.buildSessionFactory();
    }

    public static Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
