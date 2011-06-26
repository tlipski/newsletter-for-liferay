package pl.net.bluesoft.rnd.newsletter.gui;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.newsletter.model.HibernateUtil;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterCategory;

import java.util.Arrays;
import java.util.List;

import static pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil.*;
import static pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil.simpleScaffoldForJPA;

/**
 * Newsletter administrators pane, allows administrator to manage newsletter categories.
 * This pane is available inside Liferay's Control Panel.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class NewsletterAdminPane extends VerticalLayout {

    private VerticalLayout categoriesList = new VerticalLayout();

    public NewsletterAdminPane() {
        initUI();
        loadData();
    }

    private void initUI() {
         setSpacing(true);
         categoriesList.setSpacing(true);

         addComponent(categoriesList);
         addComponent(button("newsletter.admin.add", new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent clickEvent) {
                 NewsletterCategory cat = new NewsletterCategory();
                 cat.setDescription("");
                 cat.setNewsletterInfoFrom("");
                 cat.setNewsletterInfoMessage("");
                 displayEditWindow(cat);
             }
         }));
     }

    private void loadData() {
        categoriesList.removeAllComponents();
        for (final NewsletterCategory cat : (List<NewsletterCategory>)HibernateUtil.getSession().createCriteria(NewsletterCategory.class).list()) {
            categoriesList.addComponent(hl(
                    simpleLabel(cat.getDescription(), "120px"),
                    simpleLabel(cat.getNewsletterInfoFrom(), "150px"),
                    simpleLabel(cat.getNewsletterInfoMessage(), "180px"),
                    button("newsletter.admin.edit.change", new Button.ClickListener() {
                        public void buttonClick(Button.ClickEvent event) {
                            displayEditWindow((NewsletterCategory) HibernateUtil.getSession().get(NewsletterCategory.class, cat.getId()));
                        }
                    }),
                    button("newsletter.admin.edit.remove", new Button.ClickListener() {
                        public void buttonClick(Button.ClickEvent event) {
                            HibernateUtil.getSession().delete(HibernateUtil.getSession().get(NewsletterCategory.class, cat.getId()));
                            loadData();
                        }
                    })
            ));
        }
    }


    private void displayEditWindow(final NewsletterCategory cat) {
        final Window w = new Window(getValue("newsletter.admin.edit.caption"));

        final Form form = simpleScaffoldForJPA(cat, "newsletter.admin.edit.form",
                Arrays.asList("description", "newsletterInfoMessage", "newsletterInfoFrom"));
        VerticalLayout vl = vl(form,
                hl("",
                        button("newsletter.admin.edit.ok", new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                if (form.isValid()) {
                                    form.commit();
                                    HibernateUtil.getSession().saveOrUpdate(cat);
                                    getApplication().getMainWindow().removeWindow(w);
                                    loadData();
                                } else {
                                    getWindow().showNotification("newsletter.admin.edit.validation-failed",
                                            Window.Notification.TYPE_WARNING_MESSAGE);
                                }
                            }
                        }),
                        button("newsletter.admin.edit.cancel", new Button.ClickListener() {
                            @Override
                            public void buttonClick(Button.ClickEvent event) {
                                getApplication().getMainWindow().removeWindow(w);
                            }
                        })
                ));
        vl.setMargin(true);
        w.setContent(vl);
        w.setWidth("600px");
        w.center();
        getApplication().getMainWindow().addWindow(w);
    }
}
