// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Map;
import java.util.Iterator;
import no.schibstedsok.front.searchportal.result.SearchResultItem;


/**
 * Created by IntelliJ IDEA.
 * User: itthkjer
 * Date: 21.okt.2005
 * Time: 09:40:46
 * To change this template use File | Settings | File Templates.
 */
public class FindFileFormat implements ResultHandler {

    public void handleResult(final Context cxt, final Map parameters) {

        for (final Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final SearchResultItem searchResultItem = (SearchResultItem) iterator.next();
            final String url = searchResultItem.getField("url");

            //print out the following fileformats after title
			if (url.endsWith(".pdf"))
				searchResultItem.addField("fileformat", "[pdf]");
            else if (url.endsWith(".doc"))
				searchResultItem.addField("fileformat", "[word]");
            else if (url.endsWith(".ppt"))
				searchResultItem.addField("fileformat", "[power point]");
            else if (url.endsWith(".xls"))
				searchResultItem.addField("fileformat", "[excel]");
            else if (url.endsWith(".txt"))
				searchResultItem.addField("fileformat", "[txt]");

        }
    }
}
