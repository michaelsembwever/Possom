// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Map;
import java.util.Iterator;
import no.schibstedsok.searchportal.result.SearchResultItem;


/**
 * Created by IntelliJ IDEA.
 * User: itthkjer
 * Date: 21.okt.2005
 * Time: 09:40:46
 * To change this template use File | Settings | File Templates.
 */
public class FindFileFormat implements ResultHandler {

    public void handleResult(final Context cxt, final Map parameters) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String url = item.getField("url");

            //print out the following fileformats after title
			if (url.endsWith(".pdf"))
				item.addField("fileformat", "[pdf]");
            else if (url.endsWith(".doc"))
				item.addField("fileformat", "[word]");
            else if (url.endsWith(".ppt"))
				item.addField("fileformat", "[power point]");
            else if (url.endsWith(".xls"))
				item.addField("fileformat", "[excel]");
            else if (url.endsWith(".txt"))
				item.addField("fileformat", "[txt]");

        }
    }
}
