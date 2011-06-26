package pl.net.bluesoft.rnd.newsletter.gui;

import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;

import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.*;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Simple vaadin helper methods/functions, making the overall Vaadin code shorter and simpler.
 * Supports locale as selected by user, but a change of of Locale in Liferay will require user to restart Vaadin
 * application.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class VaadinUtil {

    private static Map<Thread, Locale> LOCALE_THREAD_MAP = new HashMap<Thread, Locale>();

    public static synchronized void setThreadLocale(Locale l) {
        LOCALE_THREAD_MAP.put(Thread.currentThread(), l);
    }

    public static synchronized void unsetThreadLocale() {
        LOCALE_THREAD_MAP.remove(Thread.currentThread());
    }


    public static String getValue(String key) {
        return nvl(ResourceBundle.getBundle("newsletter-messages",
                nvl(LOCALE_THREAD_MAP.get(Thread.currentThread()), Locale.getDefault())).getString(key), key);
    }

    public static Label label(String key) {
        return new Label(getValue(key));
    }

    public static Label simpleLabel(String text, String width) {
        Label l = new Label(text);
        l.setWidth(width);
        return l;
    }

    public static Label htmlLabel(String key) {
        return new Label(getValue(key), Label.CONTENT_XHTML);
    }

    public static Button button(String key) {
        return new Button(getValue(key));
    }

    public static Button button(String key, Button.ClickListener clickListener) {
        Button b = button(key);
        b.addListener(clickListener);
        return b;
    }

    public static void displayNotification(Window w, String titleKey) {
        displayNotification(w, titleKey, null);
    }

    public static void displayNotification(Window w, String titleKey, String details) {
        Window.Notification n;
        if (details != null)
            n = new Window.Notification(getValue(titleKey), getValue(details));
        else
            n = new Window.Notification(getValue(titleKey));
        n.setDelayMsec(-1);
        w.showNotification(n);
    }

    public static HorizontalLayout hl(Component... components) {
        return hl("100%", components);
    }

    public static HorizontalLayout hl(String width, Component... components) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setWidth(width);
        for (Component c : components) hl.addComponent(c);
        return hl;
    }

    public static VerticalLayout vl(Component... components) {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setWidth("100%");
        for (Component c : components) vl.addComponent(c);
        return vl;
    }

    public static Form simpleScaffoldForJPA(Object value, String i18nPrefix, Collection<String> requiredFieldNames) {
        Form form = new Form();
        form.setWidth("100%");
        form.setWriteThrough(false);
        form.setInvalidCommitted(false);
        form.setDescription(getValue(i18nPrefix + ".form-caption"));
        BeanItem bi = new BeanItem(value);
        form.setItemDataSource(bi);
        Class cls = value.getClass();
        form.setVisibleItemProperties(new String[]{});
        for (java.lang.reflect.Field f : cls.getDeclaredFields()) {
            if (f.getAnnotation(Id.class) != null) { //hide JPA id field
                continue;
            }
            if (f.getType().equals(String.class)) { //at this moment, we support only string object
                com.vaadin.ui.Field field;
                String fieldName = f.getName();
                field = f.getAnnotation(Lob.class) != null ? new TextArea() : new TextField();
                if (requiredFieldNames.contains(fieldName)) {
                    field.setRequired(true);
                    field.setRequiredError(getValue(i18nPrefix + "." + fieldName + ".required-error"));
                }
                field.setCaption(getValue(i18nPrefix + "." + fieldName + ".caption"));
                field.setWidth("100%");
                field.setPropertyDataSource(bi.getItemProperty(fieldName));
                form.addField(fieldName, field);
            }
        }
        return form;

    }


}
