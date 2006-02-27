// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResultItem;

/**
 * TvEnrichmentDateFormatHandler is part of no.schibstedsok.front.searchportal.result
 * Transform fast inputdate to how it will be displayed in tv enrichment . Tv Enrichment
 * will most likely only display Hour of day.
 *
 * @author Ola Marius Sagli <a href="ola@schibstedsok.no">ola at schibstedsok</a>
 * @version 0.1
 * @vesrion $Revision$, $Author$, $Date$
 */
public class TvEnrichmentDateFormatHandler implements ResultHandler {

    private String targetField;
    private String sourceField;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(final String targetField) {
        this.targetField = targetField;
    }

    public void handleResult(final Context cxt, final Map parameters) {
    	// TODO: for performance reasons, is SimpleDateFormat usage avoidable?
    	final SimpleDateFormat inputDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final SimpleDateFormat outputDF = new SimpleDateFormat("HH:mm");

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            final String docDateTime = searchResultItem.getField(sourceField);

            try {
                final String hour = outputDF.format(inputDF.parse(docDateTime));
                searchResultItem.addField(targetField, hour);
            } catch (ParseException e) {
            }
        }
    }
}
