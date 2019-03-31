package argerd.ru;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID id;
    private String title;
    private Date date;
    private boolean solved;
    private boolean requiresPolice;
    private String suspect;
    private String phoneNumberOfSuspect;

    public Crime(UUID id) {
        this.id = id;
        date = new Date();
    }

    public Crime() {
        this(UUID.randomUUID());
    }

    public boolean isRequiresPolice() {
        return requiresPolice;
    }

    public void setRequiresPolice(boolean requiresPolice) {
        this.requiresPolice = requiresPolice;
    }

    public String getTitle() {
        return title;
    }

    public UUID getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getSuspect() {
        return suspect;
    }

    public void setSuspect(String suspect) {
        this.suspect = suspect;
    }

    public String getPhoneNumberOfSuspect() {
        return phoneNumberOfSuspect;
    }

    public void setPhoneNumberOfSuspect(String phoneNumberOfSuspect) {
        this.phoneNumberOfSuspect = phoneNumberOfSuspect;
    }
}
