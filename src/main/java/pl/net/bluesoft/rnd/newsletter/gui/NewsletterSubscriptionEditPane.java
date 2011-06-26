package pl.net.bluesoft.rnd.newsletter.gui;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import org.hibernate.criterion.Order;
import pl.net.bluesoft.rnd.newsletter.model.HibernateUtil;
import pl.net.bluesoft.rnd.newsletter.model.NewsletterCategory;

import java.util.List;

import static pl.net.bluesoft.rnd.newsletter.gui.VaadinUtil.*;


/**
 * @author tlipski@bluesoft.net.pl
 */
public class NewsletterSubscriptionEditPane extends VerticalLayout {

	private NewsletterCategory selectedCategory;
	private SaveCallback callback;
	private ComboBox categoriesBox;

	public NewsletterSubscriptionEditPane(NewsletterCategory selectedCategory, SaveCallback callback) {
		this.selectedCategory = selectedCategory;
		this.callback = callback;
		initUI();
		loadData();
	}

	private void initUI() {
		setSpacing(true);
		addComponent(htmlLabel("newsletter.subscription.edit.info"));
		categoriesBox = new ComboBox();
		categoriesBox.setItemCaptionMode(ComboBox.ITEM_CAPTION_MODE_PROPERTY);
		categoriesBox.setItemCaptionPropertyId("description");
		categoriesBox.setImmediate(true);
		categoriesBox.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
		categoriesBox.setNewItemsAllowed(false);
		categoriesBox.setWidth("100%");
		categoriesBox.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
				if (categoriesBox.getValue() != null)
					selectedCategory = (NewsletterCategory) categoriesBox.getValue();
			}
		});
		addComponent(hl(categoriesBox, button("newsletter.subscription.edit.refresh", new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				selectedCategory = (NewsletterCategory) categoriesBox.getValue();
				loadData();
			}
		})));
		addComponent(button("newsletter.subscription.edit.save", new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				callback.onSave((NewsletterCategory) categoriesBox.getValue());
				VaadinUtil.displayNotification(getWindow(), "newsletter.subscription.edit.save-succesfull");
			}
		}));


	}

	private void loadData() {
		List<NewsletterCategory> categories = HibernateUtil.getSession()
				.createCriteria(NewsletterCategory.class)
				.addOrder(Order.asc("description")).list();
		BeanItemContainer bic = new BeanItemContainer(categories);
		categoriesBox.setContainerDataSource(bic);
		for (Object o :bic.getItemIds()) {
			NewsletterCategory cat = (NewsletterCategory) o;
			if (selectedCategory != null && cat.getId() == selectedCategory.getId()) {
				categoriesBox.setValue(o);
			}
		}

	}

	public static interface SaveCallback {
		void onSave(NewsletterCategory newCategory);
	}
}
