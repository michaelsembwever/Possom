// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @version $Id
 */
public final class FieldEscapeHandler implements ResultHandler {
    
    private final FieldEscapeResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public FieldEscapeHandler(final ResultHandlerConfig config){
        this.config = (FieldEscapeResultHandlerConfig)config;
    }

    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            final String value = item.getField(config.getSourceField());
            item.addField(config.getTargetField(), StringEscapeUtils.escapeJavaScript(value));
        }
    }
}
