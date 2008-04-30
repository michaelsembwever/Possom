package no.sesat.search.sitemap;

import java.net.URI;
import java.util.Date;

/**
 * This class is a representation of a single entry in a google sitemaps file.
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 */
public final class Page {

    public enum Frequency {
        ALWAYS,
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY,
        NEVER
    }

    private final URI location;
    private final Date lastModified;
    private final Frequency frequency;
    private final double priority;

    /**
     * Creates a new page of a site map.
     *
     * @param location the URL of the page.
     */
    public Page(final URI location) {
        this.location = location;

        this.lastModified = null;
        this.frequency = Frequency.WEEKLY;
        this.priority = 0.5;
    }

    /**
     * Creates a new page of a site map.
     *
     * @param location the URL of the page.
     * @param lastModified the date of last modification.
     * @param frequency the frequency at which the page is updated.
     * @param priority the priority of the page.
     */
    public Page(final URI location, final Date lastModified, final Frequency frequency, final double priority) {
        this.location = location;
        this.lastModified = lastModified;
        this.priority = priority;
        this.frequency = frequency;
    }

    /**
     * The URL of the page. Never null.
     *
     * @return the URL.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * The date the page was last modified. Optional and might be null.
     *
     * @return The last modification date.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * The frequence at which the page is usually updated.
     *
     * @return the update frequency.
     */
    public Frequency getFrequency() {
        return frequency;
    }

    /**
     * The priority of the page.
     *
     * @return The priority of the page.
     */
    public double getPriority() {
        return priority;
    }
}
