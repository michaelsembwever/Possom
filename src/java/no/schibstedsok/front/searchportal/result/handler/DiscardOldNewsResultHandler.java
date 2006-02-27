// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.SearchResultItem;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class DiscardOldNewsResultHandler implements ResultHandler {

    private String sourceField;
    private long maxAgeInMilliseconds = Long.MAX_VALUE;

    //TODO: for performance reasons, is SimpleDateFormat usage avoidable?
    private static transient DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void handleResult(final Context cxt, final Map parameters) {


        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            String docTime = searchResultItem.getField(sourceField);

            docTime = docTime.replaceAll("T", " ").replaceAll("Z", " ").trim();

            if (docTime != null) {
                try {
                    final long age = System.currentTimeMillis() - df.parse(docTime).getTime();

                    if (age > maxAgeInMilliseconds) {
                        iterator.remove();
                    }

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}