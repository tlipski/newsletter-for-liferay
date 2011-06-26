package pl.net.bluesoft.rnd.newsletter.model;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.classic.Session;


/**
 * Created by IntelliJ IDEA.
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

    public static Session openSession() {
        return null;//TODO DELETE this method sessionFactory.openSession();
    }

    public static Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
