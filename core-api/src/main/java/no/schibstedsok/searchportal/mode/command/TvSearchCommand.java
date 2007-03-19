/*
 * Copyright (2005-2007) Schibsted Søk AS
 *
 */
package no.schibstedsok.searchportal.mode.command;

import no.schibstedsok.searchportal.mode.config.TvSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.SearchResult;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:anders.johan.jamtli@sesam.no">Anders Johan Jamtli</a>
 * @version <tt>$Revision: 2567 $</tt>
 */
public class TvSearchCommand extends AbstractSimpleFastSearchCommand {

    private static final Logger LOG = Logger.getLogger(TvSearchCommand.class);

    protected final StringBuilder defaultChannelFilter = new StringBuilder();
    private String additionalFilter;

    /** Creates a new instance of TvSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public TvSearchCommand(final Context cxt) {

        super(cxt);
        LOG.debug("Creating TvSearchCommand");

//            defaultChannelFilter = new StringBuilder(super.getAdditionalFilter());

        defaultChannelFilter.append("+(");
        for (String channel : getTvSearchConfiguration().getDefaultChannels()) {
            defaultChannelFilter.append(" sgeneric5nav:");
            defaultChannelFilter.append(channel);
        }
        defaultChannelFilter.append(')');
        additionalFilter = "";

    }

    protected int getResultsToReturn() {
        final String sortByString = this.getParameters().get("userSortBy") != null ? (String) this.getParameters().get("userSortBy") : "channel";
        if ("day".equals(sortByString)) {
            return 15;
        }
        if (getRunningQuery().getQuery().isBlank()) {
            return getTvSearchConfiguration().getResultsToFetch();
        } else {
            return getTvSearchConfiguration().getResultsToReturn();
        }
    }

    public SearchResult execute() {
        final String sortByString = this.getParameters().get("userSortBy") != null ? (String) this.getParameters().get("userSortBy") : "channel";
        SearchResult sr = null;

        if ("day".equals(sortByString)) {
            sr = new BasicSearchResult(this);
            int totalHits = 0;
            for (int i = 0; i < 7; i++) {
                setAdditionalFilter(" +sgeneric7nav:" + i);
                SearchResult result = super.execute();
                sr.getResults().addAll(result.getResults());
                totalHits += result.getHitCount();
            }
            sr.setHitCount(totalHits);
        } else {
            sr = super.execute();
        }

        return sr;
    }

    private TvSearchConfiguration getTvSearchConfiguration() {
        return (TvSearchConfiguration) getSearchConfiguration();
    }

    protected String getAdditionalFilter() {
        /* Only fetch default channels on blank query */
        if (getRunningQuery().getQuery().isBlank() && !getParameters().containsKey("nav_channels") && !getParameters().containsKey("nav_categories")) {
            return additionalFilter + " " + defaultChannelFilter.toString();
        }

        if (getRunningQuery().getQuery().isBlank() && getParameters().containsKey("output") && getParameters().get("output").equals("rss")) {
            return additionalFilter + " " + defaultChannelFilter.toString();
        }

        return additionalFilter;
    }

    protected void setAdditionalFilter(String additionalFilter) {
        this.additionalFilter = additionalFilter;
    }
}
