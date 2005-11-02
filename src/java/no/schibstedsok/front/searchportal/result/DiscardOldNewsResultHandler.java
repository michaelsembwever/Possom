package no.schibstedsok.front.searchportal.result;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class DiscardOldNewsResultHandler implements ResultHandler {

    private static Log log = LogFactory.getLog(DiscardOldNewsResultHandler.class);

    private String sourceField;
    private long maxAgeInMilliseconds = Long.MAX_VALUE;

    //TODO: for performance reasons, is SimpleDateFormat usage avoidable?
    private static transient DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void handleResult(SearchResult result, Map parameters) {


        for (Iterator iterator = result.getResults().iterator(); iterator.hasNext();) {
            SearchResultItem searchResultItem = (SearchResultItem) iterator.next();

            String docTime = searchResultItem.getField(sourceField);

            docTime = docTime.replaceAll("T", " ").replaceAll("Z", " ").trim();

            if (docTime != null) {
                try {
                    long age = System.currentTimeMillis() - df.parse(docTime).getTime();

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