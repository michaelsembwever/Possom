package no.schibstedsok.front.searchportal.result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

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

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public void handleResult(SearchResult result, Map parameters) {
    	// TODO: for performance reasons, is SimpleDateFormat usage avoidable?
    	SimpleDateFormat inputDF=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat outputDF = new SimpleDateFormat("HH:mm");

        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            String docDateTime = searchResultItem.getField(sourceField);

            try {
                String hour = outputDF.format(inputDF.parse(docDateTime));
                searchResultItem.addField(targetField, hour);
            } catch (ParseException e) {
            }
        }
    }
}
