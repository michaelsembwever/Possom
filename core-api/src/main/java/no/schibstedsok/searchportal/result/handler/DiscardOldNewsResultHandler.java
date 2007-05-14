// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class DiscardOldNewsResultHandler implements ResultHandler {

    private String sourceField;
    private long maxAgeInMilliseconds = Long.MAX_VALUE;

    //TODO: for performance reasons, is SimpleDateFormat usage avoidable?
    private static transient DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final DiscardOldNewsResultHandlerConfig config;
    
    public DiscardOldNewsResultHandler(final ResultHandlerConfig config){
        this.config = (DiscardOldNewsResultHandlerConfig)config;
    }
    
    public void handleResult(final Context cxt, final DataModel datamodel) {


        for (ResultItem searchResultItem : cxt.getSearchResult().getResults()) {

            String docTime = searchResultItem.getField(sourceField);

            docTime = docTime.replaceAll("T", " ").replaceAll("Z", " ").trim();

            if (docTime != null) {
                try {
                    final long age = System.currentTimeMillis() - df.parse(docTime).getTime();

                    if (age > maxAgeInMilliseconds) {
                        cxt.getSearchResult().removeResult(searchResultItem);
                    }

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}