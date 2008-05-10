/* Copyright (2007-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * BlendingNewsSearchCommand.java
 *
 * Created on May 12, 2006, 2:19 PM
 *
 */

package no.sesat.search.mode.command;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import no.sesat.search.mode.config.BlendingNewsCommandConfig;
import no.sesat.search.result.BasicResultList;
import no.sesat.search.result.BasicResultItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;

/**
 * Temporary search command while we wait for neo-collapsing functionality
 * to be available in fast. This command is used to get the latest news article
 * from a number of selected sources. A separate search is done for each source.
 *
 *
 * @version $Id$
 */
public class BlendingNewsSearchCommand extends NewsSearchCommand {

    private static final String FAST_DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final BlendingNewsCommandConfig cfg;
    private String additionalFilter;
    private boolean fakeResultsToReturn = false;
    private ResultList<ResultItem> result;


    /** Creates a new instance of NewsSearchCommand
     *
     * @param cxt Search command context.
     */
    public BlendingNewsSearchCommand(final Context cxt) {

        super(cxt);

        cfg = (BlendingNewsCommandConfig) cxt.getSearchConfiguration();
    }

    public ResultList<? extends ResultItem> execute() {

        int totalHitCount = 0;

        ResultList<ResultItem> blended = new BasicResultList<ResultItem>();

        fakeResultsToReturn = true;

        for (String filter : cfg.getFiltersToBlend()) {
            setAdditionalFilter(filter);
            ResultList<ResultItem> result = (ResultList<ResultItem>) super.execute();
            blended.addResults(result.getResults());
            totalHitCount += result.getHitCount();
        }

        fakeResultsToReturn = false;

        blended.setHitCount(totalHitCount);

        final DateFormat df = new SimpleDateFormat(FAST_DATE_FMT);

        if (FAST_DATE_FMT.endsWith("'Z'")) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        blended.sortResults(new DocDateTimeComparator(df));

        return blended;
    }

    protected String getAdditionalFilter() {
        return super.getAdditionalFilter() + " " + additionalFilter;
    }

    private void setAdditionalFilter(final String filter) {
        this.additionalFilter = filter;
    }


    protected int getOffset(){
        return super.getOffset() / cfg.getFiltersToBlend().size();
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

            final BasicResultItem i1 = (BasicResultItem) o1;
            final BasicResultItem i2 = (BasicResultItem) o2;

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