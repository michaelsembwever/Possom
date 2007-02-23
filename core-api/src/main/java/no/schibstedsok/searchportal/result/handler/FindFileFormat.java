// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;


/**
 * Created by IntelliJ IDEA.
 * User: itthkjer
 * Date: 21.okt.2005
 * Time: 09:40:46
 * To change this template use File | Settings | File Templates.
 */
public class FindFileFormat implements ResultHandler {

    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String url = item.getField("url");

            //print out the following fileformats after title
            if (url.toLowerCase().endsWith(".pdf"))
                item.addField("fileformat", "[pdf]");
            else if (url.toLowerCase().endsWith(".doc"))
                item.addField("fileformat", "[word]");
            else if (url.toLowerCase().endsWith(".ppt"))
                item.addField("fileformat", "[power point]");
            else if (url.toLowerCase().endsWith(".xls"))
                item.addField("fileformat", "[excel]");
            else if (url.toLowerCase().endsWith(".txt"))
                item.addField("fileformat", "[txt]");

        }
    }
}
