package pl.net.bluesoft.rnd.newsletter.portlets;

import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import pl.net.bluesoft.rnd.newsletter.gui.NewsletterSubscriptionEditPane;
import pl.net.bluesoft.rnd.newsletter.gui.NewsletterSubscriptionPane;
import pl.net.bluesoft.rnd.newsletter.model.HibernateUtil;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterCategory;

import javax.portlet.*;
import java.io.IOException;

/**
 * Newsletter application portlet, separating Vaadin classes from Liferay context and supporting two modes of
 * operation: view (available to all users) and edit (available to portlet owner or an administrator).
 *
 * @author tlipski@bluesoft.net.pl
 */
public class NewsletterPortletApplication extends Application implements PortletApplicationContext2.PortletListener {
	private static final String NEWSLETTER_CATEGORY_ID = "newsletter.category-id";
	private NewsletterSubscriptionPane subscriptionPane;
	private PortletMode previousMode = null;
	private PortletPreferences preferences = null;

	@Override
	public void init() {

		final Window mainWindow = new Window();
		setMainWindow(mainWindow);

		ApplicationContext applicationContext = getContext();
		if (applicationContext instanceof PortletApplicationContext2) {
			PortletApplicationContext2 portletCtx = (PortletApplicationContext2) applicationContext;
			portletCtx.addPortletListener(this, this);
		} else {
			mainWindow.addComponent(new Label("Please use this application from a Portlet"));
			return;
		}
	}

	@Override
	public void handleRenderRequest(RenderRequest renderRequest, RenderResponse renderResponse, Window window) {
		try {
			this.preferences = renderRequest.getPreferences();
			PortletMode portletMode = renderRequest.getPortletMode();
			if (!portletMode.equals(previousMode)) {
				getMainWindow().setContent(new VerticalLayout());
				if (portletMode.equals(PortletMode.VIEW)) {
					String val = preferences.getValue(NEWSLETTER_CATEGORY_ID, null);
					if (val == null) {
						getMainWindow().addComponent(new Label("Please configure this portlet."));
						return;
					}
					long id = Long.parseLong(val);
					NewsletterCategory newsletterCategory = (NewsletterCategory) HibernateUtil.getSession().get(NewsletterCategory.class, id);
					subscriptionPane = new NewsletterSubscriptionPane(newsletterCategory);
					getMainWindow().addComponent(subscriptionPane);
				} else if (portletMode.equals(PortletMode.EDIT)) {
					NewsletterCategory newsletterCategory = null;
					String val = preferences.getValue(NEWSLETTER_CATEGORY_ID, null);
					if (val != null) {
						long id = Long.parseLong(val);
						newsletterCategory = (NewsletterCategory) HibernateUtil.getSession().get(NewsletterCategory.class, id);
					}

					NewsletterSubscriptionEditPane editPane = new NewsletterSubscriptionEditPane(newsletterCategory, new NewsletterSubscriptionEditPane.SaveCallback() {

						@Override
						public void onSave(NewsletterCategory newCategory) {
							try {
								if (newCategory == null)
									preferences.reset(NEWSLETTER_CATEGORY_ID);
								else
									preferences.setValue(NEWSLETTER_CATEGORY_ID, String.valueOf(newCategory.getId()));
								preferences.store();
							} catch (ReadOnlyException e) {
								throw new RuntimeException(e);
							} catch (ValidatorException e) {
								throw new RuntimeException(e);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					});
					getMainWindow().addComponent(editPane);

				}
			}
			previousMode = portletMode;
			if (subscriptionPane != null)
				subscriptionPane.setUrl(PortalUtil.getPortalURL(renderRequest) + PortalUtil.getCurrentURL(renderRequest));
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void handleActionRequest(ActionRequest actionRequest, ActionResponse actionResponse, Window window) {
		//nothing

	}

	@Override
	public void handleEventRequest(EventRequest eventRequest, EventResponse eventResponse, Window window) {
		//nothing

	}

	@Override
	public void handleResourceRequest(ResourceRequest resourceRequest, ResourceResponse resourceResponse, Window window) {
		//nothing
	}
}
