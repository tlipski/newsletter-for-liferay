package pl.net.bluesoft.rnd.newsletter.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Newsletter-sending job, collecting all of the subscribers.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
public class NewsletterJob {
    @Id
    @GeneratedValue
    private long id;

    public static final String STATE_NEW = "N";
    public static final String STATE_PROCESSING = "P";
    public static final String STATE_FINISHED = "F";
    private String state;

    private String messageTitle;
    private String messageSender;

	private Calendar creationTime;

    @Lob
    private String messageBody;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name="job_id")
    private Set<NewsletterJobRecipient> recipients;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public Set<NewsletterJobRecipient> getRecipients() {
        if (recipients == null) recipients = new HashSet<NewsletterJobRecipient>();
        return recipients;
    }

    public void setRecipients(Set<NewsletterJobRecipient> recipients) {
        this.recipients = recipients;
    }

	public Calendar getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Calendar creationTime) {
		this.creationTime = creationTime;
	}
}
