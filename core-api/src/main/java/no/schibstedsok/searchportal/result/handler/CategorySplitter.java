// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;


/**
 * @author itthkjer
 * @version $Id$
 */
public final class CategorySplitter implements ResultHandler {
    
    private final CategorySplitterResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public CategorySplitter(final ResultHandlerConfig config){
        this.config = (CategorySplitterResultHandlerConfig)config;
    }

    /** {@inherit}
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final ResultItem i : cxt.getSearchResult().getResults()) {
            
            final String ypbransje = i.getField("ypbransje");
            ResultItem item = i;

            if (ypbransje != null) {
                //splits bransje to show categories under YIP page
                final String[]  split = ypbransje.split("FASTpbFAST");
                for (String s : split){
                    item = item.addToMultivaluedField("manyCategories", s.trim());
                }
                cxt.getSearchResult().replaceResult(i, item);
            }

        }
    }
}
