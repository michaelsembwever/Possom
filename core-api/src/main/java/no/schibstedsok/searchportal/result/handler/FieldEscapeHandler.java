// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * @deprecated everything the templates access should be escaped or encoded, making this operation redundant.
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

        final ResultList<ResultItem> rl = cxt.getSearchResult();
        
        for (final ResultItem item : rl.getResults()) {
            final String value = item.getField(config.getSourceField());
            // XXX is there a reason to escapeJavaScript instead of escapeHtml ? if so please document
            rl.replaceResult(item, item.addField(config.getTargetField(), StringEscapeUtils.escapeJavaScript(value)));
        }
    }
}
