// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;


/**
 * Created by IntelliJ IDEA.
 * User: itthkjer
 * Date: 24.okt.2005
 * Time: 09:36:39
 */

public class CategorySplitter implements ResultHandler {

    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String ypbransje = item.getField("ypbransje");

            if (ypbransje != null) {
                //splits bransje to show categories under YIP page
                final String[]  split = ypbransje.split("FASTpbFAST");
                for (int i = 0; i < split.length; i++)
                    item.addToMultivaluedField("manyCategories", split[i].trim());
            }

        }
    }
}
