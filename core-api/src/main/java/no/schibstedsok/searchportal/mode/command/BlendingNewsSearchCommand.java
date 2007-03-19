// Copyright (2007) Schibsted Søk AS
/*
 * BlendingNewsSearchCommand.java
 *
 * Created on May 12, 2006, 2:19 PM
 *
 */

package no.schibstedsok.searchportal.mode.command;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.BlendingNewsSearchConfiguration;
import no.schibstedsok.searchportal.result.BasicSearchResult;
import no.schibstedsok.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.searchportal.result.SearchResult;

/**
 * Temporary search command while we wait for neo-collapsing functionality
 * to be available in fast. This command is used to get the latest news article
 * from a number of selected sources. A separate search is done for each source.
 *
 * @author maek
 */
public class BlendingNewsSearchCommand extends NewsSearchCommand {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final BlendingNewsSearchConfiguration cfg;
    private String additionalFilter;
    private boolean fakeResultsToReturn = false;
    private SearchResult result;


    /** Creates a new instance of NewsSearchCommand
     *
     * @param cxt Search command context.
     * @param parameters Search command parameters.
     */
    public BlendingNewsSearchCommand(final Context cxt) {

        super(cxt);

        cfg = (BlendingNewsSearchConfiguration) cxt.getSearchConfiguration();
    }

    public SearchResult execute() {

        int totalHitCount = 0;

        SearchResult blended = new BasicSearchResult(this);

        fakeResultsToReturn = true;

        for (String filter : cfg.getFiltersToBlend()) {
            setAdditionalFilter(filter);
            SearchResult result = super.execute();
            blended.getResults().addAll(result.getResults());
            totalHitCount += result.getHitCount();
        }

        fakeResultsToReturn = false;

        blended.setHitCount(totalHitCount);

        final DateFormat df = new SimpleDateFormat(FAST_DATE_FMT);

        if (FAST_DATE_FMT.endsWith("'Z'")) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        Collections.sort(blended.getResults(), new DocDateTimeComparator(df));

        return blended;
    }

    protected String getAdditionalFilter() {
        return super.getAdditionalFilter() + " " + additionalFilter;
    }

    private void setAdditionalFilter(final String filter) {
        this.additionalFilter = filter;
    }

    /**
     * Returns the offset in the result set. If paging is enabled for the
     * current search configuration the offset to the current page will be
     * added to the parameter.
     *
     * @param i the current offset.
     * @return i plus the offset of the current page.
     */
    protected int getCurrentOffset(final int i) {
        return Integer.parseInt(getParameter("offset")) / cfg.getFiltersToBlend().size();
    }

    protected int getResultsToReturn() {
       return (fakeResultsToReturn ? cfg.getDocumentsPerFilter() : cfg.getResultsToReturn());
    }

    private class DocDateTimeComparator<SearchResultItem> implements Comparator {

        private final DateFormat df;

        public DocDateTimeComparator(final DateFormat df) {
            this.df = df;
        }

        public int compare(final Object o1 , final Object o2) {

            final BasicSearchResultItem i1 = (BasicSearchResultItem) o1;
            final BasicSearchResultItem i2 = (BasicSearchResultItem) o2;

            try {
                Date d1 = df.parse(i1.getField("docdatetime"));
                Date d2 = df.parse(i2.getField("docdatetime"));
                return d2.compareTo(d1);
            } catch (ParseException ex) {
                return 0;
            }
        }
    }
}