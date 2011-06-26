package pl.net.bluesoft.rnd.newsletter.model;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
public class NewsletterJobRecipient {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
	@JoinColumn(name="job_id")
    private NewsletterJob job;

    private String status; //"N" - new ,"P" - processing, "S" - sent, "E" - error
    private String recipient; //recipient email

    @Lob
    private String errorMessage;
    public static final String STATUS_NEW = "N";
    public static final String STATUS_PROCESSING = "P";
    public static final String STATUS_SENT = "S";
    public static final String STATUS_ERROR = "E";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public NewsletterJob getJob() {
        return job;
    }

    public void setJob(NewsletterJob job) {
        this.job = job;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
