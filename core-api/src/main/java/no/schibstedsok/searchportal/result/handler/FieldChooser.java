// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.Collection;
import java.util.Iterator;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class FieldChooser implements ResultHandler {

    private final FieldChooserResultHandlerConfig config;
    
    public FieldChooser(final ResultHandlerConfig config){
        this.config = (FieldChooserResultHandlerConfig)config;
    }

    public void handleResult(final Context cxt, final DataModel datamodel) {
        
        final Collection<String> fields = config.getFields();
        
        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {
            for (final Iterator iterator1 = fields.iterator(); iterator1.hasNext();) {
                
                final String fieldName = (String) iterator1.next();
                if (item.getField(fieldName) != null) {
                    item.addField(config.getTargetField(), item.getField(fieldName));
                    break;
                }
            }
        }
    }

    
}
