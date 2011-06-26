package pl.net.bluesoft.rnd.newsletter.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Newsletter category, which can have subscribers and can be used to send a newsletter to them.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
public class NewsletterCategory {
    @Id
    @GeneratedValue
    private long id;

    private String description;
    @Lob
    private String newsletterInfoMessage;
	private String newsletterInfoFrom;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name="category_id")
    private Set<NewsletterSubscription> subscriptions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNewsletterInfoMessage() {
        return newsletterInfoMessage;
    }

    public void setNewsletterInfoMessage(String newsletterInfoMessage) {
        this.newsletterInfoMessage = newsletterInfoMessage;
    }

	public String getNewsletterInfoFrom() {
		return newsletterInfoFrom;
	}

	public void setNewsletterInfoFrom(String newsletterInfoFrom) {
		this.newsletterInfoFrom = newsletterInfoFrom;
	}

    public Set<NewsletterSubscription> getSubscriptions() {
        if (subscriptions == null) subscriptions = new HashSet<NewsletterSubscription>();
        return subscriptions;
    }

    public void setSubscriptions(Set<NewsletterSubscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
