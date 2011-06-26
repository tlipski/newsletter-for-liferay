package pl.net.bluesoft.rnd.newsletter.gui;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.hibernate.criterion.Order;
import pl.net.bluesoft.rnd.newsletter.model.*;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import static pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil.*;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * Newsletter sending pane - used newsletter content manager to send newsletter emails.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class NewsletterSendingPane extends VerticalLayout {

	private ComboBox categoriesBox;
	private TextField titleField = new TextField();
	private RichTextArea bodyArea = new RichTextArea();

	public NewsletterSendingPane() {
		initUI();
		loadData();
	}

	private void initUI() {

		setSpacing(true);
		categoriesBox = new ComboBox();
		categoriesBox.setItemCaptionMode(ComboBox.ITEM_CAPTION_MODE_PROPERTY);
		categoriesBox.setItemCaptionPropertyId("description");
		categoriesBox.setImmediate(true);
		categoriesBox.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		categoriesBox.setNewItemsAllowed(false);

		categoriesBox.setWidth("300px");

		titleField.setInputPrompt(getValue("newsletter.sending.title.prompt"));
		titleField.setWidth("600px");

		bodyArea.setHeight("400px");
		bodyArea.setWidth("600px");
		bodyArea.setImmediate(true);

		addComponent(htmlLabel("newsletter.sending.info"));
		addComponent(hl("",
		                categoriesBox, button("newsletter.sending.refresh", new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				loadData();
			}
		})));
		addComponent(titleField);
		addComponent(bodyArea);
		addComponent(button("newsletter.sending.send", new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {

				final String title = String.valueOf(titleField.getValue()).trim();
				final String body = String.valueOf(bodyArea.getValue()).trim();
				final NewsletterCategory category = (NewsletterCategory) categoriesBox.getValue();
				if (!hasText(title) || !hasText(body.replaceAll("<[^>]+>","")) || category == null) {
					getApplication().getMainWindow().showNotification(
							getValue("newsletter.sending.validation-failed"),
							Window.Notification.TYPE_WARNING_MESSAGE);
					return;
				}
				final Window w = new Window(getValue("newsletter.sending.confirm.title"));
				VerticalLayout vl = vl(
						htmlLabel("newsletter.sending.confirm.info"),
						new Label(title),
						new Label(body, Label.CONTENT_XHTML),
						hl("",
						   button("newsletter.sending.confirm.ok", new Button.ClickListener() {
							@Override
							public void buttonClick(Button.ClickEvent clickEvent) {
								sendNewsletter(title, body, category);
								getApplication().getMainWindow().removeWindow(w);
								displayNotification(getWindow(), "newsletter.sending.confirm.started");
							}
						}),
						   button("newsletter.sending.confirm.cancel", new Button.ClickListener() {
							   @Override
							   public void buttonClick(Button.ClickEvent clickEvent) {
								   getApplication().getMainWindow().removeWindow(w);
							   }
						   })));
				vl.setMargin(true);
				w.setContent(vl);
				w.center();
				w.setModal(true);
				w.setWidth("600px");
				getApplication().getMainWindow().addWindow(w);
			}
		}));


	}

	private void loadData() {
		List<NewsletterCategory> categories = HibernateUtil.getSession()
				.createCriteria(NewsletterCategory.class)
				.addOrder(Order.asc("description")).list();
		BeanItemContainer bic = new BeanItemContainer(categories);
		categoriesBox.setContainerDataSource(bic);
	}

	private void sendNewsletter(String title, String body, NewsletterCategory category) {

        category = (NewsletterCategory) HibernateUtil.getSession().get(NewsletterCategory.class, category.getId());
        NewsletterJob newsletterJob = new NewsletterJob();
        newsletterJob.setCreationTime(Calendar.getInstance());
        newsletterJob.setMessageBody(body);
        newsletterJob.setMessageSender(category.getNewsletterInfoFrom());
        newsletterJob.setMessageTitle(title);
        newsletterJob.setState(NewsletterJob.STATE_NEW);
        newsletterJob.setRecipients(new HashSet<NewsletterJobRecipient>());
        for (NewsletterSubscription subscription : category.getSubscriptions()) {
            NewsletterJobRecipient rcpt = new NewsletterJobRecipient();
            rcpt.setJob(newsletterJob);
            rcpt.setRecipient(subscription.getEmail());
            rcpt.setStatus(NewsletterJobRecipient.STATUS_NEW);
            newsletterJob.getRecipients().add(rcpt);
        }
        HibernateUtil.getSession().saveOrUpdate(newsletterJob);
	}
}
