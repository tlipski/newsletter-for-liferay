package pl.net.bluesoft.rnd.newsletter.model;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
public class NewsletterSubscription {
    @Id
    @GeneratedValue
    private long id;

    private String email;
    private String confirmationKey;
    private String cancellationKey;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="category_id")
    private NewsletterCategory category;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public NewsletterCategory getCategory() {
        return category;
    }

    public void setCategory(NewsletterCategory category) {
        this.category = category;
    }

    public String getConfirmationKey() {
        return confirmationKey;
    }

    public void setConfirmationKey(String confirmationKey) {
        this.confirmationKey = confirmationKey;
    }

    public String getCancellationKey() {
        return cancellationKey;
    }

    public void setCancellationKey(String cancellationKey) {
        this.cancellationKey = cancellationKey;
    }
}
