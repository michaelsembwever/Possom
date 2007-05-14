// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import org.apache.commons.lang.StringUtils;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class MultiValuedFieldCollector implements ResultHandler {
    
    private final MultivaluedFieldCollectorResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public MultiValuedFieldCollector(final ResultHandlerConfig config){
        this.config = (MultivaluedFieldCollectorResultHandlerConfig)config;
    }
    
    /** {@inherit}
     */
    public void handleResult(final Context cxt, final DataModel datamodel) {
        
        final Map<String,String> fieldMap = config.getFieldMap();

        for (final ResultItem item : cxt.getSearchResult().getResults()) {
            for (final Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                final String field = (String) fields.next();

                final String fieldValue = item.getField(field);

                if (fieldValue != null) {
                    final String[]  values = StringUtils.split(fieldValue, ' ');

                    for (int i = 0; i < values.length; i++) {
                        
                        final String value = values[i];

                        final String format = value.length() == 8
                            ? value.substring(0, 2) 
                                    + ' ' + value.substring(2, 4) 
                                    + ' ' + value.substring(4, 6) 
                                    + ' ' + value.substring(6, 8)
                            : value;
                        
                        cxt.getSearchResult().replaceResult(
                                item, 
                                item.addToMultivaluedField((String) fieldMap.get(field), format));
                    }

                }
            }
        }
    }
}
