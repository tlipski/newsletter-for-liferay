package pl.net.bluesoft.rnd.newsletter.gui;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.newsletter.mailing.MailingHelper;
import pl.net.bluesoft.rnd.newsletter.model.HibernateUtil;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterCategory;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterSubscription;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil.*;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * Main subscription pane, allowing user to subscribe and unsubscribe from a newsletter.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class NewsletterSubscriptionPane extends VerticalLayout {
	private NewsletterCategory newsletterCategory;

	private String url;
	private Label newsletterDescriptionLabel = htmlLabel("newsletter.subscription.info");
	private TextField emailField = new TextField();
	private Button subscribeButton = button("newsletter.subscription.subscribe");
	private Button unsubscribeButton = button("newsletter.subscription.unsubscribe");
	private Button confirmationLink = button("newsletter.subscription.code.link");

	//taken from http://www.regular-expressions.info/email.html
    private static final String EMAIL_REGEXP = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
    private MailingHelper mailingHelper = new MailingHelper();

    public NewsletterSubscriptionPane(NewsletterCategory newsletterCategory) {
		this.newsletterCategory = newsletterCategory;
		initComponents();
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private void initComponents() {
		//additional properties
		emailField.setInputPrompt(getValue("newsletter.subscription.email.prompt"));
		emailField.setWidth("100%");
		confirmationLink.setStyleName(BaseTheme.BUTTON_LINK);
		setWidth("100%");
		setSpacing(true);
		//layout components
		addComponent(newsletterDescriptionLabel);
		addComponent(emailField);
		addComponent(hl("", subscribeButton, unsubscribeButton));
		addComponent(confirmationLink);
		setComponentAlignment(confirmationLink, Alignment.BOTTOM_RIGHT);

		//add listeners
		subscribeButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				handleSubscribe();
			}
		});
		unsubscribeButton.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				handleUnsubscribe();
			}
		});
		confirmationLink.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				displayConfirmationCodeDialog();
			}
		});
	}

	private void displayConfirmationCodeDialog() {

		final Window w = new Window(getValue("newsletter.subscription.code.title"));
		final TextField tf = new TextField();
		tf.setWidth("100%");
		VerticalLayout vl = vl(
				label("newsletter.subscription.code.message"),
				tf,
				hl("",
				   button("newsletter.subscription.code.ok", new Button.ClickListener() {
					   @Override
					   public void buttonClick(Button.ClickEvent clickEvent) {
						   String key = String.valueOf(tf.getValue()).toUpperCase().trim();
						   List<NewsletterSubscription> subscriptions =
								   findSubscriptionForKey(key);
						   if (subscriptions.isEmpty()) {
							   getWindow().showNotification(getValue("newsletter.subscription.code.invalid-code"),
							                                Window.Notification.TYPE_WARNING_MESSAGE);
							   return;
						   } else {
							   for (NewsletterSubscription subscription : subscriptions) {
								   if (nvl(subscription.getConfirmationKey(), "").equals(key)) {//confirm subscription
									   subscription.setConfirmationKey(null);
									   HibernateUtil.getSession().saveOrUpdate(subscription);
									   VaadinUtil.displayNotification(getWindow(), "newsletter.subscription.code.confirmed");
								   }
								   if (nvl(subscription.getCancellationKey(), "").equals(key)) {//remove subscription
									   subscription.setCancellationKey(null);
									   HibernateUtil.getSession().delete(subscription);
									   VaadinUtil.displayNotification(getWindow(), "newsletter.subscription.code.removed");
								   }
							   }
						   }

						   getWindow().removeWindow(w);
					   }
				   }),
				   button("newsletter.subscription.code.cancel", new Button.ClickListener() {
					   @Override
					   public void buttonClick(Button.ClickEvent clickEvent) {
						   getWindow().removeWindow(w);
					   }
				   }))
		);
		vl.setMargin(true);

		w.setContent(vl);
		w.setWidth("300px");
		w.center();
		w.setModal(true);
		getWindow().addWindow(w);
	}

	 private void sendNotificationEmail(String email, String key, String titleKey, String bodyKey) {
        mailingHelper.sendEmail(email,
                newsletterCategory.getNewsletterInfoFrom(),
                expandTemplate(titleKey,
                        new String[]{"url", url},
                        new String[]{"categoryName", newsletterCategory.getDescription()}
                ),
                expandTemplate(bodyKey,
                        new String[]{"url", url},
                        new String[]{"key", key},
                        new String[]{"email", email},
                        new String[]{"categoryName", newsletterCategory.getDescription()},
                        new String[]{"message", newsletterCategory.getNewsletterInfoMessage()}
                ),
                true);
    }

	private void handleSubscribe() {
		String email = validateEmail();
		if (email == null) return;
		List<NewsletterSubscription> subscriptions = getSubscriptionsForEmail(email);
		if (!subscriptions.isEmpty()) {
			getWindow().showNotification(getValue("newsletter.subscription.validation.already-registered"), Window.Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		String key = getUniqueKey(email);


		NewsletterSubscription subscription = new NewsletterSubscription();
		subscription.setCategory((NewsletterCategory) HibernateUtil.getSession().get(NewsletterCategory.class, newsletterCategory.getId()));
		subscription.setConfirmationKey(key);
		subscription.setEmail(email);
		HibernateUtil.getSession().saveOrUpdate(subscription);

		//send a confirmation email

		sendNotificationEmail(email,
		                      key,
		                      "newsletter.subscription.confirmation-email.title",
		                      "newsletter.subscription.confirmation-email.body");


		VaadinUtil.displayNotification(getWindow(), "newsletter.subscription.confirmation-sent",
		                               "newsletter.subscription.confirmation-sent.details");

	}



	//of course, in a more sophisticated solution, freemarker would work much better
	private String expandTemplate(String templateKey, String[]... vars) {
		String tmpl = getValue(templateKey);
		for (String[] var : vars) {
			tmpl = tmpl.replace("${" + var[0] + "}", var[1]);
		}
		return tmpl;
	}

	private String getUniqueKey(String email) {
		//generate random registration key
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		String s = Hex.encodeHexString(md.digest((email + System.currentTimeMillis() + Math.random()).getBytes())).toUpperCase();
		if (!findSubscriptionForKey(s).isEmpty())
			return getUniqueKey(email);
		return s;
	}

	private List<NewsletterSubscription> findSubscriptionForKey(String s) {
		return HibernateUtil.getSession().createCriteria(NewsletterSubscription.class).add(Restrictions.or(Restrictions.eq("confirmationKey", s),
		                                                                                                   Restrictions.eq("cancellationKey", s))).list();
	}

	private List<NewsletterSubscription> getSubscriptionsForEmail(String email) {
		return HibernateUtil.getSession().createCriteria(NewsletterSubscription.class)
				.add(Restrictions.eq("email", email))
				.add(Restrictions.isNull("confirmationKey"))
				.add(Restrictions.eq("category", newsletterCategory)).list();
	}

	private String validateEmail() {
		String email = String.valueOf(emailField.getValue()).trim().toLowerCase();
		if (!hasText(email) || !email.matches(EMAIL_REGEXP)) {
			getWindow().showNotification(getValue("newsletter.subscription.validation.message"), Window.Notification.TYPE_WARNING_MESSAGE);
			return null;
		}
		return email;
	}

	private void handleUnsubscribe() {
		String email = validateEmail();
		if (email == null) return;
		List<NewsletterSubscription> subscriptions = getSubscriptionsForEmail(email);
		if (subscriptions.isEmpty()) {
			getWindow().showNotification(getValue("newsletter.subscription.validation.not-registered"), Window.Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		NewsletterSubscription subscription = subscriptions.get(0);
		String key = getUniqueKey(email);
		subscription.setCancellationKey(key);
		HibernateUtil.getSession().saveOrUpdate(subscription);

		sendNotificationEmail(email,
		                      key,
		                      "newsletter.subscription.cancellation-email.title",
		                      "newsletter.subscription.cancellation-email.body");

		VaadinUtil.displayNotification(getWindow(), "newsletter.subscription.confirmation-sent",
		                               "newsletter.subscription.confirmation-sent.details");
	}
}
