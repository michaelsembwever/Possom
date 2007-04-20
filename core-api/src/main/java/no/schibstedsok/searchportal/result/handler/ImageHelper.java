// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;


import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.SearchResultItem;
import org.apache.commons.lang.StringUtils;

/**
 * @author ssthkjer
 * @version $Id$
 */
public final class ImageHelper implements ResultHandler {
    
    private final ImageHelperResultHandlerConfig config;
    
    /**
     * 
     * @param config 
     */
    public ImageHelper(final ResultHandlerConfig config){
        this.config = (ImageHelperResultHandlerConfig)config;
    }
    
    /** {@inherit} **/
    public void handleResult(final Context cxt, final DataModel datamodel) {
        
        final Map<String,String> fieldMap = config.getFieldMap();

        for (final SearchResultItem item : cxt.getSearchResult().getResults()) {

            for (final Iterator fields = fieldMap.keySet().iterator(); fields.hasNext();) {
                final String field = (String) fields.next();
                final String fieldValue = item.getField(field);

                if (field.equals("ypmastermarks")) {

                    if (fieldValue != null) {
                        final String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            final String value = values[i];
                            item.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                } else {
                    if (fieldValue != null) {
                        final String[] values = StringUtils.split(fieldValue, ';');

                        for (int i = 0; i < values.length; i++) {
                            final String value = values[i];
                            item.addToMultivaluedField((String) fieldMap.get(field), value);
                        }

                    }
                }
            }
        }
    }
    
}
